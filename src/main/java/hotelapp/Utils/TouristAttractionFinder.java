package hotelapp.Utils;

import hotelapp.Data.HotelObjectData;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Class responsible for getting tourist attractions near each hotel from the Google Places API.
 *  Also scrapes some data about hotels from expedia html webpage.
 */
public class TouristAttractionFinder {

    private static final String host = "maps.googleapis.com";
    private static final String path = "/maps/api/place/textsearch/json";

    private ThreadSafeHotelData lData;

    /** Constructor for TouristAttractionFinder
     *
     * @param hdata Thread Safe Hotel Data passed to constructor
     */
    public TouristAttractionFinder(ThreadSafeHotelData hdata) {
        this.lData = hdata;
    }
    /**
     * Call to Google Places API to get nearby places from a particular hotel location with specified radius
     * @param radius radius in miles
     * @return
     */
    public List<String> callGeocodeAPI(int radius, String cityname, String lat, String lng) {
        String s = "";
        String[] city = cityname.split(" ");
        StringBuilder cityName= new StringBuilder();
        for(String si:city) {
            cityName.append(si).append("%20");
        }
        String urlString ="https://"+ host + path + "?query=tourist%20attractions+in+"+cityName.toString() +"&location="+ lat+","+ lng + radius*1600 + "&key="+ ParserUtils.getAPIKeyFromConfig();;
        URL url;
        PrintWriter out = null;
        BufferedReader in = null;
        SSLSocket socket = null;
        try {
            url = new URL(urlString);

            SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();

            // HTTPS uses port 443
            socket = (SSLSocket) factory.createSocket(url.getHost(), 443);

            // output stream for the secure socket
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
            String request = getRequest(url.getHost(), url.getPath() + "?" + url.getQuery());

            out.println(request); // send a request to the server
            out.flush();

            // input stream for the secure socket.
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String line;
            StringBuffer sb = new StringBuffer();
            boolean check = false;
            while ((line = in.readLine()) != null) {
                if(line.indexOf('{') != -1)
                    check = true;
                if(check) {
                    sb.append(line);
                }
            }
            s = sb.toString();
        } catch (IOException e) {
            System.out.println(
                    "An IOException occured while writing to the socket stream or reading from the stream: " + e);
        } finally {
            try {
                // close the streams and the socket
                out.close();
                in.close();
                socket.close();
            } catch (IOException e) {
                System.out.println("An exception occured while trying to close the streams or the socket: " + e);
            }
        }
        return ParserUtils.getAttractions(s);
    }

    /**
     * Takes a host and a string containing path/resource/query and creates a
     * string of the HTTP GET request
     *
     * @param host
     * @param pathResourceQuery
     * @return
     */
    private static String getRequest(String host, String pathResourceQuery) {
        String request = "GET " + pathResourceQuery + " HTTP/1.1" + System.lineSeparator() // GET
                // request
                + "Host: " + host + System.lineSeparator() // Host header required for HTTP/1.1
                + "Connection: close" + System.lineSeparator() // make sure the server closes the
                // connection after we fetch one page
                + System.lineSeparator();
        return request;
    }

    /**
     * 
     * @param hotelID Respective hotel id for which description is being added
     * @return Path of the HTML file containing the description information
     */
    private static Path getHTMLFilePath(String hotelID) {
        Path p = null;
        String dir = "input/html/";
        File folder = new File(dir);
        File[] list = folder.listFiles();
        assert list != null;
        for (File file:list) {
            if(file.toString().contains(hotelID)) {
                p =  file.toPath();
                break;
            }
        }
        return p;
    }
}
