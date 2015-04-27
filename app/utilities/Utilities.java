package utilities;

import java.io.FileReader;
import java.io.IOException;
import play.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * 
 * @author zhivka
 * 
 */
public class Utilities {

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
			Logger.error(e.toString());
		} finally {
			if (queryFile != null) {
				try {
					queryFile.close();
				} catch (IOException e) {
					Logger.error(e.toString());
				}
			}
		}
		return new String[] {};
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
			Logger.error(e.toString());
		} finally {
			if (fr != null) {
				try {
					fr.close();
				} catch (IOException e) {
					Logger.error(e.toString());
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

	public static Location[] readMonumentsLocation() {
		JSONParser parser = new JSONParser();
		FileReader fr = null;
		Location[] locations;
		try {
			fr = new FileReader("./app/assets/monuments_location.json");
			JSONObject locsJSON = (JSONObject) parser.parse(fr);

			JSONArray jArray = (JSONArray) locsJSON.get("locations");
			String[] locationsName = new String[jArray.size()];

			for (int i = 0; i < jArray.size(); ++i) {
				locationsName[i] = (String) jArray.get(i);
			}

			locations = new Location[locationsName.length];
			for (int i = 0; i < locationsName.length; ++i) {
				JSONObject loc = (JSONObject) locsJSON.get(locationsName[i]);

				double latitude = (double) loc.get("latitude");
				double longitude = (double) loc.get("longitude");
				double radius = (double) loc.get("radius");
				locations[i] = new Location(locationsName[i], latitude,
						longitude, radius);
			}
			return locations;
		} catch (IOException e) {
			Logger.error(e.toString());
		} catch (ParseException e) {
			Logger.error(e.toString());
		} finally {
			if (fr != null) {
				try {
					fr.close();
				} catch (IOException e) {
					Logger.error(e.toString());
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
