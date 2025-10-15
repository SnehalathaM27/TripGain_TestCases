package com.tripgain.collectionofpages;

import java.time.Duration;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.tripgain.common.Log;
import com.tripgain.common.ScreenShots;

public class NewDesign_Buses_SerachPage {
	WebDriver driver;

	public NewDesign_Buses_SerachPage (WebDriver driver)
	{
		PageFactory.initElements(driver, this);
		this.driver=driver;
	}


	//Method to click on hotels 
	
	public void clickOnBuses() throws InterruptedException {
		Thread.sleep(2000);
		driver.findElement(By.xpath("//span[text()='Bus']")).click();
	}


	
	

	public void enterfromLocForBuses(String city,Log Log) {
	    try {
	        // Optional: adjust zoom
	        JavascriptExecutor js = (JavascriptExecutor) driver;
	        js.executeScript("document.body.style.zoom='80%'");

	        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(70));
	        WebElement searchField = wait.until(ExpectedConditions.elementToBeClickable(
	            By.xpath("//input[@id='from']")));

	        searchField.clear();
	        searchField.sendKeys(city);
	        Thread.sleep(1000);

	        List<WebElement> listOfProperty = wait.until(
	            ExpectedConditions.visibilityOfAllElementsLocatedBy(
	                By.xpath("(//div[contains(@class,'tg-select__option')])[1]"))
	        );

	        if (!listOfProperty.isEmpty()) {
	            Thread.sleep(2000);
	            WebElement firstOption = listOfProperty.get(0);
	            String selectedLocation = firstOption.getText();
	            firstOption.click();
	            Thread.sleep(1000);

	            // Log the selected location
	            Log.ReportEvent("PASS", "Selected from Location: " + selectedLocation);
	        } else {
	            Log.ReportEvent("FAIL", "No options found in the dropdown.");
	        }
	    } catch (Exception e) {
	        Log.ReportEvent("ERROR", "Failed to select from location: " + e.getMessage());
	    }
	}


	
	public void enterToLocForBuses(String city,Log Log) {
	    try {
	        // Optional: adjust zoom
	        JavascriptExecutor js = (JavascriptExecutor) driver;
	        js.executeScript("document.body.style.zoom='80%'");

	        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(70));
	        WebElement searchField = wait.until(ExpectedConditions.elementToBeClickable(
	            By.xpath("//input[@id='to']")));

	        searchField.clear();
	        searchField.sendKeys(city);
	        Thread.sleep(1000);

	        List<WebElement> listOfProperty = wait.until(
	            ExpectedConditions.visibilityOfAllElementsLocatedBy(
	                By.xpath("(//div[contains(@class,'tg-select__option')])[1]"))
	        );

	        if (!listOfProperty.isEmpty()) {
	            Thread.sleep(2000);
	            WebElement firstOption = listOfProperty.get(0);
	            String selectedLocation = firstOption.getText();
	            firstOption.click();
	            Thread.sleep(1000);

	            // Log the selected location
	            Log.ReportEvent("PASS", "Selected To Location: " + selectedLocation);
	        } else {
	            Log.ReportEvent("FAIL", "No options found in the dropdown.");
	        }
	    } catch (Exception e) {
	        Log.ReportEvent("ERROR", "Failed to select To location: " + e.getMessage());
	    }
	}
	@FindBy(xpath = "(//*[@class='custom_datepicker_input'])[1]")
	WebElement datePickerInput;
	
	//Method to Select Check-In Date By Passing Two Paramenters(Date and MounthYear)
	public void selectDate(String day, String MonthandYear,Log Log) throws InterruptedException {
	    TestExecutionNotifier.showExecutionPopup();

	    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
	    datePickerInput.click();

	    // Wait until the calendar is visible and get the current month & year
	    wait.until(ExpectedConditions.visibilityOfElementLocated(
	        By.xpath("//div[@class='custom-header']")
	    ));

	    String currentMonthYear = driver.findElement(
	        By.xpath("//div[@class='custom-header']")
	    ).getText();

	    // Loop to navigate to the desired month
	    while (!currentMonthYear.equals(MonthandYear)) {
	        driver.findElement(By.xpath("(//button[contains(@class,'nav-arrow')])[2]")).click();
	        wait.until(ExpectedConditions.textToBePresentInElementLocated(
	            By.xpath("//*[@class='custom-header']"),
	            MonthandYear
	        ));

	        currentMonthYear = driver.findElement(
	            By.xpath("//*[@class='custom-header']")
	        ).getText();
	    }

	    // Once the correct month is displayed, select the day
	    WebElement dateElement = wait.until(ExpectedConditions.elementToBeClickable(
	        By.xpath("(//div[@class='react-datepicker'])[1]//span[text()='" + day + "']")
	    ));

	    ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", dateElement);
	    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", dateElement);

	    Log.ReportEvent("INFO", "Selected Date: " + day + " " + MonthandYear);
	}
	
	public void SearchBus( Log Log, ScreenShots screenshots) {
	    driver.findElement(By.xpath("//button[text()='Search Bus']")).click();

	    boolean isMessageDisplayed = driver.findElements(By.xpath("//div[text()='Start your Journey!']")).size() > 0;

	    if (isMessageDisplayed) {
	        System.out.println("Message displayed: Start your Journey!");
	    } else {
	        String pageText = driver.findElement(By.tagName("body")).getText();
	        Log.ReportEvent("FAIL", "Failed to find 'Start your Journey!' message after clicking 'Search Bus'. Current page text: " + pageText);
	        screenshots.takeScreenShot1();

	        throw new RuntimeException("Test failed: 'Start your Journey!' message NOT found.");
	    }
	}



}
