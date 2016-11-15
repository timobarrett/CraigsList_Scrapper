package CraigsList.ScreenScrape.example;
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

public class ScrapeApts {
    private static final String priceName = "result-price";
    private static final String locateName = "result-hood";
    private static final String titleName = "result-title";
    private static final String info = "housing";
    private static String availMode = "availabilityMode=0";
    private static Document doc;
    private int mTotalCount = 0;
    private int mProcessedCount = 0;
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
        String mResultsFile = "/users/tim/Documents/queryResults.txt";
        BufferedWriter bw = openFile(mResultsFile);
        while (mTotalCount > mProcessedCount) {
            queryProcess();
            processPage(bw);
        }
        writeToFile(bw, "Total read in at start = " + String.valueOf(mTotalCount) + " Proccesed count = " + String.valueOf(mProcessedCount));
        writeToFile(bw, "Unique Listing Count = " + mUniqueListingSet.size());
        closeFile(bw);
    }

    /**
     * //https://losangeles.craigslist.org/search/apa?query=Pasadena
     * //https://losangeles.craigslist.org/search/apa?s=100&availabilityMode=0&query=Pasadena
     * //https://losangeles.craigslist.org/search/apa?query=glendale&max_price=2000&availabilityMode=0
     * //https://losangeles.craigslist.org/search/apa?query=pasadena&min_price=500&max_price=2000&availabilityMode=0
     *
     * @return query string
     */
    private String buildQuery() {
        String baseSearchUrl = "https://losangeles.craigslist.org/search/apa";
        String mLocateQuery = "query=";
        String mMinPrice = "min_price=";
        String mMaxPrice = "max_price=";
        StringBuilder queryUri = new StringBuilder(baseSearchUrl);
        if (mTotalCount != 0 && mProcessedCount != 0) {
            queryUri.append("?s=").append(Integer.toString(mProcessedCount)).append("&");
        } else {
            queryUri.append("?");
        }
        if (!processArgs.getCityName().isEmpty()) {
            queryUri.append(mLocateQuery).append(processArgs.getCityName());
        }
        if (processArgs.getMinPrice() != 0) {
            queryUri.append("&").append(mMinPrice).append(processArgs.getMinPrice());
        }
        if (processArgs.getMaxPrice() != 0) {
            queryUri.append("&").append(mMaxPrice).append(processArgs.getMaxPrice());
        }
        System.out.println("QUERY STRING = " + queryUri.toString());
        return queryUri.toString();
    }

    /**
     * This method performs the actual query getting the landing page
     * for the city
     */
    private void queryProcess() {
        String mDocumentDom = "/users/tim/Documents/domList.txt";
        String query = buildQuery();
        try {
            doc = Jsoup.connect(query).get();
        } catch (IOException io) {
            System.out.println("Error connecting to " + query + " - " + io.getMessage());
        }
        BufferedWriter bw = openFile(mDocumentDom);
        if (bw != null) {
            writeToFile(bw, doc.toString());
            closeFile(bw);
        }

        Elements total = doc.getElementsByClass("totalcount");
        mTotalCount = Integer.parseInt(total.get(0).text());

    }

    /**
     * get the city/location if present, the url of the post, and the rent amount
     */
    //TODO - if description is blank printout the stupid title - what about parsing and blocking $value
    public void processPage(BufferedWriter bw) {
        int procCnt = 0;
        String baseUrl = "https://losangeles.craigslist.org";

        Elements resultVals = doc.getElementsByClass("result-info");
        for (Element result : resultVals) {
            Elements links = result.getElementsByTag("a");
            String linkHref = links.attr("href");
            if (!linkHref.contains("#") && linkHref.contains("apa")) {
                writeToFile(bw, baseUrl + linkHref + "\r\n");  //this gets a clickable link to the listing
                if (!mUniqueListingSet.contains(baseUrl + linkHref)) {
                    mUniqueListingSet.add(baseUrl + linkHref);
                }
            }
            String title = result.getElementsByClass(titleName).get(0).text();
            //the above can yield # BR 2 BA Property Available for rent in North Hollywood $2900
            Elements resultInfo = result.getElementsByClass("result-meta");
            writeToFile(bw, resultInfo.get(0).getElementsByClass(priceName).text() + "  " +
                    resultInfo.get(0).getElementsByClass(info).text() + "  " +
                    resultInfo.get(0).getElementsByClass(locateName).text().replace("(", "").replace(")", "")+"\r\n");
            procCnt++;
        }
        //   System.out.println("COUNT = " + resultVals.size());
        mProcessedCount += procCnt;
    }

    /**
     * writes text to file
     *
     * @param bw - bufferedWriter
     * @param content - string content to write
     */
    public void writeToFile(BufferedWriter bw, String content) {
        //<div id="mapcontainer" data-arealat="34.052200" data-arealon="-118.242996">
        try {
            bw.write(content);
        } catch (IOException io) {
            System.out.println("Exception file processing = " + io.getMessage());
        }
    }

    /**
     * open file for text writing
     *
     * @param filename - name of file to open
     * @return BufferedWriter for file to write
     */
    public BufferedWriter openFile(String filename) {
        File file;
        BufferedWriter bw = null;
        try {
            file = new File(filename);
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            bw = new BufferedWriter(fw);
        } catch (IOException io) {
            bw = null;
            System.out.println("Error opening file " + filename);
        }
        return bw;
    }

    /**
     * close the file.
     *
     * @param bw bufferredwriter
     */
    public void closeFile(BufferedWriter bw) {
        try {
            bw.close();
        } catch (IOException io) {
            System.out.println("Errior closing file");
        }
    }
}

