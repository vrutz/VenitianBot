package bot;

import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.VenitianWSocket;
import play.Logger;
import play.libs.Json;
import status.Classifier;
import status.RankedStatus;
import status.SimpleStatus;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;

import java.util.ArrayList;
import java.util.List;

public class TwitterStreamListener implements StatusListener {

    List<VenitianWSocket> sockets;

    public TwitterStreamListener() {
        sockets = new ArrayList<VenitianWSocket>();
    }

    /**
     * Called each time a status corresponding to the filter given in VenitianBot.streamTweets() is sent
     * from the Twitter Streaming API to our Bot.
     * If the tweet is from a featured user, we retweet.
     * If it is relevant, we send it to the Web interface and reply to it
     * If it is a reply to us, we advertise ourselves shamelessly
     * @param arg0 the status received
     */
    @Override
    public void onStatus(Status arg0) {
        Logger.debug(arg0.getUser().getScreenName());
        RankedStatus status = Classifier.classify(arg0);

        if (status.isFeatured()) {
            VenitianBot.INSTANCE.retweet(status.getContent().getId());
        } else if(arg0.getInReplyToScreenName().toLowerCase().replaceAll(" ", "")
                .equals(VenitianBot.INSTANCE.getScreenName()) ||
                arg0.getText().startsWith("@" + VenitianBot.INSTANCE.getScreenName())) {
            String reply = VenitianBot.INSTANCE.advertise(status);
            Logger.info("Advertised " + reply + " in response to " + arg0.getText());
        } else if (status.isRelevant()) {
            Logger.info("Status ranked " + status.getRank() +
                    ", from: " + status.getContent().getUser().getScreenName());
            Logger.info("\t" + status.getContent().getText());

//			VenitianBot.INSTANCE.getDB().insertIntoDB(status);
            ObjectNode htmlStatus = new SimpleStatus(status).toJson();
//            Logger.debug("Sending " + Json.stringify(htmlStatus) + " to all websockets!");

            String rep = VenitianBot.INSTANCE.replyTo(status);

            if (!"".equals(rep))
                Logger.info("Replied: \n to: " + status.getContent().getText() + "\n with: " + rep);

            for (VenitianWSocket socket : sockets) {
                socket.sendMessage(Json.stringify(htmlStatus));
            }
        }
    }

    /**
     * Adds a websocket to the stream when a new connection is opened.
     * This happens when a new user opens venetianbot.tk:8080/tweets
     * @param socket the websocket to add
     * @return true if the socket was successfully added to the list, false otherwise
     */
    public boolean addWSocket(VenitianWSocket socket) {
        return sockets.add(socket);
    }

    /**
     * This is calls when the connection is closed, i.e. tab is closed
     * So we remove the websocket snice we won't send updates anymore to that client
     * @param socket the socket to remove
     */
    public void removeWSocket(VenitianWSocket socket) {
        sockets.remove(socket);
    }

    /**
     * not overriden
     * @param arg0 not overriden
     */
    @Override
    public void onException(Exception arg0) {
    }

    /**
     * not overriden
     * @param arg0 not overriden
     */
    @Override
    public void onDeletionNotice(StatusDeletionNotice arg0) {
    }

    /**
     * not overriden
     * @param arg0 not overriden
     */
    @Override
    public void onScrubGeo(long arg0, long arg1) {
    }

    /**
     * not overriden
     * @param arg0 not overriden
     */
    @Override
    public void onStallWarning(StallWarning arg0) {
    }

    /**
     * not overriden
     * @param arg0 not overriden
     */
    @Override
    public void onTrackLimitationNotice(int arg0) {
    }
}
