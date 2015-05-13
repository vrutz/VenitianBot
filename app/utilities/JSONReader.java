package utilities;

import tweets.LocationBox;
import tweets.Place;

/**
 * Created by mathieu on 10/05/15.
 */
public class JSONReader {
    public static Place[] places = Utilities.readMonumentsLocation();
    public static LocationBox veniceLocation = Utilities.readGeoLocation();
    public static String[] keyWords = Utilities.readKeywords();
    public static String[] keyWordsGeneral = Utilities.readKeywordsGeneral();
    public static String[] blackListWords = Utilities.readKeywordsBlackList();

    /*public static void init() {
        places = Utilities.readMonumentsLocation();
        veniceLocation = Utilities.readGeoLocation();
        keyWords = Utilities.readKeywords();
        keyWordsGeneral = Utilities.readKeywordsGeneral();
        blackListWords = Utilities.readKeywordsBlackList();
    }

    public static Place[] getPlaces() {
        return places;
    }

    public static LocationBox getVeniceLocation() {
        return veniceLocation;
    }

    public static String[] getKeyWords() {
        return keyWords;
    }

    public static String[] getKeyWordsGeneral() {
        return keyWordsGeneral;
    }

    public static String[] getBlackListWords() {
        return blackListWords;
    }*/
}
