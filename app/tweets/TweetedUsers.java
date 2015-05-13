package tweets;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Zhivka Gucevska
 */

/**
 * This class keeps track of the users the Bot has tweeted: it reads the saved users from the last session of the bot
 * and saves the tweeted users when the bot is stopped. If enables the bot to check if a user can be tweeted. A user
 * can be tweeted if she has not been tweeted in the last 2 days.
 */
public class TweetedUsers {
    private static final long FREEZE_TIME = 2 * 86400000; //2 days //1 day = 86400000 millis
    private static final String FILE_PATH = "./app/assets/tweeted-users.csv";

    private HashMap<Long, Long> users;

    public TweetedUsers() {
        users = utilities.Utilities.readTweetedUsers(FILE_PATH);
        cleanUsers();
    }

    /**
     * @param userID the user we want to tweet
     * @return We can tweet the user only if she has not been tweeted the last 2 days. This function checks if it's the case
     */
    public boolean isTweetable(Long userID) {
        return !users.containsKey(userID) || System.currentTimeMillis() - FREEZE_TIME < users.get(userID);
    }

    /**
     * This function should be called every time we tweet someone
     *
     * @param userID the user we have tweeted
     */
    public void addUser(Long userID) {
        users.put(userID, System.currentTimeMillis());
    }

    /**
     * This function should be called before shutting down the bot - it will write to a file all the users that have been
     * tweeted in the current session. It can also be called during 1 time per day to update the file
     */
    public void saveUsers() {
        cleanUsers();
        utilities.Utilities.writeTweetedUsers(users, FILE_PATH);
    }

    /**
     * This function removes the outdated users i.e. users that have not been tweeted in more than 2 days (can change)
     */
    public void cleanUsers() {
        Iterator<Map.Entry<Long, Long>> iter = users.entrySet().iterator();
        while(iter.hasNext()) {
            Map.Entry<Long, Long> entry = iter.next();
            if(entry.getValue() < System.currentTimeMillis() - FREEZE_TIME) {
                iter.remove();
            }
        }
    }
}
