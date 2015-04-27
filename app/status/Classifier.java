package status;

import java.util.ArrayList;
import java.util.List;

import twitter4j.Status;
import utilities.Location;
import utilities.LocationBox;
import utilities.Utilities;

public class Classifier {
	private static Location[] monumentsLocation;
	private static LocationBox veniseLocation;
	private static String[] monumentsKeyword;

	private static int threshold = 8;
	// weight of the location in ranking
	public static int alpha = 8;
	// weight of the exact location in ranking
	public static int beta = 12;
	// weight of the hashtags and keywords in ranking
	public static int gama = 8;

	public static void init() {
		monumentsLocation = Utilities.readMonumentsLocation();
		veniseLocation = Utilities.readGeoLocation();
		monumentsKeyword = Utilities.getMonumentsKeyword(monumentsLocation);
	}

	public static List<RankedStatus> classifyByLocation(List<Status> statuses) {
		List<RankedStatus> rankedStatuses = new ArrayList<RankedStatus>();

		for (Status status : statuses) {
			rankedStatuses.add(classify(status));
		}

		return rankedStatuses;
	}

	public static RankedStatus classify(Status status) {
		RankedStatus rankedStatus = new RankedStatus(status, 0);
		if(rankedStatus.getContent().getText().toLowerCase().contains("venice beach")) return rankedStatus;


		if (rankedStatus.getLocation() != null) {
			for (Location loc : monumentsLocation) {
				if (loc.contains(rankedStatus.getLocation()))
					rankedStatus.incRank(beta);
			}
			if (rankedStatus.getRank() == 0) {
				if (veniseLocation.contains(rankedStatus.getLocation()))
					rankedStatus.incRank(alpha);
				else return rankedStatus; // Location available but not in Venice -> do we keep that or not?
			}
		}

		for (String monument : monumentsKeyword) {
			if (rankedStatus.getContent().getText().toLowerCase()
					.contains(monument))
				rankedStatus.incRank(gama);
		}

		if (rankedStatus.getContent().getText().toLowerCase()
				.contains("venice"))
			rankedStatus.incRank(gama);

		rankedStatus.setRelevance(rankedStatus.getRank() >= threshold);
		return rankedStatus;
	}

}
