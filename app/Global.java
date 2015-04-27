import bot.VenitianBot;
import play.Application;
import play.GlobalSettings;
import play.Logger;
import play.api.Play;

import java.sql.SQLException;

/**
 * Created by Valentin on 01/04/15.
 */
public class Global extends GlobalSettings {
    @Override
    public void onStart(Application app){
        Logger.info("Application started!");
        try {
            VenitianBot.INSTANCE.init();
        } catch(SQLException e) {
            Logger.error("Could not create the DB");
            Logger.error(e.toString());
            Play.stop();
        }
    }

    @Override
    public void onStop(Application app){
        Logger.info("Application stopped!");
        VenitianBot.INSTANCE.stopStream();
        VenitianBot.INSTANCE.getDB().closeConnection();
    }
}
