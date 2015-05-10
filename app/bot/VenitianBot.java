package bot;

import controllers.VenitianWSocket;
import play.Logger;
import play.libs.Json;

import java.util.List;

import status.Classifier;
import status.RankedStatus;
import status.SimpleStatus;
import status.StatusDatabase;
import twitter4j.*;
import utilities.LocationBox;
import utilities.Response;
import utilities.Responses;

import java.sql.SQLException;
import java.util.*;

import static utilities.Utilities.*;

public enum VenitianBot {
    INSTANCE;

    private static final String SCREEN_NAME = "VenitianBot";
    private static final long TWITTER_ID = 2926745097L;
    private Twitter twitter;
    private TwitterStream stream;
    private StatusDatabase db;

    public final int MIN_SLEEP_TIME = 5000;
    private int sleepTime = 5000;

    private LocationBox veniceLocation;

    private TwitterStreamListener streamListener;
    private boolean initialized = false;

    // keep users we have replied
    private Set<String> usersTweeted = new TreeSet<>();
    private PriorityQueue<RankedStatus> rankedTweets = new PriorityQueue<>();

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

    private String[] featuredUsers = readFeaturedUsers();

    public TwitterStreamListener getStreamListener() {
        return streamListener;
    }

    public StatusDatabase getDB() {
        return db;
    }

    public void init() throws SQLException {
        if (!initialized) {
            twitter = TwitterFactory.getSingleton();
            veniceLocation = readGeoLocation();
            Classifier.init();
            Logger.info("Classifier initialized");
            initResponses();
            Logger.info("Replies loaded");
            db = new StatusDatabase().init();
            Logger.info("Database up");
            streamListener = new TwitterStreamListener();
            streamTweets();
            Logger.info("Streaming tweets");
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
        Set<String> tagSet = new HashSet<>();
        tagSet.addAll(Arrays.asList(tags));
        responses.addResponse(new Response(tagSet, answers[answerIndex++]));
    }

    public String getScreenName() {
        return SCREEN_NAME;
    }

    public long getTwitterID() {
        return TWITTER_ID;
    }

    /**
     *
     */
    public void streamTweets() {
        if (stream == null) {
            stream = new TwitterStreamFactory().getInstance();
        }
        stream.addListener(streamListener);

        FilterQuery filter = new FilterQuery();
        // Get tweets in english
        filter.language(new String[]{"en"});
        // OR Track keywords from the json
        filter.track(readKeywords());
        // OR Get tweets from the Venice region
        double[][] venice = {
                // South West corner
                {veniceLocation.getSW().getLatitude(),
                        veniceLocation.getSW().getLongitude()},
                // North East corner
                {veniceLocation.getNE().getLatitude(),
                        veniceLocation.getNE().getLongitude()},};
        filter.locations(venice);

        stream.filter(filter);
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

    public void retweet(long statusID) {
        try {
            twitter.retweetStatus(statusID);
        } catch (TwitterException e) {
            Logger.error("Failed to retweet status : " + statusID);
        }

        Logger.info("Retweeted status: " + statusID);
    }

    /**
     * The tweet passed as parametar is added to the encountered tweets by now.
     * If we need to post a the given time, we take the head of the
     * PriorityQueue, that is the highest ranked tweet from the tweets we have
     *
     * @param tweet tweet to be added to the PriorityQueue
     * @return the reply
     */
    public String replyTo(RankedStatus tweet) {
        rankedTweets.add(tweet);
        count++;
        RankedStatus chosenTweet;
        Logger.debug("Received tweets " + count);
        if (count % 10 == 0) {
            Logger.debug("In if");

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
            Logger.debug("Got answer" + answer);


            StatusUpdate statusReply = new StatusUpdate(answer.getTweet());
            try {
                Status replied = reply(chosenTweet.getContent().getId(), statusReply);

                Logger.debug("preparing reply!");
                String replyText = (replied == null) ? null : replied.getText();
                SimpleStatus simpleReply = new SimpleStatus(new java.sql.Date(new Date().getTime()), replyText);
                Logger.debug(Json.stringify(simpleReply.toBotJson()));
                for (VenitianWSocket socket : streamListener.sockets) {
                    Logger.debug("Bot tweets!");
                    socket.sendMessage(Json.stringify(simpleReply.toBotJson()));
                }
                return replyText;
            } catch (TwitterException e) {
                Logger.error("Could not reply");
            }

            return null;
        }
        return "";

    }

    public String advertise() {
        tweet(shamelessAdvertise);
        return shamelessAdvertise;
    }

    public String advertise(RankedStatus s) {
        StatusUpdate shamelessReply = new StatusUpdate(shamelessAdvertise);
        try {
            Status replied = reply(s.getContent().getId(), shamelessReply);
            return (replied == null) ? null : replied.getText();
        } catch (TwitterException e) {
            Logger.error("Could not advertise shamelessly: " + e.getErrorMessage());
        }
        return null;
    }

    public String advertise(long replyToStatusId) {
        StatusUpdate shamelessReply = new StatusUpdate(shamelessAdvertise);
        try {
            Status replied = reply(replyToStatusId, shamelessReply);
            return (replied == null) ? null : replied.getText();
        } catch (TwitterException e) {
            Logger.error("Could not advertise shamelessly: " + e.getErrorMessage());
        }
        return null;
    }

    public Status reply(long replyToStatusId, StatusUpdate statusReply) throws TwitterException {
        statusReply.setInReplyToStatusId(replyToStatusId);
//        return twitter.updateStatus(statusReply);
        return null;
    }

    public List<String> getFeaturedUsers() {
        return Arrays.asList(featuredUsers);
    }
}
