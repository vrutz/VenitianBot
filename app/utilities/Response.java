package utilities;

import java.util.HashSet;
import java.util.Set;

/**
 * Represent an answer that we can send on twitter. A response has a set of
 * associated keywords that can be used to increase its rank, as well as the
 * actual answer.
 *
 * @author mathieu
 */
public class Response implements Comparable<Response> {
    private static final int MAXIMAL_RANK = 100;
    private Set<String> tags;
    private String tweet;
    private int rank;

    public Response(Set<String> tags, String tweet) {
        this.tags = new HashSet<String>(tags);
        this.tweet = tweet;
        this.rank = MAXIMAL_RANK;
    }

    public Response(Response response) {
        this.tags = response.getTags();
        this.tweet = response.getTweet();
        this.rank = MAXIMAL_RANK;
    }

    public Set<String> getTags() {
        return new HashSet<String>(tags);
    }

    public void addTag(String newTag) {
        tags.add(newTag);
    }

    public boolean containsTag(String tag) {
        return tags.contains(tag);
    }

    public String getTweet() {
        return tweet;
    }

    public void setTweet(String newTweet) {
        tweet = newTweet;
    }

    public int getRank() {
        return rank;
    }

    public void incRank() {
        rank--;
    }

    @Override
    public int compareTo(Response r2) {
        return rank - r2.rank;
    }

    @Override
    public String toString() {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("{");
        for (String tag : tags) {
            strBuilder.append(tag + ", ");
        }
        strBuilder.append("} ");
        strBuilder.append(tweet);

        return strBuilder.toString();
    }
}
