package bot;

import controllers.VenitianWSocket;
import play.Logger;
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
        sockets = new ArrayList<>();
    }

    @Override
    public void onStatus(Status arg0) {
        RankedStatus status = Classifier.classify(arg0);


        if (status.isRelevant()) {
            Logger.info("Status ranked " + status.getRank() +
                    ", from: " + status.getContent().getUser().getScreenName());
            Logger.info("\t" + status.getContent().getText());

//			VenitianBot.INSTANCE.getDB().insertIntoDB(status);
            String htmlStatus = new SimpleStatus(status).toHTML();
            Logger.debug("Sending " + htmlStatus + " to all websockets!");

            String rep = VenitianBot.INSTANCE.replyTo(status);

            if (!"".equals(rep))
                System.out.println("Replied: \n to: " + status.getContent().getText() + "\n with: " + rep);

            for (VenitianWSocket socket : sockets) {
                socket.sendMessage(htmlStatus);
                Logger.debug("Send htmlStatus: " + htmlStatus);
            }
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
