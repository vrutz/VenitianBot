package status;

import play.db.ebean.Model;
import twitter4j.Status;
import utilities.Location;

/**
 * Encapsulates the content of a fetched status, after processing, together with
 * the additional information learned from classification.
 *
 * @author zhivka
 *
 */
public class RankedStatus extends Model implements Comparable<RankedStatus> {
	private String user;
	private Status content; // will be immutable
	private int rank;
	private Location location; // will be immutable
	private boolean isRelevant;

	public RankedStatus(Status content, int rank) {
		this.content = content;
		this.rank = rank;
		if(content.getGeoLocation() != null)
			this.location = new Location(content.getGeoLocation());
		else this.location =null;

		this.isRelevant = false;
	}

	public Status getContent() {
		return content; // need to return a copy
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int newRank) {
		this.rank = newRank; // not sure if we put a setter, will the rank
		// decrease?
	}

	/**
	 * Increments the rank by increment and returns the new rank
	 *
	 * @param increment
	 *            the quantity we want to add to the current rank
	 * @return
	 */
	public int incRank(int increment) {
		rank += increment;
		return rank;
	}

	/**
	 * Returns the location of the tweet
	 *
	 * @return
	 */
	public Location getLocation() {
		if (location == null)
			return null;

		return new Location(location.getName(), location.getLatitude(),
				location.getLongitude(), location.getRadius());
	}

	/**
	 * Sets the location of the object to location if the current location has
	 * not been initialized yet. If the location of the tweet has previously
	 * been initialized, it doesn't modify it.
	 *
	 * @param location
	 */
	public void setLocation(Location location) {
		if (this.location == null)
			this.location = location;
	}

	public boolean isRelevant() {
		return isRelevant;
	}

	public void setRelevance(boolean relevant) {
		isRelevant = relevant;
	}

	/**
	 * Compares two RankedStatus objects by their ranks.
	 */
	@Override
	public int compareTo(RankedStatus r2) {
		// compareTo should return < 0 if this is supposed to be
		// less than r2, > 0 if this is supposed to be greater than
		// r2 and 0 if they are supposed to be equal
		if (rank < r2.rank)
			return -1;
		if (rank > r2.rank)
			return 1;

		return 0;
	}
}