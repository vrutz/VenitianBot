package controllers;

import bot.VenitianBot;
import play.libs.F;
import play.mvc.WebSocket;
import play.Logger;

/**
 * Created by Valentin on 14/04/15.
 */
public class VenitianWSocket extends WebSocket<String> {
    private Out<String> out;

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

    public void sendMessage(String htmlStatus) {
        out.write(htmlStatus);
    }
}
