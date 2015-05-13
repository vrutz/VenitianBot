package tweets;

import twitter4j.GeoLocation;

public class Location {
    private static double EARTH_RADIUS = 6371.0;

    private String name;
    private double latitude;
    private double longitude;
    private double radius;

    public Location(String name, double latitude, double longitude,
                    double radius) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
    }

    public Location(double latitude, double longitude) {
        this("", latitude, longitude, 0.0);
    }

    public Location(GeoLocation geoLoc) {
        this(geoLoc.getLatitude(), geoLoc.getLongitude());
    }

    public String getName() {
        return name;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getRadius() {
        return radius;
    }

    /*
     * Calculate the great-circle distance (shortest distance between two points
     * on the surface of a sphere) given the latitude and longitude coordinates.
     * Use the Haversine formula (precise but costly to compute).
     */
    // Only used in tests for now
    public double getGreatCircleDistance(double latitudeDegree,
                                         double longitudeDegree) {
        double latitudeRadian1 = Math.PI * this.latitude / 360;
        double latitudeRadian2 = Math.PI * latitudeDegree / 360;
        double deltaLatitude = Math.PI * (latitudeDegree - this.latitude) / 360;
        double deltaLongitude = Math.PI * (longitudeDegree - this.longitude)
                / 360;

        double a = Math.sin(deltaLatitude / 2) * Math.sin(deltaLatitude / 2)
                + Math.cos(latitudeRadian1) * Math.cos(latitudeRadian2)
                * Math.sin(deltaLongitude / 2) * Math.sin(deltaLongitude / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }

    /*
     * Use the Equirectangular projection to compute the distance between two
     * locations. It is fast to compute but less accurate than the one using
     * Haversine formula. It should be enough for our purpose though, it is
     * still accurate for small distances (ok for monuments and we don't need
     * high accuracy for Venice location).
     */
    public double getEquirectangularDistance(double latitudeDegree,
                                             double longitudeDegree) {
        double latitudeRadian1 = Math.PI * this.latitude / 360;
        double latitudeRadian2 = Math.PI * latitudeDegree / 360;
        double longitudeRadian1 = Math.PI * this.longitude / 360;
        double longitudeRadian2 = Math.PI * longitudeDegree / 360;

        double x = (longitudeRadian2 - longitudeRadian1)
                * Math.cos((latitudeRadian1 + latitudeRadian2) / 2);
        double y = latitudeRadian2 - latitudeRadian1;

        return Math.sqrt(x * x + y * y) * EARTH_RADIUS;
    }

    public boolean contains(double latitude, double longitude) {
        double distance = getEquirectangularDistance(latitude, longitude);

        return distance < radius;
    }

    public boolean contains(GeoLocation geoLoc) {
        return this.contains(geoLoc.getLatitude(), geoLoc.getLongitude());
    }

    public boolean contains(Location location) {
        return this.contains(location.getLatitude(), location.getLongitude());
    }

}
