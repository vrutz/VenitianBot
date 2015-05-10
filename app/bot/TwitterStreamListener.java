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

    @Override
    public void onStatus(Status arg0) {
        Logger.debug(arg0.getUser().getScreenName());
        RankedStatus status = Classifier.classify(arg0);

        if (status.isFeatured()){
            VenitianBot.INSTANCE.retweet(status.getContent().getId());
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

        if (arg0.getInReplyToScreenName().toLowerCase().replaceAll(" ", "").equals("venitianbot")) {
            String reply = VenitianBot.INSTANCE.advertise(status);
            Logger.info("Advertised " + reply + " in response to " + arg0.getText());
        }


    }

    public boolean addWSocket(VenitianWSocket socket) {
        return sockets.add(socket);
    }

    public void removeWSocket(VenitianWSocket socket) {
        sockets.remove(socket);
    }

    @Override
    public void onException(Exception arg0) {
    }

    @Override
    public void onDeletionNotice(StatusDeletionNotice arg0) {
    }

    @Override
    public void onScrubGeo(long arg0, long arg1) {
    }

    @Override
    public void onStallWarning(StallWarning arg0) {
    }

    @Override
    public void onTrackLimitationNotice(int arg0) {
    }
}
