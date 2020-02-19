package hotelapp.Data;

/**
 * Class used to store Review Data from Db during session
 */
public class ReviewDbData {

    private String reviewTitle;
    private String reviewText;
    private String userName;
    private String rating;
    private String time;
    private String hotelId;
    private String hotelName;

    /** Constructor for this class */
    public ReviewDbData(String reviewTitle, String reviewText, String userName, String rating, String time, String hotelId, String hotelName) {
        this.reviewText = reviewText;
        this.reviewTitle = reviewTitle;
        this.userName = userName;
        this.time = time;
        this.rating = rating;
        this.hotelId = hotelId;
        this.hotelName = hotelName;
    }

    /** List of getters and setters */


    public String getHotelId() {
        return hotelId;
    }

    public String getHotelName() {
        return hotelName;
    }

    public String getRating() {
        return rating;
    }

    public String getReviewTitle() {
        return reviewTitle;
    }

    public String getUserName() {
        return userName;
    }

    public String getTime() {
        return time;
    }

    public String getReviewText() {
        return reviewText;
    }

    public void setReviewTitle(String reviewTitle) {
        this.reviewTitle = reviewTitle;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
