package controllers;

import bot.VenitianBot;
import play.libs.F;
import play.mvc.WebSocket;
import play.mvc.WebSocket.In;
import play.mvc.WebSocket.Out;
import play.Logger;

/**
 * Created by Valentin on 14/04/15.
 * This class represents the websocket through which the browser client-side and the server communicate.
 * This is mostly used for sending tweets to display to the interface
 */
public class VenitianWSocket extends WebSocket<String> {
    private Out<String> out;

    /**
     * This callback is activated when the websocket is ready to be used
     * We add a callback to the socket so that when one of the streams is closed, then we close the socket
     * @param in the In stream (client -> server)
     * @param out the Out stream (server -> client)
     */
    @Override
    public void onReady(In<String> in, Out<String> out) {
        Logger.debug("Connected");
        final VenitianWSocket self = this;
        in.onMessage(new F.Callback<String>() {
            @Override
            public void invoke(String s) throws Throwable {
                Logger.debug("Received " + s + " through web socket");
            }
        });

        in.onClose(new F.Callback0() {
            public void invoke() {
                VenitianBot.INSTANCE.getStreamListener().removeWSocket(self);
                Logger.debug("Disconnected");
            }
        });
        this.out = out;
    }

    /**
     * Sends a message from server to client through the socket
     * @param htmlStatus the status as HTML to be sent to the client
     */
    public void sendMessage(String htmlStatus) {
        out.write(htmlStatus);
    }
}
