package crawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.*;
// /import java.util.*;

/**
 * Created by NIC on 5/31/17.
 */
public class SubCategoryLinkCrawler {

    private final String proxyUser = "bittiger";
    private final String proxyPassword = "cs504";
    private List<String> proxyList;
    private int indexForProxyList = 0;
    private List<String>titleListSelector;
    private List<String>resultSizeSelector;
    private  HashSet<String> crawledUrl;


    public SubCategoryLinkCrawler(String proxyPath){
        initProxyList(proxyPath);
        initSelector();
    }
    private void initSelector(){
        titleListSelector = new ArrayList<>();
        titleListSelector.add(" > div > div:nth-child(3) > div:nth-child(1) > a > h2");
        titleListSelector.add("> div > div:nth-child(5) > div:nth-child(1) > a > h2");
        titleListSelector.add("> div > div.a-row.a-spacing-none > div.a-row.a-spacing-mini > a > h2");
        resultSizeSelector = new ArrayList<>();
        resultSizeSelector.add("li[class=s-result-item  celwidget ]");
        resultSizeSelector.add("li[class=s-result-item s-result-card-for-container a-declarative celwidget ]");
    }

    public void exploreSubCategoryLinks(String categoryUrlPath, String subCategoryUrlPath) throws IOException {

        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        headers.put("Accept-Encoding", "gzip, deflate, sdch, br");
        headers.put("Accept-Language", "en-US,en;q=0.8");
        String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.95 Safari/537.36";

        File file = new File(subCategoryUrlPath);
        if(!file.exists()){
            file.createNewFile();

        }
        BufferedWriter bw = new BufferedWriter(new FileWriter(file.getAbsoluteFile()));


        BufferedReader br = new BufferedReader(new FileReader(categoryUrlPath));

        String urlLine;
        Queue<String> queue = new LinkedList<>();
        Set<String>hashSet = new HashSet<>();
        while ((urlLine = br.readLine()) != null){
            if(hashSet.contains(urlLine)){
                continue;
            }
            hashSet.add(urlLine);
            queue.offer(urlLine);

        }
        while (!queue.isEmpty()){
            changeProxy();
            //change ip for every url
            String url = queue.poll();
            if (url.isEmpty())
                continue;
            url = url.trim();
            System.out.println("category link need to be crawled " +url);
            try {
                Document doc = Jsoup.connect(url).headers(headers).userAgent(USER_AGENT).maxBodySize(0).timeout(1000000).get();
                Set<String>subUrlHash = new HashSet<>();
                //Elements elements = doc.select("li[class=sub-categories__list__item]>a");
                if(doc != null){
                    //Elements elements = doc.select("a[class=sub-categories__list__item__link]");
                    //Elements elements = doc.select("div[class=bxc-grid__image   bxc-grid__image--light]");
                    Elements elements = doc.select("span[class=nav-a-content]");
                    System.out.println(elements.size());
                    if(elements.size() == 0){
                        queue.offer(url);
                    }
                    for(int i = 2; i <= elements.size(); i++){
                        String css = "#nav-subnav > a:nth-child(" + Integer.toString(i) +")";
                        Element element = doc.select(css).first();
                        if(element != null){
                            String href = element.attr("href");
                            if(subUrlHash.contains(href)){
                                continue;
                            }
                            subUrlHash.add(href);
                            System.out.println("https://www.amazon.com"+href);
                            bw.write("https://www.amazon.com"+href);
                            bw.newLine();

                        }

                    }
                    Thread.sleep(2000);

                }

            } catch (IllegalArgumentException e) {
                System.out.println(e.toString());
                queue.offer(url);

            }catch (InterruptedException ex){
                Thread.currentThread().interrupt();
            }
        }

        bw.close();

    }

    public void getDetailProductInfo (String subCategoryUrlPath, String productDetailLogPath)throws IOException {
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        headers.put("Accept-Encoding", "gzip, deflate, sdch, br");
        headers.put("Accept-Language", "en-US,en;q=0.8");
        String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.95 Safari/537.36";

        BufferedReader br = new BufferedReader(new FileReader(subCategoryUrlPath));


        BufferedWriter bwDetail = new BufferedWriter(new FileWriter(new File(productDetailLogPath).getAbsoluteFile()));
        BufferedWriter bwError = new BufferedWriter(new FileWriter(new File(productDetailLogPath).getAbsoluteFile()));
        String urlLine;
        while ((urlLine = br.readLine())!=null){
            try {
                Document doc = Jsoup.connect(urlLine).headers(headers).userAgent(USER_AGENT).timeout(1000000).get();
                changeProxy();
                int resultSize = getResultSzie(doc);
                System.out.println("product list page need to be crawled " +urlLine);
                System.out.println(resultSize);
                if(resultSize == 0){
                    bwError.write(urlLine);
                    continue;
                }
                for(int i = 0; i < resultSize; i++){
                    String detailUrl = getDetailUrlFromDoc(doc, i);
                    if(detailUrl.isEmpty()){
                        continue;
                    }


                    String title = getTitleFromDoc(doc,i);
                    if(title == ""){
                        System.out.println("Empty title");
                    }
                    System.out.println("title --> " + title);
                    bwDetail.write("title --> " + title);
                    bwDetail.newLine();
                }

            }catch (IllegalArgumentException e){

                System.out.println(e.toString());
                System.out.println(urlLine);
                bwError.write(urlLine);
                continue;
            }

        }


    }

    private int getResultSzie(Document doc){
        for(String resultSize: resultSizeSelector){
            Elements elements = doc.select(resultSize);
            if(elements.size() != 0){
                return elements.size();
            }
        }
        return  0;
    }

    private String getTitleFromDoc(Document doc, int itemNum){
        for(String titleSelector: titleListSelector){
            String titleEleSelector = "#result_"+Integer.toString(itemNum) + titleSelector;
            Element titleEle = doc.select(titleEleSelector).first();
            if(titleEle !=  null){
                return titleEle.text();
            }
        }

        return "";
    }

    private String getDetailUrlFromDoc(Document doc, int itemNum){
        String detailUrlSelector = "#result_"+Integer.toString(itemNum)+" > div > div:nth-child(3) > div:nth-child(1) > a";
        Element detailUrlEle = doc.select(detailUrlSelector).first();
        if(detailUrlEle != null){
            String detailUrl = detailUrlEle.attr("href");
            String normalizedUrl = normalizeUrl(detailUrl);
            if(crawledUrl.contains(normalizedUrl)){
                return "";
            }
            crawledUrl.add(normalizedUrl);
            return normalizedUrl;
        }else {
            return "";
        }

    }

    private String normalizeUrl(String url) {
        int i = url.indexOf("ref");
        return i == -1 ? url : url.substring(0, i - 1);

    }

    private  void initProxyList(String proxyFile) {
        proxyList = new ArrayList<>();
        try(BufferedReader br = new BufferedReader(new FileReader(proxyFile))) {
            String line;
            while((line = br.readLine()) != null) {
                String[] fields = line.split(",");
                String ip = fields[0].trim();
                proxyList.add(ip);
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
        Authenticator.setDefault(
                new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(proxyUser, proxyPassword.toCharArray());
                    }
                }
        );

        System.setProperty("http.proxyUser", proxyUser);
        System.setProperty("http.proxyPassword", proxyPassword);
        System.setProperty("socksProxyPort", "61336");
    }
    public  void changeProxy() {
        indexForProxyList = (indexForProxyList + 1) % proxyList.size();
        String proxy = proxyList.get(indexForProxyList);
        System.setProperty("socksProxyHost", proxy);
    }
}
