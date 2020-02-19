package hotelapp.TravelApp;

import hotelapp.TravelApp.TravelAdvisorServlets.CreateServlet;
import hotelapp.TravelApp.LoginServlets.LoginRedirectServlet;
import hotelapp.TravelApp.LoginServlets.LoginRegisterServlet;
import hotelapp.TravelApp.LoginServlets.LoginUserServlet;
import hotelapp.TravelApp.LoginServlets.LoginWelcomeServlet;
import hotelapp.TravelApp.TravelAdvisorServlets.SearchServlet;
import hotelapp.TravelApp.TravelAdvisorServlets.TouristAttractionsServlet;
import hotelapp.TravelApp.TravelAdvisorServlets.UpdateReviewServlet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.velocity.app.VelocityEngine;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;

/**
 * Demonstrates simple user registration, login, and session tracking. This
 * is a simplified example, and **NOT** secure.
 * This comprehensive example is provided by Prof. Engle.
 */
public class LoginServer {
	private static Logger log = LogManager.getLogger();

	public static void main(String[] args) {
		int PORT = 8080;
		Server server = new Server(PORT);

		ServletContextHandler handler = new ServletContextHandler();
		handler.addServlet(LoginUserServlet.class,     "/login");
		handler.addServlet(LoginRegisterServlet.class, "/register");
		handler.addServlet(LoginWelcomeServlet.class,  "/welcome");
		handler.addServlet(LoginRedirectServlet.class, "/*");
		handler.addServlet(SearchServlet.class, "/search");
		handler.addServlet(TouristAttractionsServlet.class, "/attractions");
		handler.addServlet(CreateServlet.class, "/create");
		handler.addServlet(UpdateReviewServlet.class, "/update");
		handler.setResourceBase("static");

		// initialize Velocity
		VelocityEngine velocity = new VelocityEngine();
		velocity.init();

		handler.setAttribute("templateEngine", velocity);
		server.setHandler(handler);
		log.info("Starting server on port " + PORT + "...");
		try {
			server.start();
			server.join();

			log.info("Exiting...");
		}
		catch (Exception ex) {
			log.fatal("Interrupted while running server.", ex);
			System.exit(-1);
		}
	}
}