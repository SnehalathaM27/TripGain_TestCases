package com.tripgain.collectionofpages;

import java.time.Duration;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

import com.tripgain.common.Log;
import com.tripgain.common.ScreenShots;

public class Implementation_Corporate_TravellersPage {
	WebDriver driver;

	public Implementation_Corporate_TravellersPage (WebDriver driver)
	{
		PageFactory.initElements(driver, this);
		this.driver=driver;
	}
	
	//Method to clcik on view detail button in Corporate Travellers page 
	public void clickOnViewDetailBtnInCorpTravellers(String email) {
	    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

	    WebElement viewDetailBtn = wait.until(ExpectedConditions.elementToBeClickable(
	        By.xpath("//div[normalize-space(text())='" + email + "']/ancestor::div[contains(@class,'corporate_traveller__detail_container')]//button[normalize-space(text())='View Detail']")
	    ));
	    viewDetailBtn.click();
	    System.out.println(" Clicked on 'View Detail' for: " + email);
	}
	
	public void clcikOnEditBtn() {
		  try {
			    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

		        WebElement editBtn = wait.until(ExpectedConditions.elementToBeClickable(
		            By.xpath("(//button[contains(@class,'tg-icon-btn_md')])[7]") 
		        ));
		        editBtn.click();
		        System.out.println(" Clicked on 'Edit' button.");
		    } catch (TimeoutException e) {
		        System.out.println("Edit button not found or not clickable");
		    }
	}


	//Method to clcik on select grade dropdown 
	
	
	public String[] clickOnSelectGrade(String gradeOption, Log log) {
	    driver.findElement(By.xpath("//span[normalize-space(text())='Select Grade']/following::div[contains(@class,'tg-select-box__dropdown-indicator')][1]")).click();

	    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
	    wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(@class,'tg-select-box__menu')]")));
	    
	    String optionXpath = "//span[@class='tg-select-option-label' and normalize-space(text())='" + gradeOption + "']";
	    WebElement option = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(optionXpath)));
	    option.click();

	    System.out.println("Selected grade option: " + gradeOption);
	    log.ReportEvent("INFO", "Selected grade option: " + gradeOption);

	    return new String[]{gradeOption};
	}
	
	public String[] clickOnSelectGender(String genderOption, Log log) {
	    driver.findElement(By.xpath("//span[normalize-space(text())='Gender']/following::div[contains(@class,'tg-select-box__dropdown-indicator')][1]")).click();

	    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
	    wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(@class,'tg-select-box__menu-list')]")));
	    
	    String optionXpath = "//span[@class='tg-select-option-label' and normalize-space(text())='" + genderOption + "']";
	    WebElement option = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(optionXpath)));
	    option.click();

	    System.out.println("Selected gender option: " + genderOption);
	    log.ReportEvent("INFO", "Selected gender option: " + genderOption);

	    return new String[]{genderOption};
	}

	
	//Method to click on save btn
	public void clickOnSaveBtn() {
		driver.findElement(By.xpath("//button[text()='Save']")).click();
	}
	
	
	//Method to clcik on emeulate after selecting grade
	public void emulateAfterSelectedGrade() {
		driver.findElement(By.xpath("//div[contains(@class,'triplayout')]//span[text()='Emulate User']")).click();
	}
	
	public String[] clickOnTravellerProfile() throws InterruptedException {
	    Thread.sleep(1000);

	    driver.findElement(By.xpath("//div[@class='MuiAvatar-root MuiAvatar-circular MuiAvatar-colorDefault css-1vhh6nc']/following-sibling::button")).click();
	    Thread.sleep(1000);

	    driver.findElement(By.xpath("//span[normalize-space(text())='Profile']")).click();
	    Thread.sleep(3000);

	    WebElement gradeTextElement = driver.findElement(By.xpath("//div[normalize-space(text())='Grade']/following-sibling::div"));
	    String gradeText = gradeTextElement.getText().trim();

	    System.out.println("Grade from Traveller Profile: " + gradeText);
	    return new String[]{gradeText};
	}

	
	//Method to validate grade from selecetd to traveller profile
	
	public void validateGradesFromSelectedToTravellerProfile(String[] selectedGrades,String[] travellerProfileGrade,  Log log, ScreenShots screenshots) {
	    if (travellerProfileGrade == null || travellerProfileGrade.length == 0) {
	        log.ReportEvent("FAIL", "traveller Profile Grade is missing.");
	        screenshots.takeScreenShot1();
	        Assert.fail("traveller Profile Grade is missing.");
	        return;
	    }
	    if (selectedGrades == null || selectedGrades.length == 0) {
	        log.ReportEvent("FAIL", "selecetd grade  is missing.");
	        screenshots.takeScreenShot1();
	        Assert.fail("selecetd grade  is missing.");
	        return;
	    }

	    String travellerGrade = travellerProfileGrade[0].trim();
	    String selectedgrade = selectedGrades[0].trim();

	    if (!selectedgrade.equalsIgnoreCase(travellerGrade)) {
	        log.ReportEvent("FAIL", "Grade mismatch! selected grade: '" + selectedgrade + "', traveller Profile grade: '" + travellerGrade + "'");
	        screenshots.takeScreenShot1();
	        Assert.fail("grades mismatch between selected to traveller profile grade section");
	    } else {
	        log.ReportEvent("PASS", "grades matches between selected to traveller profile " + selectedgrade);
	    }
	}
	
	public void validateHotelPricesAgainstGradePolicy(String[] selectedGrades, int priceLimit, Log log, ScreenShots screenshots) {
	    try {
	        log.ReportEvent("INFO", "Starting hotel price validation for grade - Price limit: " + priceLimit);
	        
	        // Keep scrolling until no more hotels load
	        JavascriptExecutor js = (JavascriptExecutor) driver;
	        int previousCount = 0;
	        int currentCount = 0;
	        int scrollCount = 0;
	        
	        do {
	            previousCount = currentCount;
	            
	            // Scroll to bottom
	            js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
	            Thread.sleep(3000);
	            
	            // Get current hotel count
	            List<WebElement> hotelCards = driver.findElements(By.xpath("//div[@class='hcard']"));
	            currentCount = hotelCards.size();
	            scrollCount++;
	            
	            // Stop if no new hotels loaded or after 20 scrolls
	            if (currentCount == previousCount) {
	                break;
	            }
	            
	        } while (scrollCount < 20);
	        
	        // Final hotel cards
	        List<WebElement> hotelCards = driver.findElements(By.xpath("//div[@class='hcard']"));
	        log.ReportEvent("INFO", "Final total hotels: " + hotelCards.size());
	        
	        boolean allCorrect = true;
	        
	        // Check each hotel card
	        for (int i = 0; i < hotelCards.size(); i++) {
	            WebElement hotelCard = hotelCards.get(i);
	            
	            // Get hotel price
	            WebElement priceElement = hotelCard.findElement(By.xpath(".//div[contains(@class,'tg-hl-price')]"));
	            String priceText = priceElement.getText().replace("₹", "").replace(",", "").trim();
	            int hotelPrice = Integer.parseInt(priceText);
	            
	            // Get policy text
	            WebElement policyElement = hotelCard.findElement(By.xpath(".//div[contains(@class,'tg-policy')]"));
	            String actualPolicy = policyElement.getText();
	            
	            // Convert to lowercase for case-insensitive comparison
	            String actualPolicyLower = actualPolicy.toLowerCase();
	            
	            // Check if policy is correct - Only log failures
	            if (hotelPrice <= priceLimit) {
	                // Price is within limit - should be IN POLICY
	                if (!actualPolicyLower.contains("in policy")) {
	                    log.ReportEvent("FAIL", "Hotel " + (i+1) + " - Price: " + hotelPrice + " - Should be IN POLICY but found: " + actualPolicy);
	                    allCorrect = false;
	                }
	            } else {
	                // Price exceeds limit - should be OUT OF POLICY
	                if (!actualPolicyLower.contains("out of policy")) {
	                    log.ReportEvent("FAIL", "Hotel " + (i+1) + " - Price: " + hotelPrice + " - Should be OUT OF POLICY but found: " + actualPolicy);
	                    allCorrect = false;
	                }
	            }
	        }
	        
	        // Final result
	        if (allCorrect) {
	            log.ReportEvent("PASS", "All " + hotelCards.size() + " hotels follow grade policy correctly! Price limit: " + priceLimit);
	        } else {
	            log.ReportEvent("FAIL", "Some hotels don't follow grade policy! Price limit: " + priceLimit);
	            screenshots.takeScreenShot1();
	            Assert.fail("Hotel policy validation failed - Some hotels don't follow grade policy with limit: " + priceLimit);
	        }
	        
	    } catch (Exception e) {
	        log.ReportEvent("FAIL", "Error during hotel validation: " + e.getMessage());
	        screenshots.takeScreenShot1();
	        Assert.fail("Error during hotel validation: " + e.getMessage());
	    }
	}
	
	
	public void validateBusesPricesAgainstGradePolicy(String[] selectedGrades, int priceLimit, Log log, ScreenShots screenshots) {
	    try {
	        log.ReportEvent("INFO", "Starting hotel price validation for grade - Price limit: " + priceLimit);
	        
	        // Keep scrolling until no more hotels load
	        JavascriptExecutor js = (JavascriptExecutor) driver;
	        int previousCount = 0;
	        int currentCount = 0;
	        int scrollCount = 0;
	        
	        do {
	            previousCount = currentCount;
	            
	            // Scroll to bottom
	            js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
	            Thread.sleep(3000);
	            
	            // Get current hotel count
	            List<WebElement> busCards = driver.findElements(By.xpath("//div[contains(@class,'buscard-root')]"));
	            currentCount = busCards.size();
	            scrollCount++;
	            
	            // Stop if no new hotels loaded or after 20 scrolls
	            if (currentCount == previousCount) {
	                break;
	            }
	            
	        } while (scrollCount < 20);
	        
	        // Final hotel cards
	        List<WebElement> busCards = driver.findElements(By.xpath("//div[contains(@class,'buscard-root')]"));
	        log.ReportEvent("INFO", "Final total hotels: " + busCards.size());
	        
	        boolean allCorrect = true;
	        
	        // Check each hotel card
	        for (int i = 0; i < busCards.size(); i++) {
	            WebElement busCard = busCards.get(i);
	            
	            // Get hotel price
	            WebElement priceElement = busCard.findElement(By.xpath(".//div[contains(@class,'tg-bus-fare')]"));
	            String priceText = priceElement.getText().replace("₹", "").replace(",", "").trim();
	            int busPrice = Integer.parseInt(priceText);
	            
	            // Get policy text
	            WebElement policyElement = busCard.findElement(By.xpath(".//div[contains(@class,'tg-policy')]"));
	            String actualPolicy = policyElement.getText();
	            
	            // Convert to lowercase for case-insensitive comparison
	            String actualPolicyLower = actualPolicy.toLowerCase();
	            
	            // Check if policy is correct - Only log failures
	            if (busPrice <= priceLimit) {
	                // Price is within limit - should be IN POLICY
	                if (!actualPolicyLower.contains("in policy")) {
	                    log.ReportEvent("FAIL", "Bus " + (i+1) + " - Price: " + busPrice + " - Should be IN POLICY but found: " + actualPolicy);
	                    allCorrect = false;
	                }
	            } else {
	                // Price exceeds limit - should be OUT OF POLICY
	                if (!actualPolicyLower.contains("out of policy")) {
	                    log.ReportEvent("FAIL", "Bus " + (i+1) + " - Price: " + busPrice + " - Should be OUT OF POLICY but found: " + actualPolicy);
	                    allCorrect = false;
	                }
	            }
	        }
	        
	        // Final result
	        if (allCorrect) {
	            log.ReportEvent("PASS", "All " + busCards.size() + " Bus follow grade policy correctly! Price limit: " + priceLimit);
	        } else {
	            log.ReportEvent("FAIL", "Some Buses don't follow grade policy! Price limit: " + priceLimit);
	            screenshots.takeScreenShot1();
	            Assert.fail("Bus policy validation failed - Some hotels don't follow grade policy with limit: " + priceLimit);
	        }
	        
	    } catch (Exception e) {
	        log.ReportEvent("FAIL", "Error during bus validation: " + e.getMessage());
	        screenshots.takeScreenShot1();
	        Assert.fail("Error during bus validation: " + e.getMessage());
	    }
	}

}
