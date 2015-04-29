package bot;

import org.h2.jdbcx.JdbcDataSource;
import play.Logger;
import status.Classifier;
import status.RankedStatus;
import status.StatusDatabase;
import twitter4j.*;
import utilities.LocationBox;
import utilities.Response;
import utilities.Responses;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

import static utilities.Utilities.readKeywords;

public enum VenitianBot {
    INSTANCE;

    private Twitter twitter;
    private TwitterStream stream;
    private StatusDatabase db;

    private static final String TABLE_NAME = "tweetsTable";
    private static final String ID = "id";
    private static final String FAVORITE_COUNT = "favoriteCount";
    private static final String LATITUDE = "latitdue";
    private static final String LONGITUDE = "longitude";
    private static final String RETWEET_COUNT = "retweetCount";

    // Credentials and related constants
    private final String CREDENTIALS_FILE = "twitter4j.properties";
    private final String CONSUMER_KEY = "oauth.consumerKey";
    private final String CONSUMER_VALUE = "yWRk3akWd4RcBAnrEokZLnZvI";
    private final String CONSUMER_SECRET_KEY = "oauth.consumerSecret";
    private final String CONSUMER_SECRET_VALUE = "jgd9zn4g2L2Nzq4FSwwBxXIl3l33ASq0xQwuWVwghqVJ4S1lwb";
    private final String TOKEN_KEY = "oauth.accessToken";
    private final String TOKEN_VALUE = "2926745097-e3AJzjC1VU6tcaexd82RNAbw1b4HPybN8nnIbgp";
    private final String TOKEN_SECRET_KEY = "oauth.accessTokenSecret";
    private final String TOKEN_SECRET_VALUE = "sSBvDS6UEssFyYhKkrc5OWH7fG4H6BC7QM3sUHpq9yEa7";

    public final int MIN_SLEEP_TIME = 5000;
    private int sleepTime = 5000;

    private LocationBox veniseLocation;

    private TwitterStreamListener streamListener;
    private boolean initialized = false;

    private Connection conn;

    // keep users we have replied
    private Set<String> usersTweeted = new TreeSet<String>();
    private PriorityQueue<RankedStatus> rankedTweets = new PriorityQueue<RankedStatus>();

    private int answerIndex = 0;

    // replies
    private int count = 0;


    // We should take into account that we must add the person name in the tweet -> need to have some free characters
    private Responses responses;

    private String[] answers = {
            "Did you know that the town dates back to the mid 400s, over 1500 years ago #Venice  ",
            "Did you know that the Venice Republic was the biggest power of the Mediterranean during 1300's-1500's  #Venice  ",
            "Did you know that Venice lies on an archipelago made up of 118 flat islands from the Veneto coast, in the northeastern Mediterranean  ",
            "Did you know that Venice is made up of 118 islands, 416 bridges, 177 canals, 127 campi (squares).  ",
            "The Veneto town is visited by 18 millions tourists a year, on average 50,000 a day. #Venice  ",
            "Fun fact: Venice sits on an archipelago, which is basically a group of small islands. The city has 118 islands altogether  ",
            "Did you know that #Venice contains approximately 7,000 chimneys. They come in 10 different styles and shapes.  ",
            "There are 170 bell towers in #Venice. They were an important form of communication. #SanMarco is one of the tallest.  ",
            "Fun fact: Each year #Venice receives 18 million tourists. This equates to approximately 50,000 visitors each day.  ",
            "Did you know that #Venice is divided by quarters. There are six altogether.  ",
            "Did you know that by the 18th century, there were over 200 #churches in #Venice  ",
            "Did you know that #AcquaAlta, or high water, happens when the tide is 8.9916 cm above normal height #Venice  ",
            "Did you know that #Venice is divided into six sestieri, or districts  ",
            "Did you know that #Venice is sinking at the rate of 1-2 millimeters a year  ",
            "Did you know only about 60,000 #Venetians live in the historic center of #Venice",
            "Fun fact: #Venice was named after the ancient Veneti people.  ",
            "Fun fact: #Venice was the major #Mediterranean maritime power in the 14th to 16th centuries.  ",
            "Did you know there are 400+ pedestrian footbridges spanning the canals  ",
            "Fun fact: #Venice has over 450 #palaces (palazzi) and important buildings built in a mixture of styles, #Gothic, #Byzantine, #Baroque …  ", // palace
            "Fun fact: #Venice has 177 canals and over 400 bridges.  ",// canal, bridge
            "Fun fact: Only 3 to 4 Gondolier licenses are issued annually - there are only 400 licensed Gondolas operating in Venice today.  ", // gondola
            "Fun fact: #Venice got its first female gondolier in 2010  ", // gondola
            "Fun fact: Giacomo Casanova, Marco Polo, and Antonio Vivaldi were born in #Venice  ", // casanova, marcopolo, vivaldi
            "Did you know that the San Marco bell tower - or campanile - is the Italy's fifth tallest bell tower, measuring 98,6mt/275ft.  ", // sanmarco, belltower
            "Fun fact: until Accademia Bridge was built in 1854, Rialto Bridge was the only place through which one could cross the canal on foot.", // accademia, rialtobridge
            "Did you know that the Rialto Bridge design was done by a Swiss engineer: Antonio da Ponte.", // rialtobridge
            "Fun fact: Three of the city’s bridges have been around since ancient times: #Rialto, #Accademia and the #Scalzi  ", // roaltobridge, accademia, scalzi
            "Did you know that near the Rialto Bridge is the famous Rialto fish marketthat has been on the same site for over 1,000 year", // grandcanal, roaltobridge, accademia, scalzi
            "Did you know that 3 major bridges cross the #GrandCanal – #Accademia, #Rialto and #Scalzi  ", // grandcanal, roaltobridge, accademia, scalzi
            "Did you know that there are 3 ancient bridges over the Grand Canal: Rialto bridge, Accademia, Scalzi (Ferrovia).  ", // grandcanal, roaltobridge, accademia, scalzi
            "Fun fact: The #GrandCanal is the region’s largest canal. Possessing a unique S-shape, the canal splits the city in half.  ", // grandcanal
            "Fun fact: There are over 170 buildings that line the #GrandCanal  ", // grandcanal
            "The biggest and longest canal is the S-shaped Grand Canal, splitting the town in two.  ", // grandcanal
            "Did you know that the Grand Canal in Venice is 3800m long." // grandcanal

    };

    private String formURL = "goo.gl/forms/fyx0PSmBzk";
    private String shamelessAdvertise = "Hey, I'm just a simple bot. Tell me more here: " + formURL;

    public int getSleepTime() {
        return sleepTime;
    }

    public void setSleepTime(int time) {
        sleepTime = (time >= MIN_SLEEP_TIME) ? time : MIN_SLEEP_TIME;
    }

    public TwitterStreamListener getStreamListener() {
        return streamListener;
    }

    public StatusDatabase getDB() {
        return db;
    }

    public void init() throws SQLException {
        if (!initialized) {
            initCredentials();
            twitter = TwitterFactory.getSingleton();
            veniseLocation = utilities.Utilities.readGeoLocation();
            Classifier.init();
            db = new StatusDatabase()/*.init()*/;
//		    db.drop();
            streamListener = new TwitterStreamListener();
            streamTweets();
            initialized = true;
        }
    }

    // TODO: refactor by putting tags and answer in a JSON file and parse it to initialize the responses
    public void initResponses() {
        responses = new Responses();
        for (int i = 0; i < 18; ++i) {
            addAnswer(new String[]{});
        }
        addAnswer(new String[]{"palace"});

        addAnswer(new String[]{"canal", "bridge"});

        addAnswer(new String[]{"gondola"});
        addAnswer(new String[]{"gondola"});

        addAnswer(new String[]{"casanova", "marcopolo", "vivaldi"});

        addAnswer(new String[]{"sanmarco", "belltower"});

        addAnswer(new String[]{"accademia", "rialtobridge"});

        addAnswer(new String[]{"rialtobridge"});

        addAnswer(new String[]{"roaltobridge", "accademia", "scalzi"});

        addAnswer(new String[]{"grandcanal", "roaltobridge", "accademia", "scalzi"});
        addAnswer(new String[]{"grandcanal", "roaltobridge", "accademia", "scalzi"});
        addAnswer(new String[]{"grandcanal", "roaltobridge", "accademia", "scalzi"});

        addAnswer(new String[]{"grandcanal"});
        addAnswer(new String[]{"grandcanal"});
        addAnswer(new String[]{"grandcanal"});
        addAnswer(new String[]{"grandcanal"});
    }

    private void addAnswer(String[] tags) {
        Set<String> tagSet = new HashSet<String>();
        tagSet.addAll(Arrays.asList(tags));
        responses.addResponse(new Response(tagSet, answers[answerIndex++]));

    }

    /**
     * Stores the necessary keys to access the API in a file. The keys will be
     * used automatically when requests are performed
     * <p/>
     * Consumer key and secrets as well as token key and secret can be found at
     * https://apps.twitter.com/app/7947759/keys
     */
    public void initCredentials() {
        Properties properties = new Properties();
        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            // Stores the consumer key and secret
            properties.setProperty(CONSUMER_KEY, CONSUMER_VALUE);
            properties.setProperty(CONSUMER_SECRET_KEY, CONSUMER_SECRET_VALUE);

            // Stores the token key and secret
            properties.setProperty(TOKEN_KEY, TOKEN_VALUE);
            properties.setProperty(TOKEN_SECRET_KEY, TOKEN_SECRET_VALUE);

            outputStream = new FileOutputStream(CREDENTIALS_FILE);
            properties.store(outputStream, CREDENTIALS_FILE);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) { // Do nothing
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) { // Do nothing
                }
            }
        }
    }

    private void initDatabse() throws SQLException {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:˜./test");
        ds.setUser("sa");
        ds.setPassword("sa");
        conn = ds.getConnection();

		/*
         * conn.createStatement().execute("CREATE TABLE " + TABLE_NAME + " (" +
		 * ID + " bigint," + FAVORITE_COUNT + " int," + LATITUDE + " float," +
		 * LONGITUDE + " float," + RETWEET_COUNT + " int)");
		 */

    }

    /**
     *
     */
    public void streamTweets() {
        if (stream == null) {
            stream = new TwitterStreamFactory().getInstance();
            stream.addListener(streamListener);

            FilterQuery filter = new FilterQuery();
            // Get tweets in english
            filter.language(new String[]{"en"});
            // OR Track keywords from the json
            filter.track(readKeywords());
            // OR Get tweets from the Venice region
            double[][] venice = {
                    // South West corner
                    {veniseLocation.getSW().getLatitude(),
                            veniseLocation.getSW().getLongitude()},
                    // North East corner
                    {veniseLocation.getNE().getLatitude(),
                            veniseLocation.getNE().getLongitude()},};
            filter.locations(venice);

            stream.filter(filter);
        }
    }

    public void stopStream() {
        if (stream != null) {
            stream.shutdown();
            stream = null;
        }
        initialized = false;
    }

    /**
     * Publishes a tweet
     *
     * @param status the tweet that will be published
     */
    public void tweet(String status) {
        try {
            twitter.updateStatus(status);
        } catch (TwitterException e) {
            Logger.error("Failed to update status to: " + status);
        }
        Logger.info("Status updated to: " + status);
    }

    /**
     * The tweet passed as parametar is added to the encountered tweets by now.
     * If we need to post a the given time, we take the head of the
     * PriorityQueue, that is the highest ranked tweet from the tweets we have
     *
     * @param tweet
     * @return
     */
    public String replyTo(RankedStatus tweet) {
        rankedTweets.add(tweet);
        count++;
        RankedStatus chosenTweet = tweet;
        if (count % 50 == 0) {
            do {
                chosenTweet = rankedTweets.poll(); // can't be empty since we
                // add an elem just before
            } while (usersTweeted.contains(chosenTweet.getContent().getUser()
                    .getScreenName()));

            // add that we have replied already to this user
            usersTweeted
                    .add(chosenTweet.getContent().getUser().getScreenName());

            // get the most relevant answer for this tweet
            Response answer = responses.getFirst(chosenTweet.getTags());


            String reply = "@"
                    + chosenTweet.getContent().getUser().getScreenName() + " "
                    + answer.getTweet();
//            SimpleStatus simpleReply = new SimpleStatus(new Date(new java.util.Date().getTime()), reply);
//            for(VenitianWSocket socket: VenitianBot.INSTANCE.getStreamListener().sockets) {
//                socket.sendMessage(simpleReply.toBotJson().toString());
//            }
            // tweet(reply);
            return reply;
        }
        return "";

    }

    public String advertise(RankedStatus s) {
        String rep = "@" + s.getContent().getUser().getScreenName() + " " + shamelessAdvertise;
        tweet(rep);
        return rep;
    }


}
