package bot;

import play.Logger;
import play.db.DB;
import status.Classifier;
import status.RankedStatus;
import status.SimpleStatus;
import status.StatusDatabase;
import twitter4j.*;
import utilities.Location;
import utilities.LocationBox;
import utilities.Utilities;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static utilities.Utilities.readKeywords;

public enum VenitianBot {
    INSTANCE;

    private Twitter twitter;
    private TwitterStream stream;
    private StatusDatabase db;

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

    // Classification constants
    private final int STRICT_LOCATION_WEIGHT = 10;
    private final int GLOBAL_LOCATION_WEIGHT = 5;
    private final int KEYWORD_MONUMENT_WEIGHT = 6;
    private final int KEYWORD_VENICE_WEIGHT = 4;

    private int classificationThreshold = 10;

    public final int MIN_SLEEP_TIME = 5000;
    private int sleepTime = 5000;

    private LocationBox veniseLocation;
    private Location[] monumentsLocation;
    private String veniseKeyword = "venice";
    private String[] monumentsKeyword;
    private TwitterStreamListener streamListener;
    private boolean initialized = false;

    public int getSleepTime() {
        return sleepTime;

    }

    public void setSleepTime(int time) {
        sleepTime = (time >= MIN_SLEEP_TIME) ? time : MIN_SLEEP_TIME;
    }

    public StatusDatabase getDB() {
        return db;
    }

    public TwitterStreamListener getStreamListener() {
        return streamListener;
    }

    public void init() throws SQLException {
        if (!initialized) {
            initCredentials();
            twitter = TwitterFactory.getSingleton();
            veniseLocation = Utilities.readGeoLocation();
            monumentsLocation = Utilities.readMonumentsLocation();
            monumentsKeyword = Utilities.getMonumentsKeyword(monumentsLocation);

            Classifier.init();
            db = new StatusDatabase()/*.init()*/;
//		db.drop();
            streamListener = new TwitterStreamListener();
            streamTweets();
            initialized = true;
        }
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
//		InputStream inputStream = null;
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
//			if (inputStream != null) {
//				try {
//					inputStream.close();
//				} catch (IOException e) { // Do nothing
//				}
//			}
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) { // Do nothing
                }
            }
        }
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
                            veniseLocation.getNE().getLongitude()}};
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
}
