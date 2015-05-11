package bot;

import controllers.VenitianWSocket;
import play.Logger;
import play.libs.Json;

import java.util.List;

import status.Classifier;
import status.RankedStatus;
import status.SimpleStatus;
import status.StatusDatabase;
import tweets.TweetedUsers;
import twitter4j.*;
import utilities.*;

import java.sql.SQLException;
import java.util.*;

import static utilities.Utilities.*;

public enum VenitianBot {
    INSTANCE;

    private static final long TWITTER_ID = 2926745097L;
    private Twitter twitter;
    private TwitterStream stream;
    private StatusDatabase db;

    private LocationBox veniceLocation;

    private TwitterStreamListener streamListener;
    private boolean initialized = false;

    // keep users we have replied
    private TweetedUsers tweetedUsers = new TweetedUsers();
    private PriorityQueue<RankedStatus> rankedTweets = new PriorityQueue<>();

    // replies
    private int count = 0;


    // We should take into account that we must add the person name in the tweet -> need to have some free characters
    private Responses responses;

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
            veniceLocation = JSONReader.veniceLocation;

            Classifier.init();
            Logger.info("Classifier initialized");

            db = new StatusDatabase().init();
            Logger.info("Database up");

            responses = readResponses();
            streamListener = new TwitterStreamListener();
            streamTweets();
            Logger.info("Streaming tweets");
            initialized = true;
        }
        Logger.info("VenitianBot initialized");
    }

    public Responses readResponses() {
        Responses newResponses = new Responses();
        List<Response> respList = Utilities.readResponses();

        for (Response resp : respList) {
            newResponses.addResponseInMemory(resp);
        }
        return newResponses;
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
            Logger.info("Created stream observer");
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
                        veniceLocation.getNE().getLongitude()}};
        filter.locations(venice);
        // OR those directed to/from the VenitianBot
        filter.follow(new long[]{TWITTER_ID});

        stream.filter(filter);
        Logger.info("Filtering stream for english OR for the defined keywords OR from venice OR to/from the bot");
    }

    public void stopStream() {
        if (stream != null) {
            stream.shutdown();
            stream = null;
            Logger.info("Stream observer shutdown");
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
            } while (!tweetedUsers.isTweetable(chosenTweet.getContent().getUser().getId()));

            // add that we have replied already to this user
            tweetedUsers.addUser(chosenTweet.getContent().getUser().getId());

            // get the most relevant answer for this tweet
            Logger.debug("Berfore get first");
            Response answer = responses.getFirst(chosenTweet.getTags());
            Logger.debug("Got answer " + answer);


            String statusReply = "@" + chosenTweet.getContent().getUser().getScreenName() +
                            " " + answer.getTweet();
            try {
                Logger.debug("preparing reply: " + statusReply);
                String replied = reply(chosenTweet.getContent().getId(), statusReply);

                SimpleStatus simpleReply =
                        new SimpleStatus(new java.sql.Date(new Date().getTime()), replied,
                                chosenTweet.getContent().getUser().getScreenName(),
                                chosenTweet.getContent().getText());
                Logger.debug(Json.stringify(simpleReply.toBotJson()));
                for (VenitianWSocket socket : streamListener.sockets) {
                    Logger.debug("Bot tweets!");
                    socket.sendMessage(Json.stringify(simpleReply.toBotJson()));
                }
                return replied;
            } catch (TwitterException e) {
                Logger.error("Could not reply");
            }

            return null;
        }
        return "";

    }

    public String advertise() {
        Logger.info("Shamelessly advertising");
        tweet(shamelessAdvertise);
        return shamelessAdvertise;
    }

    public String advertise(RankedStatus s) {
        Logger.info("Shamelessly advertising");
        try {
            String replied = reply(s.getContent().getId(),
                    "@" + s.getContent().getUser().getScreenName() + " " + shamelessAdvertise);
            return replied;
        } catch (TwitterException e) {
            Logger.error("Could not advertise shamelessly: " + e.getErrorMessage());
        }
        return null;
    }

    public String advertise(long replyToStatusId, String screenName) {
        Logger.info("Shamelessly advertising");
        try {
            String replied = reply(replyToStatusId, "@"+ screenName + " "+ shamelessAdvertise);
            return replied;
        } catch (TwitterException e) {
            Logger.error("Could not advertise shamelessly: " + e.getErrorMessage());
        }
        return null;
    }

    public String reply(long replyToStatusId, String statusReply) throws TwitterException {
        Logger.info("Replied to status with ID: " + replyToStatusId);
        StatusUpdate update = new StatusUpdate(statusReply);
        update.setInReplyToStatusId(replyToStatusId);
        return twitter.updateStatus(update).getText();
    }

    public List<String> getFeaturedUsers() {
        return Arrays.asList(featuredUsers);
    }

    public void stopBot() {
        Logger.debug("Stopping the bot");
        tweetedUsers.saveUsers();
        stopStream();
        getDB().closeConnection();
    }
}
