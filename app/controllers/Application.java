package controllers;

import bot.VenitianBot;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;
import views.html.display_tweets;
import views.html.index;

public class Application extends Controller {

    public static Result index() {
        return ok(index.render("Your new application is ready."));
    }

    public static Result displayTweets() {
        return ok(display_tweets.render(VenitianBot.INSTANCE.getDB().getTweets()));
    }

    public static WebSocket<String> getSocket() {
        VenitianWSocket newWSocket = new VenitianWSocket();
        VenitianBot.INSTANCE.getStreamListener().addWSocket(newWSocket);
        return newWSocket;
    }

    public static Result config() {
        return ok(views.html.conf.render());
    }
}
