import bot.VenitianBot;
import play.Application;
import play.GlobalSettings;
import play.Logger;
import play.api.mvc.Results;
import play.libs.F;
import play.libs.Scala;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;
import scala.Tuple2;
import scala.collection.Seq;

import java.util.ArrayList;
import java.util.List;

import static play.core.j.JavaResults.*;

/**
 * Created by Valentin on 01/04/15.
 * These are the settings of the Play application
 * Mostly callbacks for when a page is loaded and requests received
 */
@SuppressWarnings("unused")
public class Global extends GlobalSettings {

    /**
     * Called when someone loads a page
     * @param app the Play application
     */
    @Override
    public void onStart(Application app) {
        Logger.info("Application started!");
        VenitianBot.INSTANCE.init();
    }

    /**
     * Called when someone closes a page
     * @param app the Play application
     */
    @Override
    public void onStop(Application app) {
        Logger.info("Application stopped!");
        VenitianBot.INSTANCE.stopBot();
    }

    /**
     * Request wrapper that adds the "Access-Control-Allow-Origin: *" header to each request going out.
     * Mandatory for CORS (when the client uses HTTP actions other than GET like POST)
     */
    private class ActionWrapper extends Action.Simple {
        public ActionWrapper(Action<?> action) {
            this.delegate = action;
        }

        /**
         * Adds the CORS header to the response
         */
        @Override
        public F.Promise<Result> call(Http.Context ctx) throws java.lang.Throwable {
            F.Promise<Result> result = this.delegate.call(ctx);
            Http.Response response = ctx.response();
            response.setHeader("Access-Control-Allow-Origin", "*");
            return result;
        }
    }

    /**
     * Adds the required CORS header "Access-Control-Allow-Origin" to successfull requests
     */
    @Override
    public Action<?> onRequest(Http.Request request, java.lang.reflect.Method actionMethod) {
        return new ActionWrapper(super.onRequest(request, actionMethod));
    }

    private static class CORSResult implements Result {
        final private play.api.mvc.Result wrappedResult;

        public CORSResult(Results.Status status) {
            List<Tuple2<String, String>> list = new ArrayList<>();
            Tuple2<String, String> t = new Tuple2<>("Access-Control-Allow-Origin","*");
            list.add(t);
            Seq<Tuple2<String, String>> seq = Scala.toSeq(list);
            wrappedResult = status.withHeaders(seq);
        }

        public play.api.mvc.Result toScala() {
            return this.wrappedResult;
        }
    }

    /**
    * Adds the required CORS header "Access-Control-Allow-Origin" to bad requests
    */
    @Override
    public F.Promise<Result> onBadRequest(Http.RequestHeader request, String error) {
        return F.Promise.<Result>pure(new CORSResult(BadRequest()));
    }

    /**
    * Adds the required CORS header "Access-Control-Allow-Origin" to requests that causes an exception
    */
    @Override
    public F.Promise<Result> onError(Http.RequestHeader request, Throwable t) {
        return F.Promise.<Result>pure(new CORSResult(InternalServerError()));
    }

    /**
    * Adds the required CORS header "Access-Control-Allow-Origin" when a route was not found
    */
    @Override
    public F.Promise<Result> onHandlerNotFound(Http.RequestHeader request) {
        return F.Promise.<Result>pure(new CORSResult(NotFound()));
    }
}
