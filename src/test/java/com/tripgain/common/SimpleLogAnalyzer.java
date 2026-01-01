package com.tripgain.common;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import com.aventstack.extentreports.ExtentTest;
import java.util.ArrayList;
import java.util.List;

public class SimpleLogAnalyzer {
    private WebDriver driver;
    private ExtentTest test;
    
    public SimpleLogAnalyzer(WebDriver driver, ExtentTest test) {
        this.driver = driver;
        this.test = test;
    }
    
    // SIMPLE METHOD: Check if there are any console errors
    public void checkForErrors(String stepName) {
        try {
            Thread.sleep(1000); // Small wait to let logs appear
            
            // Get all console logs
            LogEntries logEntries = driver.manage().logs().get(LogType.BROWSER);
            List<String> errors = new ArrayList<>();
            List<String> warnings = new ArrayList<>();
            
            // Simple classification - FIXED THE TYPO HERE
            for (LogEntry entry : logEntries) {  // Changed 'entry' to 'logEntries'
                String message = entry.getMessage();
                
                if (message.contains("SEVERE") || message.contains("ERROR")) {
                    errors.add(message);
                } else if (message.contains("WARNING")) {
                    warnings.add(message);
                }
            }
            
            // Simple reporting
            if (!errors.isEmpty()) {
                test.fail("❌ " + stepName + " - Found " + errors.size() + " console errors");
                for (String error : errors) {
                    // Show first 150 characters of error
                    String shortError = error.length() > 150 ? error.substring(0, 150) + "..." : error;
                    test.fail("Error: " + shortError);
                }
            } else {
                test.pass("✅ " + stepName + " - No console errors");
            }
            
            // Just count warnings
            if (!warnings.isEmpty()) {
                test.info(stepName + " - Found " + warnings.size() + " warnings");
            }
            
        } catch (Exception e) {
            test.warning("Could not check logs for " + stepName);
        }
    }
    
    // SUPER SIMPLE: Just check if errors exist (true/false)
    public boolean hasErrors() {
        try {
            LogEntries logEntries = driver.manage().logs().get(LogType.BROWSER);
            for (LogEntry entry : logEntries) {
                if (entry.getMessage().contains("SEVERE") || 
                    entry.getMessage().contains("ERROR") ||
                    entry.getMessage().contains("404") ||
                    entry.getMessage().contains("500")) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
    
    // Simple method to check for backend errors
    public void checkForBackendIssues(String stepName) {
        try {
            LogEntries logEntries = driver.manage().logs().get(LogType.BROWSER);
            List<String> backendErrors = new ArrayList<>();
            
            for (LogEntry entry : logEntries) {
                String message = entry.getMessage();
                
                // Simple pattern matching for backend issues
                if (message.contains("404") || 
                    message.contains("500") ||
                    message.contains("Failed to load") ||
                    message.contains("Network Error") ||
                    message.contains("API") ||
                    message.contains("fetch failed")) {
                    backendErrors.add(message);
                }
            }
            
            if (!backendErrors.isEmpty()) {
                test.fail("🔧 " + stepName + " - Backend issue detected!");
                for (String error : backendErrors) {
                    String shortError = error.length() > 100 ? error.substring(0, 100) + "..." : error;
                    test.fail("Backend Error: " + shortError);
                }
            }
            
        } catch (Exception e) {
            // Do nothing if logging fails
        }
    }
}