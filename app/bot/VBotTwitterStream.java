package bot;

import play.Logger;
import twitter4j.*;

/**
 * From Valentin with love on 10/05/15.
 */
public class VBotTwitterStream implements StatusListener {
    @Override
    public void onStatus(Status status) {
        /*
        * We will get:
        *     - Tweets created by the user.
        *     - Tweets which are retweeted by the user.
        *     - Replies to any Tweet created by the user.
        *     - Retweets of any Tweet created by the user.
        *     - Manual replies, created without pressing a reply button (e.g. “@twitterapi I agree”).
        * */
        Logger.info("Received status: " + status.getText());
        if (!status.getUser().getScreenName().equals(VenitianBot.INSTANCE.getScreenName()) && !status.isRetweet()) {
            /*
            * Just got a tweet not tweeted by us and which is not a retweet of any of our tweets
            * So compared to the previous, this should give us:
            *     - Replies to any Tweet created by the user.
            *     - Manual replies, created without pressing a reply button (e.g. “@twitterapi I agree”).
            * Which are essentially all replies to our tweets.
            */
            if (status.getInReplyToUserId() == VenitianBot.INSTANCE.getTwitterID()) {
                VenitianBot.INSTANCE.advertise(status.getId());
            } else {
                Logger.error("Recevied tweet should be a reply to the VenitianBot");
            }
        }
    }

    @Override
    public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
        Logger.info("Status deleted: " + statusDeletionNotice.toString());
    }

    @Override
    public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
        Logger.warn("Track limitation notice: " + numberOfLimitedStatuses);
    }

    @Override
    public void onScrubGeo(long userId, long upToStatusId) {
        Logger.info("Scrub geo: " + userId);
    }

    @Override
    public void onStallWarning(StallWarning warning) {
        Logger.warn("Stall Warning: " + warning.getMessage());
    }

    @Override
    public void onException(Exception ex) {
        Logger.error("Exception: " + ex.getMessage());
    }
}
