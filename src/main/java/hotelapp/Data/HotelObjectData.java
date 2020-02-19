package hotelapp.Data;


import java.util.ArrayList;
import java.util.List;

/**
 * Class used to store Hotel Object
 * */
public class HotelObjectData {
    private String f;
    private String id;
    private HotelLocationData ll;
    private String ci;
    private String pr;
    private String ad;
    private List<TouristAttraction> ta;
    private String propDescp;
    private String areaDescp;


    /** Constructor used to create object type Hotel */
    public HotelObjectData(String name, String id, HotelLocationData coordinates, String location, String state, String address) {
        this.f = name;
        this.id = id;
        this.ll = coordinates;
        this.ci = location;
        this.pr = state;
        this.ad = address;
    }

    /**
     * toString
     * @return string representation of this fruit
     */
    @Override
    public String toString() {
        return f + ": " + id + System.lineSeparator()
                + ad + System.lineSeparator()
                + ci + ", " + pr + System.lineSeparator();
    }

    /** Returns the name of the hotel object */
    public String getName() {
        return this.f;
    }

    /** Returns the Hotel ID */
    public String getID() {
        return this.id;
    }

    /** Returns the Hotel City */
    public String getCity() {
        return this.ci;
    }

    /** Returns the latitude of the hotel */
    public double getLatitude() {
        return this.ll.getLatitude();
    }

    /** Returns the longitude of the hotel */
    public double getLongitude() {
        return this.ll.getLongitude();
    }

    /** Returns the State the Hotel is located in */
    public String getState() {
        return this.pr;
    }

    /** Returns the Address the Hotel is located in */
    public String getAddress() {
        return this.ad;
    }

    /** Adds area description for given hotel */
    public void setAreaDescription(String areaDescription) {
        this.areaDescp = areaDescription;
    }

    /** Adds hotel description for given hotel */
    public void setHotelDescription(String hotelDescription) {
        this.propDescp = hotelDescription;
    }

    /** Returns hotel description for given hotel */
    public String getPropertyDescription() {
        return this.propDescp;
    }

    /** Returns area description for given hotel */
    public String getAreaDescription() {
        return  this.areaDescp;
    }

    /** Returns list of attractions near given hotel */
    public List<String> getAttractions() {
        List<String> result = new ArrayList<>();
        for (TouristAttraction touristAttraction : ta) {
            result.add(touristAttraction.getName());
        }
        return result;
    }
}
