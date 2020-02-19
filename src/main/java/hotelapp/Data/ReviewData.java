package hotelapp.Data;

import java.util.Date;

/**
 * Class used to store Hotel Review Objects
 */

public class ReviewData {

    private static final String USER_ANONYMOUS = "Anonymous";

    private String hotelID;
    private String reviewID;
    private int rating;
    private String reviewText;
    private String title;
    private String nickName;
    private Date date;
    private boolean isRecommended;

    public ReviewData(String hotelID, String reviewID, int ratingScore, String reviewText, String title, String userNickName, Date reviewTime, boolean isRecommended) {
        this.hotelID = hotelID;
        this.reviewID = reviewID;
        this.rating = ratingScore;
        this.reviewText = reviewText;
        this.title = title;
        this.nickName = !userNickName.isEmpty() ? userNickName:USER_ANONYMOUS;
        this.date = reviewTime;
        this.isRecommended = isRecommended;
    }


    /**
     * toString
     * @return string representation of this fruit
     */
    @Override
    public String toString() {
        return  "--------------------" +
                System.lineSeparator()+
                "Review by " + nickName + " on " +
                date + System.lineSeparator() +
                "Rating: " + rating + System.lineSeparator() +
                title + System.lineSeparator() +
                reviewText + System.lineSeparator();
    }


    /**
    returns the Review ID
     */
    public String getReviewID() {
        return reviewID;
    }

    /**
    Returns nick name of reviewer
     */
    public String getNickName() {
        return nickName;
    }

    /**
    returns date when the review was logged
     */
    public Date getDate() {
        return date;
    }

    /**
    returns review title
     */
    public String title() {
        return title;
    }

    /**
    returns the review content
     */
    public String getReviewText() {
        return reviewText;
    }

    /**
    returns the hotel rating
     */
    public int getRating() {
        return rating;
    }

    /**
     returns the hotel rating
     */
    public boolean getRecommended() {
        return isRecommended;
    }
}
