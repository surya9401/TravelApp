package hotelapp.TravelApp.TravelAdvisorServlets;

import hotelapp.TravelApp.LoginServlets.LoginBaseServlet;
import hotelapp.TravelApp.DBUtils.DatabaseHandler;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Class to Create Reviews for Hotel
 */
public class CreateServlet extends LoginBaseServlet {

    private String currentHotelId;

    /**
     *
     * @param request server request
     * @param response server response
     * @throws IOException exceptions
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        PrintWriter out = response.getWriter();
        // Redirects to create form
        if(request.getParameter("hotelId") != null) {
            currentHotelId = request.getParameter("hotelId");

            String hotelName = request.getParameter("hotelName");

            response.setContentType("text/html");
            response.setStatus(HttpServletResponse.SC_OK);

            VelocityEngine ve = (VelocityEngine) request.getServletContext().getAttribute("templateEngine");
            VelocityContext context = new VelocityContext();
            Template template = ve.getTemplate("templates/travelAdvisorCreateReview.html");

            context.put("hotelName", hotelName);
            context.put("userName", getUsername(request));
            StringWriter writer = new StringWriter();
            template.merge(context, writer);
            out.println(writer.toString());
        }
        // Creates the review
        else if(request.getParameter("title") != null) {
            String title = request.getParameter("title");
            String review = request.getParameter("review");
            String rating = request.getParameter("rating");
            String username = getUsername(request);
            if(currentHotelId != null) {
                DatabaseHandler.getInstance().insertReviewsIntoDatabase(currentHotelId, null, rating, title, review, null, getDateForDb(), username);
            }
            response.setContentType("text/html");
            response.setStatus(HttpServletResponse.SC_OK);

            VelocityEngine ve = (VelocityEngine) request.getServletContext().getAttribute("templateEngine");
            VelocityContext context = new VelocityContext();
            Template template = ve.getTemplate("templates/travelAdvisorReviewCreated.html");

            context.put("userName", getUsername(request));
            StringWriter writer = new StringWriter();
            template.merge(context, writer);
            out.println(writer.toString());
        }

        response.setStatus(HttpServletResponse.SC_OK);
    }
}
