package hotelapp.TravelApp.DBUtils;

import hotelapp.Data.HotelDbData;
import hotelapp.Data.ReviewDbData;
import hotelapp.TravelApp.LoginServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Handles all database-related actions. Uses singleton design pattern.
 * Example of Prof. Engle
 *
 * @see LoginServer
 */
public class TravelAdvisorDatabaseDriver {

	private static Logger log = LogManager.getLogger();

	/**
	 * Makes sure only one database handler is instantiated.
	 */
	private static TravelAdvisorDatabaseDriver singleton = new TravelAdvisorDatabaseDriver();

	/**
	 * Used to determine if necessary tables are provided.
	 */
	private static final String TABLES_SQL =
			"SHOW TABLES LIKE 'login_users';";

	/**
	 * Used to create necessary tables for this example.
	 */
	private static final String CREATE_SQL =
			"CREATE TABLE login_users (" +
					"userid INTEGER AUTO_INCREMENT PRIMARY KEY, " +
					"username VARCHAR(32) NOT NULL UNIQUE, " +
					"password CHAR(64) NOT NULL, " +
					"usersalt CHAR(32) NOT NULL);";

	/**
	 * Used to insert a new user into the database.
	 */
	private static final String REGISTER_SQL =
			"INSERT INTO login_users (username, password, usersalt) " +
					"VALUES (?, ?, ?);";

	/**
	 * Used to determine if a username already exists.
	 */
	private static final String USER_SQL =
			"SELECT username FROM login_users WHERE username = ?";

	/**
	 * Used to retrieve the salt associated with a specific user.
	 */
	private static final String SALT_SQL =
			"SELECT usersalt FROM login_users WHERE username = ?";

	/**
	 * Used to authenticate a user.
	 */
	private static final String AUTH_SQL =
			"SELECT username FROM login_users " +
					"WHERE username = ? AND password = ?";

	/**
	 * Used to remove a user from the database.
	 */
	private static final String DELETE_SQL =
			"DELETE FROM login_users WHERE username = ?";

	/**
	 * Used to configure connection to database.
	 */
	private DatabaseConnector db;

	/**
	 * Used to generate password hash salt for user.
	 */
	private Random random;

	/**
	 * Initializes a database handler for the Login example. Private constructor
	 * forces all other classes to use singleton.
	 */
	private TravelAdvisorDatabaseDriver() {
		Status status = Status.OK;
		random = new Random(System.currentTimeMillis());

		try {
			db = new DatabaseConnector("database.properties");
			status = db.testConnection() ? setupTables() : Status.CONNECTION_FAILED;
		} catch (FileNotFoundException e) {
			status = Status.MISSING_CONFIG;
		} catch (IOException e) {
			status = Status.MISSING_VALUES;
		}

		if (status != Status.OK) {
			log.fatal(status.message());
		}
	}

	/**
	 * Gets the single instance of the database handler.
	 *
	 * @return instance of the database handler
	 */
	public static TravelAdvisorDatabaseDriver getInstance() {
		return singleton;
	}

	/**
	 * Checks to see if a String is null or empty.
	 *
	 * @param text - String to check
	 * @return true if non-null and non-empty
	 */
	public static boolean isBlank(String text) {
		return (text == null) || text.trim().isEmpty();
	}

	/**
	 * Checks if necessary table exists in database, and if not tries to
	 * create it.
	 */
	private Status setupTables() {
		Status status = Status.ERROR;

		try (
				Connection connection = db.getConnection();
				Statement statement = connection.createStatement();
		) {
			if (!statement.executeQuery(TABLES_SQL).next()) {
				// Table missing, must create
				log.debug("Creating tables...");
				statement.executeUpdate(CREATE_SQL);

				// Check if create was successful
				if (!statement.executeQuery(TABLES_SQL).next()) {
					status = Status.CREATE_FAILED;
				} else {
					status = Status.OK;
				}
			} else {
				log.debug("Tables found.");
				status = Status.OK;
			}
		} catch (Exception ex) {
			status = Status.CREATE_FAILED;
			log.debug(status, ex);
		}

		return status;
	}

	/**
	 * Tests if a user already exists in the database. Requires an active
	 * database connection.
	 *
	 * @param connection - active database connection
	 * @param user       - username to check
	 * @return Status.OK if user does not exist in database
	 * @throws SQLException
	 */
	private Status duplicateUser(Connection connection, String user) {

		assert connection != null;
		assert user != null;

		Status status = Status.ERROR;

		try (
				PreparedStatement statement = connection.prepareStatement(USER_SQL);
		) {
			statement.setString(1, user);

			ResultSet results = statement.executeQuery();
			status = results.next() ? Status.DUPLICATE_USER : Status.OK;
		} catch (SQLException e) {
			log.debug(e.getMessage(), e);
			status = Status.SQL_EXCEPTION;
		}

		return status;
	}

	/**
	 * Tests if a user already exists in the database.
	 *
	 * @param user - username to check
	 * @return Status.OK if user does not exist in database
	 * @see #duplicateUser(Connection, String)
	 */
	public Status duplicateUser(String user) {
		Status status = Status.ERROR;

		try (
				Connection connection = db.getConnection();
		) {
			status = duplicateUser(connection, user);
		} catch (SQLException e) {
			status = Status.CONNECTION_FAILED;
			log.debug(e.getMessage(), e);
		}

		return status;
	}

	/**
	 * Returns the hex encoding of a byte array.
	 *
	 * @param bytes  - byte array to encode
	 * @param length - desired length of encoding
	 * @return hex encoded byte array
	 */
	public static String encodeHex(byte[] bytes, int length) {
		BigInteger bigint = new BigInteger(1, bytes);
		String hex = String.format("%0" + length + "X", bigint);

		assert hex.length() == length;
		return hex;
	}

	/**
	 * Calculates the hash of a password and salt using SHA-256.
	 *
	 * @param password - password to hash
	 * @param salt     - salt associated with user
	 * @return hashed password
	 */
	public static String getHash(String password, String salt) {
		String salted = salt + password;
		String hashed = salted;

		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(salted.getBytes());
			hashed = encodeHex(md.digest(), 64);
		} catch (Exception ex) {
			log.debug("Unable to properly hash password.", ex);
		}

		return hashed;
	}

	/**
	 * Registers a new user, placing the username, password hash, and
	 * salt into the database if the username does not already exist.
	 *
	 * @param newuser - username of new user
	 * @param newpass - password of new user
	 * @return status ok if registration successful
	 */
	private Status registerUser(Connection connection, String newuser, String newpass) {

		Status status = Status.ERROR;

		byte[] saltBytes = new byte[16];
		random.nextBytes(saltBytes);

		String usersalt = encodeHex(saltBytes, 32);
		String passhash = getHash(newpass, usersalt);

		try (
				PreparedStatement statement = connection.prepareStatement(REGISTER_SQL);
		) {
			statement.setString(1, newuser);
			statement.setString(2, passhash);
			statement.setString(3, usersalt);
			statement.executeUpdate();

			status = Status.OK;
		} catch (SQLException ex) {
			status = Status.SQL_EXCEPTION;
			log.debug(ex.getMessage(), ex);
		}

		return status;
	}

	/**
	 * Registers a new user, placing the username, password hash, and
	 * salt into the database if the username does not already exist.
	 *
	 * @param newuser - username of new user
	 * @param newpass - password of new user
	 * @return status.ok if registration successful
	 */
	public Status registerUser(String newuser, String newpass) {
		Status status = Status.ERROR;
		log.debug("Registering " + newuser + ".");

		// make sure we have non-null and non-emtpy values for login
		if (isBlank(newuser) || isBlank(newpass)) {
			status = Status.INVALID_LOGIN;
			log.debug(status);
			return status;
		}

		// try to connect to database and test for duplicate user
		System.out.println(db);

		try (
				Connection connection = db.getConnection();
		) {
			status = duplicateUser(connection, newuser);

			// if okay so far, try to insert new user
			if (status == Status.OK) {
				status = registerUser(connection, newuser, newpass);
			}
		} catch (SQLException ex) {
			status = Status.CONNECTION_FAILED;
			log.debug(status, ex);
		}

		return status;
	}

	/**
	 * Gets the salt for a specific user.
	 *
	 * @param connection - active database connection
	 * @param user       - which user to retrieve salt for
	 * @return salt for the specified user or null if user does not exist
	 * @throws SQLException if any issues with database connection
	 */
	private String getSalt(Connection connection, String user) throws SQLException {
		assert connection != null;
		assert user != null;

		String salt = null;

		try (
				PreparedStatement statement = connection.prepareStatement(SALT_SQL);
		) {
			statement.setString(1, user);

			ResultSet results = statement.executeQuery();

			if (results.next()) {
				salt = results.getString("usersalt");
			}
		}

		return salt;
	}

	/**
	 * Checks if the provided username and password match what is stored
	 * in the database. Requires an active database connection.
	 *
	 * @param username - username to authenticate
	 * @param password - password to authenticate
	 * @return status.ok if authentication successful
	 * @throws SQLException
	 */
	private Status authenticateUser(Connection connection, String username,
									String password) throws SQLException {

		Status status = Status.ERROR;

		try (
				PreparedStatement statement = connection.prepareStatement(AUTH_SQL);
		) {
			String usersalt = getSalt(connection, username);
			String passhash = getHash(password, usersalt);

			statement.setString(1, username);
			statement.setString(2, passhash);

			ResultSet results = statement.executeQuery();
			status = results.next() ? status = Status.OK : Status.INVALID_LOGIN;
		} catch (SQLException e) {
			log.debug(e.getMessage(), e);
			status = Status.SQL_EXCEPTION;
		}

		return status;
	}

	/**
	 * Checks if the provided username and password match what is stored
	 * in the database. Must retrieve the salt and hash the password to
	 * do the comparison.
	 *
	 * @param username - username to authenticate
	 * @param password - password to authenticate
	 * @return status.ok if authentication successful
	 */
	public Status authenticateUser(String username, String password) {
		Status status = Status.ERROR;

		log.debug("Authenticating user " + username + ".");

		try (
				Connection connection = db.getConnection();
		) {
			status = authenticateUser(connection, username, password);
		} catch (SQLException ex) {
			status = Status.CONNECTION_FAILED;
			log.debug(status, ex);
		}

		return status;
	}

	/**
	 * Removes a user from the database if the username and password are
	 * provided correctly.
	 *
	 * @param username - username to remove
	 * @param password - password of user
	 * @return status.OK if removal successful
	 */
	private Status removeUser(Connection connection, String username, String password) {
		Status status = Status.ERROR;

		try (
				PreparedStatement statement = connection.prepareStatement(DELETE_SQL);
		) {
			statement.setString(1, username);

			int count = statement.executeUpdate();
			status = (count == 1) ? Status.OK : Status.INVALID_USER;
		} catch (SQLException ex) {
			status = Status.SQL_EXCEPTION;
			log.debug(status, ex);
		}

		return status;
	}

    /**
     * Updates Time to Display for last login
     * @param username name of user logged in
     * @param date time
     */
	public void updateTimeToDisplay(String username, String date) {
		Status status;

		log.debug("Updating Time " + username + ".");

		try (
				Connection connection = db.getConnection();
				PreparedStatement statement = connection.prepareStatement("UPDATE login_users \n" +
						"SET \n" +
						"    logintime = ?" +
						"WHERE\n" +
						"    username = ?;");
		) {
			statement.setString(1, date);
			statement.setString(2, username);

			int count = statement.executeUpdate();
        } catch (SQLException ex) {
			status = Status.SQL_EXCEPTION;
			log.debug(status, ex);
		}
	}

    /**
     * Updates Placeholder time which is copied to the time being displayed when logged out
     * @param username name of user
     * @param date time
     */
	public void updateTimeTemp(String username, String date) {
		Status status;

		log.debug("Updating Time " + username + ".");

		try (
				Connection connection = db.getConnection();
				PreparedStatement statement = connection.prepareStatement("UPDATE login_users \n" +
						"SET \n" +
						"    logintimeTemp = ?" +
						"WHERE\n" +
						"    username = ?;");
		) {
			statement.setString(1, date);
			statement.setString(2, username);

			int count = statement.executeUpdate();
        } catch (SQLException ex) {
			status = Status.SQL_EXCEPTION;
			log.debug(status, ex);
		}
	}

    /**
     * Fetches last login time
     * @param username user logged in
     * @return time
     */
	public String getLastLoginTime(String username) {
		Status status;
		String time = null;
		log.debug("Updating Time " + username + ".");

		try (
				Connection connection = db.getConnection();
				PreparedStatement statement = connection.prepareStatement("SELECT logintime from login_users \n" +
						"where username = ?");
		) {
			statement.setString(1, username);

			ResultSet results = statement.executeQuery();
			while (results.next()) {
				time = results.getString("logintime");
			}
		} catch (SQLException ex) {
			status = Status.SQL_EXCEPTION;
			log.debug(status, ex);
		}

		return time;
	}

    /**
     *
     * @param username user logged in
     * @return time
     */
	public String getLastTempTime(String username) {
		Status status;
		String time = null;
		log.debug("Updating Time " + username + ".");

		try (
				Connection connection = db.getConnection();
				PreparedStatement statement = connection.prepareStatement("SELECT logintimeTemp from login_users \n" +
						"where username = ?");
		) {
			statement.setString(1, username);

			ResultSet results = statement.executeQuery();
			while (results.next()) {
				time = results.getString("logintimeTemp");
			}
		} catch (SQLException ex) {
			status = Status.SQL_EXCEPTION;
			log.debug(status, ex);
		}

		return time;
	}


    /**
     *
     * @param cityName city name of hotel
     * @param keyword word to search in name
     * @return list of hotels
     */
	public List<HotelDbData> searchHotels(String cityName, String keyword) {
		Status status;
		List<HotelDbData> hotelData = new ArrayList<>();
		try (
				Connection connection = db.getConnection();
				PreparedStatement statement1 = connection.prepareStatement("SELECT id,name,city,address,lat,lng from hoteldata \n" +
						"where city LIKE ? AND name LIKE ?");
		) {
			statement1.setString(1, "%" + cityName + "%");
			statement1.setString(2, "%" + keyword + "%");


			ResultSet results1 = statement1.executeQuery();
			while (results1.next()) {
				String lat = results1.getString("lat");
				String lng = results1.getString("lng");
				String address = results1.getString("address");
				String hotelId = results1.getString("id");
				String hotelName = results1.getString("name");

				String city = results1.getString("city");
				String website = getLinkUsingHotelID(hotelId, results1.getString("name"), city);

				try (
						PreparedStatement statement2 = connection.prepareStatement("SELECT title,user,review,rating,date from review_data \n" +
								"where hotelId = ?");
				) {
					statement2.setString(1, hotelId);
					ResultSet results2 = statement2.executeQuery();
					int count = 0;
					int averageRatings = 0;
					List<ReviewDbData> reviewData = new ArrayList<>();
					while (results2.next()) {
						String title = results2.getString("title");
						String review = results2.getString("review");
						String user = results2.getString("user");
						String rating = results2.getString("rating");
						String time = results2.getString("date");
						averageRatings += Integer.parseInt(results2.getString("rating"));
						count++;

						reviewData.add(0, new ReviewDbData(title, review, user, getRatingBased(rating), time, hotelId, hotelName));
					}
					hotelData.add(0, new HotelDbData(hotelId, hotelName, address, website, reviewData, city, lat, lng, (double) averageRatings / count));
				}
			}
		} catch (SQLException ex) {
			status = Status.SQL_EXCEPTION;
			log.debug(status, ex);
		}

		return hotelData;
	}

    /**
     *
     * @param hotelId hotel Id
     * @param name name of hotel
     * @param city hotel city
     * @return link of the expedia page to hotel
     */
	private String getLinkUsingHotelID(String hotelId, String name, String city) {
		StringBuilder link = new StringBuilder("https://www.expedia.com/");
		String[] nameWords = name.split(" ");
		String[] citywords = city.split(" ");
		for (String s : nameWords) {
			link.append(s).append("-");
		}
		for (String s : citywords) {
			link.append(s).append("-");
		}
		link = new StringBuilder(link.substring(0, link.length() - 1));
		link.append(".h").append(hotelId).append(".Hotel-Information");
		return link.toString();
	}


    /**
     * Returns list of expedia links persisted
     * @param username user name
     * @return list of links
     */
	public List<String> getExpediaLinks(String username) {
		Status status;
		List<String> list = new ArrayList<>();
		try (
				Connection connection = db.getConnection();
				PreparedStatement statement = connection.prepareStatement("SELECT * from expedia_links \n" +
						"where user = ?");
		) {
			statement.setString(1, username);

			ResultSet results = statement.executeQuery();
			while (results.next()) {
				list.add(results.getString("link"));
			}
		} catch (SQLException ex) {
			status = Status.SQL_EXCEPTION;
			log.debug(status, ex);
		}
		return list;
	}

    /**
     * get saved hotel list
     * @param username user logged in
     * @return list of hotels
     */
	public List<String> getSavedHotels(String username) {
		Status status;
		List<String> list = new ArrayList<>();
		try (
				Connection connection = db.getConnection();
				PreparedStatement statement = connection.prepareStatement("SELECT * from saved_hotels \n" +
						"where user = ?");
		) {
			statement.setString(1, username);

			ResultSet results = statement.executeQuery();
			while (results.next()) {
				list.add(results.getString("name"));
			}
		} catch (SQLException ex) {
			status = Status.SQL_EXCEPTION;
			log.debug(status, ex);
		}
		return list;
	}

    /**
     * clearing saved list when user desires
     * @param username user logged in
     */
	public void clearSavedHotels(String username) {
		Status status;
		List<String> list = new ArrayList<>();
		try (
				Connection connection = db.getConnection();
				PreparedStatement statement = connection.prepareStatement("delete from saved_hotels where user = ?");
		) {
			statement.setString(1, username);
			statement.executeUpdate();
		} catch (SQLException ex) {
			status = Status.SQL_EXCEPTION;
			log.debug(status, ex);
		}
	}

    /**
     * get reviews provided by current user
     * @param username user logged in
     * @return list of reviews
     */
	public List<ReviewDbData> getUserReviews(String username) {
		List<ReviewDbData> list = new ArrayList<>();
		Status status;
		try (
				Connection connection = db.getConnection();
				PreparedStatement statement = connection.prepareStatement("SELECT hotelId,title,user,review,rating,date from review_data \n" +
						"where user = ?");
		) {
			statement.setString(1, username);

			ResultSet results = statement.executeQuery();

			while (results.next()) {
				String title = results.getString("title");
				String review = results.getString("review");
				String user = results.getString("user");
				String rating = results.getString("rating");
				String time = results.getString("date");
				String hotelId = results.getString("hotelId");
				String hotelName = getHotelName(hotelId);
				list.add(0, new ReviewDbData(title, review, user, getRatingBased(rating), time, hotelId, hotelName));
			}
		} catch (SQLException ex) {
			status = Status.SQL_EXCEPTION;
			log.debug(status, ex);
		}
		return list;
	}

    /**
     * add a new expedia link to db
     * @param username user logged in
     * @param link link to be added
     */
	public void addExpediaLinks(String username, String link) {
		Status status;
		try (
				Connection connection = db.getConnection();
				PreparedStatement statement = connection.prepareStatement("SELECT link from expedia_links \n" +
						"where user = ?");
		) {
			statement.setString(1, username);

			ResultSet results = statement.executeQuery();
			if (!results.next()) {
				try (
						PreparedStatement statementInsert = connection.prepareStatement("INSERT INTO expedia_links (user, link) VALUES (?, ?)");
				) {
					statementInsert.setString(1, username);
					statementInsert.setString(2, link);
					statementInsert.executeUpdate();
				}
			} else {
				boolean present = false;
				while (results.next()) {
					if (results.getString("link").equals(link))
						present = true;
				}
				if (!present) {
					try (
							PreparedStatement statementInsert = connection.prepareStatement("INSERT INTO expedia_links (user, link) VALUES (?, ?)");
					) {
						statementInsert.setString(1, username);
						statementInsert.setString(2, link);
						statementInsert.executeUpdate();
					}
				}
			}
		} catch (SQLException ex) {
			status = Status.SQL_EXCEPTION;
			log.debug(status, ex);
		}
	}

    /**
     * clear list of persisted links in db
     * @param username user logged in
     */
	public void clearExpediaLinks(String username) {
		Status status;
		try (
				Connection connection = db.getConnection();
				PreparedStatement statement = connection.prepareStatement("delete from expedia_links where user = ?");
		) {
			statement.setString(1, username);
			statement.executeUpdate();
		} catch (SQLException ex) {
			status = Status.SQL_EXCEPTION;
			log.debug(status, ex);
		}
	}

    /**
     * update an existing review
     * @param username user logged in
     * @param titleChange title of review
     * @param reivew review text
     * @param rating review rating
     * @param hotelId hotel id for the review
     */
	public void updateReview(String username, String titleChange, String reivew, String rating, String hotelId) {
		Status status;
		try (
				Connection connection = db.getConnection();
				PreparedStatement statement = connection.prepareStatement("UPDATE review_data \n" +
						"SET \n" +
						"    title = ?,review = ?, rating = ? " +
						"WHERE\n" +
						"    user = ? AND hotelId= ?;");
		) {
			statement.setString(1, titleChange);
			statement.setString(2, reivew);
			statement.setString(3, rating);
			statement.setString(4, username);
			statement.setString(5, hotelId);

			int count = statement.executeUpdate();
        } catch (SQLException ex) {
			status = Status.SQL_EXCEPTION;
			log.debug(status, ex);
		}
	}

    /**
     * Delete a review
     * @param hotelId hotel id of review
     * @param username user logged in
     * @param title title of review
     */
	public void deleteReview(String hotelId, String username, String title) {
		Status status;
		try (
				Connection connection = db.getConnection();
				PreparedStatement statement = connection.prepareStatement("delete from review_data \n" +
						"WHERE\n" +
						"    user = ? AND hotelId= ? AND title = ?;");
		) {
			statement.setString(1, username);
			statement.setString(2, hotelId);
			statement.setString(3, title);

			int count = statement.executeUpdate();
			status = (count == 1) ? Status.OK : Status.INVALID_USER;
		} catch (SQLException ex) {
			status = Status.SQL_EXCEPTION;
			log.debug(status, ex);
		}
	}

    /**
     * Get hotel name using hotel id
     * @param hotelId id of hotel
     * @return hotel name
     */
	private String getHotelName(String hotelId) {
		Status status;
		String name = null;
		try (
				Connection connection = db.getConnection();
				PreparedStatement statement = connection.prepareStatement("SELECT name from hoteldata \n" +
						"where id = ?");
		) {
			statement.setString(1, hotelId);

			ResultSet results = statement.executeQuery();
			if (results.next()) {
				name = results.getString("name");
			}
		} catch (SQLException ex) {
			status = Status.SQL_EXCEPTION;
			log.debug(status, ex);
		}
		return name;
	}

    /**
     * Save Hotel
     * @param username user logged in
     * @param hotelName hotel name
     */
	public void saveHotel(String username, String hotelName) {
		Status status;
		try (
				Connection connection = db.getConnection();
				PreparedStatement statement = connection.prepareStatement("insert into saved_hotels \n" +
						"set user = ?, name = ?");
		) {
			statement.setString(1, username);
			statement.setString(2, hotelName);

			statement.executeUpdate();
		} catch (SQLException ex) {
			status = Status.SQL_EXCEPTION;
			log.debug(status, ex);
		}
	}

    /**
     * get list of reviews by hotel Id
     * @param hotelId hotel Id
     * @return list of reviews
     */
	public List<ReviewDbData> getHotelReviewsByHotelId(String hotelId) {
		List<ReviewDbData> list = new ArrayList<>();
		Status status;
		try (
				Connection connection = db.getConnection();
				PreparedStatement statement = connection.prepareStatement("SELECT title,user,review,rating,date from review_data \n" +
						"where hotelId = ?");
		) {
			statement.setString(1, hotelId);

			ResultSet results = statement.executeQuery();

			while (results.next()) {
				String title = results.getString("title");
				String review = results.getString("review");
				String user = results.getString("user");
				String rating = results.getString("rating");
				String time = results.getString("date");
				String hotelName = getHotelName(hotelId);
				list.add(0, new ReviewDbData(title, review, user, getRatingBased(rating), time, hotelId, hotelName));
			}
		} catch (SQLException ex) {
			status = Status.SQL_EXCEPTION;
			log.debug(status, ex);
		}
		return list;
	}

    /**
     * retrieve hotel data
     * @param hotelName name of hotel
     * @return hotelData
     */
	public HotelDbData getHotelDataUsingHotelName(String hotelName) {
		Status status;
		HotelDbData hotelData = null;
		try (
				Connection connection = db.getConnection();
				PreparedStatement statement1 = connection.prepareStatement("SELECT * from hoteldata \n" +
						"where name = ?");
		) {
			statement1.setString(1, hotelName);


			ResultSet results1 = statement1.executeQuery();
			while (results1.next()) {
				String lat = results1.getString("lat");
				String lng = results1.getString("lng");
				String address = results1.getString("address");
				String hotelId = results1.getString("id");

				String city = results1.getString("city");
				String website = getLinkUsingHotelID(hotelId, results1.getString("name"), city);

				try (
						PreparedStatement statement2 = connection.prepareStatement("SELECT title,user,review,rating,date from review_data \n" +
								"where hotelId = ?");
				) {
					statement2.setString(1, hotelId);
					ResultSet results2 = statement2.executeQuery();
					int count = 0;
					int averageRatings = 0;
					List<ReviewDbData> reviewData = new ArrayList<>();
					while (results2.next()) {
						String title = results2.getString("title");
						String review = results2.getString("review");
						String user = results2.getString("user");
						String rating = results2.getString("rating");
						String time = results2.getString("date");
						averageRatings += Integer.parseInt(results2.getString("rating"));
						count++;

						reviewData.add(0, new ReviewDbData(title, review, user, getRatingBased(rating), time, hotelId, hotelName));
					}
					hotelData = new HotelDbData(hotelId, hotelName, address, website, reviewData, city, lat, lng, (double) averageRatings / count);
				}
			}
		} catch (SQLException ex) {
			status = Status.SQL_EXCEPTION;
			log.debug(status, ex);
		}

		return hotelData;
	}

    /**
     * get rating string based on number (1-5)
     * @param rating
     * @return
     */
	private String getRatingBased(String rating) {
		String result = null;
		try {
			switch (Integer.parseInt(rating)) {
				case 1:
					result = "1-Very Poor";
					break;
				case 2:
					result = "2-Poor";
					break;
				case 3:
					result = "3-Average";
					break;
				case 4:
					result = "4-Good";
					break;
				case 5:
					result = "5-Very Good";
					break;
				default:
					break;
			}
		}
		catch(Exception e) {
			System.out.println(e);
		}
		return result;
	}
}
