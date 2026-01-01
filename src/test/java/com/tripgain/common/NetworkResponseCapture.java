package com.tripgain.common;

import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.client.ClientUtil;
import net.lightbody.bmp.core.har.Har;
import net.lightbody.bmp.core.har.HarEntry;
import net.lightbody.bmp.core.har.HarRequest;
import net.lightbody.bmp.core.har.HarResponse;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import java.util.concurrent.TimeUnit;

public class NetworkResponseCapture {
    
    private static BrowserMobProxy proxy;
    private static WebDriver driver;
    
    public static void main(String[] args) throws Exception {
        setupProxyAndDriver();
        captureNetworkResponses();
        tearDown();
    }
    
    public static void setupProxyAndDriver() {
        try {
            // 1. Start BrowserMob Proxy server
            proxy = new BrowserMobProxyServer();
            proxy.start(8080);
            
            // 2. Create Selenium proxy configuration
            Proxy seleniumProxy = ClientUtil.createSeleniumProxy(proxy);
            
            // 3. Configure Chrome options
            ChromeOptions options = new ChromeOptions();
            options.setProxy(seleniumProxy);
            options.addArguments("--ignore-certificate-errors");
            options.addArguments("--start-maximized");
            
            // 4. Initialize ChromeDriver
            System.setProperty("webdriver.chrome.driver", "path/to/chromedriver");
            driver = new ChromeDriver(options);
            
            // 5. Create a new HAR to capture network traffic
            proxy.newHar("TestCapture");
            
            System.out.println("Proxy and Driver setup complete!");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void captureNetworkResponses() {
        try {
            // Navigate to your website
            driver.get("https://jsonplaceholder.typicode.com/");
            
            // Wait for page to load and API calls to happen
            Thread.sleep(5000);
            
            // Get the HAR data
            Har har = proxy.getHar();
            
            System.out.println("\n=== CAPTURED API RESPONSES ===\n");
            
            // Iterate through all entries
            for (HarEntry entry : har.getLog().getEntries()) {
                HarRequest request = entry.getRequest();
                HarResponse response = entry.getResponse();
                
                String url = request.getUrl();
                int statusCode = response.getStatus();
                String responseBody = response.getContent() != null ? 
                                     response.getContent().getText() : "No Content";
                
                // Filter for specific APIs (adjust as needed)
                if (url.contains("api") || url.contains("json")) {
                    System.out.println("URL: " + url);
                    System.out.println("Status: " + statusCode);
                    System.out.println("Response Body: " + responseBody);
                    System.out.println("---------------------------");
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void captureSpecificAPI(String apiUrlPattern) {
        try {
            // Create a new HAR with a specific name
            proxy.newHar("APICapture");
            
            // Navigate or trigger API calls
            driver.get("https://your-website.com");
            
            // Wait for API calls
            Thread.sleep(5000);
            
            // Get HAR data
            Har har = proxy.getHar();
            
            // Filter and display specific API responses
            for (HarEntry entry : har.getLog().getEntries()) {
                String url = entry.getRequest().getUrl();
                
                if (url.contains(apiUrlPattern)) {
                    System.out.println("\nFound Matching API:");
                    System.out.println("URL: " + url);
                    System.out.println("Method: " + entry.getRequest().getMethod());
                    System.out.println("Status: " + entry.getResponse().getStatus());
                    System.out.println("Response: " + entry.getResponse().getContent().getText());
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
        if (proxy != null) {
            proxy.stop();
        }
        System.out.println("Cleanup complete!");
    }
}
