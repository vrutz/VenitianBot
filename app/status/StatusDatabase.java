package status;

import play.Logger;
import play.db.DB;
import twitter4j.Status;
import utilities.Location;

import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Valentin on 08/04/15.
 * This class represents the database into which we store the tweets we ranked as interesting
 */
public class StatusDatabase {

    private Connection connection;

    //Database
    private static final String TABLE_NAME = "tweetsTable";
    private static final String ID = "id";
    private static final String DATE = "date";
    private static final String USER = "user";
    private static final String CONTENT = "content";
    private static final String FAVORITE_COUNT = "favoriteCount";
    private static final String LATITUDE = "latitude";
    private static final String LONGITUDE = "longitude";
    private static final String RETWEET_COUNT = "retweetCount";
    private static final String RANK = "rank";

    public StatusDatabase() {
        connection = DB.getConnection();
    }

    /**
     * Initializes the table in the database
     * @return the databse object (this)
     */
    public StatusDatabase init() {
        try {
            connection.createStatement().execute(
                    "CREATE TABLE " + TABLE_NAME + " (" +
                            ID + " BIGINT NOT NULL AUTO_INCREMENT, " +
                            USER + " CHAR(18) NOT NULL, " +
                            DATE + " DATE NOT NULL, " +
                            CONTENT + " CHAR(140) NOT NULL, " +
                            FAVORITE_COUNT + " INT, " +
                            RETWEET_COUNT + " INT, " +
                            LATITUDE + " DOUBLE, " +
                            LONGITUDE + " DOUBLE, " +
                            RANK + " INT, " +
                            "PRIMARY KEY (" + ID + "))");
        } catch (SQLException e) {
            Logger.debug("Database already exists or impossible to create.");
        }

        return this;
    }

    /**
     * Closes the connection to the database
     */
    public void closeConnection() {
        try {
            connection.close();
        } catch (SQLException e) {
            Logger.error("Could not close connection to the DB");
            Logger.error(e.toString());
        }
    }

    /**
     * Removes all the tweets form the table
     * @return true if all rows where removed, false otherwise
     */
    public boolean clean() {
        try {
            connection.createStatement().execute("DELETE FROM " + TABLE_NAME);
        } catch (SQLException e) {
            Logger.error("Could not delete all rows in table " + TABLE_NAME);
            Logger.error(e.toString());
            return false;
        }
        return true;
    }

    /**
     * Drops the table itself
     * @return true if the table was dropped successfully, false otherwise
     */
    public boolean drop() {
        try {
            connection.createStatement().execute("DROP TABLE " + TABLE_NAME);
        } catch (SQLException e) {
            Logger.error("Could not drop table " + TABLE_NAME);
            Logger.error(e.toString());
            return false;
        }
        return true;
    }

    /**
     * Adds a row representing a tweet to the table
     * @param user the user of the interesting tweet
     * @param date the date of the interesting tweet
     * @param content the content of the interesting tweet
     * @param favCount the favorite count of the interesting tweet
     * @param retwtCount the retweet count of the interesting tweet
     * @param loc the location of the interesting tweet
     * @param rank the rank of the interesting tweet
     * @return true if the tweet was inserted successfully, false otherwise
     */
    public boolean insertIntoDB(String user, Date date, String content,
                                int favCount, int retwtCount, Location loc,
                                int rank) {
        Logger.debug("INSERT INTO " + TABLE_NAME + "(" +
                USER + "," + DATE + "," + CONTENT + "," +
                FAVORITE_COUNT + "," + RETWEET_COUNT + "," +
                LATITUDE + "," + LONGITUDE + "," +
                RANK +
                ") VALUES (" +
                user + "," + new Date(date.getTime()).toString() + ",\'" +
                content + "\'," + favCount + "," + retwtCount + "," +
                loc.getLatitude() + "," + loc.getLongitude() + "," +
                rank + ")");
        try {
            connection.createStatement().execute(
                    "INSERT INTO " + TABLE_NAME + "(" +
                            USER + "," + DATE + "," + CONTENT + "," +
                            FAVORITE_COUNT + "," + RETWEET_COUNT + "," +
                            LATITUDE + "," + LONGITUDE + "," +
                            RANK +
                            ") VALUES (" +
                            user + "," + new Date(date.getTime()).toString() + ",\'" +
                            content + "\'," + favCount + "," + retwtCount + "," +
                            loc.getLatitude() + "," + loc.getLongitude() + "," +
                            rank + ")"
            );
        } catch (SQLException e) {
            Logger.error(e.toString());
            return false;
        }
        return true;
    }

    /**
     * Inserts a RankedStatus into the table
     * @param status the RankedStatus to add to the table
     * @return true if the tweet was inserted successfully, false otherwise
     */
    public boolean insertIntoDB(RankedStatus status) {
        Status tweet = status.getContent();
        return insertIntoDB(
                tweet.getUser().getScreenName(),
                new Date(tweet.getCreatedAt().getTime()), tweet.getText(),
                tweet.getFavoriteCount(), tweet.getRetweetCount(),
                status.getLocation(), status.getRank());
    }

    /**
     * @return all tweets in the table
     */
    public List<SimpleStatus> getTweets() { // since has format "dd-MM-yyyy"
        List<SimpleStatus> statuses = new ArrayList<>();
        try {
            Statement stmt = connection.createStatement();
            ResultSet res = stmt.executeQuery(
                    "SELECT TOP 10 * FROM " + TABLE_NAME + " " +
                            "WHERE " + DATE + " >= CURRENT_DATE");

            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            while (res.next()) {
                try {
                    statuses.add(new SimpleStatus(
                            new Date(df.parse(res.getString(DATE)).getTime()),
                            res.getString(USER), res.getString(CONTENT),
                            res.getInt(FAVORITE_COUNT), res.getInt(RETWEET_COUNT),
                            new Location(res.getDouble(LATITUDE),
                                    res.getDouble(LONGITUDE)),
                            res.getInt(RANK)));
                } catch (ParseException e) {
                    Logger.error("Could not parse " + res.getString(DATE));
                    Logger.error(e.toString());
                }
            }
        } catch (SQLException e) {
            Logger.error("Could not retrieve tweets");
            Logger.error(e.toString());
        }
        return statuses;
    }

}
