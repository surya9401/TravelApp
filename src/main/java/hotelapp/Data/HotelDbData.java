package hotelapp.Data;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Data Class for storing dynamic Db Data
 */
public class HotelDbData {

    private String id;

    private String name;

    private String address;

    private String linkAddress;

    private String cityName;

    private List<ReviewDbData> reviewData;

    private String lat;

    private String lng;

    private String averageRatings;

    private static DecimalFormat df = new DecimalFormat("0.00");


    /** Constructor for this class */
    public HotelDbData(String id, String name, String address, String linkAddress, List<ReviewDbData> reviewData, String cityName, String lat, String lng, double averageRatings) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.linkAddress = linkAddress;
        this.reviewData = reviewData;
        this.cityName = cityName;
        this.lat = lat;
        this.lng = lng;
        this.averageRatings = df.format(averageRatings);
    }

    /**
     * Below List of Getters and Setters
     */


    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getLink() {
        return linkAddress;
    }

    public String getCity() {
        return cityName;
    }

    public List<ReviewDbData> getReviewData() {
        return reviewData;
    }

    public String getAverageRatings() {
        return averageRatings;
    }

    public String getId() {
        return id;
    }

    public String getLat() {
        return lat;
    }

    public String getLng() {
        return lng;
    }
}
