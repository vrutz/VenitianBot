package utilities;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * @author zhivka
 */
public class Utilities {
    private final static Logger LOG = LogManager.getLogger(Utilities.class);

    public static String consHashtags(String keywords) {
        if (keywords != null || " ".equals(keywords)) {
            return keywords.replaceAll(" ", " #");
        }
        return keywords;
    }

    public static String[] readKeywords() {
        JSONParser parser = new JSONParser();
        FileReader queryFile = null;
        try {
            queryFile = new FileReader("./app/assets/query.json");
            JSONObject queryJSON = (JSONObject) parser.parse(queryFile);
            JSONArray hashtags = (JSONArray) queryJSON.get("hashtags");

            String[] hashtagsArray = new String[hashtags.size()];

            for (int i = 0; i < hashtagsArray.length; ++i) {
                hashtagsArray[i] = "" + hashtags.get(i);
            }

            return hashtagsArray;
        } catch (Exception e) {
            LOG.error(e);
        } finally {
            if (queryFile != null) {
                try {
                    queryFile.close();
                } catch (IOException e) {
                    LOG.error(e);
                }
            }
        }
        return new String[]{};
    }

    public static String[] readKeywordsGeneral() {
        JSONParser parser = new JSONParser();
        FileReader queryFile = null;
        try {
            queryFile = new FileReader("./app/assets/query.json");
            JSONObject queryJSON = (JSONObject) parser.parse(queryFile);
            JSONArray hashtags = (JSONArray) queryJSON.get("hashtags-general");

            String[] hashtagsArray = new String[hashtags.size()];

            for (int i = 0; i < hashtagsArray.length; ++i) {
                hashtagsArray[i] = "" + hashtags.get(i);
            }

            return hashtagsArray;
        } catch (Exception e) {
            LOG.error(e);
        } finally {
            if (queryFile != null) {
                try {
                    queryFile.close();
                } catch (IOException e) {
                    LOG.error(e);
                }
            }
        }
        return new String[]{};
    }

    public static String[] readKeywordsBlackList() {
        JSONParser parser = new JSONParser();
        FileReader queryFile = null;
        try {
            queryFile = new FileReader("./app/assets/query.json");
            JSONObject queryJSON = (JSONObject) parser.parse(queryFile);
            JSONArray hashtags = (JSONArray) queryJSON.get("blacklist");

            String[] hashtagsArray = new String[hashtags.size()];

            for (int i = 0; i < hashtagsArray.length; ++i) {
                hashtagsArray[i] = "" + hashtags.get(i);
            }

            return hashtagsArray;
        } catch (Exception e) {
            LOG.error(e);
        } finally {
            if (queryFile != null) {
                try {
                    queryFile.close();
                } catch (IOException e) {
                    LOG.error(e);
                }
            }
        }
        return new String[]{};
    }

    public static LocationBox readGeoLocation() {
        JSONParser parser = new JSONParser();
        FileReader fr = null;
        try {
            fr = new FileReader("./app/assets/query.json");
            JSONObject queryJSON = (JSONObject) parser.parse(fr);
            Location veniceSW = getLocationByName(queryJSON, "veniceSW");
            Location veniceNE = getLocationByName(queryJSON, "veniceNE");
            LocationBox loc = new LocationBox(veniceSW, veniceNE);
            return loc;
        } catch (Exception e) {
            LOG.error(e);
        } finally {
            if (fr != null) {
                try {
                    fr.close();
                } catch (IOException e) {
                    LOG.error(e);
                }
            }
        }
        return null;
    }

    private static Location getLocationByName(JSONObject json,
                                              String locationName) {
        JSONObject location = (JSONObject) json.get(locationName);

        double latitude = (double) location.get("latitude");
        double longitude = (double) location.get("longitude");
        double radius = (double) location.get("radius");

        Location loc = new Location(locationName, latitude, longitude, radius);
        return loc;
    }

    public static Place[] readMonumentsLocation() {
        JSONParser parser = new JSONParser();
        FileReader fr = null;
        Place[] monuments;
        try {
            fr = new FileReader("./app/assets/monuments_location.json");
            JSONObject placesJSON = (JSONObject) parser.parse(fr);

            JSONArray places = (JSONArray) placesJSON.get("places");
            String[] placesName = new String[places.size()];

            for (int i = 0; i < placesName.length; ++i) {
                placesName[i] = (String) places.get(i);
            }

            monuments = new Place[placesName.length];
            for (int i = 0; i < placesName.length; ++i) {
                String placeName = placesName[i];
                JSONObject place = (JSONObject) placesJSON.get(placeName);

                JSONArray tags = (JSONArray) place.get("tags");
                HashSet<String> tagSet = new HashSet<String>();

                for (int j = 0; j < tags.size(); ++j) {
                    tagSet.add((String) tags.get(j));
                }

                double latitude = (double) place.get("latitude");
                double longitude = (double) place.get("longitude");
                double radius = (double) place.get("radius");
                monuments[i] = new Place(placeName, tagSet, latitude,
                        longitude, radius);
                System.out.println(monuments[i]);
            }
            return monuments;
        } catch (IOException e) {
            LOG.error(e);
        } catch (ParseException e) {
            LOG.error(e);
        } finally {
            if (fr != null) {
                try {
                    fr.close();
                } catch (IOException e) {
                    LOG.error(e);
                }
            }
        }
        return null;
    }

    public static String[] getMonumentsKeyword(Location[] locations) {
        String[] monumentsKeyword = new String[locations.length];
        for (int i = 0; i < locations.length; ++i) {
            monumentsKeyword[i] = locations[i].getName();
        }
        return monumentsKeyword;
    }

}
