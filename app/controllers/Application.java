package controllers;

import bot.VenitianBot;
import com.fasterxml.jackson.databind.JsonNode;
import play.Logger;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;
import utilities.Response;
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

    @BodyParser.Of(BodyParser.Json.class)
    public static Result postConfig() {
        JsonNode json = request().body().asJson();
        Logger.debug("body of json is" + json.asText());
        String tweet = json.findValue("tweet").textValue();
        Logger.debug("tweet to add: " + tweet);
        VenitianBot.INSTANCE.readResponses().addNewResponse(new Response(tweet));
        return ok("The tweet has successfully been added to the db.");
    }
}
