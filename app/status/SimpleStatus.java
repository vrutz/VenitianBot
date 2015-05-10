package status;

import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;
import twitter4j.Status;
import utilities.Location;

import java.sql.Date;

/**
 * Created by Valentin on 08/04/15.
 */
public class SimpleStatus {

    private final Date DATE;
    private final String USER;
    private final String CONTENT;
    private final String REPLY_SCREEN_NAME;
    private final String REPLY_CONTENT;
    private final int FAVORITE_COUNT;
    private final int RETWEET_COUNT;
    private final Location LOCATION;
    private final int RANK;

    public SimpleStatus(Date date, String content, String replyScreenName, String replyContent) {
        this.DATE = date;
        this.USER = "VenitianBot";
        this.CONTENT = content;
        this.FAVORITE_COUNT = 0;
        this.RETWEET_COUNT = 0;
        this.LOCATION = null;
        this.RANK = 0;
        this.REPLY_SCREEN_NAME = replyScreenName;
        this.REPLY_CONTENT = replyContent;
    }

    public SimpleStatus(Date date, String user, String content,
                        int favCount, int retweetCount, Location loc, int rank) {
        this.DATE = date;
        this.USER = user;
        this.CONTENT = content;
        this.FAVORITE_COUNT = favCount;
        this.RETWEET_COUNT = retweetCount;
        this.LOCATION = loc;
        this.RANK = rank;
        this.REPLY_SCREEN_NAME = "";
        this.REPLY_CONTENT = "";
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
        this.REPLY_SCREEN_NAME = "";
        this.REPLY_CONTENT = "";
    }

    public ObjectNode toJson() {
        return Json.newObject().put("level", getLevel())
                .put("user", USER)
                .put("content", CONTENT)
                .put("date", DATE.toString())
                .put("rank", (100 - RANK));
    }

    public ObjectNode toBotJson() {
        return Json.newObject().put("level", getLevel())
                .put("replyToScreenName", REPLY_SCREEN_NAME)
                .put("replyToContent", REPLY_CONTENT)
                .put("content", CONTENT)
                .put("date", DATE.toString());
    }

    private String getLevel() {
        if(RANK > 80) {
            return"panel-danger";
        } else if(RANK > 50) {
            return "panel-warning";
        } else if(RANK > 30) {
            return "panel-info";
        } else {
            return "panel-success";
        }
    }
}
