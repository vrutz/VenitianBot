package utilities;

import java.util.HashSet;
import java.util.Iterator;
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

    // If no tags are given, they will be extracted from the tweet
    public Response(String tweet) {
        this.tweet = tweet;
        this.tags = extractTags();
        this.rank = MAXIMAL_RANK;
    }

    public Response(String tweet, Set<String> tags) {
        this.tags = new HashSet<>(tags);
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


    private Set<String> extractTags() {
        Set<String> tags = new HashSet<>();

        for(String keyWord: JSONReader.keyWords) {
            if(tweet.toLowerCase().replaceAll(" ", "").contains(keyWord)) {
                tags.add(keyWord);
            }
        }

        for(String keyWord: JSONReader.keyWordsGeneral) {
            if(tweet.toLowerCase().replaceAll(" ", "").contains(keyWord)) {
                tags.add(keyWord);
            }
        }

        return tags;
    }

    @Override
    public String toString() {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("{");

        Iterator<String> iter = tags.iterator();
        if(iter.hasNext()) {
            strBuilder.append(iter.next());
        }

        while(iter.hasNext()) {
            strBuilder.append(", ").append(iter.next());
        }

        strBuilder.append("} ");
        strBuilder.append(tweet);

        return strBuilder.toString();
    }
}
