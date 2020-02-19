package hotelapp.TravelApp.LoginServlets;

import hotelapp.TravelApp.DBUtils.TravelAdvisorDatabaseDriver;
import hotelapp.TravelApp.DBUtils.Status;
import hotelapp.TravelApp.LoginServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides base functionality to all servlets in this example.
 * Example of Prof. Engle
 * @see LoginServer
 */
@SuppressWarnings("serial")
public class LoginBaseServlet extends HttpServlet {

	/** Error Logger */
	static Logger log = LogManager.getLogger();

	/** Instance of Db Handler */
	protected static final TravelAdvisorDatabaseDriver dbhandler = TravelAdvisorDatabaseDriver.getInstance();

	/** prepare general response */
	protected void prepareResponse(String title, HttpServletResponse response) {
		try {
			PrintWriter writer = response.getWriter();

			writer.printf("<!DOCTYPE html>%n%n");
			writer.printf("<html lang=\"en\">%n%n");
			writer.printf("<link rel=\"stylesheet\" href=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css\">%n");
			writer.printf("<script src=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js\"></script>");
			writer.printf("<head>%n");
			writer.printf("\t<title>%s</title>%n", title);
			writer.printf("\t<meta charset=\"utf-8\">%n");
			writer.printf("</head>%n%n");

			writer.printf("<div class=\"jumbotron text-center\">%n");
			writer.printf("<h1>MyTravelAdvisor </h1>%n");
			writer.printf("</div>");
			writer.printf("<body>%n%n");
			writer.printf("<div class=\"container\">");
			writer.printf("<div class=\"row\">");
			writer.printf("<div class=\"col-sm-4\">");
		}
		catch (IOException ex) {
			log.warn("Unable to prepare HTTP response.");
			return;
		}
	}

	/** get hotel Map */
	protected String getHotelNameMap(String hotelName) {
		String s = "";
		String[] city = hotelName.split(" ");
		StringBuilder name= new StringBuilder();
		for(String si:city) {
			name.append(si).append("%20");
		}
		return name.toString();
	}

	/** generic response for login flow */
	void finishResponse(HttpServletResponse response) {
		try {
			PrintWriter writer = response.getWriter();

			writer.printf("%n");
			writer.printf("<p style=\"font-size: 10pt; font-style: italic;\">");
			writer.printf("Last updated at %s.", getDate());
			writer.printf("</p>%n%n");
			writer.printf("</div>");
			writer.printf("</div>");
			writer.printf("</div>");
			writer.printf("</body>%n");
			writer.printf("</html>%n");

			writer.flush();

			response.setStatus(HttpServletResponse.SC_OK);
			response.flushBuffer();
		}
		catch (IOException ex) {
			log.warn("Unable to finish HTTP response.");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		}
	}

	/** response for welcome page */
	void finishResponseWelcome(HttpServletRequest request, HttpServletResponse response) {
		try {
			PrintWriter writer = response.getWriter();

			writer.printf("%n");
			writer.printf("<p style=\"font-size: 10pt; font-style: italic;\">");
			if(getCookieMap(request).containsKey("loginTime"))
				writer.printf("Last Login at %s.", dbhandler.getLastLoginTime(getUsername(request)));
			writer.printf("</p>%n%n");
			writer.printf("</div>");
			writer.printf("</div>");
			writer.printf("</div>");
			writer.printf("</body>%n");
			writer.printf("</html>%n");

			writer.flush();

			response.setStatus(HttpServletResponse.SC_OK);
			response.flushBuffer();
		}
		catch (IOException ex) {
			log.warn("Unable to finish HTTP response.");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		}
	}

	/** date format for displaying logged in time */
	protected String getDate() {
		String format = "hh:mm a 'on' EEE, MMM dd, yyyy";
		DateFormat dateFormat = new SimpleDateFormat(format);
		return dateFormat.format(Calendar.getInstance().getTime());
	}

	/** date format for inserting into Db */
	protected String getDateForDb() {
		String format = "EEE MMM dd HH:mm:ss zzz yyyy";
		DateFormat dateFormat = new SimpleDateFormat(format);
		return dateFormat.format(Calendar.getInstance().getTime());
	}

	/** returns the cookie map */
	protected Map<String, String> getCookieMap(HttpServletRequest request) {
		HashMap<String, String> map = new HashMap<String, String>();

		Cookie[] cookies = request.getCookies();

		if (cookies != null) {
			for (Cookie cookie : cookies) {
				map.put(cookie.getName(), cookie.getValue());
			}
		}

		return map;
	}

	/** Clears list of cookies */
	protected void clearCookies(HttpServletRequest request, HttpServletResponse response) {
		Cookie[] cookies = request.getCookies();

		if(cookies == null) {
			return;
		}

		for(Cookie cookie : cookies) {
			cookie.setValue("");
			cookie.setMaxAge(0);
			response.addCookie(cookie);
		}
	}

	/**
	 *
	 * @param errorName name of error
	 * @return status
	 */
	protected String getStatusMessage(String errorName) {
		Status status = null;

		try {
			status = Status.valueOf(errorName);
		}
		catch (Exception ex) {
			log.debug(errorName, ex);
			status = Status.ERROR;
		}

		return status.toString();
	}

	/**
	 *
	 * @param code message code
	 * @return status
	 */
	protected String getStatusMessage(int code) {
		Status status = null;

		try {
			status = Status.values()[code];
		}
		catch (Exception ex) {
			log.debug(ex.getMessage(), ex);
			status = Status.ERROR;
		}

		return status.toString();
	}

	/**
	 * get current username
	 * @param request
	 * @return username
	 */
	protected String getUsername(HttpServletRequest request) {
		Map<String, String> cookies = getCookieMap(request);

		String login = cookies.get("login");
		String user  = cookies.get("name");

		if ((login != null) && login.equals("true") && (user != null)) {
			// this is not safe!
			return user;
		}

		return null;
	}
}