package crawler;

/**
 * Created by NIC on 5/30/17.
 */
public class CrawlerMain {

    static final String categoryUrlPath = "/Users/NIC/Documents/504_BankEnd/HW/HW5_PriceMonitor/originalURL.txt";
    static final String subCategoryUrlPath="/Users/NIC/Documents/504_BankEnd/HW/HW5_PriceMonitor/logURL.txt";
    static final String proxyPath = "/Users/NIC/Documents/504_BankEnd/HW/HW3_Clawer/proxylist_bittiger.csv";
    //static final String productDetailLogPath = "/Users/NIC/Documents/504_BankEnd/HW/HW5_PriceMonitor/productDetail.txt";
    public static void main(String args[]) throws Exception {

        SubCategoryCrawler subCategoryCrawler = new SubCategoryCrawler(proxyPath);
        //subCategoryCrawler.exploreSubCategoryLinks(categoryUrlPath, subCategoryUrlPath);

        subCategoryCrawler.getDetailProductInfo(subCategoryUrlPath);
        //subCategoryCrawler.getDetailProductInfo(subCategoryUrlPath,productDetailLogPath);









    }

}
