// Add these imports at the top of your file
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.v138.network.Network;
import org.openqa.selenium.devtools.v138.network.model.Request;
import org.openqa.selenium.devtools.v138.network.model.Response;
import org.openqa.selenium.devtools.v138.network.model.RequestWillBeSent;
import org.openqa.selenium.devtools.v138.network.model.ResponseReceived;

import com.aventstack.extentreports.ExtentTest;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class RealNetworkRequest {
    // Add this field to your existing fields
    private DevTools devTools;
    private List<RealNetworkRequest> networkRequests = new ArrayList<>();
    private boolean networkMonitoringEnabled = false;
    
    // Inner class for REAL network requests (like in Network tab)
    private class RealNetworkRequest1 {
        String requestId;
        String url;
        String method;
        String status;
        String statusText;
        String responseBody;
        String requestHeaders;
        String responseHeaders;
        long timestamp;
        long duration;
        boolean hasError;
        
        boolean isSuccessful() {
            try {
                if (status == null) return false;
                int statusCode = Integer.parseInt(status);
                return statusCode >= 200 && statusCode < 300;
            } catch (Exception e) {
                return false;
            }
        }
        
        String getApiName() {
            if (url == null) return "Unknown API";
            
            String urlLower = url.toLowerCase();
            
            // Match the APIs from your Network tab screenshot
            if (urlLower.contains("dispatch.jsp")) {
                if (urlLower.contains("expr") || urlLower.contains("search")) return "Search API";
                if (urlLower.contains("busbokdingapproval")) return "Bus Booking Approval API";
                if (urlLower.contains("getcorporatestinfo")) return "Corporate Info API";
                if (urlLower.contains("getcorporatetravellers")) return "Corporate Travellers API";
                if (urlLower.contains("authtoken")) return "Auth Service API";
                return "Service Dispatch API";
            }
            
            if (urlLower.contains("get-corporate-booking-config")) return "Corporate Config API";
            if (urlLower.contains("get-all-corporate-departments")) return "Departments API";
            if (urlLower.contains("get-all-projects")) return "Projects API";
            if (urlLower.contains("get-all-costcenters")) return "Cost Centers API";
            if (urlLower.contains("generic-content-fields-by-channel")) return "Content API";
            
            // Login/Approval APIs
            if (urlLower.contains("login")) return "Login API";
            if (urlLower.contains("approval")) return "Approval API";
            if (urlLower.contains("auth")) return "Auth API";
            
            return "Network Request";
        }
    }
    
    // Add to your constructor
    public RealNetworkRequest(WebDriver driver, ExtentTest test,Log log) {
        this.driver = driver;
        this.test = test;
        this.js = (JavascriptExecutor) driver;
        
        // Initialize DevTools if using Chrome
        if (driver instanceof ChromeDriver) {
            try {
                this.devTools = ((ChromeDriver) driver).getDevTools();
                this.devTools.createSession();
                log.ReportEvent("info", "✅ Chrome DevTools initialized for Network monitoring");
            } catch (Exception e) {
                log.ReportEvent("warning", "⚠️ Could not initialize DevTools: " + e.getMessage());
            }
        }
    }
    
    // ========== REAL NETWORK TAB MONITORING METHODS ==========
    
    /**
     * ENABLE REAL NETWORK MONITORING - Captures ALL network requests like Network tab
     * This is what you see in F12 -> Network tab
     */
    public void enableRealNetworkMonitoring(Log log) {
        if (!(driver instanceof ChromeDriver)) {
            log.ReportEvent("warning", "⚠️ Real Network monitoring only works with Chrome browser");
            return;
        }
        
        try {
            // Enable Network domain
            devTools.send(Network.enable(
                Optional.empty(), 
                Optional.empty(), 
                Optional.empty()
            ));
            
            // Clear previous requests
            networkRequests.clear();
            
            // Listen to ALL requests (like Network tab)
            devTools.addListener(Network.requestWillBeSent(), request -> {
                RealNetworkRequest netReq = new RealNetworkRequest();
                netReq.requestId = request.getRequestId().toString();
                netReq.url = request.getRequest().getUrl();
                netReq.method = request.getRequest().getMethod();
                netReq.timestamp = System.currentTimeMillis();
                netReq.requestHeaders = request.getRequest().getHeaders().toString();
                
                synchronized (networkRequests) {
                    networkRequests.add(netReq);
                }
                
                // Log to console for visibility
                System.out.println("[REAL-NETWORK] REQUEST: " + netReq.method + " " + netReq.url);
            });
            
            // Listen to ALL responses
            devTools.addListener(Network.responseReceived(), response -> {
                String requestId = response.getRequestId().toString();
                RealNetworkRequest netReq = findNetworkRequest(requestId);
                
                if (netReq != null) {
                    netReq.status = String.valueOf(response.getResponse().getStatus());
                    netReq.statusText = response.getResponse().getStatusText();
                    netReq.responseHeaders = response.getResponse().getHeaders().toString();
                    netReq.duration = System.currentTimeMillis() - netReq.timestamp;
                    netReq.hasError = !netReq.isSuccessful();
                    
                    // Get response body if needed
                    if (netReq.hasError || netReq.url.contains("dispatch.jsp") || netReq.url.contains("approval")) {
                        getResponseBody(requestId, netReq);
                    }
                    
                    System.out.println("[REAL-NETWORK] RESPONSE: " + netReq.status + " " + netReq.url);
                    
                    // FAIL TEST if API returns error (status >= 300)
                    if (netReq.hasError) {
                        failTestForFailedApi(netReq);
                    }
                }
            });
            
            // Listen to loading failed events (404, CORS, etc.)
            devTools.addListener(Network.loadingFailed(), loadingFailed -> {
                String requestId = loadingFailed.getRequestId().toString();
                RealNetworkRequest netReq = findNetworkRequest(requestId);
                
                if (netReq != null) {
                    netReq.status = "ERROR";
                    netReq.statusText = loadingFailed.getErrorText();
                    netReq.hasError = true;
                    
                    System.out.println("[REAL-NETWORK] FAILED: " + netReq.statusText + " " + netReq.url);
                    
                    // FAIL TEST for failed network requests
                    failTestForFailedApi(netReq);
                }
            });
            
            networkMonitoringEnabled = true;
            log.ReportEvent("info", "✅ REAL Network monitoring enabled (like Network tab)");
            
        } catch (Exception e) {
            log.ReportEvent("warning", "⚠️ Error enabling network monitoring: " + e.getMessage());
        }
    }
    
    /**
     * Get response body for specific request
     */
    private void getResponseBody(String requestId, RealNetworkRequest netReq) {
        try {
            CompletableFuture<String> responseBodyFuture = devTools.send(Network.getResponseBody(
                org.openqa.selenium.devtools.v138.network.model.RequestId.fromString(requestId)
            ));
            
            String responseBody = responseBodyFuture.get().getBody();
            if (responseBody != null && !responseBody.isEmpty()) {
                netReq.responseBody = responseBody;
            }
        } catch (Exception e) {
            // Response body might not be available
        }
    }
    
    /**
     * Find network request by ID
     */
    private RealNetworkRequest findNetworkRequest(String requestId) {
        synchronized (networkRequests) {
            return networkRequests.stream()
                .filter(req -> req.requestId.equals(requestId))
                .findFirst()
                .orElse(null);
        }
    }
    
    /**
     * FAIL THE TEST if API fails
     */
    private void failTestForFailedApi(RealNetworkRequest failedApi,Log log) {
        log.ReportEvent("fail", "❌ NETWORK API FAILED: " + failedApi.getApiName());
        ReportEvent("fail", "   URL: " + truncateUrl(failedApi.url, 100));
        ReportEvent("fail", "   Status: " + failedApi.status + " " + failedApi.statusText);
        ReportEvent("fail", "   Method: " + failedApi.method);
        
        if (failedApi.responseBody != null && !failedApi.responseBody.isEmpty()) {
            String errorMsg = extractErrorMessageFromBody(failedApi.responseBody);
            if (errorMsg != null && !errorMsg.isEmpty()) {
                ReportEvent("fail", "   Error: " + errorMsg);
            }
            
            // Show snippet for debugging
            String snippet = failedApi.responseBody.length() > 200 ? 
                failedApi.responseBody.substring(0, 200) + "..." : failedApi.responseBody;
            ReportEvent("info", "   Response: " + snippet);
        }
        
        // CRITICAL - Fail the test for backend API errors
        ReportEvent("fail", "🚨 TEST FAILING DUE TO BACKEND API ERROR");
        
        // You can throw an exception to fail the test immediately
        // throw new AssertionError("Backend API failed: " + failedApi.url + " - Status: " + failedApi.status);
    }
    
    /**
     * CHECK AND REPORT ALL NETWORK REQUESTS
     * Similar to your screenshot's Network tab
     */
    public void checkAllNetworkRequests(String stepName) {
        if (!networkMonitoringEnabled) {
            enableRealNetworkMonitoring();
            try { Thread.sleep(3000); } catch (Exception e) {} // Wait for some traffic
        }
        
        synchronized (networkRequests) {
            if (networkRequests.isEmpty()) {
                ReportEvent("warning", "⚠️ " + stepName + " - No network requests captured");
                return;
            }
            
            ReportEvent("info", "📊 " + stepName + " - NETWORK TAB ANALYSIS");
            ReportEvent("info", "Total requests: " + networkRequests.size());
            
            int successCount = 0;
            int errorCount = 0;
            List<RealNetworkRequest> failedApis = new ArrayList<>();
            
            for (RealNetworkRequest req : networkRequests) {
                if (req.isSuccessful()) {
                    successCount++;
                    ReportEvent("pass", "✅ " + req.getApiName() + " (" + req.method + ")");
                    ReportEvent("info", "   Status: " + req.status + " | Time: " + req.duration + "ms");
                } else {
                    errorCount++;
                    failedApis.add(req);
                    ReportEvent("fail", "❌ " + req.getApiName() + " (" + req.method + ")");
                    ReportEvent("info", "   Status: " + req.status + " | Time: " + req.duration + "ms");
                    ReportEvent("info", "   URL: " + truncateUrl(req.url, 80));
                }
            }
            
            // Summary
            ReportEvent("info", "=".repeat(60));
            ReportEvent("info", "📈 NETWORK SUMMARY:");
            ReportEvent("pass", "   Successful: " + successCount);
            
            if (errorCount > 0) {
                ReportEvent("fail", "   Failed: " + errorCount + " (BACKEND ISSUES)");
                
                // Show failed APIs in detail
                ReportEvent("fail", "🔴 FAILED APIs:");
                for (RealNetworkRequest failedApi : failedApis) {
                    ReportEvent("fail", "   • " + failedApi.getApiName() + " - Status: " + failedApi.status);
                    ReportEvent("info", "     URL: " + truncateUrl(failedApi.url, 100));
                    
                    if (failedApi.responseBody != null) {
                        String error = extractErrorMessageFromBody(failedApi.responseBody);
                        if (error != null) {
                            ReportEvent("info", "     Error: " + error);
                        }
                    }
                }
                
                // FAIL the test
                ReportEvent("fail", "🚨 TEST FAILING: " + errorCount + " backend API(s) failed");
            } else {
                ReportEvent("pass", "✅ All network requests successful");
            }
        }
    }
    
    /**
     * CHECK SPECIFIC API FROM NETWORK TAB
     * Like checking dispatch.jsp or approval APIs
     */
    public void checkNetworkApi(String stepName, String apiKeyword) {
        if (!networkMonitoringEnabled) {
            enableRealNetworkMonitoring();
            try { Thread.sleep(2000); } catch (Exception e) {}
        }
        
        ReportEvent("info", "🔍 " + stepName + " - Checking Network API: " + apiKeyword);
        
        synchronized (networkRequests) {
            List<RealNetworkRequest> matchingApis = networkRequests.stream()
                .filter(req -> req.url.toLowerCase().contains(apiKeyword.toLowerCase()))
                .collect(java.util.stream.Collectors.toList());
            
            if (matchingApis.isEmpty()) {
                ReportEvent("warning", "⚠️ API '" + apiKeyword + "' not found in Network tab");
                return;
            }
            
            ReportEvent("info", "Found " + matchingApis.size() + " matching API calls");
            
            for (RealNetworkRequest api : matchingApis) {
                if (api.isSuccessful()) {
                    ReportEvent("pass", "✅ " + api.getApiName() + " - SUCCESS");
                    ReportEvent("info", "   Status: " + api.status + " | Method: " + api.method);
                    ReportEvent("info", "   Time: " + api.duration + "ms");
                } else {
                    ReportEvent("fail", "❌ " + api.getApiName() + " - FAILED");
                    ReportEvent("fail", "   Status: " + api.status + " " + api.statusText);
                    ReportEvent("info", "   Method: " + api.method + " | URL: " + truncateUrl(api.url, 100));
                    
                    if (api.responseBody != null) {
                        String error = extractErrorMessageFromBody(api.responseBody);
                        if (error != null) {
                            ReportEvent("fail", "   Error: " + error);
                        }
                        
                        // Special handling for status 300
                        if ("300".equals(api.status)) {
                            ReportEvent("fail", "   ⚠️ STATUS 300: Approval workflow issue detected!");
                        }
                    }
                    
                    // FAIL test for this API error
                    ReportEvent("fail", "🚨 TEST FAILING: " + api.getApiName() + " returned error");
                }
            }
        }
    }
    
    /**
     * MONITOR FOR FAILED APIS AND AUTO-FAIL TEST
     * Call this after important actions (like clicking approval)
     */
    public void monitorAndFailOnApiErrors(String stepName) {
        if (!networkMonitoringEnabled) {
            enableRealNetworkMonitoring();
        }
        
        try { Thread.sleep(3000); } catch (Exception e) {} // Wait for APIs
        
        synchronized (networkRequests) {
            List<RealNetworkRequest> failedApis = networkRequests.stream()
                .filter(req -> !req.isSuccessful())
                .collect(java.util.stream.Collectors.toList());
            
            if (!failedApis.isEmpty()) {
                ReportEvent("fail", "🔴 " + stepName + " - BACKEND API ERRORS DETECTED");
                ReportEvent("fail", "   Failed APIs: " + failedApis.size());
                
                for (RealNetworkRequest failedApi : failedApis) {
                    ReportEvent("fail", "   ❌ " + failedApi.getApiName() + " - Status: " + failedApi.status);
                    
                    // Special handling for approval API
                    if (failedApi.url.contains("approval") && "300".equals(failedApi.status)) {
                        ReportEvent("fail", "   🚨 CRITICAL: Approval workflow configuration error!");
                    }
                }
                
                // Throw exception to fail test
                throw new AssertionError(stepName + " - " + failedApis.size() + " backend API(s) failed");
            } else {
                ReportEvent("pass", "✅ " + stepName + " - All backend APIs successful");
            }
        }
    }
    
    /**
     * GET NETWORK TAB SUMMARY - Like your screenshot
     */
    public void getNetworkTabSummary(String stepName) {
        if (!networkMonitoringEnabled) {
            enableRealNetworkMonitoring();
            try { Thread.sleep(4000); } catch (Exception e) {} // Capture more traffic
        }
        
        synchronized (networkRequests) {
            ReportEvent("info", "🌐 " + stepName + " - NETWORK TAB SUMMARY");
            ReportEvent("info", "Total Requests: " + networkRequests.size());
            
            // Group by API type
            Map<String, List<RealNetworkRequest>> apiGroups = new HashMap<>();
            for (RealNetworkRequest req : networkRequests) {
                String apiType = req.getApiName();
                apiGroups.computeIfAbsent(apiType, k -> new ArrayList<>()).add(req);
            }
            
            // Display like Network tab
            for (Map.Entry<String, List<RealNetworkRequest>> entry : apiGroups.entrySet()) {
                String apiName = entry.getKey();
                List<RealNetworkRequest> apis = entry.getValue();
                
                int success = (int) apis.stream().filter(RealNetworkRequest::isSuccessful).count();
                int failed = apis.size() - success;
                
                String statusIcon = failed > 0 ? "❌" : "✅";
                ReportEvent("info", statusIcon + " " + apiName + ": " + success + "✓ / " + failed + "✗");
                
                // Show failed ones
                if (failed > 0) {
                    for (RealNetworkRequest failedApi : apis) {
                        if (!failedApi.isSuccessful()) {
                            ReportEvent("fail", "   • " + failedApi.method + " - Status: " + failedApi.status);
                            ReportEvent("info", "     " + truncateUrl(failedApi.url, 120));
                        }
                    }
                }
            }
            
            // Calculate totals like Network tab
            long totalSize = networkRequests.stream()
                .filter(req -> req.responseBody != null)
                .mapToLong(req -> req.responseBody.length())
                .sum() / 1024; // in KB
            
            ReportEvent("info", "📦 Total Transferred: ~" + totalSize + " KB");
        }
    }
    
    // ========== HELPER METHODS ==========
    
    private String extractErrorMessageFromBody(String responseBody) {
        if (responseBody == null || responseBody.isEmpty()) return null;
        
        try {
            // JSON error messages
            if (responseBody.contains("\"message\":")) {
                Pattern pattern = Pattern.compile("\"message\":\"([^\"]+)\"");
                Matcher matcher = pattern.matcher(responseBody);
                if (matcher.find()) {
                    return matcher.group(1);
                }
            }
            
            if (responseBody.contains("\"error\":")) {
                Pattern pattern = Pattern.compile("\"error\":\"([^\"]+)\"");
                Matcher matcher = pattern.matcher(responseBody);
                if (matcher.find()) {
                    return matcher.group(1);
                }
            }
            
            // Common errors
            if (responseBody.contains("There is no approval workflow")) {
                return "No approval workflow configured";
            }
            
            if (responseBody.contains("Failed to send")) {
                return "Request failed to send";
            }
            
            return null;
        } catch (Exception e) {
            return null;
        }
    }
    
    private String truncateUrl(String url, int length) {
        if (url == null) return "";
        if (url.length() > length) {
            return url.substring(0, length) + "...";
        }
        return url;
    }
    
    // ========== COMBINED MONITORING METHOD ==========
    
    /**
     * ULTIMATE MONITORING: Console + Network Tab + Auto-Fail
     */
    public void ultimateMonitoring(String stepName) {
        ReportEvent("info", "🚀 " + stepName + " - ULTIMATE MONITORING STARTED");
        
        // 1. Enable real network monitoring
        enableRealNetworkMonitoring();
        
        // 2. Enable forced console logging
        enableNetworkMonitoring();
        
        // 3. Wait for activity
        try { Thread.sleep(5000); } catch (Exception e) {}
        
        // 4. Check Console errors
        checkConsoleErrorsWithFormat(stepName + " - Console Check");
        
        // 5. Check Network tab errors (this will fail test if APIs fail)
        checkAllNetworkRequests(stepName + " - Network Check");
        
        // 6. Specific API checks
        checkNetworkApi(stepName + " - Dispatch APIs", "dispatch.jsp");
        checkNetworkApi(stepName + " - Approval API", "approval");
        
        ReportEvent("info", "✅ " + stepName + " - Ultimate monitoring completed");
    }
    
    // Add this method to clean up
    public void stopNetworkMonitoring() {
        if (devTools != null && networkMonitoringEnabled) {
            try {
                devTools.send(Network.disable());
                networkMonitoringEnabled = false;
                ReportEvent("info", "🛑 Network monitoring stopped");
            } catch (Exception e) {
                // Ignore
            }
        }
    }
    
    // Continue with your existing methods...
} // End of class