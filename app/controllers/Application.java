package controllers;

import bot.VenitianBot;
import com.fasterxml.jackson.databind.JsonNode;
import play.Logger;
import play.api.libs.json.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;
import tweets.Response;
import views.html.display_tweets;
import views.html.index;

public class Application extends Controller {

    /**
     * Index page. Not useful for the bot though
     *
     * @return the HTTP response with the index page
     */
    public static Result index() {
        return ok(index.render("Your new application is ready."));
    }

    /**
     * Page where we display all the received tweets and the tweets the bot replies to and with.
     *
     * @return the HTTP response with the display_tweets page
     */
    public static Result displayTweets() {
        return ok(display_tweets.render(VenitianBot.INSTANCE.getDB().getTweets()));
    }

    /**
     * Creates a new Web socket from the new connection to display_tweets
     *
     * @return the WebSocket in which browser and server will communicate
     */
    public static WebSocket<String> getSocket() {
        VenitianWSocket newWSocket = new VenitianWSocket();
        VenitianBot.INSTANCE.getStreamListener().addWSocket(newWSocket);
        return newWSocket;
    }

    /**
     * The configuration page for the bot
     *
     * @return the HTTP response with the conf page
     */
    public static Result config() {
        return ok(views.html.conf.render());
    }

    /**
     * HTTP POST request for modifying the configuration
     *
     * @return the HTTP OK response if all goes well
     */
//    @BodyParser.Of(BodyParser.Json.class)
    public static Result postConfig() {
        JsonNode json = request().body().asJson();
        String tweet = json.findPath("tweet").asText();
        Logger.debug("tweet to add: " + tweet);
        VenitianBot.INSTANCE.readResponses().addNewResponse(new Response(tweet));
        return ok("The tweet has successfully been added to the db.");
    }
}
