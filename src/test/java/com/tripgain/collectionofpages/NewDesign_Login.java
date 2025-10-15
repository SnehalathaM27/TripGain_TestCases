package com.tripgain.collectionofpages;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

import com.tripgain.common.Log;
import com.tripgain.common.ScreenShots;

public class NewDesign_Login {
	WebDriver driver;

	public NewDesign_Login (WebDriver driver)
	{
		PageFactory.initElements(driver, this);
		this.driver=driver;
	}

	
	
	@FindBy(name="username")
	public WebElement tripGainUserName;

	@FindBy(xpath = "//input[@name='password']") 
	public WebElement tripGainPassword;

	@FindBy(xpath = "//button[@type='submit']")
	public WebElement button;	
	

	//Method to Click on Login Button
	public void clickButton() throws InterruptedException {
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(80));
		wait.until(ExpectedConditions.elementToBeClickable(button));
		button.click();
     

	}
	
	//Method to Enter UserName
		public void enterUserName(String userName)
		{
			tripGainUserName.sendKeys(userName);
		}

		//Method to Enter Password
		public void enterPasswordName(String password)
		{
			tripGainPassword.sendKeys(password);
		}
	
	
		//Method to Verify Home Page is Displayed
	    public void verifyHomePageIsDisplayed(Log Log,ScreenShots ScreenShots) throws InterruptedException {
	        try {
	            WebElement homePageLogo=driver.findElement(By.xpath("//span[text()='Flight']"));
	            if(homePageLogo.isDisplayed())
	            {
	                Log.ReportEvent("PASS", "Home Page is displayed Successful");
	            }
	            else
	            {
	                Log.ReportEvent("FAIL", "Home Page is not displayed");
	                ScreenShots.takeScreenShot1();                
	            }        
	        }
	        catch(Exception e)
	        {
	            Log.ReportEvent("FAIL", "Home Page is not displayed");
	            ScreenShots.takeScreenShot1();
	            e.printStackTrace();
	        }
	    
	    }
	   
	    public void clickOnTravel() {
	    	driver.findElement(By.xpath("//span[text()='Travel']")).click();
	    }
    
   
 }
