package hotelapp.TravelApp.TravelAdvisorServlets;

import hotelapp.TravelApp.LoginServlets.LoginBaseServlet;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Servlet to Update Reviews
 */
public class UpdateReviewServlet extends LoginBaseServlet {

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        if(request.getParameter("titleChange") != null) {

            response.setContentType("text/html");
            response.setStatus(HttpServletResponse.SC_OK);

            VelocityEngine ve = (VelocityEngine) request.getServletContext().getAttribute("templateEngine");
            VelocityContext context = new VelocityContext();
            dbhandler.updateReview(getUsername(request),request.getParameter("titleChange"),  request.getParameter("review"), request.getParameter("rating"), request.getParameter("hotelId"));
            response.sendRedirect("/search");
        }
        response.setStatus(HttpServletResponse.SC_OK);
}


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        if(request.getParameter("title") != null) {
            System.out.println("kk");
        }
    }
}
