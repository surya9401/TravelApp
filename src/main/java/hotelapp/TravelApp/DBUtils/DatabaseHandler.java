package hotelapp.TravelApp.DBUtils;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Properties;

/** Problem 4: In doPost method of WelcomeServlet,
 *  we can call DatabaseHandler.getInstance().insertNameIntoDatabase(name);
 *  where name is the parameter of the request (after the usual "cleaning").
 */
public class DatabaseHandler {
    /** Makes sure only one database handler is instantiated. */
    private static DatabaseHandler singleton = new DatabaseHandler();

    /**
     * Private constructor. So that nobody can create another instance of DatabaseHandler
     */
    private DatabaseHandler() {  }

    /**
     * Load properties file
     *
     * @param configPath
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    private Properties loadConfig(String configPath) throws FileNotFoundException, IOException {
        Properties config = new Properties();
        config.load(new FileReader(configPath));

        return config;
    }


    /**
     * Used DataLoader for inserting all data from config to Database
     * @param hotelId hotel Id
     * @param hotelName hotel name
     * @param city hotel's city
     * @param state hotel state
     * @param streetAddress address of hotel
     * @param lat latitude
     * @param lon longitude
     */
    public void insertHotelsIntoDatabase(String hotelId, String hotelName, String city, String state, String streetAddress, double lat,
                                         double lon) {
        try {
            String query = "INSERT INTO hoteldata (id, name, city, state, address, lat, lng) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?);";
            Properties config = loadConfig("database.properties");
            String uri = String.format("jdbc:mysql://%s/%s", config.getProperty("hostname"),
                    config.getProperty("database"));
            uri = uri + "?serverTimezone=UTC";

            PreparedStatement sql; // prepared statement
            try (Connection dbConnection = DriverManager.getConnection(uri, config.getProperty("username"),
                    config.getProperty("password"))) {

                sql = dbConnection.prepareStatement(query);
                sql.setString(1, hotelId);
                sql.setString(2, hotelName);
                sql.setString(3, city);
                sql.setString(4, state);
                sql.setString(5, streetAddress);
                sql.setString(6, String.valueOf(lat));
                sql.setString(7, String.valueOf(lon));
                sql.executeUpdate();

            }
        } catch (Exception e) {
            System.err.println("Unable to connect properly to database.");
            System.err.println(e.getMessage());
        }
    }

    /**
     * Used by DataLoader to insert into data base from config
     * @param hotelId hotel id
     * @param reviewId review id
     * @param rating rating
     * @param reviewTitle review title
     * @param review reivew
     * @param isRecom whether recommended
     * @param date date of submission
     * @param username username creating the review
     */
    public void insertReviewsIntoDatabase(String hotelId, String reviewId, String rating, String reviewTitle, String review,
                                          String isRecom, String date, String username) {
        try {
            String query = "INSERT INTO review_data (hotelId, reviewId, rating, title, review, recommended, date, user) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?);";
            Properties config = loadConfig("database.properties");
            String uri = String.format("jdbc:mysql://%s/%s", config.getProperty("hostname"),
                    config.getProperty("database"));
            uri = uri + "?serverTimezone=UTC";

            PreparedStatement sql; // prepared statement
            try (Connection dbConnection = DriverManager.getConnection(uri, config.getProperty("username"),
                    config.getProperty("password"))) {

                sql = dbConnection.prepareStatement(query);
                sql.setString(1, hotelId);
                sql.setString(2, reviewId);
                sql.setString(3, rating);
                sql.setString(4, reviewTitle);
                sql.setString(5, review);
                sql.setString(6, isRecom);
                sql.setString(7, date);
                sql.setString(8, username);
                sql.executeUpdate();

            }
        } catch (Exception e) {
            System.err.println("Unable to connect properly to database.");
            System.err.println(e.getMessage());
        }
    }

    /**
     * Gets the single instance of the database handler.
     *
     * @return instance of the database handler
     */
    public static DatabaseHandler getInstance() {
        return singleton;
    }

}
