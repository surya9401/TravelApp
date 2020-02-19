package hotelapp.TravelApp.LoginServlets;

import hotelapp.TravelApp.LoginServer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Handles display of user information.
 * Example of Prof. Engle
 * @see LoginServer
 */

public class LoginWelcomeServlet extends LoginBaseServlet {

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		String user = getUsername(request);

		if (user != null) {
			prepareResponse("Welcome", response);
			PrintWriter out = response.getWriter();
			out.println("<h1>Hello " + user + "!</h1>");
			out.println("<p><a href=\"/login?logout\">(logout)</a></p>");
			out.println("<p><a href=\"/search?\">Search For Hotels</a></p>");
			out.print("");

			finishResponseWelcome(request, response);
		}
		else {
			response.sendRedirect("/login");
		}
		response.setStatus(HttpServletResponse.SC_OK);
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		doGet(request, response);
		response.setStatus(HttpServletResponse.SC_OK);
	}
}