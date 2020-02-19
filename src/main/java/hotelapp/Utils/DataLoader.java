package hotelapp.Utils;

import com.google.gson.Gson;
import hotelapp.Data.HotelObjectData;
import hotelapp.Data.Hotels;
import hotelapp.Data.ReviewData;
import hotelapp.TravelApp.DBUtils.DatabaseHandler;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** This class uses Jetty & servlets to implement server serving hotel info */
public class DataLoader {

    /** Keyword to identify path for hotel data */
    private static final String HOTELS = "-hotels";

    /** Keyword to identify path for hotel review data */
    private static final String REVIEWS = "-reviews";

    private static final int EXPECTED_INPUTS = 4;

    /** Assigning hotel data path */
    private static String HOTEL_PATH = "";

    /** Assigning hotel review path */
    private static String REVIEW_PATH = "";


    private static ThreadSafeHotelData hdata = new ThreadSafeHotelData();

    //Object of executor service for concurrency implementation
    private static ExecutorService executor;

    /**
     *
     * @param args unused
     * @throws Exception
     */
    public static void main(String[] args) {
        assignPath(args);

        executor = Executors.newFixedThreadPool(1);

        loadHotelInfo(HOTEL_PATH);
        loadReviews(Paths.get(REVIEW_PATH));
        System.out.println("DONE");
    }

    /**
     * Method to assign the paths for hotel data and review data
     * @param inputs - Arguments passed at runtime
     */
    private static void assignPath(String[] inputs) {
        if(inputs.length == EXPECTED_INPUTS) {
            if(inputs[0].equals(HOTELS) && inputs[2].equals(REVIEWS)) {
                HOTEL_PATH = inputs[1];
                REVIEW_PATH = inputs[3];
            }
            else {
                HOTEL_PATH = inputs[3];
                REVIEW_PATH = inputs[1];
            }
        }
        else {
            System.out.println("\nIncorrect number of input arguments\n");
            System.exit(1);
        }
    }

    public static ThreadSafeHotelData getData() {
        ThreadSafeHotelData copy;
        copy = hdata;
        return copy;
    }

    private static void loadHotelInfo(String jsonFilename) {
        List<HotelObjectData> hotelList;
        Gson gson = new Gson();
        try (FileReader br = new FileReader(jsonFilename))  {
            Hotels hotel = gson.fromJson(br, Hotels.class);
            hotelList = hotel.getClonedData();
            for (HotelObjectData hotelObjectData : hotelList) {
                hdata.addHotel(hotelObjectData.getID(), hotelObjectData.getName(), hotelObjectData.getCity(), hotelObjectData.getState(), hotelObjectData.getAddress(), hotelObjectData.getLatitude(), hotelObjectData.getLongitude());
                DatabaseHandler.getInstance().insertHotelsIntoDatabase(hotelObjectData.getID(), hotelObjectData.getName(), hotelObjectData.getCity(), hotelObjectData.getState(), hotelObjectData.getAddress(), hotelObjectData.getLatitude(), hotelObjectData.getLongitude());
            }
        }
        catch(IOException e) {
            System.out.println("Could not read the file: " + e);
        }
    }

    /** Loads reviews from json files. Recursively processes subfolders.
     *  Each json file with reviews should be processed concurrently (you need to create a new runnable job for each
     *  json file that you encounter)
     *  @param dir path of directory
     */
    private static void loadReviews(Path dir) {
        File folder = new File(dir.toString());
        File[] list = folder.listFiles();
        List<String> pathOfFiles = new ArrayList<>();
        assert list != null;
        for (File file : list) {
            if (file.isFile()) {
                pathOfFiles.add(file.toString());
            } else if (file.isDirectory()) {
                // To identify files in remaining directories
                loadReviews(file.getAbsoluteFile().toPath());
            }
        }
        parseReviewFiles(pathOfFiles);
    }

    /**
     * Parses list of files retrieved
     * @param pathOfFiles - list of file paths retrieved
     */
    private static void parseReviewFiles(List<String> pathOfFiles) {
        for (String pathOfFile : pathOfFiles) {
            parseAndGetReviewsByFile(pathOfFile);
        }
    }

    /**
     * Parses each file containing reviews using executor service
     * @param filePath path of the file
     */
    private static void parseAndGetReviewsByFile(String filePath) {
        executor.execute(() -> {
            ThreadSafeHotelData localHData = new ThreadSafeHotelData();
            try {
                localHData = ParserUtils.parseReviewFile(filePath);
            } finally {
                if(localHData.reviewDataSize() > 0) {
                    addAllReview(localHData);
                }
            }
        });
    }

    /**
     * Adds all reviews from Local HData to Big HData
     * @param localHData HData obtained from the executed Thread
     */
    private static void addAllReview(ThreadSafeHotelData localHData) {
        List<String> hotelIDs = new ArrayList<>(localHData.getReviewDataKeys());
        for (String hotelID : hotelIDs) {
            if (hdata.reviewKeyPresent(hotelID)) {
                new TreeSet<>(new ReviewComparator());
                TreeSet<ReviewData> data;
                data = hdata.reviewDataUsingHotelID(hotelID);
                data.addAll(localHData.reviewDataUsingHotelID(hotelID));
                hdata.addReviewData(hotelID, data);
            } else {
                new TreeSet<>(new ReviewComparator());
                TreeSet<ReviewData> data;
                data = localHData.reviewDataUsingHotelID(hotelID);
                hdata.addReviewData(hotelID, data);
            }
        }
    }

}
