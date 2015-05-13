package tweets;

import org.apache.commons.lang3.tuple.Pair;

import tweets.Location;
import twitter4j.GeoLocation;

public class LocationBox {
    private final Pair<Location, Location> box;

    public LocationBox(Location swCorner, Location neCorner) {
        box = Pair.of(swCorner, neCorner);
    }

    public Location getSW() {
        return box.getLeft();
    }

    public Location getNE() {
        return box.getRight();
    }

    public boolean contains(GeoLocation location) {
        return contains(new Location("", location.getLatitude(), location.getLongitude(), 0.0));
    }

    public boolean contains(Location location) {
        return getSW().getLatitude() <= location.getLatitude()
                && getSW().getLongitude() <= location.getLongitude()
                && getNE().getLatitude() >= location.getLatitude()
                && getNE().getLongitude() >= location.getLongitude();
    }
}
