package hotelapp.Data;

import java.util.ArrayList;
import java.util.List;

/** This class stores an ArrayList of Person-s.
 *  Used while parsing a json file that contains info about several people.
 */
public class Hotels {

    /** List for hotel data (cloned and sent to concerned class */
    private ArrayList<HotelObjectData> sr;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (HotelObjectData data : sr) {
            sb.append(data);
        }
        return sb.toString();
    }

    /** Method to return cloned data of the original Hotel Data
     * Encapsulation preserved
     * @return list of cloned hotel data
     */
    public List<HotelObjectData> getClonedData() {
        List<HotelObjectData> copy;
        copy = sr;
        return copy;
    }
}
