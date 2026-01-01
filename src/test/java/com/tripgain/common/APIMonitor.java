package com.tripgain.common;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class APIMonitor {
    private WebDriver driver;
    private ExtentTest test;
    private JavascriptExecutor js;
    
    public APIMonitor(WebDriver driver, ExtentTest test) {
        this.driver = driver;
        this.test = test;
        this.js = (JavascriptExecutor) driver;
    }
    
    /**
     * COMPREHENSIVE API MONITORING - This WILL capture API calls
     */
    public void captureAllAPIs(String stepName) {
        try {
            logInfo("🌐 " + stepName + " - COMPREHENSIVE API MONITORING");
            
            // METHOD 1: Inject BEFORE any actions
            injectAPIMonitorEarly();
            
            // Wait for any ongoing API calls
            Thread.sleep(3000);
            
            // METHOD 2: Capture from performance logs
            capturePerformanceEntries();
            
            // METHOD 3: Capture from browser console
            captureConsoleAPIs(stepName);
            
            // METHOD 4: Use JavaScript to intercept
            captureJavaScriptAPIs();
            
            // METHOD 5: Network request interception
            captureNetworkRequests();
            
        } catch (Exception e) {
            logWarning("API Monitoring error: " + e.getMessage());
        }
    }
    
    /**
     * Inject API monitor VERY EARLY (call this immediately after page load)
     */
    public void injectAPIMonitorEarly() {
        try {
            String monitorScript = 
                "// ========== EARLY API MONITOR ==========\n" +
                "(function() {\n" +
                "    // Create storage\n" +
                "    window.__apiMonitor = {\n" +
                "        requests: [],\n" +
                "        responses: [],\n" +
                "        errors: []\n" +
                "    };\n" +
                "    \n" +
                "    // Store original fetch\n" +
                "    const originalFetch = window.fetch;\n" +
                "    \n" +
                "    // Override fetch\n" +
                "    window.fetch = function() {\n" +
                "        const args = arguments;\n" +
                "        const url = args[0];\n" +
                "        const method = args[1]?.method || 'GET';\n" +
                "        const timestamp = new Date().toISOString();\n" +
                "        \n" +
                "        // Store request\n" +
                "        const requestId = 'req_' + Date.now() + '_' + Math.random();\n" +
                "        const request = {\n" +
                "            id: requestId,\n" +
                "            type: 'fetch',\n" +
                "            url: url,\n" +
                "            method: method,\n" +
                "            timestamp: timestamp,\n" +
                "            requestHeaders: args[1]?.headers,\n" +
                "            requestBody: args[1]?.body\n" +
                "        };\n" +
                "        \n" +
                "        // FORCE LOG to console (for Selenium)\n" +
                "        console.log('[FORCED-API-LOG] REQUEST: ' + method + ' ' + url);\n" +
                "        \n" +
                "        window.__apiMonitor.requests.push(request);\n" +
                "        \n" +
                "        // Execute and monitor\n" +
                "        return originalFetch.apply(this, args)\n" +
                "            .then(response => {\n" +
                "                const responseTimestamp = new Date().toISOString();\n" +
                "                \n" +
                "                // Clone to read response\n" +
                "                const clone = response.clone();\n" +
                "                return clone.text().then(text => {\n" +
                "                    // FORCE LOG response\n" +
                "                    console.log('[FORCED-API-LOG] RESPONSE: ' + response.status + ' ' + url);\n" +
                "                    \n" +
                "                    // Store response\n" +
                "                    request.responseTime = responseTimestamp;\n" +
                "                    request.status = response.status;\n" +
                "                    request.statusText = response.statusText;\n" +
                "                    request.responseBody = text;\n" +
                "                    request.completed = true;\n" +
                "                    \n" +
                "                    window.__apiMonitor.responses.push(request);\n" +
                "                    \n" +
                "                    // Log errors\n" +
                "                    if (response.status >= 400) {\n" +
                "                        console.error('[FORCED-API-ERROR] ' + response.status + ' ' + url);\n" +
                "                        console.error('[FORCED-API-DATA] ' + text.substring(0, 500));\n" +
                "                        window.__apiMonitor.errors.push(request);\n" +
                "                    }\n" +
                "                    \n" +
                "                    return response;\n" +
                "                });\n" +
                "            })\n" +
                "            .catch(error => {\n" +
                "                console.error('[FORCED-API-FAIL] ' + url + ' - ' + error.message);\n" +
                "                request.error = error.message;\n" +
                "                window.__apiMonitor.errors.push(request);\n" +
                "                throw error;\n" +
                "            });\n" +
                "    };\n" +
                "    \n" +
                "    // Monitor XMLHttpRequest\n" +
                "    const originalXHR = window.XMLHttpRequest;\n" +
                "    \n" +
                "    window.XMLHttpRequest = function() {\n" +
                "        const xhr = new originalXHR();\n" +
                "        const request = {};\n" +
                "        \n" +
                "        const originalOpen = xhr.open;\n" +
                "        xhr.open = function(method, url) {\n" +
                "            request.method = method;\n" +
                "            request.url = url;\n" +
                "            request.timestamp = new Date().toISOString();\n" +
                "            \n" +
                "            // FORCE LOG\n" +
                "            console.log('[FORCED-XHR-LOG] REQUEST: ' + method + ' ' + url);\n" +
                "            \n" +
                "            window.__apiMonitor.requests.push(request);\n" +
                "            \n" +
                "            this.addEventListener('load', function() {\n" +
                "                console.log('[FORCED-XHR-LOG] RESPONSE: ' + this.status + ' ' + url);\n" +
                "                \n" +
                "                request.status = this.status;\n" +
                "                request.statusText = this.statusText;\n" +
                "                request.responseBody = this.responseText;\n" +
                "                request.completed = true;\n" +
                "                \n" +
                "                window.__apiMonitor.responses.push(request);\n" +
                "                \n" +
                "                if (this.status >= 400) {\n" +
                "                    console.error('[FORCED-XHR-ERROR] ' + this.status + ' ' + url);\n" +
                "                    window.__apiMonitor.errors.push(request);\n" +
                "                }\n" +
                "            });\n" +
                "            \n" +
                "            this.addEventListener('error', function() {\n" +
                "                console.error('[FORCED-XHR-FAIL] ' + url);\n" +
                "                request.error = 'Network error';\n" +
                "                window.__apiMonitor.errors.push(request);\n" +
                "            });\n" +
                "            \n" +
                "            return originalOpen.apply(this, arguments);\n" +
                "        };\n" +
                "        \n" +
                "        return xhr;\n" +
                "    };\n" +
                "    \n" +
    "    console.log('[API-MONITOR] Early injection COMPLETE');\n" +
    "})();";
            
            js.executeScript(monitorScript);
            logInfo("✅ Early API monitor injected");
            
        } catch (Exception e) {
            logWarning("Could not inject early monitor: " + e.getMessage());
        }
    }
    
    /**
     * Capture APIs from browser console (MOST RELIABLE)
     */
    private void captureConsoleAPIs(String stepName) {
        try {
            logInfo("🔍 Checking browser console for API calls...");
            
            LogEntries logs = driver.manage().logs().get(LogType.BROWSER);
            List<APICall> apiCalls = new ArrayList<>();
            
            // Patterns to look for
            List<Pattern> patterns = Arrays.asList(
                Pattern.compile("(POST|GET|PUT|DELETE)\\s+(https?://[^\\s]+)"),
                Pattern.compile("statuscode:\\s*(\\d{3})", Pattern.CASE_INSENSITIVE),
                Pattern.compile("dispatch\\.jsp[^\\s]*"),
                Pattern.compile("BUSBOKDINGAPPROVAL", Pattern.CASE_INSENSITIVE),
                Pattern.compile("\"status\":\\s*\"(OK|NOK)\""),
                Pattern.compile("status:\\s*\"(OK|NOK)\"")
            );
            
            for (LogEntry entry : logs) {
                String message = entry.getMessage();
                
                // Check if it's an API-related log
                boolean isAPI = false;
                for (Pattern pattern : patterns) {
                    if (pattern.matcher(message).find()) {
                        isAPI = true;
                        break;
                    }
                }
                
                if (isAPI) {
                    APICall apiCall = extractAPICallFromLog(message);
                    if (apiCall != null) {
                        apiCalls.add(apiCall);
                    } else {
                        // Log raw message for debugging
                        logInfo("Raw API log: " + truncate(message, 200));
                    }
                }
            }
            
            // Report findings
            if (!apiCalls.isEmpty()) {
                logInfo("📊 Found " + apiCalls.size() + " API calls in console");
                
                int success = 0, failed = 0;
                for (APICall api : apiCalls) {
                    if (api.isSuccess()) {
                        success++;
                        logPass(api.toString());
                    } else {
                        failed++;
                        logFail(api.toString());
                        
                        // Show error details
                        if (api.errorMessage != null) {
                            logInfo("   Error: " + api.errorMessage);
                        }
                        if (api.responseBody != null && api.responseBody.length() < 500) {
                            logInfo("   Response: " + api.responseBody);
                        }
                    }
                }
                
                logInfo("📈 SUMMARY: " + success + " successful, " + failed + " failed");
                
                // Check for specific approval API
                for (APICall api : apiCalls) {
                    if (api.url != null && api.url.contains("BUSBOKDINGAPPROVAL")) {
                        logInfo("🎯 APPROVAL API FOUND: " + api.status);
                        if (api.status == 300) {
                            logFail("❌ CRITICAL: Approval API returned 300!");
                            logInfo("   This usually means: 'No approval workflow configured'");
                        }
                    }
                }
                
            } else {
                logWarning("⚠️ No API calls found in console for: " + stepName);
                logInfo("Possible reasons:");
                logInfo("1. API calls not logged to console");
                logInfo("2. Try checking manually: Press F12 -> Network tab");
                logInfo("3. The application might not be making expected API calls");
            }
            
        } catch (Exception e) {
            logWarning("Error capturing console APIs: " + e.getMessage());
        }
    }
    
    /**
     * Extract API call from log message
     */
    private APICall extractAPICallFromLog(String logMessage) {
        try {
            APICall api = new APICall();
            
            // Extract URL
            Pattern urlPattern = Pattern.compile("https?://[^\\s\"']+");
            Matcher urlMatcher = urlPattern.matcher(logMessage);
            if (urlMatcher.find()) {
                api.url = urlMatcher.group();
                api.name = getAPIName(api.url);
            }
            
            // Extract method
            if (logMessage.contains("POST")) api.method = "POST";
            else if (logMessage.contains("GET")) api.method = "GET";
            else api.method = "UNKNOWN";
            
            // Extract status code
            Pattern statusPattern = Pattern.compile("status(?:code)?[=:]\\s*(\\d{3})", Pattern.CASE_INSENSITIVE);
            Matcher statusMatcher = statusPattern.matcher(logMessage);
            if (statusMatcher.find()) {
                try {
                    api.status = Integer.parseInt(statusMatcher.group(1));
                } catch (NumberFormatException e) {
                    api.status = 0;
                }
            }
            
            // Extract error message
            Pattern errorPattern = Pattern.compile("message[=:]\\s*\"([^\"]+)\"", Pattern.CASE_INSENSITIVE);
            Matcher errorMatcher = errorPattern.matcher(logMessage);
            if (errorMatcher.find()) {
                api.errorMessage = errorMatcher.group(1);
            }
            
            // Check for OK/NOK status
            if (logMessage.contains("status: \"NOK\"") || logMessage.contains("\"status\":\"NOK\"")) {
                api.isSuccess = false;
            } else if (logMessage.contains("status: \"OK\"") || logMessage.contains("\"status\":\"OK\"")) {
                api.isSuccess = true;
            }
            
            return api;
            
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Get API name from URL
     */
    private String getAPIName(String url) {
        if (url == null) return "Unknown API";
        
        String urlLower = url.toLowerCase();
        
        if (urlLower.contains("dispatch.jsp")) {
            if (urlLower.contains("login")) return "Login API";
            if (urlLower.contains("expr") || urlLower.contains("search")) return "Search API";
            if (urlLower.contains("busbokdingapproval")) return "Approval API";
            if (urlLower.contains("getcorporatestinfo")) return "Corporate API";
            return "Service Dispatch API";
        }
        
        // Extract last meaningful part
        String[] parts = url.split("/");
        for (int i = parts.length - 1; i >= 0; i--) {
            if (!parts[i].isEmpty() && !parts[i].matches(".*\\.(jsp|html|php)$")) {
                return parts[i] + " API";
            }
        }
        
        return "API Call";
    }
    
    /**
     * Performance entries capture
     */
    private void capturePerformanceEntries() {
        try {
            String script = 
                "try {\n" +
                "    const resources = performance.getEntriesByType('resource');\n" +
                "    const apiResources = resources.filter(r => \n" +
                "        r.name.includes('dispatch.jsp') || \n" +
                "        r.name.includes('http') && (r.name.includes('/api/') || r.name.includes('jsp'))\n" +
                "    );\n" +
                "    return apiResources.map(r => ({\n" +
                "        name: r.name,\n" +
                "        duration: r.duration,\n" +
                "        type: r.initiatorType\n" +
                "    }));\n" +
                "} catch(e) {\n" +
                "    return [];\n" +
                "}";
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> resources = (List<Map<String, Object>>) js.executeScript(script);
            
            if (resources != null && !resources.isEmpty()) {
                logInfo("📊 Performance entries: " + resources.size());
                for (Map<String, Object> resource : resources) {
                    String name = (String) resource.get("name");
                    if (name != null && name.contains("dispatch.jsp")) {
                        logInfo("   Found: " + getAPIName(name));
                    }
                }
            }
            
        } catch (Exception e) {
            // Ignore
        }
    }
    
    /**
     * Capture from JavaScript storage
     */
    private void captureJavaScriptAPIs() {
        try {
            String script = 
                "if (!window.__apiMonitor) return { requests: 0, responses: 0, errors: 0 };\n" +
                "return {\n" +
                "    requests: window.__apiMonitor.requests.length,\n" +
                "    responses: window.__apiMonitor.responses.length,\n" +
                "    errors: window.__apiMonitor.errors.length\n" +
                "};";
            
            @SuppressWarnings("unchecked")
            Map<String, Object> stats = (Map<String, Object>) js.executeScript(script);
            
            if (stats != null) {
                logInfo("📊 JavaScript monitor stats:");
                logInfo("   Requests: " + stats.get("requests"));
                logInfo("   Responses: " + stats.get("responses"));
                logInfo("   Errors: " + stats.get("errors"));
            }
            
        } catch (Exception e) {
            // Ignore
        }
    }
    
    /**
     * Capture network requests
     */
    private void captureNetworkRequests() {
        try {
            // Enable logging if not already
            String enableLogging = 
                "console.log('[NETWORK] Enabling network logging');\n" +
                "return true;";
            
            js.executeScript(enableLogging);
            
        } catch (Exception e) {
            // Ignore
        }
    }
    
    /**
     * MANUAL CHECK: Guide tester to check manually
     */
    public void manualAPICheck(String stepName) {
        logInfo("🔧 MANUAL CHECK REQUIRED for: " + stepName);
        logInfo("Please check manually:");
        logInfo("1. Press F12 to open DevTools");
        logInfo("2. Go to Network tab");
        logInfo("3. Look for calls to 'dispatch.jsp'");
        logInfo("4. Check response status codes (look for 300, 400, 500)");
        logInfo("5. Look for 'BUSBOKDINGAPPROVAL' in requests");
    }
    
    /**
     * Check specifically for Approval API failure
     */
    public boolean checkApprovalAPI() {
        try {
            logInfo("🎯 Checking specifically for Approval API...");
            
            LogEntries logs = driver.manage().logs().get(LogType.BROWSER);
            boolean foundApprovalAPI = false;
            
            for (LogEntry entry : logs) {
                String message = entry.getMessage();
                
                if (message.contains("BUSBOKDINGAPPROVAL")) {
                    foundApprovalAPI = true;
                    logInfo("✅ Found Approval API call");
                    
                    // Check status
                    if (message.contains("300") || message.contains("statuscode: 300")) {
                        logFail("❌ Approval API returned STATUS 300!");
                        logInfo("   This usually means: No approval workflow configured");
                        return false;
                    }
                    
                    if (message.contains("NOK") || message.contains("\"status\":\"NOK\"")) {
                        logFail("❌ Approval API returned NOK status");
                        return false;
                    }
                    
                    if (message.contains("OK") || message.contains("\"status\":\"OK\"")) {
                        logPass("✅ Approval API successful");
                        return true;
                    }
                }
            }
            
            if (!foundApprovalAPI) {
                logWarning("⚠️ No Approval API call found in logs");
            }
            
            return false;
            
        } catch (Exception e) {
            logWarning("Error checking Approval API: " + e.getMessage());
            return false;
        }
    }
    
    // ========== HELPER CLASSES ==========
    
    private class APICall {
        String name;
        String url;
        String method;
        int status;
        String errorMessage;
        String responseBody;
        boolean isSuccess;
        
        boolean isSuccess() {
            return (status >= 200 && status < 300) || isSuccess;
        }
        
        @Override
        public String toString() {
            return method + " " + name + " - Status: " + (status > 0 ? status : "Unknown");
        }
    }
    
    // ========== LOGGING HELPERS ==========
    
    private void logInfo(String message) {
        test.log(Status.INFO, message);
    }
    
    private void logPass(String message) {
        test.log(Status.PASS, message);
    }
    
    private void logFail(String message) {
        test.log(Status.FAIL, message);
    }
    
    private void logWarning(String message) {
        test.log(Status.WARNING, message);
    }
    
    private String truncate(String text, int length) {
        if (text == null || text.length() <= length) return text;
        return text.substring(0, length) + "...";
    }
}