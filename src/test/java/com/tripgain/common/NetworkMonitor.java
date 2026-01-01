package com.tripgain.common;

import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.client.ClientUtil;
import net.lightbody.bmp.core.har.Har;
import net.lightbody.bmp.core.har.HarEntry;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class NetworkMonitor {
    
    private BrowserMobProxy proxy;
    private WebDriver driver;
    private List<ApiResponse> capturedResponses;
    
    public NetworkMonitor() {
        this.capturedResponses = new ArrayList<>();
    }
    
    public void startMonitoring() {
        try {
            // Start proxy
            proxy = new BrowserMobProxyServer();
            proxy.start(0); // Auto-select port
            
            // Create Selenium proxy
            Proxy seleniumProxy = ClientUtil.createSeleniumProxy(proxy);
            
            // Chrome options
            ChromeOptions options = new ChromeOptions();
            options.setProxy(seleniumProxy);
            options.addArguments("--ignore-certificate-errors");
            options.addArguments("--disable-notifications");
            
            // Optional: Add extensions or other arguments
            // options.addArguments("--headless"); // Uncomment for headless
            
            // Initialize driver
            System.setProperty("webdriver.chrome.driver", "path/to/chromedriver");
            driver = new ChromeDriver(options);
            driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
            
            // Start capturing
            proxy.newHar("network-monitor");
            
            System.out.println("Monitoring started on port: " + proxy.getPort());
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to start network monitoring", e);
        }
    }
    
    public void captureAPIs(String url) {
        try {
            // Clear previous captures
            proxy.newHar("api-capture");
            capturedResponses.clear();
            
            // Navigate to URL
            driver.get(url);
            
            // Wait for page load and API calls
            Thread.sleep(5000);
            
            // Get captured data
            Har har = proxy.getHar();
            
            // Process entries
            for (HarEntry entry : har.getLog().getEntries()) {
                String requestUrl = entry.getRequest().getUrl();
                int status = entry.getResponse().getStatus();
                String method = entry.getRequest().getMethod();
                
                // Capture response body if available
                String responseBody = "";
                if (entry.getResponse().getContent() != null && 
                    entry.getResponse().getContent().getText() != null) {
                    responseBody = entry.getResponse().getContent().getText();
                }
                
                // Store in list
                ApiResponse apiResp = new ApiResponse(requestUrl, method, status, responseBody);
                capturedResponses.add(apiResp);
                
                // Print to console
                System.out.println("\nAPI Call Captured:");
                System.out.println("URL: " + requestUrl);
                System.out.println("Method: " + method);
                System.out.println("Status: " + status);
                if (responseBody.length() > 0) {
                    System.out.println("Response (first 500 chars): " + 
                                      responseBody.substring(0, Math.min(500, responseBody.length())));
                }
                System.out.println("----------------------------------");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void captureAPIsWithFilter(String url, List<String> filters) {
        try {
            proxy.newHar("filtered-capture");
            driver.get(url);
            Thread.sleep(5000);
            
            Har har = proxy.getHar();
            
            for (HarEntry entry : har.getLog().getEntries()) {
                String requestUrl = entry.getRequest().getUrl();
                
                // Check if URL matches any filter
                boolean matchesFilter = filters.stream()
                    .anyMatch(filter -> requestUrl.contains(filter));
                
                if (matchesFilter) {
                    String responseBody = entry.getResponse().getContent() != null ? 
                                        entry.getResponse().getContent().getText() : "";
                    
                    System.out.println("\n[FILTERED] API Response:");
                    System.out.println("URL: " + requestUrl);
                    System.out.println("Status: " + entry.getResponse().getStatus());
                    System.out.println("Response: " + responseBody);
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public List<ApiResponse> getResponsesByPattern(String pattern) {
        List<ApiResponse> filtered = new ArrayList<>();
        for (ApiResponse resp : capturedResponses) {
            if (resp.getUrl().contains(pattern)) {
                filtered.add(resp);
            }
        }
        return filtered;
    }
    
    public void printAllCapturedResponses() {
        System.out.println("\n=== ALL CAPTURED RESPONSES ===");
        for (ApiResponse resp : capturedResponses) {
            System.out.println(resp);
            System.out.println("---------------------------");
        }
    }
    
    public void stopMonitoring() {
        if (driver != null) {
            driver.quit();
        }
        if (proxy != null) {
            proxy.stop();
        }
        System.out.println("Monitoring stopped.");
    }
    
    // Model class for API responses
    public static class ApiResponse {
        private String url;
        private String method;
        private int statusCode;
        private String responseBody;
        private Date timestamp;
        
        public ApiResponse(String url, String method, int statusCode, String responseBody) {
            this.url = url;
            this.method = method;
            this.statusCode = statusCode;
            this.responseBody = responseBody;
            this.timestamp = new Date();
        }
        
        // Getters
        public String getUrl() { return url; }
        public String getMethod() { return method; }
        public int getStatusCode() { return statusCode; }
        public String getResponseBody() { return responseBody; }
        public Date getTimestamp() { return timestamp; }
        
        @Override
        public String toString() {
            return String.format(
                "API Response:\nURL: %s\nMethod: %s\nStatus: %d\nTimestamp: %s\nResponse: %s",
                url, method, statusCode, timestamp,
                responseBody.length() > 200 ? 
                    responseBody.substring(0, 200) + "..." : responseBody
            );
        }
    }
    
    // Main method for testing
    public static void main(String[] args) {
        NetworkMonitor monitor = new NetworkMonitor();
        
        try {
            // 1. Start monitoring
            monitor.startMonitoring();
            
            // 2. Navigate to a site and capture all APIs
            monitor.captureAPIs("https://jsonplaceholder.typicode.com/");
            
            // 3. Capture specific APIs with filter
            List<String> filters = Arrays.asList("posts", "users");
            monitor.captureAPIsWithFilter("https://jsonplaceholder.typicode.com/", filters);
            
            // 4. Get specific responses
            List<ApiResponse> postsResponses = monitor.getResponsesByPattern("posts");
            System.out.println("\nFound " + postsResponses.size() + " posts API responses");
            
            // 5. Print all captured responses
            monitor.printAllCapturedResponses();
            
        } finally {
            // 6. Cleanup
            monitor.stopMonitoring();
        }
    }
}
