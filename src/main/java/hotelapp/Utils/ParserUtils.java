package hotelapp.Utils;
import com.google.gson.stream.JsonReader;

import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class ParserUtils {

    /**
     * Helper method to parse the responseData in json format from Google Places API
     * @param jsonResult obtained from google places API
     * @return List of Tourist Attraction Objects
     */
    public static List<String> getAttractions(String jsonResult) {
        List<String> ta = new ArrayList<>();
        try {
            String name = "", id ="", address= "";
            double rating=0.0;
            JsonReader jsonReader = new JsonReader(new StringReader(jsonResult));
            jsonReader.beginObject();
            while (jsonReader.hasNext()) {
                if (jsonReader.nextName().equals("results")) {
                    jsonReader.beginArray();
                    while (jsonReader.hasNext()) {
                        jsonReader.beginObject();
                        while (jsonReader.hasNext()) {
                            String input = jsonReader.nextName();
                            switch (input) {
                                case "formatted_address":
                                    address = jsonReader.nextString();
                                    break;
                                case "id":
                                    id = jsonReader.nextString();
                                    break;
                                case "rating":
                                    rating = jsonReader.nextDouble();
                                    break;
                                case "name":
                                    name = jsonReader.nextString();
                                    break;
                                default:
                                    jsonReader.skipValue();
                                    break;
                            }
                        }
                        jsonReader.endObject();
                        ta.add(name);
                    }
                    jsonReader.endArray();
                }
                else {
                    jsonReader.skipValue();
                }
            }
            jsonReader.endObject();

        } catch (Exception e) {
            System.out.println("Could not read line");
        }
        return ta;
    }

    /**
     * Parser function to get the API key from config.json
     * @return API Key for Google
     */
    public static String getAPIKeyFromConfig() {
        String result = "";
        try {
            JsonReader jsonReader = new JsonReader(new FileReader("input/config.json"));
            jsonReader.beginObject();
            while(jsonReader.hasNext()) {
                if (jsonReader.nextName().equals("apikey")) {
                    result = jsonReader.nextString();
                }
                else {
                    jsonReader.skipValue();
                }
            }
            jsonReader.endObject();
        } catch (Exception e) {
            System.out.println("Could not read line");
        }
        return result;
    }

    /**
     * Returns Local HData for thread which is currently executing
     * @param filePath Path of File
     * @return Local HData for current thread
     */
    public static ThreadSafeHotelData parseReviewFile(String filePath) {
        ThreadSafeHotelData localHData = new ThreadSafeHotelData();
        int hotelID=0;
        int ratingScore=0;
        String userNickName="";
        String reviewID = "";
        String title = "";
        String reviewtime="";
        String reviewText="";
        boolean isRecommended=false;
        try (JsonReader jsonReader = new JsonReader(new FileReader(filePath))) {
            jsonReader.beginObject();
            while (jsonReader.hasNext()) {
                if(jsonReader.nextName().equals("reviewDetails")) {
                    jsonReader.beginObject();
                    while(jsonReader.hasNext()) {
                        String name = jsonReader.nextName();
                        if(name.equals("reviewSummaryCollection")) {
                            jsonReader.beginObject();
                            while(jsonReader.hasNext()) {
                                if(jsonReader.nextName().equals("reviewSummary")) {
                                    jsonReader.beginArray();
                                    while(jsonReader.hasNext()) {
                                        jsonReader.beginObject();
                                        while(jsonReader.hasNext()) {
                                            if (jsonReader.nextName().equals("hotelId")) {
                                                hotelID = jsonReader.nextInt();
                                            } else {
                                                jsonReader.skipValue();
                                            }
                                        }
                                        jsonReader.endObject();
                                    }
                                    jsonReader.endArray();
                                }
                                else {
                                    jsonReader.skipValue();
                                }
                            }
                            jsonReader.endObject();
                        }
                        else if(name.equals("reviewCollection")) {
                            jsonReader.beginObject();
                            while(jsonReader.hasNext()) {
                                if (jsonReader.nextName().equals("review")) {
                                    jsonReader.beginArray();
                                    while (jsonReader.hasNext()) {
                                        jsonReader.beginObject();
                                        while(jsonReader.hasNext()) {
                                            String nameRet = jsonReader.nextName();
                                            switch (nameRet) {
                                                case "reviewId":
                                                    reviewID = jsonReader.nextString();
                                                    break;
                                                case "userNickname":
                                                    userNickName = jsonReader.nextString();
                                                    break;
                                                case "title":
                                                    title = jsonReader.nextString();
                                                    break;
                                                case "reviewSubmissionTime":
                                                    reviewtime = jsonReader.nextString();
                                                    break;
                                                case "reviewText":
                                                    reviewText = jsonReader.nextString();
                                                    break;
                                                case "ratingOverall":
                                                    ratingScore = jsonReader.nextInt();
                                                    break;
                                                case "isRecommended":
                                                    if(jsonReader.nextString().equals("YES"))
                                                        isRecommended = true;
                                                    break;
                                                default:
                                                    jsonReader.skipValue();
                                                    break;
                                            }
                                        }
                                        jsonReader.endObject();

                                        localHData.addReview(String.valueOf(hotelID), reviewID, ratingScore, title, reviewText, isRecommended, reviewtime, userNickName);

                                    }
                                    jsonReader.endArray();
                                }
                            }
                            jsonReader.endObject();
                        }
                        else{
                            jsonReader.skipValue();
                        }
                    }
                    jsonReader.endObject();
                }
                else{
                    jsonReader.skipValue();
                }
            }
            jsonReader.endObject();
        } catch (IOException e) {
            System.out.println("Could not read from file");
        }
        return localHData;
    }
}
