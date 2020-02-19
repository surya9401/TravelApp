package hotelapp.TravelApp.LoginServlets;

import hotelapp.TravelApp.LoginServer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Redirects to welcome page or login page depending on whether user
 * session is detected.
 * Example of Prof. Engle
 *
 * @see LoginServer
 */
@SuppressWarnings("serial")
public class LoginRedirectServlet extends LoginBaseServlet {

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		if(request.getParameter("titleChange") != null) {
			String title = request.getParameter("titleChange");
			String review = request.getParameter("review");
			String rating = request.getParameter("rating");
			String hotelId = request.getParameter("hotelId");
			response.sendRedirect("/update?"+"titleChange="+title+"&review="+review+"&rating="+rating+"&hotelId="+hotelId);
		}
		else  if(request.getParameter("cityName") != null) {
			String city = request.getParameter("cityName");
			String keyword = request.getParameter("keyword");
			response.sendRedirect("/search?" + "cityName=" + city + "&keyword=" + keyword);
		}
		else if(request.getParameter("title") != null) {
			String title = request.getParameter("title");
			String review = request.getParameter("review");
			String rating = request.getParameter("rating");
			response.sendRedirect("/create?"+"title="+title+"&review="+review+"&rating="+rating);
		}
		else if (getUsername(request) != null) {
			response.sendRedirect("/welcome");
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