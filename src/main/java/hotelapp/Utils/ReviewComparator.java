package hotelapp.Utils;

import hotelapp.Data.ReviewData;

import java.util.Comparator;

public class ReviewComparator implements Comparator<ReviewData> {

    @Override
    public int compare(ReviewData r1, ReviewData r2) {
        if(r2.getDate().compareTo(r1.getDate()) == 0) {
            //If dates are same, compare by user nick name
            if(r1.getNickName().compareTo(r2.getNickName()) == 0) {
                //If user nick names are same, compare by review ID
                return r1.getReviewID().compareTo(r2.getReviewID());
            }
            else {
                return r1.getNickName().compareTo(r2.getNickName());
            }
        }
        else
            return r2.getDate().compareTo(r1.getDate());
    }
}
