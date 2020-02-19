package hotelapp.TravelApp.LoginServlets;

import hotelapp.TravelApp.DBUtils.Status;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class LoginRegisterServlet extends LoginBaseServlet {

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException {

		prepareResponse("Register New User", response);

		PrintWriter out = response.getWriter();
		String error = request.getParameter("error");

		if(error != null) {
			String errorMessage = getStatusMessage(error);
			out.println("<p style=\"color: red;\">" + errorMessage + "</p>");
		}

		printForm(out);
		finishResponse(response);
		response.setStatus(HttpServletResponse.SC_OK);
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		prepareResponse("Register New User", response);

		String newuser = request.getParameter("user");
		String newpass = request.getParameter("pass");
		if(!validatePassword(newpass)) {
			Status status = Status.INVALID_PASSWORD;
			String url = "/register?error=" + status.name();
			url = response.encodeRedirectURL(url);
			response.sendRedirect(url);
		}
		else {
			Status status = dbhandler.registerUser(newuser, newpass);
			dbhandler.updateTimeToDisplay(newuser, getDate());
			dbhandler.updateTimeTemp(newuser, getDate());

			if(status == Status.OK) {
				response.sendRedirect(response.encodeRedirectURL("/login?newuser=true"));
			}
			else {
				String url = "/register?error=" + status.name();
				url = response.encodeRedirectURL(url);
				response.sendRedirect(url);
			}
		}
		response.setStatus(HttpServletResponse.SC_OK);
	}

	/**
	 * Validate's password backend
	 * @param password
	 * @return
	 */
	private boolean validatePassword(String password) {
		boolean result = false;
		int num = 0;
		int uletter = 0;
		int special = 0;
		char[] array = password.toCharArray();
		for(Character c:array) {
			if (Character.isDigit(c))
				num++;
			if (Character.isUpperCase(c))
				uletter++;
			else
				special++;
		}
		result = num > 0 && uletter>0 && special > 0;
		return result;
	}

	/** Prints the general form */
	private void printForm(PrintWriter out) {
		assert out != null;

		out.println("<head>");
		out.println("\t<script src =\"js/validatePassword.js\">");
		out.println("\t</script>");
		out.println("\t</head>");
		out.println("<form action=\"/register\" method=\"post\">");
		out.println("<table border=\"0\">");
		out.println("\t<tr>");
		out.println("\t\t<td>Username:</td>");
		out.println("\t\t<td><input type=\"text\" name=\"user\" size=\"30\"></td>");
		out.println("\t</tr>");
		out.println("\t<tr>");
		out.println("\t\t<td>Password:</td>");
		out.println("\t\t<td><input type=\"password\" name=\"pass\" onkeyup=\"validatePassword(this);\" size=\"30\"></td>");
		out.println("</tr>");
		out.println("</table>");
		out.println("<p><input type=\"submit\" value=\"Register\"></p>");
		out.println("</form>");
	}
}