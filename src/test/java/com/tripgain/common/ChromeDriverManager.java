package com.tripgain.common;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import java.util.logging.Level;

public class ChromeDriverManager {
    
    public static WebDriver createChromeDriverWithDevTools() {
        System.out.println("🚀 Creating ChromeDriver with DevTools support...");
        
        // Set ChromeDriver path if not in PATH
        // System.setProperty("webdriver.chrome.driver", "C:/path/to/chromedriver.exe");
        
        ChromeOptions options = new ChromeOptions();
        
        // Add arguments for better performance
        options.addArguments("--start-maximized");
        options.addArguments("--disable-notifications");
        options.addArguments("--disable-popup-blocking");
        options.addArguments("--disable-extensions");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--remote-allow-origins=*");
        
        // Enable browser logging
        LoggingPreferences logPrefs = new LoggingPreferences();
        logPrefs.enable(LogType.BROWSER, Level.ALL);
        logPrefs.enable(LogType.PERFORMANCE, Level.ALL);
        options.setCapability("goog:loggingPrefs", logPrefs);
        
        // Enable DevTools
        options.setExperimentalOption("w3c", true);
        
        // Create driver
        ChromeDriver driver = new ChromeDriver(options);
        
        System.out.println("✅ ChromeDriver created successfully");
        return driver;
    }
}