package status;

import twitter4j.Status;
import utilities.Location;

import java.io.Serializable;
import java.sql.Date;

/**
 * Created by Valentin on 08/04/15.
 */
public class SimpleStatus {

    private final Date DATE;
    private final String USER;
    private final String CONTENT;
    private final int FAVORITE_COUNT;
    private final int RETWEET_COUNT;
    private final Location LOCATION;
    private final int RANK;

    public SimpleStatus(Date date, String user, String content,
                        int favCount, int retweetCount, Location loc, int rank) {
        this.DATE = date;
        this.USER = user;
        this.CONTENT = content;
        this.FAVORITE_COUNT = favCount;
        this.RETWEET_COUNT = retweetCount;
        this.LOCATION = loc;
        this.RANK = rank;
    }

    public SimpleStatus(RankedStatus status) {
        Status s = status.getContent();
        this.DATE = new Date(s.getCreatedAt().getTime());
        this.USER = s.getUser().getScreenName();
        this.CONTENT = s.getText();
        this.FAVORITE_COUNT = s.getFavoriteCount();
        this.RETWEET_COUNT = s.getFavoriteCount();
        this.LOCATION = status.getLocation();
        this.RANK = status.getRank();
    }

    public String toHTML() {
        return "<div class=\"panel panel-default status\">" +
                "<div class=\"panel-heading\">" + USER + "</div>" +
                "<div class =\"panel-content\">" +
                "<p> Tweet: " + CONTENT + "</p>" +
                "<p> Date: " + DATE.toString() + "</p>" +
                "<p> Rank: " + RANK + "</p>" +
                "</div>" +
                "</div>";
    }
}
