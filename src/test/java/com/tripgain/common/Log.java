package com.tripgain.common;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.markuputils.Markup;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class Log {
    WebDriver driver;
    ExtentTest test;
    JavascriptExecutor js;
    List<NetworkCall> capturedCalls = new ArrayList<>();
    
    // Inner class for NetworkCall
    private class NetworkCall {
        String url;
        String method;
        String status;
        String statusText;
        String response;
        String time;
        String duration;
        
        boolean isSuccessful() {
            try {
                if (status == null) return false;
                if (status.equals("ERROR")) return false;
                int statusCode = Integer.parseInt(status);
                return statusCode >= 200 && statusCode < 300;
            } catch (Exception e) {
                return false;
            }
        }
        
        String getApiName() {
            if (url == null) return "Unknown API";
            
            String urlLower = url.toLowerCase();
            
            if (urlLower.contains("dispatch.jsp")) {
                if (urlLower.contains("expr5") || urlLower.contains("search")) return "Search API";
                if (urlLower.contains("busbokdingapproval")) return "Bus Booking Approval API";
                if (urlLower.contains("getcorporatestinfo")) return "Corporate Info API";
                if (urlLower.contains("getcorporatetravellers")) return "Corporate Travellers API";
                return "Service Dispatch API";
            }
            
            if (urlLower.contains("get-corporate-booking-config")) return "Corporate Config API";
            if (urlLower.contains("get-all-corporate-departments")) return "Departments API";
            if (urlLower.contains("get-all-projects")) return "Projects API";
            if (urlLower.contains("generic-content-fields-by-channel")) return "Content API";
            
            // Generic names
            if (urlLower.contains("search")) return "Search API";
            if (urlLower.contains("book")) return "Booking API";
            if (urlLower.contains("approval")) return "Approval API";
            if (urlLower.contains("auth") || urlLower.contains("login")) return "Auth API";
            
            return "API Call";
        }
        
        String getErrorMessage() {
            if (response == null) return null;
            
            // Extract error from JSON response
            try {
                if (response.contains("\"message\":")) {
                    Pattern pattern = Pattern.compile("\"message\":\"([^\"]+)\"");
                    Matcher matcher = pattern.matcher(response);
                    if (matcher.find()) {
                        return matcher.group(1);
                    }
                }
                
                if (response.contains("\"error\":")) {
                    Pattern pattern = Pattern.compile("\"error\":\"([^\"]+)\"");
                    Matcher matcher = pattern.matcher(response);
                    if (matcher.find()) {
                        return matcher.group(1);
                    }
                }
                
                // Common errors
                if (response.contains("There is no approval workflow")) {
                    return "No approval workflow configured";
                }
                
                if (response.contains("Failed to send the bus booking request")) {
                    return "Booking request failed";
                }
                
            } catch (Exception e) {
                // Ignore
            }
            
            return null;
        }
    }
    
    public Log(WebDriver driver, ExtentTest test) {
        this.driver = driver;
        this.test = test;
        this.js = (JavascriptExecutor) driver;
    }
    
    public String WARNINGFontColorPrefix = "<span style='font-weight:bold;'><font color='red';font-size:16px; line-height:20px>";
    public String WARNINGFontColorSuffix = "</font></span>";
    public String InfoFontColorPrefix = "<span style='font-weight:bold;'><font color='gold';font-size:16px; line-height:20px>";
    public String InfoFontColorSuffix = "</font></span>";
    public String SuccessFontColorPrefix = "<span style='font-weight:bold;'><font color='green';font-size:16px; line-height:20px>";
    public String SuccessFontColorSuffix = "</font></span>";
    public String INFOFontColorPrefix = "<span><font color='blue';font-size:16px; line-height:20px>";
    public String INFOFontColorSuffix = "</font></span>";
    public String FAILFontColorPrefix = "<span><font color='red';font-size:16px; line-height:20px>";
    public String FAILFontColorSuffix = "</font></span>";
    
    public void ReportEvent(String sStatus, String sDec) {
        if (sStatus.equalsIgnoreCase("pass")) {
            test.log(Status.PASS, SuccessFontColorPrefix + sDec + SuccessFontColorSuffix);
        }
        if (sStatus.equalsIgnoreCase("fail")) {
            test.log(Status.FAIL, WARNINGFontColorPrefix + sDec + WARNINGFontColorSuffix);
        }
        if ((sStatus.equalsIgnoreCase("info")) || (sStatus.equalsIgnoreCase(""))) {
            test.log(Status.INFO, INFOFontColorPrefix + sDec + INFOFontColorSuffix);
        }
    }

    public void ReportEvent(String sStatus, Markup sDec) {
        if (sStatus.equalsIgnoreCase("pass")) {
            test.log(Status.PASS, SuccessFontColorPrefix + sDec + SuccessFontColorSuffix);
        }
        if (sStatus.equalsIgnoreCase("fail")) {
            test.log(Status.FAIL, WARNINGFontColorPrefix + sDec + WARNINGFontColorSuffix);
        }
        if ((sStatus.equalsIgnoreCase("info")) || (sStatus.equalsIgnoreCase(""))) {
            test.log(Status.INFO, INFOFontColorPrefix + sDec + INFOFontColorSuffix);
        }
        if ((sStatus.equalsIgnoreCase("FAIL"))) {
            test.log(Status.FAIL, FAILFontColorPrefix + sDec + FAILFontColorSuffix);
        }
    }
    
    // ========== SIMPLE NETWORK MONITORING ==========
    
    // Method 1: Start monitoring
 // Method 1: Enhanced network monitoring that FORCES logging
    public void enableNetworkMonitoring() {
        try {
            String script = 
                "console.log('[API-MONITOR] Enhanced monitoring enabled');\n" +
                "\n" +
                "// Store ALL API calls\n" +
                "window._allApiCalls = [];\n" +
                "window._allApiResponses = [];\n" +
                "\n" +
                "// FORCE LOGGING for ALL fetch calls\n" +
                "const originalFetch = window.fetch;\n" +
                "window.fetch = function(...args) {\n" +
                "    const url = args[0];\n" +
                "    const method = args[1]?.method || 'GET';\n" +
                "    const timestamp = new Date().toLocaleTimeString();\n" +
                "    \n" +
                "    // FORCE LOG the request\n" +
                "    console.log('[FORCED-API-CALL] ' + method + ' ' + url + ' | Time: ' + timestamp);\n" +
                "    \n" +
                "    // Store request\n" +
                "    const requestId = Date.now() + '_' + Math.random();\n" +
                "    const apiRequest = {\n" +
                "        id: requestId,\n" +
                "        url: url,\n" +
                "        method: method,\n" +
                "        requestTime: timestamp,\n" +
                "        type: 'FETCH'\n" +
                "    };\n" +
                "    window._allApiCalls.push(apiRequest);\n" +
                "    \n" +
                "    // Make the request\n" +
                "    return originalFetch.apply(this, args)\n" +
                "        .then(response => {\n" +
                "            // Clone to read response\n" +
                "            const clone = response.clone();\n" +
                "            return clone.text().then(text => {\n" +
                "                const responseTime = new Date().toLocaleTimeString();\n" +
                "                \n" +
                "                // FORCE LOG the response\n" +
                "                console.log('[FORCED-API-RESPONSE] ' + response.status + ' ' + url + ' | Time: ' + responseTime);\n" +
                "                \n" +
                "                // Log response data for errors\n" +
                "                if (response.status >= 300) {\n" +
                "                    console.error('[FORCED-API-ERROR] ' + response.status + ' ' + url);\n" +
                "                    console.error('[FORCED-API-ERROR-DATA] ' + (text.length > 200 ? text.substring(0, 200) + '...' : text));\n" +
                "                }\n" +
                "                \n" +
                "                // Store response\n" +
                "                apiRequest.responseTime = responseTime;\n" +
                "                apiRequest.status = response.status;\n" +
                "                apiRequest.statusText = response.statusText;\n" +
                "                apiRequest.response = text;\n" +
                "                window._allApiResponses.push(apiRequest);\n" +
                "                \n" +
                "                return response;\n" +
                "            });\n" +
                "        })\n" +
                "        .catch(error => {\n" +
                "            console.error('[FORCED-API-FAILED] ' + url + ' - ' + error.message);\n" +
                "            apiRequest.error = error.message;\n" +
                "            apiRequest.status = 'ERROR';\n" +
                "            window._allApiResponses.push(apiRequest);\n" +
                "            throw error;\n" +
                "        });\n" +
                "};\n" +
                "\n" +
                "// FORCE LOGGING for ALL XMLHttpRequest calls\n" +
                "const originalOpen = XMLHttpRequest.prototype.open;\n" +
                "XMLHttpRequest.prototype.open = function(method, url) {\n" +
                "    const timestamp = new Date().toLocaleTimeString();\n" +
        "    \n" +
        "    // FORCE LOG the request\n" +
        "    console.log('[FORCED-API-CALL] ' + method + ' ' + url + ' | Time: ' + timestamp);\n" +
        "    \n" +
        "    // Store request\n" +
        "    this._apiData = {\n" +
        "        id: Date.now() + '_' + Math.random(),\n" +
        "        url: url,\n" +
        "        method: method,\n" +
        "        requestTime: timestamp,\n" +
        "        type: 'XHR'\n" +
        "    };\n" +
        "    window._allApiCalls.push(this._apiData);\n" +
        "    \n" +
        "    // Listen for response\n" +
        "    this.addEventListener('load', function() {\n" +
        "        const responseTime = new Date().toLocaleTimeString();\n" +
        "        \n" +
        "        // FORCE LOG the response\n" +
        "        console.log('[FORCED-API-RESPONSE] ' + this.status + ' ' + url + ' | Time: ' + responseTime);\n" +
        "        \n" +
        "        // Store response\n" +
        "        this._apiData.responseTime = responseTime;\n" +
        "        this._apiData.status = this.status;\n" +
        "        this._apiData.statusText = this.statusText;\n" +
        "        this._apiData.response = this.responseText;\n" +
        "        window._allApiResponses.push(this._apiData);\n" +
        "        \n" +
        "        // Log errors\n" +
        "        if (this.status >= 300) {\n" +
        "            console.error('[FORCED-API-ERROR] ' + this.status + ' ' + url);\n" +
        "            console.error('[FORCED-API-ERROR-DATA] ' + (this.responseText.length > 200 ? this.responseText.substring(0, 200) + '...' : this.responseText));\n" +
        "        }\n" +
        "    });\n" +
        "    \n" +
        "    this.addEventListener('error', function() {\n" +
        "        console.error('[FORCED-API-FAILED] ' + url + ' - Network error');\n" +
        "        this._apiData.error = 'Network error';\n" +
        "        this._apiData.status = 'ERROR';\n" +
        "        window._allApiResponses.push(this._apiData);\n" +
        "    });\n" +
        "    \n" +
        "    return originalOpen.apply(this, arguments);\n" +
        "};\n" +
        "\n" +
        "console.log('[API-MONITOR] FORCED logging enabled - ALL API calls will be captured');";
            
            js.executeScript(script);
            ReportEvent("info", "✅ FORCED API logging enabled - All calls will be captured");
            
        } catch (Exception e) {
            ReportEvent("warning", "⚠️ Could not enable forced logging: " + e.getMessage());
        }
    }
    
    // Method 2: Capture and show network calls
    public void captureNetworkCalls(String stepName) {
        try {
            // FIRST: Inject logger to make sure it's there
            injectApiLogger();
            
            // Wait for API calls
            Thread.sleep(3000);
            
            // Method 1: Get from JavaScript storage
            List<NetworkCall> callsFromJs = getApiCallsFromJavaScript();
            
            // Method 2: Get from console logs
            List<NetworkCall> callsFromConsole = getApiCallsFromConsole();
            
            // Combine both
            List<NetworkCall> allCalls = new ArrayList<>();
            allCalls.addAll(callsFromJs);
            allCalls.addAll(callsFromConsole);
            
            // Remove duplicates
            List<NetworkCall> uniqueCalls = removeDuplicates(allCalls);
            
            // Show results
            showApiCallResults(stepName, uniqueCalls);
            
        } catch (Exception e) {
            ReportEvent("warning", "Error in captureNetworkCalls: " + e.getMessage());
        }
    }

    private List<NetworkCall> getApiCallsFromJavaScript() {
        List<NetworkCall> calls = new ArrayList<>();
        
        try {
            String script = 
                "if (!window._apiCalls || window._apiCalls.length === 0) return [];\n" +
                "return window._apiCalls.filter(call => call.completed).map(call => ({\n" +
                "    url: call.url,\n" +
                "    method: call.method,\n" +
                "    status: call.status,\n" +
                "    response: call.response,\n" +
                "    timestamp: call.timestamp\n" +
                "}));";
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> jsCalls = (List<Map<String, Object>>) js.executeScript(script);
            
            if (jsCalls != null) {
                for (Map<String, Object> jsCall : jsCalls) {
                    NetworkCall call = new NetworkCall();
                    call.url = (String) jsCall.get("url");
                    call.method = (String) jsCall.get("method");
                    Object status = jsCall.get("status");
                    call.status = status != null ? status.toString() : "unknown";
                    call.response = (String) jsCall.get("response");
                    call.time = (String) jsCall.get("timestamp");
                    calls.add(call);
                }
            }
            
        } catch (Exception e) {
            // Ignore
        }
        
        return calls;
    }

    private List<NetworkCall> getApiCallsFromConsole() {
        List<NetworkCall> calls = new ArrayList<>();
        
        try {
            LogEntries logs = driver.manage().logs().get(LogType.BROWSER);
            
            for (LogEntry entry : logs) {
                String message = entry.getMessage();
                
                // Look for API response logs
                if (message.contains("[API-RESPONSE]")) {
                    NetworkCall call = parseApiCallFromLog(message);
                    if (call != null) {
                        calls.add(call);
                    }
                }
            }
            
        } catch (Exception e) {
            // Ignore
        }
        
        return calls;
    }

    private NetworkCall parseApiCallFromLog(String logMessage) {
        try {
            NetworkCall call = new NetworkCall();
            
            // Extract URL from log
            if (logMessage.contains("URL:")) {
                int urlStart = logMessage.indexOf("URL:") + 4;
                int urlEnd = logMessage.indexOf(" |", urlStart);
                if (urlEnd == -1) urlEnd = logMessage.length();
                if (urlEnd > urlStart) {
                    call.url = logMessage.substring(urlStart, urlEnd).trim();
                }
            }
            
            // Extract status
            if (logMessage.contains("STATUS:")) {
                int statusStart = logMessage.indexOf("STATUS:") + 7;
                int statusEnd = logMessage.indexOf(" |", statusStart);
                if (statusEnd == -1) statusEnd = logMessage.length();
                if (statusEnd > statusStart) {
                    call.status = logMessage.substring(statusStart, statusEnd).trim();
                }
            }
            
            // Extract method (look in previous logs)
            call.method = "GET"; // Default
            
            return call;
            
        } catch (Exception e) {
            return null;
        }
    }

    private List<NetworkCall> removeDuplicates(List<NetworkCall> calls) {
        List<NetworkCall> unique = new ArrayList<>();
        List<String> seenUrls = new ArrayList<>();
        
        for (NetworkCall call : calls) {
            if (call.url != null && !seenUrls.contains(call.url)) {
                unique.add(call);
                seenUrls.add(call.url);
            }
        }
        
        return unique;
    }

    private void showApiCallResults(String stepName, List<NetworkCall> calls) {
        if (calls.isEmpty()) {
            ReportEvent("warning", "⚠️ " + stepName + " - No API calls captured");
            ReportEvent("info", "Possible reasons:");
            ReportEvent("info", "1. Page might have reloaded, losing JavaScript");
            ReportEvent("info", "2. No API calls were made");
            ReportEvent("info", "3. Check browser Console (F12) manually");
            return;
        }
        
        ReportEvent("info", "📊 " + stepName + " - Captured " + calls.size() + " API calls");
        
        int successCount = 0;
        int errorCount = 0;
        
        for (NetworkCall call : calls) {
            // Show API info
            String apiName = getSimpleApiName(call.url);
            boolean isSuccess = call.isSuccessful();
            
            if (isSuccess) {
                successCount++;
                ReportEvent("pass", "✅ " + apiName + " (" + call.method + ")");
                ReportEvent("info", "   Status: " + call.status);
                
                // Show summary for important APIs
                if (apiName.contains("Search") && call.response != null) {
                    if (call.response.contains("buses") || call.response.contains("results")) {
                        ReportEvent("info", "   Result: Search data received");
                    }
                }
                
            } else {
                errorCount++;
                ReportEvent("fail", "❌ " + apiName + " (" + call.method + ")");
                ReportEvent("info", "   Status: " + call.status);
                
                // Show error details
                if (call.response != null) {
                    String errorMsg = extractErrorMessage(call.response);
                    if (!errorMsg.isEmpty()) {
                        ReportEvent("info", "   Error: " + errorMsg);
                    }
                }
            }
            
            // Show URL
            if (call.url != null) {
                String shortUrl = call.url;
                if (shortUrl.length() > 80) {
                    shortUrl = shortUrl.substring(0, 80) + "...";
                }
                ReportEvent("info", "   URL: " + shortUrl);
            }
        }
        
        // Summary
        ReportEvent("info", "--- SUMMARY ---");
        if (errorCount > 0) {
            ReportEvent("fail", "❌ " + errorCount + " failed | ✅ " + successCount + " successful");
        } else {
            ReportEvent("pass", "✅ All " + successCount + " API calls successful");
        }
    }

    private String getSimpleApiName(String url) {
        if (url == null) return "API Call";
        
        String urlLower = url.toLowerCase();
        
        if (urlLower.contains("dispatch.jsp")) {
            if (urlLower.contains("expr") || urlLower.contains("search")) return "Search API";
            if (urlLower.contains("busbokdingapproval")) return "Approval API";
            if (urlLower.contains("getcorporatestinfo")) return "Corporate API";
            if (urlLower.contains("getcorporatetravellers")) return "Traveller API";
            return "Service API";
        }
        
        if (urlLower.contains("login")) return "Login API";
        if (urlLower.contains("search")) return "Search API";
        if (urlLower.contains("book")) return "Booking API";
        if (urlLower.contains("approval")) return "Approval API";
        
        return "API Call";
    }

    private String extractErrorMessage(String response) {
        if (response == null || response.isEmpty()) return "";
        
        try {
            // Look for JSON error
            if (response.contains("\"message\":")) {
                int start = response.indexOf("\"message\":\"") + 11;
                int end = response.indexOf("\"", start);
                if (end > start) {
                    return response.substring(start, end);
                }
            }
            
            // Common errors
            if (response.contains("There is no approval workflow")) {
                return "No approval workflow";
            }
            
            if (response.contains("Failed to send")) {
                return "Request failed";
            }
            
            return "See response";
            
        } catch (Exception e) {
            return "";
        }
    }

    private void showForcedResults(String stepName, List<String> apiCalls, List<String> apiResponses, List<String> apiErrors) {
        ReportEvent("info", " " + stepName + " - FORCED API Logs Analysis");
        
        if (!apiCalls.isEmpty()) {
            ReportEvent("info", "API Requests: " + apiCalls.size());
            for (String call : apiCalls) {
                ReportEvent("info", "   📤 " + call);
            }
        }
        
        if (!apiResponses.isEmpty()) {
            ReportEvent("info", "API Responses: " + apiResponses.size());
            for (String response : apiResponses) {
                ReportEvent("info", "   " + response);
            }
        }
        
        // Show captured network calls
        if (!capturedCalls.isEmpty()) {
            ReportEvent("info", "--- Detailed API Analysis (" + capturedCalls.size() + " calls) ---");
            
            int successCount = 0;
            int errorCount = 0;
            
            for (NetworkCall call : capturedCalls) {
                if (call.isSuccessful()) {
                    successCount++;
                    ReportEvent("pass", "✅ " + call.getApiName() + " (" + call.method + ")");
                    ReportEvent("info", "   Status: " + call.status + " | Time: " + call.time);
                    
                    // Show summary for successful calls
                    if (call.response != null) {
                        String summary = getApiSummary(call);
                        if (!summary.isEmpty()) {
                            ReportEvent("info", "   Result: " + summary);
                        }
                    }
                } else {
                    errorCount++;
                    ReportEvent("fail", "❌ " + call.getApiName() + " (" + call.method + ")");
                    ReportEvent("info", "   Status: " + call.status + " | Time: " + call.time);
                    
                    // Show error details
                    if (call.response != null) {
                        String error = extractError(call.response);
                        if (!error.isEmpty()) {
                            ReportEvent("info", "   Error: " + error);
                        }
                    }
                }
                
                // Show URL
                if (call.url != null) {
                    String shortUrl = truncateUrl(call.url);
                    ReportEvent("info", "   URL: " + shortUrl);
                }
            }
            
            // Summary
            ReportEvent("info", "--- SUMMARY ---");
            if (errorCount > 0) {
                ReportEvent("fail", "❌ " + errorCount + " failed | ✅ " + successCount + " successful");
            } else {
                ReportEvent("pass", "✅ All " + successCount + " API calls successful");
            }
            
        } else {
            ReportEvent("warning", "⚠️ No API calls captured with forced logging");
            ReportEvent("info", "Check if JavaScript is enabled on the page");
            ReportEvent("info", "Try manually checking Console (F12) for [FORCED-API] logs");
        }
        
        if (!apiErrors.isEmpty()) {
            ReportEvent("fail", "--- API ERRORS FOUND ---");
            for (String error : apiErrors) {
                ReportEvent("fail", "❌ " + error);
            }
        }
    }

    // Helper methods for forced logs
    private String extractForcedApiCall(String message) {
        try {
            if (message.contains("[FORCED-API-CALL]")) {
                int start = message.indexOf("[FORCED-API-CALL]") + 17;
                return message.substring(start);
            }
            return "";
        } catch (Exception e) {
            return "API Call";
        }
    }

    private String extractForcedApiResponse(String message) {
        try {
            if (message.contains("[FORCED-API-RESPONSE]")) {
                int start = message.indexOf("[FORCED-API-RESPONSE]") + 21;
                return message.substring(start);
            }
            return "";
        } catch (Exception e) {
            return "";
        }
    }

    private String extractForcedApiError(String message) {
        try {
            if (message.contains("[FORCED-API-ERROR]")) {
                int start = message.indexOf("[FORCED-API-ERROR]") + 18;
                return message.substring(start);
            }
            return "";
        } catch (Exception e) {
            return "API Error";
        }
    }

    private String getApiSummary(NetworkCall call) {
        if (call.response == null) return "";
        
        try {
            if (call.getApiName().contains("Search")) {
                return "Search data received";
            }
            if (call.getApiName().contains("Booking")) {
                return "Booking data received";
            }
            if (call.getApiName().contains("Approval")) {
                return "Approval data received";
            }
            return "Success";
        } catch (Exception e) {
            return "";
        }
    }

    private String extractError(String response) {
        if (response == null) return "";
        
        try {
            if (response.contains("\"message\":")) {
                // Extract message
                int start = response.indexOf("\"message\":\"") + 11;
                int end = response.indexOf("\"", start);
                if (end > start) {
                    return response.substring(start, end);
                }
            }
            
            // Common errors
            if (response.contains("There is no approval workflow")) {
                return "No approval workflow";
            }
            
            if (response.contains("Failed to send")) {
                return "Request failed";
            }
            
            return "Check response";
            
        } catch (Exception e) {
            return "Error";
        }
    }

    private String truncateUrl(String url) {
        if (url == null) return "";
        if (url.length() > 70) {
            return url.substring(0, 70) + "...";
        }
        return url;
    }
    
    
 // Add this method to your Log.java
    public void injectApiLogger() {
        try {
            String script = 
                "// Check if already injected\n" +
                "if (window._apiLoggerInjected) {\n" +
                "    console.log('[API-LOGGER] Already injected');\n" +
                "    return true;\n" +
                "}\n" +
                "\n" +
                "console.log('[API-LOGGER] Injecting API logger...');\n" +
                "\n" +
                "// Store API calls\n" +
                "window._apiCalls = [];\n" +
                "window._apiLoggerInjected = true;\n" +
                "\n" +
                "// Capture ALL fetch calls\n" +
                "const originalFetch = window.fetch;\n" +
                "window.fetch = function(...args) {\n" +
                "    const url = args[0];\n" +
                "    const method = args[1]?.method || 'GET';\n" +
                "    const timestamp = new Date().toISOString();\n" +
                "    \n" +
                "    // Log EVERY API call\n" +
                "    console.log('[API-CALL] METHOD:' + method + ' | URL:' + url + ' | TIME:' + timestamp);\n" +
                "    \n" +
                "    // Store call\n" +
                "    const callId = 'call_' + Date.now() + '_' + Math.random();\n" +
                "    const apiCall = {\n" +
                "        id: callId,\n" +
                "        url: url,\n" +
                "        method: method,\n" +
                "        timestamp: timestamp,\n" +
                "        type: 'fetch'\n" +
                "    };\n" +
                "    window._apiCalls.push(apiCall);\n" +
                "    \n" +
                "    // Make request\n" +
                "    return originalFetch.apply(this, args)\n" +
                "        .then(response => {\n" +
                "            const clone = response.clone();\n" +
                "            return clone.text().then(text => {\n" +
                "                // Log response\n" +
                "                console.log('[API-RESPONSE] STATUS:' + response.status + ' | URL:' + url);\n" +
                "                \n" +
                "                // Update stored call\n" +
                "                apiCall.status = response.status;\n" +
                "                apiCall.response = text.substring(0, 1000); // Limit size\n" +
                "                apiCall.completed = true;\n" +
                "                \n" +
                "                // Log errors\n" +
                "                if (response.status >= 300) {\n" +
                "                    console.error('[API-ERROR] STATUS:' + response.status + ' | URL:' + url);\n" +
                "                    console.error('[API-ERROR-DATA] ' + text.substring(0, 300));\n" +
                "                }\n" +
                "                \n" +
                "                return response;\n" +
                "            });\n" +
                "        })\n" +
                "        .catch(error => {\n" +
                "            console.error('[API-FAILED] URL:' + url + ' | ERROR:' + error.message);\n" +
                "            apiCall.error = error.message;\n" +
                "            apiCall.status = 'FAILED';\n" +
                "            return Promise.reject(error);\n" +
                "        });\n" +
                "};\n" +
                "\n" +
                "// Capture XMLHttpRequest\n" +
                "const originalOpen = XMLHttpRequest.prototype.open;\n" +
                "XMLHttpRequest.prototype.open = function(method, url) {\n" +
                "    console.log('[API-CALL] METHOD:' + method + ' | URL:' + url);\n" +
                "    \n" +
                "    this._apiCallData = {\n" +
                "        url: url,\n" +
                "        method: method,\n" +
                "        timestamp: new Date().toISOString(),\n" +
                "        type: 'xhr'\n" +
                "    };\n" +
                "    window._apiCalls.push(this._apiCallData);\n" +
                "    \n" +
                "    this.addEventListener('load', function() {\n" +
                "        console.log('[API-RESPONSE] STATUS:' + this.status + ' | URL:' + url);\n" +
                "        this._apiCallData.status = this.status;\n" +
                "        this._apiCallData.response = this.responseText.substring(0, 1000);\n" +
                "        this._apiCallData.completed = true;\n" +
                "        \n" +
                "        if (this.status >= 300) {\n" +
                "            console.error('[API-ERROR] STATUS:' + this.status + ' | URL:' + url);\n" +
                "        }\n" +
                "    });\n" +
                "    \n" +
                "    return originalOpen.apply(this, arguments);\n" +
                "};\n" +
                "\n" +
                "console.log('[API-LOGGER] Successfully injected');\n" +
                "return true;";
            
            Boolean result = (Boolean) js.executeScript(script);
           // ReportEvent("info", "✅ API logger injected: " + result);
            
        } catch (Exception e) {
          //  ReportEvent("warning", "⚠️ Could not inject API logger: " + e.getMessage());
        	System.out.println("warning ⚠️ Could not inject API logger: ");
        }
    }
    
    
    // Method 3: Show network results
    private void showNetworkResults(String stepName, List<String> apiCalls, List<String> apiErrors) {
        ReportEvent("info", "📊 " + stepName + " - API Calls Analysis");
        
        if (!apiCalls.isEmpty()) {
            ReportEvent("info", "Found " + apiCalls.size() + " API calls in console:");
            for (String call : apiCalls) {
                ReportEvent("info", "   • " + call);
            }
        }
        
        if (!apiErrors.isEmpty()) {
            ReportEvent("fail", "Found " + apiErrors.size() + " API errors:");
            for (String error : apiErrors) {
                ReportEvent("fail", "   ❌ " + error);
            }
        }
        
        // Show captured network calls
        if (!capturedCalls.isEmpty()) {
            ReportEvent("info", "--- Captured Network Calls (" + capturedCalls.size() + ") ---");
            
            int successCount = 0;
            int errorCount = 0;
            
            for (NetworkCall call : capturedCalls) {
                if (call.isSuccessful()) {
                    successCount++;
                    ReportEvent("pass", "✅ " + call.getApiName() + " (" + call.method + ")");
                    ReportEvent("info", "   Status: " + call.status + " | Time: " + call.time);
                    
                    // Show response summary
                    if (call.response != null && !call.response.isEmpty()) {
                        String summary = getResponseSummary(call);
                        if (!summary.isEmpty()) {
                            ReportEvent("info", "   Response: " + summary);
                        }
                    }
                } else {
                    errorCount++;
                    ReportEvent("fail", "❌ " + call.getApiName() + " (" + call.method + ")");
                    ReportEvent("info", "   Status: " + call.status + " | Time: " + call.time);
                    
                    // Show error details
                    if (call.response != null && !call.response.isEmpty()) {
                        String errorMsg = extractErrorFromResponse(call.response);
                        if (!errorMsg.isEmpty()) {
                            ReportEvent("info", "   Error: " + errorMsg);
                        }
                    }
                }
                
                // Show URL (shortened)
                if (call.url != null) {
                    String shortUrl = call.url.length() > 60 ? call.url.substring(0, 60) + "..." : call.url;
                    ReportEvent("info", "   URL: " + shortUrl);
                }
            }
            
            // Summary
         //   ReportEvent("info", "--- SUMMARY ---");
            if (errorCount > 0) {
                ReportEvent("fail", "❌ " + errorCount + " failed | ✅ " + successCount + " successful");
            } else {
                ReportEvent("pass", "✅ All " + successCount + " API calls successful");
            }
            
        } else if (apiCalls.isEmpty() && apiErrors.isEmpty()) {
            ReportEvent("warning", "⚠️ No API calls detected");
            ReportEvent("info", "Make sure enableNetworkMonitoring() was called");
            ReportEvent("info", "Check browser console manually (F12 -> Console)");
        }
    }
    
    // ========== HELPER METHODS ==========
    
//    private String extractApiCall(String logMessage) {
//        try {
//            if (logMessage.contains("[API] ")) {
//                int start = logMessage.indexOf("[API] ") + 6;
//                String apiCall = logMessage.substring(start);
//                
//                // Shorten if too long
//                if (apiCall.length() > 80) {
//                    apiCall = apiCall.substring(0, 80) + "...";
//                }
//                
//                return apiCall;
//            }
//            return "";
//        } catch (Exception e) {
//            return "API Call";
//        }
//    }
    
    private String extractApiResponse(String logMessage) {
        try {
            if (logMessage.contains("[API-RESPONSE] ")) {
                int start = logMessage.indexOf("[API-RESPONSE] ") + 14;
                return logMessage.substring(start);
            }
            return "";
        } catch (Exception e) {
            return "";
        }
    }
    
    private String extractApiError(String logMessage) {
        try {
            if (logMessage.contains("[API-ERROR] ")) {
                int start = logMessage.indexOf("[API-ERROR] ") + 12;
                String error = logMessage.substring(start);
                
                // Shorten if too long
                if (error.length() > 100) {
                    error = error.substring(0, 100) + "...";
                }
                
                return error;
            }
            return "";
        } catch (Exception e) {
            return "API Error";
        }
    }
    
    private String getResponseSummary(NetworkCall call) {
        if (call.response == null || call.response.isEmpty()) return "";
        
        try {
            // For search API
            if (call.getApiName().contains("Search")) {
                if (call.response.contains("buses") || call.response.contains("results")) {
                    return "Search results received";
                }
            }
            
            // For booking API
            if (call.getApiName().contains("Booking")) {
                if (call.response.contains("bookingId") || call.response.contains("reference")) {
                    return "Booking processed";
                }
            }
            
            // For approval API
            if (call.getApiName().contains("Approval")) {
                if (call.response.contains("success") || call.response.contains("OK")) {
                    return "Approval successful";
                }
            }
            
            // Generic success
            if (call.response.contains("\"status\":\"OK\"") || call.response.contains("\"success\":true")) {
                return "Success";
            }
            
            // Return first 80 chars
            if (call.response.length() > 80) {
                return call.response.substring(0, 80) + "...";
            }
            
            return call.response;
            
        } catch (Exception e) {
            return "Response received";
        }
    }
    
    private String extractErrorFromResponse(String response) {
        if (response == null || response.isEmpty()) return "";
        
        try {
            // Extract error message
            if (response.contains("\"message\":")) {
                Pattern pattern = Pattern.compile("\"message\":\"([^\"]+)\"");
                Matcher matcher = pattern.matcher(response);
                if (matcher.find()) {
                    return matcher.group(1);
                }
            }
            
            if (response.contains("\"error\":")) {
                Pattern pattern = Pattern.compile("\"error\":\"([^\"]+)\"");
                Matcher matcher = pattern.matcher(response);
                if (matcher.find()) {
                    return matcher.group(1);
                }
            }
            
            // Common errors
            if (response.contains("There is no approval workflow")) {
                return "No approval workflow configured";
            }
            
            if (response.contains("Failed to send the bus booking request")) {
                return "Booking request failed";
            }
            
            // Status-based messages
            if (response.contains("300")) return "Status 300 - Validation error";
            if (response.contains("400")) return "Status 400 - Bad request";
            if (response.contains("404")) return "Status 404 - Not found";
            if (response.contains("500")) return "Status 500 - Server error";
            
            // Return short response
            if (response.length() > 60) {
                return response.substring(0, 60) + "...";
            }
            
            return response;
            
        } catch (Exception e) {
            return "Error in response";
        }
    }
    
//----------------

//Method 1: Check for FAILED APIs only (non-200 status codes)
public void checkFailedApis(String stepName) {
 try {
     Thread.sleep(2000); // Wait for APIs
     
     ReportEvent("info", "🔍 " + stepName + " - Checking for failed APIs...");
     
     // Get browser console logs
     LogEntries logs = driver.manage().logs().get(LogType.BROWSER);
     
     boolean foundFailures = false;
     
     for (LogEntry entry : logs) {
         String message = entry.getMessage();
         
         // Look for API calls with non-200 status codes
         if (isFailedApiCall(message)) {
             foundFailures = true;
             
             // Extract API details
             ApiFailure failure = extractApiFailure(message);
             if (failure != null) {
                 showApiFailure(failure);
             }
         }
     }
     
     if (!foundFailures) {
         ReportEvent("pass", "✅ " + stepName + " - No failed API calls found");
     } else {
         ReportEvent("fail", "❌ " + stepName + " - Failed API calls detected");
     }
     
 } catch (Exception e) {
     ReportEvent("warning", "Error checking failed APIs: " + e.getMessage());
 }
}

//Method 2: Check ALL APIs but highlight failures
public void checkAllApisWithFailures(String stepName) {
 try {
     Thread.sleep(2000);
     
     ReportEvent("info", "📊 " + stepName + " - Checking all API calls");
     
     LogEntries logs = driver.manage().logs().get(LogType.BROWSER);
     
     List<ApiCall> allApis = new ArrayList<>();
     List<ApiFailure> failedApis = new ArrayList<>();
     
     for (LogEntry entry : logs) {
         String message = entry.getMessage();
         
         // Check if it's an API call
         if (isApiCall(message)) {
             ApiCall api = extractApiCall(message);
             if (api != null) {
                 allApis.add(api);
                 
                 // Check if failed
                 if (!api.isSuccess()) {
                     ApiFailure failure = convertToFailure(api);
                     failedApis.add(failure);
                 }
             }
         }
     }
     
     // Show results
     if (!allApis.isEmpty()) {
         ReportEvent("info", "Total API calls: " + allApis.size());
         
         // Show all APIs briefly
         for (ApiCall api : allApis) {
             if (api.isSuccess()) {
                 ReportEvent("pass", "✅ " + api.getName() + " - Status: " + api.getStatus());
             }
         }
         
         // Show failed APIs in detail
         if (!failedApis.isEmpty()) {
             ReportEvent("fail", "--- FAILED APIS (" + failedApis.size() + ") ---");
             for (ApiFailure failure : failedApis) {
                 showDetailedFailure(failure);
             }
         } else {
             ReportEvent("pass", "✅ All " + allApis.size() + " API calls successful");
         }
     } else {
         ReportEvent("warning", "No API calls detected");
     }
     
 } catch (Exception e) {
     ReportEvent("warning", "Error: " + e.getMessage());
 }
}

//========== HELPER CLASSES ==========

private class ApiCall {
 String url;
 String method;
 String status;
 String response;
 
 boolean isSuccess() {
     try {
         if (status == null) return false;
         int code = Integer.parseInt(status);
         return code == 200; // Only 200 is success
     } catch (Exception e) {
         return false;
     }
 }
 
 String getName() {
     if (url == null) return "API";
     if (url.contains("dispatch.jsp")) {
         if (url.contains("LOGIN")) return "Login API";
         if (url.contains("EXPR") || url.contains("search")) return "Search API";
         if (url.contains("BUSBOKDINGAPPROVAL")) return "Approval API";
         return "Service API";
     }
     return "API Call";
 }
 
 String getStatus() {
     return status != null ? status : "Unknown";
 }
}

private class ApiFailure {
 String apiName;
 String url;
 String method;
 String statusCode;
 String errorMessage;
 String responseBody;
 
 boolean isCritical() {
     if (statusCode == null) return false;
     try {
         int code = Integer.parseInt(statusCode);
         return code >= 400; // 400+ are critical errors
     } catch (Exception e) {
         return false;
     }
 }
}

//========== HELPER METHODS ==========

private boolean isFailedApiCall(String message) {
 // Check for non-200 status codes
 if (message.contains("statuscode:")) {
     // Extract status code
     String status = extractStatusFromLog(message);
     if (status != null && !status.equals("200")) {
         return true;
     }
 }
 
 // Check for error patterns
 if (message.contains("\"status\":\"NOK\"") || 
     message.contains("\"success\":false") ||
     message.contains("Failed to") ||
     message.contains("error\":") ||
     message.contains("errormessage:")) {
     return true;
 }
 
 return false;
}

private boolean isApiCall(String message) {
 return message.contains("http") || 
        message.contains("dispatch.jsp") ||
        message.contains("statuscode:") ||
        message.contains("\"status\":") ||
        (message.contains("POST") && message.contains("/")) ||
        (message.contains("GET") && message.contains("/"));
}

private ApiCall extractApiCall(String logMessage) {
 try {
     ApiCall api = new ApiCall();
     
     // Extract URL
     if (logMessage.contains("http")) {
         int start = logMessage.indexOf("http");
         int end = logMessage.indexOf(" ", start);
         if (end == -1) end = logMessage.length();
         if (end > start) {
             api.url = logMessage.substring(start, end);
         }
     }
     
     // Extract status code
     api.status = extractStatusFromLog(logMessage);
     
     // Extract method
     if (logMessage.contains("POST")) api.method = "POST";
     else if (logMessage.contains("GET")) api.method = "GET";
     else api.method = "UNKNOWN";
     
     // Extract response if present
     if (logMessage.contains("\"message\":")) {
         api.response = extractResponseFromLog(logMessage);
     }
     
     return api;
     
 } catch (Exception e) {
     return null;
 }
}

private ApiFailure extractApiFailure(String logMessage) {
 try {
     ApiFailure failure = new ApiFailure();
     
     // Extract URL
     if (logMessage.contains("http")) {
         int start = logMessage.indexOf("http");
         int end = logMessage.indexOf(" ", start);
         if (end == -1) end = logMessage.length();
         if (end > start) {
             failure.url = logMessage.substring(start, end);
         }
     }
     
     // Extract API name from URL
     failure.apiName = getApiNameFromUrl(failure.url);
     
     // Extract status code
     failure.statusCode = extractStatusFromLog(logMessage);
     
     // Extract error message
     failure.errorMessage = extractErrorMessageFromLog(logMessage);
     
     // Extract response body
     failure.responseBody = extractResponseFromLog(logMessage);
     
     // Extract method
     if (logMessage.contains("POST")) failure.method = "POST";
     else if (logMessage.contains("GET")) failure.method = "GET";
     else failure.method = "UNKNOWN";
     
     return failure;
     
 } catch (Exception e) {
     return null;
 }
}

private String extractStatusFromLog(String logMessage) {
 try {
     // Look for statuscode: pattern
     if (logMessage.contains("statuscode:")) {
         int start = logMessage.indexOf("statuscode:") + 11;
         int end = logMessage.indexOf(" ", start);
         if (end == -1) end = logMessage.indexOf(",", start);
         if (end == -1) end = logMessage.length();
         if (end > start) {
             String status = logMessage.substring(start, end).trim();
             // Clean up
             status = status.replace(",", "").replace("\"", "").replace("}", "");
             return status;
         }
     }
     
     // Look for status in JSON
     if (logMessage.contains("\"statusCode\":")) {
         int start = logMessage.indexOf("\"statusCode\":") + 13;
         int end = logMessage.indexOf(",", start);
         if (end > start) {
             return logMessage.substring(start, end).trim();
         }
     }
     
     return null;
     
 } catch (Exception e) {
     return null;
 }
}

private String extractErrorMessageFromLog(String logMessage) {
 try {
     // Look for message field
     if (logMessage.contains("\"message\":")) {
         int start = logMessage.indexOf("\"message\":\"") + 11;
         int end = logMessage.indexOf("\"", start);
         if (end > start) {
             return logMessage.substring(start, end);
         }
     }
     
     // Look for error field
     if (logMessage.contains("\"error\":")) {
         int start = logMessage.indexOf("\"error\":\"") + 9;
         int end = logMessage.indexOf("\"", start);
         if (end > start) {
             return logMessage.substring(start, end);
         }
     }
     
     // Common errors
     if (logMessage.contains("There is no approval workflow")) {
         return "There is no approval workflow for this profileid";
     }
     
     if (logMessage.contains("Failed to send the bus booking request")) {
         return "Failed to send the bus booking request for approval";
     }
     
     // Extract from plain text
     if (logMessage.contains("message:")) {
         int start = logMessage.indexOf("message:") + 8;
         int end = logMessage.indexOf("\n", start);
         if (end == -1) end = logMessage.length();
         if (end > start) {
             return logMessage.substring(start, end).trim();
         }
     }
     
     return "See response for details";
     
 } catch (Exception e) {
     return "Error extracting message";
 }
}

private String extractResponseFromLog(String logMessage) {
 try {
     // Extract JSON response
     if (logMessage.contains("{") && logMessage.contains("}")) {
         int start = logMessage.indexOf("{");
         int end = logMessage.lastIndexOf("}");
         if (end > start) {
             String json = logMessage.substring(start, end + 1);
             
             // Limit length
             if (json.length() > 300) {
                 json = json.substring(0, 300) + "...";
             }
             
             return json;
         }
     }
     
     return logMessage;
     
 } catch (Exception e) {
     return logMessage;
 }
}

//private String getApiNameFromUrl(String url) {
// if (url == null) return "Unknown API";
// 
// if (url.contains("dispatch.jsp")) {
//     if (url.contains("LOGIN")) return "Login API";
//     if (url.contains("EXPR") || url.contains("search")) return "Search API";
//     if (url.contains("BUSBOKDINGAPPROVAL")) return "Approval API";
//     if (url.contains("GETCORPORATESTINFO")) return "Corporate Info API";
//     if (url.contains("GETCORPORATETRAVELLERS")) return "Traveller API";
//     return "Service Dispatch API";
// }
// 
// return "API Call";
//}

private void showApiFailure(ApiFailure failure) {
 ReportEvent("fail", "❌ " + failure.apiName + " (" + failure.method + ")");
 ReportEvent("info", "   URL: " + truncate(failure.url, 80));
 ReportEvent("info", "   Status Code: " + failure.statusCode);
 
 if (failure.errorMessage != null && !failure.errorMessage.isEmpty()) {
     ReportEvent("info", "   Error: " + failure.errorMessage);
 }
 
 if (failure.responseBody != null && !failure.responseBody.isEmpty()) {
     ReportEvent("info", "   Response: " + truncate(failure.responseBody, 150));
 }
}

private void showDetailedFailure(ApiFailure failure) {
 String severity = failure.isCritical() ? "🔴 CRITICAL" : "🟡 WARNING";
 
 ReportEvent("fail", severity + " " + failure.apiName);
 ReportEvent("info", "   Method: " + failure.method);
 ReportEvent("info", "   Status: " + failure.statusCode);
 ReportEvent("info", "   URL: " + truncate(failure.url, 100));
 
 if (failure.errorMessage != null) {
     ReportEvent("info", "   Error Message: " + failure.errorMessage);
 }
 
 if (failure.responseBody != null) {
     ReportEvent("info", "   Full Response: " + failure.responseBody);
 }
}

private ApiFailure convertToFailure(ApiCall api) {
 ApiFailure failure = new ApiFailure();
 failure.apiName = api.getName();
 failure.url = api.url;
 failure.method = api.method;
 failure.statusCode = api.status;
 failure.responseBody = api.response;
 failure.errorMessage = extractErrorMessageFromLog(api.response != null ? api.response : "");
 return failure;
}

private String truncate(String text, int length) {
 if (text == null) return "";
 if (text.length() > length) {
     return text.substring(0, length) + "...";
 }
 return text;
}

//-----------new code for console 
////========== COMPREHENSIVE ERROR CHECKING SYSTEM ==========
//
///**
//* MASTER METHOD: Checks ALL errors - Network APIs + Console + JavaScript
//* Categorizes issues as Backend, Frontend, or UI issues
//*/
//public void checkAllErrorsAndCategorize(String stepName) {
// try {
//     Thread.sleep(1500);
//     ReportEvent("info", "🔍 " + stepName + " - COMPREHENSIVE ERROR CHECK");
//     
//     List<ErrorDetail> allErrors = new ArrayList<>();
//     
//     // 1. Check Network API Errors
//     List<ErrorDetail> networkErrors = checkNetworkApiErrors();
//     allErrors.addAll(networkErrors);
//     
//     // 2. Check Console Errors
//     List<ErrorDetail> consoleErrors = checkConsoleErrors();
//     allErrors.addAll(consoleErrors);
//     
//     // 3. Check JavaScript Errors
//     List<ErrorDetail> jsErrors = checkJavaScriptErrors();
//     allErrors.addAll(jsErrors);
//     
//     // 4. Check Status 300 specifically
//     List<ErrorDetail> status300Errors = checkForStatus300();
//     allErrors.addAll(status300Errors);
//     
//     // 5. Display categorized results
//     displayCategorizedErrors(stepName, allErrors);
//     
//     // 6. Return test result
//     if (allErrors.isEmpty()) {
//         ReportEvent("pass", "✅ " + stepName + " - No errors found");
//     } else {
//         ReportEvent("fail", "❌ " + stepName + " - Found " + allErrors.size() + " error(s)");
//     }
//     
// } catch (Exception e) {
//     ReportEvent("warning", "Error in comprehensive check: " + e.getMessage());
// }
//}
//
////========== ERROR DETAIL CLASS ==========
//
//private class ErrorDetail {
// String errorType; // BACKEND, FRONTEND, UI, JAVASCRIPT, NETWORK
// String category;  // API_ERROR, CONSOLE_ERROR, JS_ERROR, NETWORK_ERROR
// String source;    // API_CALL, BROWSER_CONSOLE, JS_EXECUTION
// String errorMessage;
// String url;
// String statusCode;
// String timestamp;
// String stackTrace;
// boolean isCritical;
// 
// boolean isBackendIssue() {
//     return errorType.equals("BACKEND") || 
//            category.contains("API") || 
//            category.contains("NETWORK") ||
//            (statusCode != null && Integer.parseInt(statusCode) >= 400);
// }
// 
// boolean isFrontendIssue() {
//     return errorType.equals("FRONTEND") || 
//            category.contains("CONSOLE") ||
//            category.contains("JAVASCRIPT") ||
//            errorMessage.contains("is not a function") ||
//            errorMessage.contains("undefined") ||
//            errorMessage.contains("Cannot read property");
// }
// 
// boolean isUIissue() {
//     return errorType.equals("UI") ||
//            errorMessage.contains("Element not found") ||
//            errorMessage.contains("Timeout") ||
//            errorMessage.contains("Stale element") ||
//            errorMessage.contains("Not clickable");
// }
//}
//
////========== SPECIFIC CHECKING METHODS ==========
//
///**
//* Check Network API Errors specifically
//*/
//private List<ErrorDetail> checkNetworkApiErrors() {
// List<ErrorDetail> errors = new ArrayList<>();
// 
// try {
//     LogEntries logs = driver.manage().logs().get(LogType.BROWSER);
//     
//     for (LogEntry entry : logs) {
//         String message = entry.getMessage();
//         
//         // Check for network/API errors
//         if (message.contains("statuscode:") || message.contains("http") || message.contains("dispatch.jsp")) {
//             
//             // Check for non-200 status codes
//             String status = extractStatusFromLog(message);
//             if (status != null && !status.equals("200")) {
//                 ErrorDetail error = new ErrorDetail();
//                 error.errorType = "BACKEND";
//                 error.category = "API_ERROR";
//                 error.source = "NETWORK_CALL";
//                 error.statusCode = status;
//                 error.url = extractUrlFromLog(message);
//                 error.errorMessage = extractErrorMessageFromLog(message);
//                 error.timestamp = new java.util.Date().toString();
//                 
//                 // Determine if critical
//                 if (status.equals("300") || status.equals("400") || status.equals("500")) {
//                     error.isCritical = true;
//                 }
//                 
//                 errors.add(error);
//             }
//             
//             // Check for "NOK" in response
//             if (message.contains("\"status\":\"NOK\"") || message.contains("\"success\":false")) {
//                 ErrorDetail error = new ErrorDetail();
//                 error.errorType = "BACKEND";
//                 error.category = "API_RESPONSE_ERROR";
//                 error.source = "NETWORK_RESPONSE";
//                 error.errorMessage = "API returned failure status";
//                 error.url = extractUrlFromLog(message);
//                 error.timestamp = new java.util.Date().toString();
//                 error.isCritical = true;
//                 
//                 errors.add(error);
//             }
//         }
//     }
// } catch (Exception e) {
//     // Ignore
// }
// 
// return errors;
//}
//
///**
//* Check Console Errors specifically
//*/
//private List<ErrorDetail> checkConsoleErrors() {
// List<ErrorDetail> errors = new ArrayList<>();
// 
// try {
//     LogEntries logs = driver.manage().logs().get(LogType.BROWSER);
//     
//     for (LogEntry entry : logs) {
//         String message = entry.getMessage();
//         String level = entry.getLevel().toString();
//         
//         // Check for SEVERE or ERROR level messages
//         if (level.equals("SEVERE") || level.equals("ERROR")) {
//             ErrorDetail error = new ErrorDetail();
//             
//             // Determine error type based on content
//             if (message.contains("404") || message.contains("500") || message.contains("Failed to load")) {
//                 error.errorType = "BACKEND";
//                 error.category = "NETWORK_ERROR";
//             } else if (message.contains("SyntaxError") || message.contains("ReferenceError") || 
//                       message.contains("TypeError")) {
//                 error.errorType = "FRONTEND";
//                 error.category = "JAVASCRIPT_ERROR";
//             } else if (message.contains("Uncaught") || message.contains("exception")) {
//                 error.errorType = "FRONTEND";
//                 error.category = "RUNTIME_ERROR";
//             } else {
//                 error.errorType = "FRONTEND";
//                 error.category = "CONSOLE_ERROR";
//             }
//             
//             error.source = "BROWSER_CONSOLE";
//             error.errorMessage = truncate(message, 200);
//             error.timestamp = new java.util.Date().toString();
//             error.isCritical = (level.equals("SEVERE"));
//             
//             errors.add(error);
//         }
//         
//         // Check for warnings that might indicate issues
//         if (level.equals("WARNING")) {
//             // Check if warning indicates a real problem
//             if (message.contains("Deprecated") || message.contains("Obsolete") || 
//                 message.contains("Invalid") || message.contains("Failed")) {
//                 
//                 ErrorDetail error = new ErrorDetail();
//                 error.errorType = "FRONTEND";
//                 error.category = "CONSOLE_WARNING";
//                 error.source = "BROWSER_CONSOLE";
//                 error.errorMessage = truncate(message, 150);
//                 error.timestamp = new java.util.Date().toString();
//                 error.isCritical = false;
//                 
//                 errors.add(error);
//             }
//         }
//     }
// } catch (Exception e) {
//     // Ignore
// }
// 
// return errors;
//}
//
///**
//* Check JavaScript Errors specifically
//*/
//private List<ErrorDetail> checkJavaScriptErrors() {
// List<ErrorDetail> errors = new ArrayList<>();
// 
// try {
//     // Inject script to check for JavaScript errors
//     String script = 
//         "var errors = [];\n" +
//         "// Check for common JavaScript issues\n" +
//         "if (typeof window.onerror === 'function') {\n" +
//         "    errors.push('JavaScript error handler is active');\n" +
//         "}\n" +
//         "// Check for console.error calls\n" +
//         "var originalConsoleError = console.error;\n" +
//         "console.error = function() {\n" +
//         "    errors.push('Console error: ' + Array.prototype.slice.call(arguments).join(' '));\n" +
//         "    originalConsoleError.apply(console, arguments);\n" +
//         "};\n" +
//         "return errors;";
//     
//     @SuppressWarnings("unchecked")
//     List<String> jsErrors = (List<String>) js.executeScript(script);
//     
//     if (jsErrors != null && !jsErrors.isEmpty()) {
//         for (String jsError : jsErrors) {
//             ErrorDetail error = new ErrorDetail();
//             error.errorType = "FRONTEND";
//             error.category = "JAVASCRIPT_RUNTIME";
//             error.source = "JS_EXECUTION";
//             error.errorMessage = jsError;
//             error.timestamp = new java.util.Date().toString();
//             error.isCritical = jsError.contains("error") || jsError.contains("Error");
//             
//             errors.add(error);
//         }
//     }
// } catch (Exception e) {
//     // Ignore JavaScript execution errors
// }
// 
// return errors;
//}
//
///**
//* Specifically check for status 300 errors
//*/
//private List<ErrorDetail> checkForStatus300() {
// List<ErrorDetail> errors = new ArrayList<>();
// 
// try {
//     LogEntries logs = driver.manage().logs().get(LogType.BROWSER);
//     
//     for (LogEntry entry : logs) {
//         String message = entry.getMessage();
//         
//         // Direct check for status 300
//         if ((message.contains("statuscode: 300") || message.contains("\"statuscode\":300")) &&
//             (message.contains("dispatch.jsp") || message.contains("BUSBOKDINGAPPROVAL"))) {
//             
//             ErrorDetail error = new ErrorDetail();
//             error.errorType = "BACKEND";
//             error.category = "APPROVAL_WORKFLOW_ERROR";
//             error.source = "APPROVAL_API";
//             error.statusCode = "300";
//             error.url = extractUrlFromLog(message);
//             error.errorMessage = extractErrorMessageFromLog(message);
//             error.timestamp = new java.util.Date().toString();
//             error.isCritical = true;
//             
//             // Add detailed context
//             if (error.errorMessage == null || error.errorMessage.isEmpty()) {
//                 error.errorMessage = "Approval API returned status 300 - Check approval workflow configuration";
//             }
//             
//             errors.add(error);
//         }
//     }
// } catch (Exception e) {
//     // Ignore
// }
// 
// return errors;
//}
//
///**
//* Display categorized errors
//*/
//private void displayCategorizedErrors(String stepName, List<ErrorDetail> errors) {
// if (errors.isEmpty()) {
//     return;
// }
// 
// ReportEvent("info", "=".repeat(70));
// ReportEvent("info", "📋 ERROR CATEGORIZATION REPORT");
// ReportEvent("info", "=".repeat(70));
// 
// // Group by type
// Map<String, List<ErrorDetail>> groupedErrors = new HashMap<>();
// for (ErrorDetail error : errors) {
//     groupedErrors.computeIfAbsent(error.errorType, k -> new ArrayList<>()).add(error);
// }
// 
// // Display by type
// for (Map.Entry<String, List<ErrorDetail>> entry : groupedErrors.entrySet()) {
//     String errorType = entry.getKey();
//     List<ErrorDetail> typeErrors = entry.getValue();
//     
//     String typeIcon = "";
//     String typeColor = "info";
//     
//     switch (errorType) {
//         case "BACKEND":
//             typeIcon = "🔴";
//             typeColor = "fail";
//             break;
//         case "FRONTEND":
//             typeIcon = "🟡";
//             typeColor = "warning";
//             break;
//         case "UI":
//             typeIcon = "🔵";
//             typeColor = "info";
//             break;
//         default:
//             typeIcon = "⚪";
//     }
//     
//     ReportEvent(typeColor, typeIcon + " " + errorType + " ISSUES (" + typeErrors.size() + ")");
//     
//     for (int i = 0; i < typeErrors.size(); i++) {
//         ErrorDetail error = typeErrors.get(i);
//         
//         ReportEvent("info", "   " + (i + 1) + ". [" + error.category + "]");
//         ReportEvent("info", "      Source: " + error.source);
//         
//         if (error.url != null) {
//             ReportEvent("info", "      URL: " + truncate(error.url, 80));
//         }
//         
//         if (error.statusCode != null) {
//             ReportEvent("info", "      Status: " + error.statusCode);
//         }
//         
//         if (error.errorMessage != null) {
//             ReportEvent("info", "      Message: " + truncate(error.errorMessage, 120));
//         }
//         
//         if (error.isCritical) {
//             ReportEvent("fail", "      ⚠️ CRITICAL ISSUE");
//         }
//         
//         ReportEvent("info", "      Time: " + error.timestamp);
//         ReportEvent("info", "      " + "-".repeat(40));
//     }
// }
// 
// // Summary
// ReportEvent("info", "📊 ERROR SUMMARY");
// ReportEvent("info", "Total Errors: " + errors.size());
// 
// long backendCount = errors.stream().filter(ErrorDetail::isBackendIssue).count();
// long frontendCount = errors.stream().filter(ErrorDetail::isFrontendIssue).count();
// long uiCount = errors.stream().filter(ErrorDetail::isUIissue).count();
// long criticalCount = errors.stream().filter(e -> e.isCritical).count();
// 
// if (backendCount > 0) {
//     ReportEvent("fail", "🔴 Backend Issues: " + backendCount + " (API/Network problems)");
// }
// 
// if (frontendCount > 0) {
//     ReportEvent("warning", "🟡 Frontend Issues: " + frontendCount + " (JavaScript/Console problems)");
// }
// 
// if (uiCount > 0) {
//     ReportEvent("info", "🔵 UI Issues: " + uiCount + " (Element/Timeout problems)");
// }
// 
// if (criticalCount > 0) {
//     ReportEvent("fail", "⚡ Critical Issues: " + criticalCount + " (Test will likely fail)");
// }
// 
// ReportEvent("info", "=".repeat(70));
//}
//
////========== SIMPLIFIED PUBLIC METHODS ==========
//
///**
//* Quick check for backend issues (APIs returning errors)
//*/
//public boolean hasBackendIssues(String stepName) {
// try {
//     Thread.sleep(1000);
//     List<ErrorDetail> backendErrors = checkNetworkApiErrors();
//     backendErrors.addAll(checkForStatus300());
//     
//     if (!backendErrors.isEmpty()) {
//         ReportEvent("fail", "🔴 " + stepName + " - BACKEND ISSUES DETECTED");
//         for (ErrorDetail error : backendErrors) {
//             ReportEvent("info", "   • " + error.errorMessage + " (Status: " + error.statusCode + ")");
//         }
//         return true;
//     }
//     return false;
// } catch (Exception e) {
//     return false;
// }
//}
//
///**
//* Quick check for frontend issues (Console/JavaScript errors)
//*/
//public boolean hasFrontendIssues(String stepName) {
// try {
//     Thread.sleep(1000);
//     List<ErrorDetail> frontendErrors = checkConsoleErrors();
//     frontendErrors.addAll(checkJavaScriptErrors());
//     
//     if (!frontendErrors.isEmpty()) {
//         ReportEvent("warning", "🟡 " + stepName + " - FRONTEND ISSUES DETECTED");
//         for (ErrorDetail error : frontendErrors) {
//             ReportEvent("info", "   • " + truncate(error.errorMessage, 80));
//         }
//         return true;
//     }
//     return false;
// } catch (Exception e) {
//     return false;
// }
//}
//
///**
//* Quick check for any issues at all
//*/
//public boolean hasAnyIssues(String stepName) {
// return hasBackendIssues(stepName) || hasFrontendIssues(stepName);
//}
//
///**
//* Strict test assertion - FAILS test if any backend issues found
//*/
//public void assertNoBackendIssues(String stepName) {
// if (hasBackendIssues(stepName)) {
//     ReportEvent("fail", "❌ TEST FAILED: Backend issues detected in " + stepName);
//     throw new AssertionError("Backend issues detected in " + stepName);
// } else {
//     ReportEvent("pass", "✅ " + stepName + " - No backend issues");
// }
//}
//
///**
//* Check and categorize specifically after approval click
//*/
//public void checkAfterApproval(String stepName) {
// ReportEvent("info", "🔍 " + stepName + " - POST-APPROVAL COMPREHENSIVE CHECK");
// 
// // 1. Check for status 300 specifically
// boolean has300 = !checkForStatus300().isEmpty();
// if (has300) {
//     ReportEvent("fail", "🚨 CRITICAL: Status 300 found in approval API");
//     ReportEvent("fail", "   This indicates approval workflow configuration issue");
//     ReportEvent("fail", "   Test should FAIL - Approval not working correctly");
// }
// 
// // 2. Check all errors
// checkAllErrorsAndCategorize(stepName);
// 
// // 3. Special check for approval API
// checkApprovalApiSpecifically();
//}
//
///**
//* Special check for approval API
//*/
//private void checkApprovalApiSpecifically() {
// try {
//     LogEntries logs = driver.manage().logs().get(LogType.BROWSER);
//     boolean foundApprovalApi = false;
//     
//     for (LogEntry entry : logs) {
//         String message = entry.getMessage();
//         
//         if (message.contains("BUSBOKDINGAPPROVAL")) {
//             foundApprovalApi = true;
//             String status = extractStatusFromLog(message);
//             
//             if (status != null) {
//                 if (status.equals("200")) {
//                     ReportEvent("pass", "✅ Approval API returned status 200 - SUCCESS");
//                 } else if (status.equals("300")) {
//                     ReportEvent("fail", "❌ Approval API returned status 300 - FAILURE");
//                     ReportEvent("info", "   This means: Approval workflow configuration issue");
//                     ReportEvent("info", "   Action required: Check profile configuration");
//                 } else {
//                     ReportEvent("fail", "❌ Approval API returned status " + status + " - FAILURE");
//                 }
//                 
//                 // Show error message if any
//                 String errorMsg = extractErrorMessageFromLog(message);
//                 if (errorMsg != null && !errorMsg.isEmpty()) {
//                     ReportEvent("info", "   Error: " + errorMsg);
//                 }
//             }
//         }
//     }
//     
//     if (!foundApprovalApi) {
//         ReportEvent("warning", "⚠️ Approval API call not found in logs");
//     }
//     
// } catch (Exception e) {
//     ReportEvent("warning", "Error checking approval API: " + e.getMessage());
// }
//}
//
////========== UTILITY METHODS ==========
//
//private String extractUrlFromLog(String logMessage) {
// try {
//     if (logMessage.contains("http")) {
//         int start = logMessage.indexOf("http");
//         int end = logMessage.indexOf(" ", start);
//         if (end == -1) end = logMessage.indexOf("\"", start);
//         if (end == -1) end = logMessage.length();
//         if (end > start) {
//             return logMessage.substring(start, end).trim();
//         }
//     }
//     return null;
// } catch (Exception e) {
//     return null;
// }
//}


//========== CONSOLE ERROR CHECKING WITH PROPER FORMATTING ==========

/**
* CHECK CONSOLE ERRORS - With clear text formatting
* This checks browser console for all types of errors and formats them properly
*/
//public void checkConsoleErrorsWithFormat(String stepName) {
// try {
//     Thread.sleep(1000);
//     
//     // Get all console logs
//     LogEntries logs = driver.manage().logs().get(LogType.BROWSER);
//     
//     List<String> severeErrors = new ArrayList<>();
//     List<String> warnings = new ArrayList<>();
//     List<String> networkErrors = new ArrayList<>();
//     List<String> javascriptErrors = new ArrayList<>();
//     List<String> infoMessages = new ArrayList<>();
//     
//     // Process each log entry
//     for (LogEntry entry : logs) {
//         String message = entry.getMessage();
//         String level = entry.getLevel().toString();
//         
//         // Format: Remove timestamp and metadata
//         String cleanMessage = cleanConsoleMessage(message);
//         
//         // Categorize by content and level
//         if (level.equals("SEVERE") || level.equals("ERROR")) {
//             // Determine error type
//             if (isNetworkError(cleanMessage)) {
//                 networkErrors.add(formatError("NETWORK ERROR", cleanMessage));
//             } else if (isJavascriptError(cleanMessage)) {
//                 javascriptErrors.add(formatError("JAVASCRIPT ERROR", cleanMessage));
//             } else {
//                 severeErrors.add(formatError("SEVERE ERROR", cleanMessage));
//             }
//         } 
//         else if (level.equals("WARNING")) {
//             warnings.add(formatWarning(cleanMessage));
//         }
//         else if (level.equals("INFO")) {
//             // Only show important INFO messages
//             if (isImportantInfo(cleanMessage)) {
//                 infoMessages.add(formatInfo(cleanMessage));
//             }
//         }
//     }
//     
//     // Display results in formatted way
//     displayConsoleResults(stepName, severeErrors, warnings, networkErrors, javascriptErrors, infoMessages);
//     
// } catch (Exception e) {
//     ReportEvent("warning", "⚠️ Error checking console: " + e.getMessage());
// }
//}

public void checkConsoleErrorsWithFormat(String stepName) {
    // Declare variables OUTSIDE try block
    List<String> severeErrors = new ArrayList<>();
    List<String> warnings = new ArrayList<>();
    List<String> networkErrors = new ArrayList<>();
    List<String> javascriptErrors = new ArrayList<>();
    List<String> infoMessages = new ArrayList<>();
    
    try {
        Thread.sleep(1000);
        
        // Get all console logs
        LogEntries logs = driver.manage().logs().get(LogType.BROWSER);
        
        // Process each log entry
        for (LogEntry entry : logs) {
            String message = entry.getMessage();
            String level = entry.getLevel().toString();
            
            // Format: Remove timestamp and metadata
            String cleanMessage = cleanConsoleMessage(message);
            
            // Categorize by content and level
            if (level.equals("SEVERE") || level.equals("ERROR")) {
                // Determine error type
                if (isNetworkError(cleanMessage)) {
                    networkErrors.add(formatError("NETWORK ERROR", cleanMessage));
                } else if (isJavascriptError(cleanMessage)) {
                    javascriptErrors.add(formatError("JAVASCRIPT ERROR", cleanMessage));
                } else {
                    severeErrors.add(formatError("SEVERE ERROR", cleanMessage));
                }
            } 
            else if (level.equals("WARNING")) {
                warnings.add(formatWarning(cleanMessage));
            }
            else if (level.equals("INFO")) {
                // Only show important INFO messages
                if (isImportantInfo(cleanMessage)) {
                    infoMessages.add(formatInfo(cleanMessage));
                }
            }
        }
        
    } catch (Exception e) {
        ReportEvent("warning", "⚠️ Error checking console: " + e.getMessage());
    }
    
    // Now display results - variables are in scope
    displayConsoleResults(stepName, severeErrors, warnings, networkErrors, javascriptErrors, infoMessages);
}

/**
* Clean console message - remove timestamps and metadata
*/
private String cleanConsoleMessage(String rawMessage) {
 try {
     // Remove WebDriver timestamp and metadata
     if (rawMessage.contains("[")) {
         int bracketStart = rawMessage.indexOf("[");
         int bracketEnd = rawMessage.indexOf("]", bracketStart);
         if (bracketEnd > bracketStart) {
             // Remove timestamp like [1702291234.123][INFO]:
             if (rawMessage.charAt(bracketStart + 1) >= '0' && rawMessage.charAt(bracketStart + 1) <= '9') {
                 rawMessage = rawMessage.substring(bracketEnd + 2).trim();
             }
         }
     }
     
     // Remove common prefixes
     String[] prefixes = {
         "http://", "https://", 
         "console.", "Uncaught ",
         "Failed to load", "404", "500",
         "SyntaxError:", "ReferenceError:", "TypeError:"
     };
     
     for (String prefix : prefixes) {
         if (rawMessage.contains(prefix)) {
             int idx = rawMessage.indexOf(prefix);
             if (idx > 0 && idx < 50) {
                 rawMessage = rawMessage.substring(idx);
                 break;
             }
         }
     }
     
     // Limit length
     if (rawMessage.length() > 300) {
         rawMessage = rawMessage.substring(0, 297) + "...";
     }
     
     return rawMessage.trim();
 } catch (Exception e) {
     return rawMessage;
 }
}

/**
* Check if message is a network error
*/
private boolean isNetworkError(String message) {
 String lower = message.toLowerCase();
 return lower.contains("404") || 
        lower.contains("500") || 
        lower.contains("failed to load") ||
        lower.contains("network error") ||
        lower.contains("http error") ||
        lower.contains("status:") ||
        lower.contains("statuscode:");
}

/**
* Check if message is a JavaScript error
*/
private boolean isJavascriptError(String message) {
 String lower = message.toLowerCase();
 return lower.contains("uncaught") ||
        lower.contains("syntaxerror") ||
        lower.contains("referenceerror") ||
        lower.contains("typeerror") ||
        lower.contains("is not a function") ||
        lower.contains("cannot read property") ||
        lower.contains("undefined") ||
        lower.contains("null is not an object");
}

/**
* Check if info message is important
*/
private boolean isImportantInfo(String message) {
 String lower = message.toLowerCase();
 return lower.contains("api") ||
        lower.contains("dispatch") ||
        lower.contains("error") ||
        lower.contains("warning") ||
        lower.contains("failed") ||
        lower.contains("success") ||
        lower.contains("status");
}

//========== FORMATTING METHODS ==========

/**
* Format error message
*/
private String formatError(String type, String message) {
 return "[" + type + "] " + message;
}

/**
* Format warning message
*/
private String formatWarning(String message) {
 return "[WARNING] " + message;
}

/**
* Format info message
*/
private String formatInfo(String message) {
 return "[INFO] " + message;
}

/**
* Display all console results in formatted way
*/
//private void displayConsoleResults(String stepName, 
//                                List<String> severeErrors,
//                                List<String> warnings,
//                                List<String> networkErrors,
//                                List<String> javascriptErrors,
//                                List<String> infoMessages) {
// 
// int totalErrors = severeErrors.size() + networkErrors.size() + javascriptErrors.size();
// 
// //ReportEvent("info", "📋 CONSOLE REPORT for: " + stepName);
// //ReportEvent("info", "=".repeat(60));
// 
// // Display SEVERE errors
// if (!severeErrors.isEmpty()) {
//     ReportEvent("fail", "🔴 SEVERE ERRORS (" + severeErrors.size() + ")");
//     for (String error : severeErrors) {
//         ReportEvent("fail", "   ❌ " + error);
//     }
//    // ReportEvent("info", "");
// }
// 
// // Display NETWORK errors
// if (!networkErrors.isEmpty()) {
//     ReportEvent("fail", "🌐 NETWORK ERRORS (" + networkErrors.size() + ")");
//     for (String error : networkErrors) {
//         ReportEvent("fail", "   🚫 " + error);
//     }
//    // ReportEvent("info", "");
// }
// 
// // Display JAVASCRIPT errors
// if (!javascriptErrors.isEmpty()) {
//     ReportEvent("fail", "💻 JAVASCRIPT ERRORS (" + javascriptErrors.size() + ")");
//     for (String error : javascriptErrors) {
//         ReportEvent("fail", "   📜 " + error);
//     }
//    // ReportEvent("info", "");
// }
// 
// // Display WARNINGS
// if (!warnings.isEmpty()) {
//     ReportEvent("warning", "⚠️  WARNINGS (" + warnings.size() + ")");
//     for (String warning : warnings) {
//         ReportEvent("warning", "   ⚠ " + warning);
//     }
//    // ReportEvent("info", "");
// }
// 
// // Display INFO messages
// if (!infoMessages.isEmpty()) {
//   //  ReportEvent("info", "  IMPORTANT INFO (" + infoMessages.size() + ")");
//     for (String info : infoMessages) {
//         ReportEvent("info", "    " + info);
//     }
// }
// 
// // Summary
//
// ReportEvent("info", "CONSOLE SUMMARY");
// 
// if (totalErrors > 0) {
//     ReportEvent("fail", "❌ TOTAL ERRORS FOUND: " + totalErrors);
//     if (!networkErrors.isEmpty()) {
//         ReportEvent("fail", "   • Network/API Issues: " + networkErrors.size() + " (BACKEND PROBLEM)");
//     }
//     if (!javascriptErrors.isEmpty()) {
//         ReportEvent("warning", "   • JavaScript Issues: " + javascriptErrors.size() + " (FRONTEND PROBLEM)");
//     }
//     if (!severeErrors.isEmpty()) {
//         ReportEvent("fail", "   • Severe Issues: " + severeErrors.size() + " (CRITICAL)");
//     }
// } else {
//     ReportEvent("pass", "✅ No console errors found");
// }
// 
//}

/**
 * Display all console results in formatted way with CLEAR messaging
 */
private void displayConsoleResults(String stepName, 
                                   List<String> severeErrors,
                                   List<String> warnings,
                                   List<String> networkErrors,
                                   List<String> javascriptErrors,
                                   List<String> infoMessages) {
    
    int totalErrors = severeErrors.size() + networkErrors.size() + javascriptErrors.size();
    
    ReportEvent("info", "📋 CONSOLE REPORT: " + stepName);
    
    // Display SEVERE errors
    if (!severeErrors.isEmpty()) {
        ReportEvent("fail", "🔴 SEVERE/CONSOLE ERRORS (" + severeErrors.size() + ")");
        for (String error : severeErrors) {
            ReportEvent("fail", "   ❌ " + error);
        }
    }
    
    // Display NETWORK errors
    if (!networkErrors.isEmpty()) {
        ReportEvent("fail", "🌐 NETWORK/API ERRORS (" + networkErrors.size() + ")");
        ReportEvent("fail", "   ⚠️ BACKEND ISSUE DETECTED - APIs returning errors");
        for (String error : networkErrors) {
            ReportEvent("fail", "   🚫 " + error);
        }
    }
    
    // Display JAVASCRIPT errors
    if (!javascriptErrors.isEmpty()) {
        ReportEvent("warning", "💻 JAVASCRIPT/FRONTEND ERRORS (" + javascriptErrors.size() + ")");
        ReportEvent("warning", "   ⚠️ FRONTEND ISSUE DETECTED - JavaScript errors");
        for (String error : javascriptErrors) {
            ReportEvent("warning", "   📜 " + error);
        }
    }
    
    // Display WARNINGS
    if (!warnings.isEmpty()) {
        ReportEvent("warning", "⚠️  WARNINGS (" + warnings.size() + ")");
        for (String warning : warnings) {
            ReportEvent("warning", "   ⚠ " + warning);
        }
    }
    
    // ========== CLEAR SUMMARY SECTION ==========
    ReportEvent("info", "🎯 CLEAR STATUS SUMMARY");
    
    // 1. NETWORK/BACKEND STATUS
    if (networkErrors.isEmpty()) {
        ReportEvent("pass", "✅ BACKEND STATUS: No API/Network errors found");
        ReportEvent("pass", "   • All APIs returned success codes (200-299)");
        ReportEvent("pass", "   • No HTTP errors (404, 500, 300, etc.)");
        ReportEvent("pass", "   • Backend services are working correctly");
    } else {
        ReportEvent("fail", "❌ BACKEND STATUS: API/Network errors detected");
        ReportEvent("fail", "   • " + networkErrors.size() + " API(s) returned errors");
        ReportEvent("fail", "   • Check backend services and API configurations");
    }
    
    // 2. FRONTEND/JAVASCRIPT STATUS
    if (javascriptErrors.isEmpty()) {
        ReportEvent("pass", "✅ FRONTEND STATUS: No JavaScript errors");
        ReportEvent("pass", "   • All JavaScript code executed successfully");
        ReportEvent("pass", "   • No uncaught exceptions or reference errors");
    } else {
        ReportEvent("warning", "⚠️ FRONTEND STATUS: JavaScript errors found");
        ReportEvent("warning", "   • " + javascriptErrors.size() + " JavaScript error(s)");
        ReportEvent("warning", "   • Check frontend code and browser compatibility");
    }
    
    // 3. OVERALL STATUS
    if (totalErrors == 0) {
        ReportEvent("pass", "✅ OVERALL STATUS: ALL CLEAR - No errors in console");
        ReportEvent("pass", "   • Backend APIs: ✓ Working");
        ReportEvent("pass", "   • Frontend JavaScript: ✓ Working");
        ReportEvent("pass", "   • Console: ✓ Clean");
    } else {
        ReportEvent("fail", "❌ OVERALL STATUS: " + totalErrors + " error(s) detected");
        
        if (!networkErrors.isEmpty()) {
            ReportEvent("fail", "   • CRITICAL: Backend/API errors - Test should FAIL");
        }
        
        if (!javascriptErrors.isEmpty()) {
            ReportEvent("warning", "   • WARNING: Frontend errors - UI may have issues");
        }
        
        if (!severeErrors.isEmpty()) {
            ReportEvent("fail", "   • CRITICAL: Severe console errors");
        }
    }
    
    // 4. DETAILED BREAKDOWN (if any errors)
    if (totalErrors > 0) {
        ReportEvent("info", "📊 DETAILED BREAKDOWN:");
        ReportEvent("info", "   Total Errors: " + totalErrors);
        
        if (!networkErrors.isEmpty()) {
            ReportEvent("fail", "   • Backend/API Errors: " + networkErrors.size());
        } else {
            ReportEvent("pass", "   • Backend/API Errors: 0 ✓");
        }
        
        if (!javascriptErrors.isEmpty()) {
            ReportEvent("warning", "   • Frontend/JS Errors: " + javascriptErrors.size());
        } else {
            ReportEvent("pass", "   • Frontend/JS Errors: 0 ✓");
        }
        
        if (!severeErrors.isEmpty()) {
            ReportEvent("fail", "   • Severe Errors: " + severeErrors.size());
        } else {
            ReportEvent("pass", "   • Severe Errors: 0 ✓");
        }
    }
    
}

//========== SIMPLE CONSOLE CHECK METHODS ==========

/**
* Quick console check - returns true if any errors found
*/
public boolean hasConsoleErrors(String stepName) {
 try {
     Thread.sleep(1000);
     LogEntries logs = driver.manage().logs().get(LogType.BROWSER);
     
     boolean hasErrors = false;
     
     for (LogEntry entry : logs) {
         String level = entry.getLevel().toString();
         String message = entry.getMessage();
         
         if (level.equals("SEVERE") || level.equals("ERROR")) {
             if (!hasErrors) {
                 ReportEvent("fail", "🔴 " + stepName + " - Console errors found:");
                 hasErrors = true;
             }
             
             String cleanMsg = cleanConsoleMessage(message);
             ReportEvent("fail", "   ❌ " + truncate(cleanMsg, 120));
         }
     }
     
     if (!hasErrors) {
         ReportEvent("pass", "✅ " + stepName + " - No console errors");
     }
     
     return hasErrors;
     
 } catch (Exception e) {
     return false;
 }
}

/**
* Check console for specific error patterns
*/
public void checkConsoleForPattern(String stepName, String pattern) {
 try {
     Thread.sleep(1000);
     LogEntries logs = driver.manage().logs().get(LogType.BROWSER);
     boolean found = false;
     
     for (LogEntry entry : logs) {
         String message = entry.getMessage();
         if (message.contains(pattern)) {
             if (!found) {
                 ReportEvent("info", "🔍 " + stepName + " - Searching for: " + pattern);
                 found = true;
             }
             
             String level = entry.getLevel().toString();
             String cleanMsg = cleanConsoleMessage(message);
             
             String status = level.equals("SEVERE") ? "❌ SEVERE" : 
                            level.equals("ERROR") ? "❌ ERROR" : 
                            level.equals("WARNING") ? "⚠️ WARNING" : "ℹ️ INFO";
             
             ReportEvent("info", status + ": " + truncate(cleanMsg, 150));
         }
     }
     
     if (!found) {
         ReportEvent("info", "✅ " + stepName + " - Pattern '" + pattern + "' not found in console");
     }
     
 } catch (Exception e) {
     ReportEvent("warning", "Error checking console pattern: " + e.getMessage());
 }
}

//========== SPECIFIC API CONSOLE CHECKING ==========

/**
* Check console for Approval API specifically
*/
public void checkConsoleForApprovalApi(String stepName) {
 try {
     Thread.sleep(2000);
     ReportEvent("info", "🔍 " + stepName + " - Checking Console for Approval API");
     
     LogEntries logs = driver.manage().logs().get(LogType.BROWSER);
     boolean foundApprovalApi = false;
     
     for (LogEntry entry : logs) {
         String message = entry.getMessage();
         
         // Look for approval API in console
         if (message.contains("BUSBOKDINGAPPROVAL") || 
             (message.contains("dispatch.jsp") && message.contains("APPROVAL"))) {
             
             foundApprovalApi = true;
             String level = entry.getLevel().toString();
             String cleanMsg = cleanConsoleMessage(message);
             
             ReportEvent("info", "📡 APPROVAL API FOUND in console:");
             
             // Show status
             String status = extractStatusFromLog(message);
             if (status != null) {
                 if (status.equals("200")) {
                     ReportEvent("pass", "   ✅ Status: 200 (SUCCESS)");
                 } else if (status.equals("300")) {
                     ReportEvent("fail", "   ❌ Status: 300 (FAILURE - Approval workflow issue)");
                 } else {
                     ReportEvent("fail", "   ❌ Status: " + status + " (FAILURE)");
                 }
             }
             
             // Show error message
             String errorMsg = extractErrorMessageFromLog(message);
             if (errorMsg != null && !errorMsg.isEmpty()) {
                 ReportEvent("info", "   📝 Error Message: " + errorMsg);
             }
             
             // Show snippet of response
             if (message.contains("{")) {
                 int start = Math.max(0, message.indexOf("{"));
                 int end = Math.min(start + 200, message.length());
                 String snippet = message.substring(start, end);
                 ReportEvent("info", "   📄 Response snippet: " + snippet);
             }
             
             ReportEvent("info", "   🔍 Full log level: " + level);
             ReportEvent("info", "   📋 Clean message: " + cleanMsg);
         }
     }
     
     if (!foundApprovalApi) {
         ReportEvent("warning", "⚠️ Approval API not found in console logs");
        /* ReportEvent("info", "Possible reasons:");
         ReportEvent("info", "1. API call wasn't made");
         ReportEvent("info", "2. Console logging disabled");
         ReportEvent("info", "3. Page reloaded after API call");?*/
     }
     
 } catch (Exception e) {
     ReportEvent("warning", "Error checking approval API in console: " + e.getMessage());
 }
}
   

//-----------------------------------devtools concept-----------------------------

//========== DEVTOOLS NETWORK MONITORING - Get Response Body ==========

//========== API RESPONSE MONITORING - Shows Success & Failure Responses ==========

/**
* MONITOR ALL API RESPONSES - Shows response bodies for ALL APIs
*/
public void monitorAllApiResponses(String stepName) {
 try {
     Thread.sleep(2000);
     ReportEvent("info", "📡 " + stepName + " - MONITORING ALL API RESPONSES");
     
     // Enable response monitoring
     String script = 
         "// Enable response monitoring\n" +
         "console.log('[API-RESP-MONITOR] Starting response monitoring');\n" +
         "\n" +
         "// Store responses\n" +
         "window._capturedResponses = [];\n" +
         "\n" +
         "// Capture fetch responses\n" +
         "const originalFetch = window.fetch;\n" +
         "window.fetch = function(...args) {\n" +
         "    const url = args[0];\n" +
         "    const method = args[1]?.method || 'GET';\n" +
         "    \n" +
         "    return originalFetch.apply(this, args)\n" +
         "        .then(response => {\n" +
         "            const clone = response.clone();\n" +
         "            return clone.text().then(responseBody => {\n" +
         "                // Store response\n" +
         "                const responseData = {\n" +
         "                    url: url,\n" +
         "                    method: method,\n" +
         "                    status: response.status,\n" +
         "                    timestamp: new Date().toLocaleTimeString(),\n" +
         "                    responseBody: responseBody\n" +
         "                };\n" +
         "                window._capturedResponses.push(responseData);\n" +
         "                \n" +
         "                // Log response\n" +
         "                const isSuccess = response.status >= 200 && response.status < 300;\n" +
         "                console.log('[API-RESPONSE] ' + response.status + ' ' + method + ' ' + url);\n" +
         "                \n" +
         "                return response;\n" +
         "            });\n" +
         "        });\n" +
         "};\n" +
         "\n" +
         "console.log('[API-RESP-MONITOR] Response monitoring enabled');";
     
     js.executeScript(script);
     
     // Wait for responses
     Thread.sleep(3000);
     
     // Get captured responses
     String getResponsesScript = 
         "if (!window._capturedResponses || window._capturedResponses.length === 0) return [];\n" +
         "return window._capturedResponses;";
     
     @SuppressWarnings("unchecked")
     List<Map<String, Object>> responseData = (List<Map<String, Object>>) js.executeScript(getResponsesScript);
     
     if (responseData == null || responseData.isEmpty()) {
         ReportEvent("info", "⚠️ No API responses captured");
         return;
     }
     
     // Display all responses
     displayAllApiResponses(stepName, responseData);
     
 } catch (Exception e) {
     ReportEvent("warning", "⚠️ Error monitoring API responses: " + e.getMessage());
 }
}

/**
* Display all API responses
*/
private void displayAllApiResponses(String stepName, List<Map<String, Object>> responseData) {
 int totalApis = responseData.size();
 int successCount = 0;
 int failedCount = 0;
 
 ReportEvent("info", "📊 " + stepName + " - API RESPONSE ANALYSIS");
 ReportEvent("info", "   Total APIs captured: " + totalApis);
 
 // Process each response
 for (Map<String, Object> data : responseData) {
     String url = (String) data.get("url");
     String method = (String) data.get("method");
     String status = data.get("status") != null ? data.get("status").toString() : "UNKNOWN";
     String responseBody = (String) data.get("responseBody");
     String timestamp = (String) data.get("timestamp");
     
     // Check if successful
     boolean isSuccess = false;
     try {
         int statusCode = Integer.parseInt(status);
         isSuccess = (statusCode >= 200 && statusCode < 300);
     } catch (Exception e) {
         isSuccess = false;
     }
     
     if (isSuccess) {
         successCount++;
     } else {
         failedCount++;
     }
     
     // Get API name
     String apiName = getApiNameFromUrl(url);
     
     // Display response
     displayApiResponse(apiName, url, method, status, responseBody, timestamp, isSuccess);
 }
 
 // Summary
 ReportEvent("info", "📈 API RESPONSE SUMMARY:");
 ReportEvent("pass", "   ✅ Successful: " + successCount + " API(s)");
 
 if (failedCount > 0) {
     ReportEvent("fail", "   ❌ Failed: " + failedCount + " API(s)");
 } else {
     ReportEvent("pass", "   ✅ Failed: 0 API(s)");
 }
 
 if (failedCount == 0) {
     ReportEvent("pass", "🎉 ALL API RESPONSES SUCCESSFUL!");
 } else {
     ReportEvent("fail", "⚠️ " + failedCount + " API RESPONSE(S) FAILED");
 }
 
 ReportEvent("info", "=".repeat(60));
}

/**
* Display single API response
*/
private void displayApiResponse(String apiName, String url, String method, String status, 
                            String responseBody, String timestamp, boolean isSuccess) {
 
 String statusIcon = isSuccess ? "✅" : "❌";
 String statusText = isSuccess ? "SUCCESS" : "FAILED";
 
 ReportEvent("info", statusIcon + " " + apiName + " - " + statusText);
 ReportEvent(isSuccess ? "pass" : "fail", "   Status: " + status);
 ReportEvent("info", "   Method: " + method);
 ReportEvent("info", "   Time: " + timestamp);
 
 if (url != null) {
     String shortUrl = url.length() > 60 ? url.substring(0, 60) + "..." : url;
     ReportEvent("info", "   URL: " + shortUrl);
 }
 
 if (responseBody != null && !responseBody.isEmpty()) {
     // Show response summary
     String summary = getResponseSummary(responseBody, isSuccess);
     ReportEvent("info", "   Response: " + summary);
     
     // Show response snippet
     if (!isSuccess || apiName.contains("Approval") || apiName.contains("Login")) {
         String snippet = responseBody.length() > 150 ? 
             responseBody.substring(0, 150) + "..." : responseBody;
         ReportEvent("info", "   📄 Response Body: " + snippet);
     }
     
     // Show success/failure details
     if (isSuccess) {
         showSuccessDetails(responseBody);
     } else {
         showFailureDetails(responseBody, status);
     }
 }
 
 ReportEvent("info", "   " + "-".repeat(40));
}

/**
* Get response summary
*/
private String getResponseSummary(String responseBody, boolean isSuccess) {
 if (responseBody == null || responseBody.isEmpty()) {
     return "Empty response";
 }
 
 if (isSuccess) {
     if (responseBody.contains("\"status\":\"OK\"")) return "Success - OK";
     if (responseBody.contains("\"success\":true")) return "Success - true";
     if (responseBody.contains("\"message\":")) {
         // Try to extract message
         try {
             Pattern pattern = Pattern.compile("\"message\":\"([^\"]+)\"");
             Matcher matcher = pattern.matcher(responseBody);
             if (matcher.find()) {
                 return "Success: " + matcher.group(1);
             }
         } catch (Exception e) {
             // Ignore
         }
     }
     return "Success response";
 } else {
     if (responseBody.contains("\"status\":\"NOK\"")) return "Failure - NOK";
     if (responseBody.contains("\"success\":false")) return "Failure - false";
     if (responseBody.contains("\"statuscode\":300")) return "Status 300 - Issue";
     if (responseBody.contains("\"message\":")) {
         // Try to extract error message
         try {
             Pattern pattern = Pattern.compile("\"message\":\"([^\"]+)\"");
             Matcher matcher = pattern.matcher(responseBody);
             if (matcher.find()) {
                 return "Error: " + matcher.group(1);
             }
         } catch (Exception e) {
             // Ignore
         }
     }
     return "Error response";
 }
}

/**
* Show success details
*/
private void showSuccessDetails(String responseBody) {
 try {
     if (responseBody.contains("\"status\":\"OK\"")) {
         ReportEvent("pass", "   ✓ Status: OK");
     }
     if (responseBody.contains("\"success\":true")) {
         ReportEvent("pass", "   ✓ Success: true");
     }
     if (responseBody.contains("\"data\":")) {
         ReportEvent("pass", "   ✓ Contains data");
     }
 } catch (Exception e) {
     // Ignore
 }
}

/**
* Show failure details
*/
private void showFailureDetails(String responseBody, String status) {
 try {
     // Extract error message
     if (responseBody.contains("\"message\":")) {
         Pattern pattern = Pattern.compile("\"message\":\"([^\"]+)\"");
         Matcher matcher = pattern.matcher(responseBody);
         if (matcher.find()) {
             ReportEvent("fail", "   ✗ Error: " + matcher.group(1));
         }
     }
     
     // Special handling for status 300
     if ("300".equals(status)) {
         ReportEvent("fail", "   ⚠️ Status 300: Approval workflow issue");
     }
 } catch (Exception e) {
     // Ignore
 }
}

/**
* Get API name from URL
*/
private String getApiNameFromUrl(String url) {
 if (url == null) return "Unknown API";
 
 String urlLower = url.toLowerCase();
 if (urlLower.contains("busbokdingapproval")) return "Approval API";
 if (urlLower.contains("login")) return "Login API";
 if (urlLower.contains("expr") || urlLower.contains("search")) return "Search API";
 if (urlLower.contains("getcorporatetravellers")) return "Traveller API";
 if (urlLower.contains("getcorporatestinfo")) return "Corporate API";
 if (urlLower.contains("dispatch.jsp")) return "Service API";
 return "API Call";
}

/**
* CHECK SPECIFIC API WITH RESPONSE - Simple version
*/
public void checkSpecificApiResponse(String stepName, String apiKeyword) {
 try {
     Thread.sleep(2000);
     ReportEvent("info", "🔍 " + stepName + " - Checking API: " + apiKeyword);
     
     // First enable monitoring
     String enableScript = 
         "// Enable response monitoring\n" +
         "console.log('[API-CHECK] Monitoring enabled for: " + apiKeyword + "');\n" +
         "\n" +
         "// Store target API response\n" +
         "window._targetApiResponse = null;\n" +
         "\n" +
         "// Capture fetch responses\n" +
         "const originalFetch = window.fetch;\n" +
         "window.fetch = function(...args) {\n" +
         "    const url = args[0];\n" +
         "    \n" +
         "    return originalFetch.apply(this, args)\n" +
         "        .then(response => {\n" +
         "            // Check if this is our target API\n" +
         "            if (url.includes('" + apiKeyword + "')) {\n" +
         "                const clone = response.clone();\n" +
         "                return clone.text().then(responseBody => {\n" +
         "                    // Store target response\n" +
         "                    window._targetApiResponse = {\n" +
         "                        url: url,\n" +
         "                        status: response.status,\n" +
         "                        responseBody: responseBody,\n" +
         "                        timestamp: new Date().toLocaleTimeString()\n" +
         "                    };\n" +
         "                    \n" +
         "                    // Log it\n" +
         "                    console.log('[TARGET-API] ' + response.status + ' ' + url);\n" +
         "                    \n" +
         "                    return response;\n" +
         "                });\n" +
         "            }\n" +
         "            return response;\n" +
         "        });\n" +
         "};";
     
     js.executeScript(enableScript);
     
     // Wait for API call
     Thread.sleep(3000);
     
     // Get the response
     String getResponseScript = 
         "if (!window._targetApiResponse) return null;\n" +
         "return window._targetApiResponse;";
     
     @SuppressWarnings("unchecked")
     Map<String, Object> responseData = (Map<String, Object>) js.executeScript(getResponseScript);
     
     if (responseData == null) {
         ReportEvent("warning", "⚠️ API '" + apiKeyword + "' not found or not called");
         return;
     }
     
     String url = (String) responseData.get("url");
     String status = responseData.get("status").toString();
     String responseBody = (String) responseData.get("responseBody");
     String timestamp = (String) responseData.get("timestamp");
     
     // Check if successful
     boolean isSuccess = false;
     try {
         int statusCode = Integer.parseInt(status);
         isSuccess = (statusCode >= 200 && statusCode < 300);
     } catch (Exception e) {
         isSuccess = false;
     }
     
     // Display result
     ReportEvent("info", "📡 API FOUND: " + getApiNameFromUrl(url));
     
     if (isSuccess) {
         ReportEvent("pass", "✅ STATUS: " + status + " - SUCCESS");
         ReportEvent("pass", "   Time: " + timestamp);
         
         if (responseBody != null && !responseBody.isEmpty()) {
             // Show success indicators
             if (responseBody.contains("\"status\":\"OK\"")) {
                 ReportEvent("pass", "   ✓ Response: Status OK");
             }
             if (responseBody.contains("\"success\":true")) {
                 ReportEvent("pass", "   ✓ Response: Success true");
             }
             
             // Show response snippet
             String snippet = responseBody.length() > 100 ? 
                 responseBody.substring(0, 100) + "..." : responseBody;
             ReportEvent("info", "   📄 Response: " + snippet);
         }
     } else {
         ReportEvent("fail", "❌ STATUS: " + status + " - FAILURE");
         ReportEvent("fail", "   Time: " + timestamp);
         
         if (responseBody != null && !responseBody.isEmpty()) {
             // Extract error message
             if (responseBody.contains("\"message\":")) {
                 Pattern pattern = Pattern.compile("\"message\":\"([^\"]+)\"");
                 Matcher matcher = pattern.matcher(responseBody);
                 if (matcher.find()) {
                     ReportEvent("fail", "   ✗ Error: " + matcher.group(1));
                 }
             }
             
             // Show response
             String response = responseBody.length() > 200 ? 
                 responseBody.substring(0, 200) + "..." : responseBody;
             ReportEvent("info", "   📄 Response: " + response);
             
             // Special for status 300
             if ("300".equals(status)) {
                 ReportEvent("fail", "   ⚠️ IMPORTANT: Status 300 indicates approval workflow issue!");
             }
         }
     }
     
 } catch (Exception e) {
     ReportEvent("warning", "Error checking API: " + e.getMessage());
 }
}




} // End of class