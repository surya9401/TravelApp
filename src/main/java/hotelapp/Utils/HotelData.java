package hotelapp.Utils;
import hotelapp.Data.HotelObjectData;
import hotelapp.Data.ReviewData;

import java.util.*;

/**
 * Class comprising of Helper methods for Hotel Data and Review Data Maps
 * Data present here is private and encapsulated from the Child (ThreadSafeHotelData)
 */

public class HotelData {

    /** Map of Hotel Data Info Key - hotelID, Value HotelData */
    private Map<String, HotelObjectData> hotelData;

    /** Map of Review Data corresponding to every Hotel Key - hotelID, Value Set of Review data for the particular hotel */
    private Map<String, TreeSet<ReviewData>> reviewData;
    /**
     * Constructor for this class
     */
    HotelData() {
        hotelData = new HashMap<>();
        reviewData = new HashMap<>();
    }

    void addDescriptions(String hotelId, String areaDescription, String hotelDescription) {
        hotelData.get(hotelId).setAreaDescription(areaDescription);
        hotelData.get(hotelId).setHotelDescription(hotelDescription);
    }

    /**
     *
     * @param hotelID - Hotel ID
     * @return If Hotel ID key is present in the Review Map
     */
    public boolean reviewKeyPresent(String hotelID) {
        return reviewData.containsKey(hotelID);
    }

    /**
     *
     * @param hotelID Hotel ID
     * @param revData TreeSet of Review Data to be added to the Review Map for input Hotel ID
     */
    public void addReviewData(String hotelID, TreeSet<ReviewData> revData) {
        reviewData.put(hotelID, revData);
    }

    /**
     *
     * @param hotelID - Hotel ID
     * @return TreeSet of Review Data corresponding to the input Hotel ID
     */
    public TreeSet<ReviewData> reviewDataUsingHotelID(String hotelID) {
        if(hotelID == null)
            return null;
        return reviewData.get(hotelID);
    }


    /**
     * Method to add a Hotel to the Map
     * @param hotelId - Hotel Id
     * @param data - Hotel Object Data
     */
    public void addHotel(String hotelId, HotelObjectData data) {
        hotelData.put(hotelId, data);
    }

    /**
     * Method to get List of all hotels sorted alphabetically
     * @return list of Hotels
     */
    public List<String> getHotels() {
        List<String> keyList = new ArrayList<>(hotelData.keySet());
        Collections.sort(keyList);
        return keyList;
    }

    /**
     *
     * @return size of Review Data Map
     */
    public int reviewDataSize() {
        return reviewData.size();
    }

    /**
     *
     * @param hotelID Hotel ID
     * @return Hotel Object Data for input Hotel ID
     */
    public HotelObjectData hotelDataUsingHotelID(String hotelID) {
        if(hotelID == null)
            return null;
        return hotelData.get(hotelID);
    }

    /**
     *
     * @return Set of Keys in the Review Map
     */
    public Set<String> getReviewDataKeys() {
        return reviewData.keySet();
    }


}
