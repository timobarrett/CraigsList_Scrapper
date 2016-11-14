/**
 * Created by tim on 11/9/2016.
 */

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

//TODO - what to do with the output?
//TODO - process parameters - max price, cityname, bed rooms
public class ScrapeApts {
    private static final String priceName = "result-price";
    private static final String locateName = "result-hood";
    private static final String titleName = "result-title";
    private static final String info = "housing";
    private static String baseSearchUrl = "https://losangeles.craigslist.org/search/apa";
    private static String baseUrl = "https://losangeles.craigslist.org";
    private static String availMode = "availabilityMode=0";
    private static Document doc;
    private int mTotalCount = 0;
    private int mProcessedCount = 0;
    private final String mLocateQuery = "query=";
    private final String mMinPrice = "min_price=";
    private final String mMaxPrice = "max_price=";
    Set<String> mUniqueListingSet = new HashSet<String>();
    ProcessArgs processArgs = null;

    public static void main(String[] args) throws IOException {
        ScrapeApts scrape = new ScrapeApts();
        scrape.processArgs = new ProcessArgs(args);
        scrape.queryProcess();
        scrape.processListings();
    }

    /**
     * procesListings
     * build query string
     * load using query string
     * process query
     */
    private void processListings() {
        while (mTotalCount > mProcessedCount) {
            queryProcess();
            processPage();
        }
        System.out.println("Total read in at start = " + String.valueOf(mTotalCount) + " Proccesed count = " + String.valueOf(mProcessedCount));
        System.out.println("Unique Listing Count = " + mUniqueListingSet.size());
    }

    /**
     * //https://losangeles.craigslist.org/search/apa?query=Pasadena
     * //https://losangeles.craigslist.org/search/apa?s=100&availabilityMode=0&query=Pasadena
     * //https://losangeles.craigslist.org/search/apa?query=glendale&max_price=2000&availabilityMode=0
     * //https://losangeles.craigslist.org/search/apa?query=pasadena&min_price=500&max_price=2000&availabilityMode=0
     *
     * @return
     */
    private String buildQuery() {
        StringBuilder queryUri = new StringBuilder(baseSearchUrl);
        if (mTotalCount != 0 && mProcessedCount != 0) {
            queryUri.append("?s=" + Integer.toString(mProcessedCount) + "&");
        } else {
            queryUri.append("?");
        }
        if (!processArgs.getCitytName().isEmpty()) {
            queryUri.append(mLocateQuery + processArgs.getCitytName());
        }
        if (processArgs.getMinPrice() != 0) {
            queryUri.append("&" + mMinPrice + processArgs.getMinPrice());
        }
        if (processArgs.getmMaxPrice() != 0) {
            queryUri.append("&" + mMaxPrice + processArgs.getmMaxPrice());
        }
        System.out.println("QUERY STRING = " + queryUri.toString());
        return queryUri.toString();
    }

    /**
     * This method performs the actual query getting the landing page
     * for the city
     */
    private void queryProcess() {
        String query = buildQuery();
        try {
            doc = Jsoup.connect(query).get();
        } catch (IOException io) {
            System.out.println("Error connecting to " + query + " - " + io.getMessage());
        }
        writeToFile();
        Elements total = doc.getElementsByClass("totalcount");
        mTotalCount = Integer.parseInt(total.get(0).text());

    }

    /**
     * get the city/location if present, the url of the post, and the rent amount
     */
    //TODO - if description is blank printout the stupid title - what about parsing and blocking $value
    public void processPage() {
        int procCnt = 0;
        Elements resultVals = doc.getElementsByClass("result-info");
        for (Element result : resultVals) {
            Elements links = result.getElementsByTag("a");
            String linkHref = links.attr("href");
            if (!linkHref.contains("#") && linkHref.contains("apa")) {
                System.out.println(baseUrl + linkHref);  //this gets a clickable link to the listing
                if (!mUniqueListingSet.contains(baseUrl + linkHref)) {
                    mUniqueListingSet.add(baseUrl + linkHref);
                }
            }
            String title = result.getElementsByClass(titleName).get(0).text();
            //the above can yield # BR 2 BA Property Available for rent in North Hollywood $2900
            Elements resultInfo = result.getElementsByClass("result-meta");
            System.out.println(resultInfo.get(0).getElementsByClass(priceName).text() + "  " +
                    resultInfo.get(0).getElementsByClass(info).text() + "  " +
                    resultInfo.get(0).getElementsByClass(locateName).text().replace("(", "").replace(")", ""));
            procCnt++;
        }
        //   System.out.println("COUNT = " + resultVals.size());
        mProcessedCount += procCnt;
    }

    /**
     * Dump the entire DOM to a file to track down elements
     *
     */
    public void writeToFile() {
        //<div id="mapcontainer" data-arealat="34.052200" data-arealon="-118.242996">
        try {
            File file = new File("/users/tim/Documents/domList.txt");
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(doc.toString());
            bw.close();
        } catch (IOException io) {
            System.out.println("Exception file processing = " + io.getMessage());
        }
    }

}

