package hotelapp.TravelApp.TravelAdvisorServlets;

import hotelapp.TravelApp.LoginServlets.LoginBaseServlet;
import hotelapp.Utils.ThreadSafeHotelData;
import hotelapp.Utils.TouristAttractionFinder;
import hotelapp.Utils.DataLoader;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

/**
 * Servlet to fetch tourist attractions
 */
public class TouristAttractionsServlet extends LoginBaseServlet {
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();

        String radius = request.getParameter("radius");
        String lat = request.getParameter("lat");
        String lng = request.getParameter("lng");
        String city = request.getParameter("cityName");
        String hotelName = request.getParameter("hotelName");
        ThreadSafeHotelData data = DataLoader.getData();
        TouristAttractionFinder ta = new TouristAttractionFinder(data);
        List<String> resp = ta.callGeocodeAPI(Integer.parseInt(radius), city, lat, lng);

        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);

        VelocityEngine ve = (VelocityEngine) request.getServletContext().getAttribute("templateEngine");
        VelocityContext context = new VelocityContext();
        Template template = ve.getTemplate("templates/travelAdvisorAttractions.html");

        context.put("hotels", resp);
        context.put("hotelName", hotelName);
        context.put("name", getUsername(request));

        StringWriter writer = new StringWriter();
        template.merge(context, writer);
        out.println(writer.toString());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }
}
