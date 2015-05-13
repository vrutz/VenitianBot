package tweets;

import tweets.Location;

import java.util.HashSet;
import java.util.Set;

public class Place extends Location {
    private Set<String> tags;

    Place(String name, Set<String> tags, double latitude, double longitude, double radius) {
        super(name, latitude, longitude, radius);
        this.tags = tags;
    }

    public Set<String> getTags() {
        return new HashSet<String>(tags);
    }

    public void addTag(String newTag) {
        tags.add(newTag);
    }

    @Override
    public String toString() {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("Name: " + this.getName() + "\nTags: ");
        for (String tag : tags) {
            strBuilder.append(tag + ", ");
        }
        strBuilder.append("\nLatitude: " + this.getLatitude());
        strBuilder.append("\nLongitude: " + this.getLongitude());
        strBuilder.append("\nRadius: " + this.getRadius());

        return strBuilder.toString();
    }
}
