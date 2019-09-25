package pendingReports;

import com.google.firebase.Timestamp;

public class PendingReports {

    private String imageURL, category, city, description, documentID, location, userID;
    private Timestamp timestamp;
    private double latitude, longitude;

    public PendingReports() {}

    public PendingReports(String imageURL, String category, String city, String description, String documentID, String location, String userID, Timestamp timestamp, double latitude, double longitude) {
        this.imageURL = imageURL;
        this.category = category;
        this.city = city;
        this.description = description;
        this.documentID = documentID;
        this.location = location;
        this.userID = userID;
        this.timestamp = timestamp;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getImageURL() {
        return imageURL;
    }

    public String getCategory() {
        return category;
    }

    public String getCity() {
        return city;
    }

    public String getDescription() {
        return description;
    }

    public String getDocumentID() {
        return documentID;
    }

    public String getLocation() {
        return location;
    }

    public String getUserID() {
        return userID;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
