package status;

import java.util.ArrayList;
import java.util.List;

import twitter4j.Status;
import utilities.LocationBox;
import utilities.Place;
import utilities.Utilities;

public class Classifier {
    private static Place[] places;
    private static LocationBox veniseLocation;
    private static String[] keyWords;
    private static String[] keyWordsGeneral;
    private static String[] blackListWords;

    private static int threshold = 8;
    // weight of the location in ranking
    public static int alpha = 4;
    // weight of the exact location in ranking
    public static int beta = 12;
    // weight of the hashtags and keywords in ranking
    public static int gama = 8;

    public static void init() {
        places = Utilities.readMonumentsLocation();
        veniseLocation = Utilities.readGeoLocation();
        keyWords = Utilities.readKeywords();
        keyWordsGeneral = Utilities.readKeywordsGeneral();
        blackListWords = Utilities.readKeywordsBlackList();
    }

    public static List<RankedStatus> classifyByLocation(List<Status> statuses) {
        List<RankedStatus> rankedStatuses = new ArrayList<RankedStatus>();

        for (Status status : statuses) {
            rankedStatuses.add(classify(status));
        }

        return rankedStatuses;
    }

    public static RankedStatus classify(Status status) {
        RankedStatus rankedStatus = new RankedStatus(status);
        String content = rankedStatus.getContent().getText().toLowerCase()
                .replaceAll(" ", "").replaceAll("#", "");

        for (String word : blackListWords) {
            if (content.contains(word))
                return rankedStatus;
        }
        // if (content.contains("beach") || content.contains("ocean")
        // || content.contains("sea") || content.contains("sand"))
        // return rankedStatus;

        if (rankedStatus.getContent().getUser().getName().toLowerCase()
                .equals("venitianbot"))
            return rankedStatus;

        if (rankedStatus.getContent().getUser().getScreenName().toLowerCase()
                .equals("venitianbot"))
            return rankedStatus;

        for (Place place : places) {
            // If a tweet has a recorded location, check if it is in this place
            if (rankedStatus.getLocation() != null) {
                if (place.contains(rankedStatus.getLocation())) {
                    rankedStatus.incRank(beta);
                    rankedStatus.addTag(place.getName());
                    System.out.println("Got precise location!");
                } else if (veniseLocation.contains(rankedStatus.getLocation())) {
                    rankedStatus.incRank(gama);
                    System.out.println("Got Venice location!");
                }
            }

            // Check if the tweet contains a keyword associated with this place
            for (String monumentTag : place.getTags()) {
                if (rankedStatus.getContent().getText().toLowerCase()
                        .contains(monumentTag)) {
                    rankedStatus.incRank(gama);
                    rankedStatus.addTag(place.getName());
                    System.out.println("Got precise tag " + monumentTag + "!");
                }
            }

        }

        // check if word contains any of the keywords
        for (String s : keyWords) {
            if (content.contains(s)) {
                rankedStatus.incRank(gama);
            }
        }
        // check for general keywords that could improve the status, we can only
        // take them into account if some of the others keywords are present,
        // otherwise they are too general
        if (rankedStatus.getRank() > 0) {
            for (String word : keyWordsGeneral) {
                if (content.contains(word)) {
                    rankedStatus.incRank(alpha);
                }
            }
        }

        rankedStatus.setRelevance(rankedStatus.getRank() >= threshold
                && !rankedStatus.getContent().getText().toLowerCase()
                .contains("beach"));
        return rankedStatus;
    }

}
