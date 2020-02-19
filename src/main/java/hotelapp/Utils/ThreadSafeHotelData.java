package hotelapp.Utils;


import hotelapp.TravelApp.DBUtils.DatabaseHandler;
import hotelapp.Data.HotelLocationData;
import hotelapp.Data.HotelObjectData;
import hotelapp.Data.ReviewData;

import java.security.InvalidParameterException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TreeSet;


/**
 * Class ThreadSafeHotelData - extends class HotelData (rename your class from project 1 as needed).
 * Thread-safe, uses ReentrantReadWriteLock to synchronize access to all data structures.
 */
public class ThreadSafeHotelData extends HotelData {

	/** Object of the lock */
	private ReentrantReadWriteLock lock;

	/**
	 * Default constructor.
	 */
	public ThreadSafeHotelData() {
		super();
		this.lock = new ReentrantReadWriteLock();
	}

	/**
	 * Overrides addHotel method from HotelData class to make it thread-safe; uses the lock.
	 * Create a Hotel given the parameters, and add it to the appropriate data
	 * structure(s).
	 *
	 * @param hotelId
	 *            - the id of the hotel
	 * @param hotelName
	 *            - the name of the hotel
	 * @param city
	 *            - the city where the hotel is located
	 * @param state
	 *            - the state where the hotel is located.
	 * @param streetAddress
	 *            - the building number and the street
	 * @param lat
	 * @param lon
	 */

	public void addHotel(String hotelId, String hotelName, String city, String state, String streetAddress, double lat,
			double lon) {
			HotelLocationData coordinates = new HotelLocationData(lat, lon);
			super.addHotel(hotelId, new HotelObjectData(hotelName, hotelId, coordinates, city, state, streetAddress));
	}

	/**
	 * Overrides addReview method from HotelData class to make it thread-safe; uses the lock.
	 *
	 * @param hotelId
	 *            - the id of the hotel reviewed
	 * @param reviewId
	 *            - the id of the review
	 * @param rating
	 *            - integer rating 1-5.
	 * @param reviewTitle
	 *            - the title of the review
	 * @param review
	 *            - text of the review
	 * @param isRecom
	 *            - whether the user recommends it or not
	 * @param date
	 *            - date of the review
	 * @param username
	 *            - the nickname of the user writing the review.
	 * @return true if successful, false if unsuccessful because of invalid date
	 *         or rating. Needs to catch and handle the following exceptions:
	 *         ParseException if the date is invalid InvalidRatingException if
	 *         the rating is out of range
	 */
	public boolean addReview(String hotelId, String reviewId, int rating, String reviewTitle, String review,
							 boolean isRecom, String date, String username) {
		if (isRatingValid(rating) && isDateValid(date)) {
			if (reviewKeyPresent(hotelId)) {
				new TreeSet<>(new ReviewComparator());
				TreeSet<ReviewData> revData;
				try {
					lock.lockRead();
					revData = reviewDataUsingHotelID(hotelId);
					revData.add(new ReviewData(hotelId, reviewId, rating, review, reviewTitle, username, getDate(date), isRecom));
					//DatabaseHandler.getInstance().insertReviewsIntoDatabase(hotelId, reviewId, String.valueOf(rating), reviewTitle, review, String.valueOf(isRecom), String.valueOf(getDate(date)), username);
				}
				finally {
					lock.unlockRead();
				}
				try {
					lock.lockWrite();
					addReviewData(hotelId, revData);
				}
				finally {
					lock.unlockWrite();
				}
			} else {
				TreeSet<ReviewData> revData = new TreeSet<>(new ReviewComparator());
				revData.add(new ReviewData(hotelId, reviewId, rating, review, reviewTitle, username, getDate(date), isRecom));
				DatabaseHandler.getInstance().insertReviewsIntoDatabase(hotelId, reviewId, String.valueOf(rating), reviewTitle, review, String.valueOf(isRecom), String.valueOf(getDate(date)), username);
				try {
					lock.lockWrite();
					addReviewData(hotelId, revData);
				}
				finally {
					lock.unlockWrite();
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * @param date raw String date
	 * @return date in SimpleDateFormat
	 */
	private Date getDate(String date) {
		try {
			DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			return format.parse(date);
		}
		catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Check if date is Valid
	 * @param date raw String date
	 * @return if date is Valid
	 */
	private boolean isDateValid(String date) {
		boolean result = false;
		try {
			DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			Date formattedDate = format.parse(date);
			if (formattedDate != null) {
				result = true;
			}
		}
		catch (ParseException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * Check if the rating is within the valid range
	 * @param rating input rating parameter
	 * @return validity
	 */
	private boolean isRatingValid(int rating) {
		try {
			return (rating >=1 && rating<=5);
		}
		catch (InvalidParameterException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Overrides a method of the parent class to make it thread-safe.
	 * Return an alphabetized list of the ids of all hotels
	 * 
	 * @return list of Hotel ID's sorted Alphabetically
	 */
	@Override
	public List<String> getHotels() {
		return super.getHotels();
	}
}
