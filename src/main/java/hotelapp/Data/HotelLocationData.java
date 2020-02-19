package hotelapp.Data;
/**
 *Class to store the latitude and longitude of the hotel
 */
public class HotelLocationData {

    /** latitude co-ordinate of the hotel */
    private double lat;

    /** longitude co-ordinate of the hotel */
    private double lng;

    public HotelLocationData(double latitude, double longitude) {
        this.lat = latitude;
        this.lng = longitude;
    }

    double getLatitude() {
        return this.lat;
    }

    double getLongitude() {
        return this.lng;
    }

    /**
     * toString
     * @return string representation of this fruit
     */
    @Override
    public String toString() {
        return "latitude='" + lat + '\'' +
                ", longitude='" + lng + '\'' +
                '}';
    }
}
