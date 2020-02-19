package hotelapp.TravelApp.TravelAdvisorServlets;

import hotelapp.Data.HotelDbData;
import hotelapp.Data.ReviewDbData;
import hotelapp.TravelApp.LoginServlets.LoginBaseServlet;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

public class SearchServlet extends LoginBaseServlet {

    private List<HotelDbData> hotelData;
    private HotelDbData currHotel;
    private List<ReviewDbData> currUserReview;

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        PrintWriter out = response.getWriter();
        // Redirects to page having hotel information from saved hotel list
        if(request.getParameter("selectedHotel") != null) {
            HotelDbData hotelData = dbhandler.getHotelDataUsingHotelName(request.getParameter("hotelName"));
            currHotel = hotelData;
            String hotelId = hotelData.getId();
            String hotelAddress = hotelData.getAddress();
            String cityName = hotelData.getCity();
            List<ReviewDbData> reviews = dbhandler.getHotelReviewsByHotelId(hotelId);
            for(ReviewDbData data:reviews) {
                if(data.getReviewTitle().equals(""))
                    data.setReviewTitle("BLANK");
                if(data.getUserName().equals(""))
                    data.setUserName("Anonymous");
            }
            VelocityEngine ve = (VelocityEngine) request.getServletContext().getAttribute("templateEngine");
            VelocityContext context = new VelocityContext();
            Template template = ve.getTemplate("templates/travelAdvisorSavedReview.html");

            context.put("hotels", reviews);
            context.put("hotelName", request.getParameter("hotelName"));
            context.put("name", getUsername(request));
            context.put("address", hotelAddress);
            context.put("hotelId", hotelId);
            context.put("cityName", cityName);
            context.put("hotelNameForMap", getHotelNameMap(request.getParameter("hotelName")));

            StringWriter writer = new StringWriter();
            template.merge(context, writer);
            out.println(writer.toString());
        }
        // Shows a list of saved hotels from database
        else if(request.getParameter("savedHotels") != null) {
            response.setContentType("text/html");
            response.setStatus(HttpServletResponse.SC_OK);
            if(request.getParameter("clear") != null) {
                dbhandler.clearSavedHotels(getUsername(request));
            }
            else if(request.getParameter("hotelName") != null){
                dbhandler.saveHotel(getUsername(request), request.getParameter("hotelName"));
                response.sendRedirect("/search");
            }
            //display all saved hotels
            else {
                VelocityEngine ve = (VelocityEngine) request.getServletContext().getAttribute("templateEngine");
                VelocityContext context = new VelocityContext();
                Template template = ve.getTemplate("templates/travelAdvisorSavedHotels.html");
                List<String> hotels = dbhandler.getSavedHotels(getUsername(request));

                context.put("hotels", hotels);
                context.put("hotelId", request.getParameter("hotelId"));
                context.put("name", getUsername(request));

                StringWriter writer = new StringWriter();
                template.merge(context, writer);
                out.println(writer.toString());
            }
        }
        // deletes a review when chosen by user
        else if(request.getParameter("deleteReview") != null) {
            String hotelId = request.getParameter("hotelId");
            String title = request.getParameter("titleReview");
            response.setContentType("text/html");
            response.setStatus(HttpServletResponse.SC_OK);
            dbhandler.deleteReview(hotelId, getUsername(request), title);
            response.sendRedirect("/search");
        }
        //shows nearby attractions withing 4 mile radius
        else if(request.getParameter("attractions") != null) {
            String radius = request.getParameter("attractions");
            response.setContentType("text/html");
            response.setStatus(HttpServletResponse.SC_OK);
            String url = "attractions?&radius="+radius+"&lat="+currHotel.getLat()+"&lng="+currHotel.getLng()+"&cityName="+currHotel.getCity()+"&hotelName="+currHotel.getName();
            response.sendRedirect(url);
        }
        //shows list of reviews provided by the current user
        else if(request.getParameter("userReviews") != null) {
            response.setContentType("text/html");
            response.setStatus(HttpServletResponse.SC_OK);
            List<ReviewDbData> reviews = dbhandler.getUserReviews(getUsername(request));
            currUserReview = reviews;
            if(reviews.size() != 0) {
                VelocityEngine ve = (VelocityEngine) request.getServletContext().getAttribute("templateEngine");
                VelocityContext context = new VelocityContext();
                Template template = ve.getTemplate("templates/travelAdvisorReviewsByUser.html");

                context.put("hotels", reviews);
                context.put("name", getUsername(request));

                StringWriter writer = new StringWriter();
                template.merge(context, writer);
                out.println(writer.toString());
            }
        }

        //redirects to form page to change a review information by current user
        else if(request.getParameter("userReviewsEdit") != null) {
            response.setContentType("text/html");
            response.setStatus(HttpServletResponse.SC_OK);
            VelocityEngine ve = (VelocityEngine) request.getServletContext().getAttribute("templateEngine");
            VelocityContext context = new VelocityContext();
            Template template = ve.getTemplate("templates/travelAdvisorCreateEdit.html");

            context.put("titleValue", request.getParameter("title"));
            context.put("reviewValue", request.getParameter("review"));
            context.put("rating", request.getParameter("rating"));
            context.put("hotelId", request.getParameter("hotelId"));
            context.put("hotelName", request.getParameter("hotelName"));
            context.put("name", getUsername(request));

            StringWriter writer = new StringWriter();
            template.merge(context, writer);
            out.println(writer.toString());

        }
        // Shows a list of saved expedia links from database
        else if(request.getParameter("expediaLinks") != null) {
            response.setContentType("text/html");
            response.setStatus(HttpServletResponse.SC_OK);
            if(request.getParameter("clear") != null) {
                dbhandler.clearExpediaLinks(getUsername(request));
            }
            else {
                List<String> links= dbhandler.getExpediaLinks(getUsername(request));

                VelocityEngine ve = (VelocityEngine) request.getServletContext().getAttribute("templateEngine");
                VelocityContext context = new VelocityContext();
                Template template = ve.getTemplate("templates/travelAdvisorExpedia.html");

                context.put("hotels", links);
                context.put("name", getUsername(request));

                StringWriter writer = new StringWriter();
                template.merge(context, writer);
                out.println(writer.toString());
            }
        }

        //Redirecting to Expedia Link and storing in database
        else if(request.getParameter("expedia") != null) {
            response.setContentType("text/html");
            response.setStatus(HttpServletResponse.SC_OK);
            dbhandler.addExpediaLinks(getUsername(request),currHotel.getLink());
            response.sendRedirect(currHotel.getLink());
        }

        //Showing List of Hotels with rating
        else if(request.getParameter("cityName") != null) {
            String cityName = request.getParameter("cityName");
            String keyword = request.getParameter("keyword");
            response.setContentType("text/html");
            response.setStatus(HttpServletResponse.SC_OK);
            hotelData = dbhandler.searchHotels(cityName, keyword);
            VelocityEngine ve = (VelocityEngine) request.getServletContext().getAttribute("templateEngine");
            VelocityContext context = new VelocityContext();
            Template template = ve.getTemplate("templates/travelAdvisor.html");

            context.put("cityName", cityName);
            context.put("hotels", hotelData);
            context.put("name", getUsername(request));

            StringWriter writer = new StringWriter();
            template.merge(context, writer);
            out.println(writer.toString());
        }

        // Showing List of reviews by Hotel Name
        else if (request.getParameter("hotelName") != null){
            response.setContentType("text/html");
            response.setStatus(HttpServletResponse.SC_OK);
            List<ReviewDbData> reviews = getReviews(request.getParameter("hotelName"), hotelData);
            VelocityEngine ve = (VelocityEngine) request.getServletContext().getAttribute("templateEngine");
            VelocityContext context = new VelocityContext();
            Template template = ve.getTemplate("templates/travelAdvisorReviews.html");
            for(ReviewDbData data:reviews) {
                if(data.getReviewTitle().equals(""))
                    data.setReviewTitle("BLANK");
                if(data.getUserName().equals(""))
                    data.setUserName("Anonymous");
            }
            context.put("cityName", currHotel.getCity());
            context.put("hotelName", currHotel.getName());
            context.put("hotels", reviews);
            context.put("hotelId", currHotel.getId());
            context.put("address", currHotel.getAddress());
            context.put("name", getUsername(request));
            context.put("hotelNameForMap", getHotelNameMap(currHotel.getName()));

            StringWriter writer = new StringWriter();
            template.merge(context, writer);
            out.println(writer.toString());
        }
        else {
            // Default Search Page
            response.setContentType("text/html");
            response.setStatus(HttpServletResponse.SC_OK);

            VelocityEngine ve = (VelocityEngine) request.getServletContext().getAttribute("templateEngine");
            VelocityContext context = new VelocityContext();
            Template template = ve.getTemplate("templates/travelAdvisorWithoutList.html");

            context.put("cityName", "California");
            context.put("name", getUsername(request));

            StringWriter writer = new StringWriter();
            template.merge(context, writer);
            out.println(writer.toString());
        }
    }

    /**
     *
     * @param hotelName hotel name
     * @param hotelData list of hotel data from Database
     * @return list of hotel reviews for specific hotel name
     */
    private List<ReviewDbData> getReviews(String hotelName, List<HotelDbData> hotelData) {
        List<ReviewDbData> id = null;
        for(HotelDbData data: hotelData) {
            if(data.getName().equals(hotelName)) {
                id = data.getReviewData();
                currHotel=data;
            }
        }
        return id;
    }


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }
}

