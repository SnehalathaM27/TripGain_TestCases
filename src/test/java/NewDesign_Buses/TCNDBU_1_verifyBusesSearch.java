package NewDesign_Buses;

import java.awt.AWTException;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.tripgain.collectionofpages.NewDesignHotelsSearchPage;
import com.tripgain.collectionofpages.NewDesign_Buses_BookingPage;
import com.tripgain.collectionofpages.NewDesign_Buses_ResultsPage;
import com.tripgain.collectionofpages.NewDesign_Buses_SerachPage;
import com.tripgain.collectionofpages.NewDesign_Hotels_BookingPage;
import com.tripgain.collectionofpages.NewDesign_Hotels_DescPage;
import com.tripgain.collectionofpages.NewDesign_Hotels_ResultsPage;
import com.tripgain.collectionofpages.NewDesign_Login;
import com.tripgain.collectionofpages.SiteChecker;
import com.tripgain.collectionofpages.SkyTravelers_Hotels_BookingPage;
import com.tripgain.collectionofpages.SkyTravelers_Hotels_DescriptionPage;
import com.tripgain.collectionofpages.SkyTravelers_Hotels_Login;
import com.tripgain.collectionofpages.SkyTravelers_Hotels_SearchPage;
import com.tripgain.collectionofpages.SkyTravelers_Hotels_confirmBookingPage;
import com.tripgain.collectionofpages.TripPlanner;
import com.tripgain.collectionofpages.Tripgain_FutureDates;
import com.tripgain.collectionofpages.Tripgain_Login;
import com.tripgain.collectionofpages.Tripgain_RoundTripResultsScreen;
import com.tripgain.collectionofpages.Tripgain_homepage;
import com.tripgain.collectionofpages.Tripgain_resultspage;
import com.tripgain.collectionofpages.policyDates;
import com.tripgain.common.DataProviderUtils;
import com.tripgain.common.ExtantManager;
import com.tripgain.common.GenerateDates;
import com.tripgain.common.Getdata;
import com.tripgain.common.Log;
import com.tripgain.common.ScreenShots;
import com.tripgain.testscripts.BaseClass;

public class TCNDBU_1_verifyBusesSearch extends BaseClass{
	WebDriver driver;    
	ExtentReports extent;
    ExtentTest test;
    String className = "";
    Log Log;  // Declare Log object
    ScreenShots screenShots;  // Declare Log object
    ExtantManager extantManager;
  
    int number=1;

    private WebDriverWait wait;

    @Test(dataProvider = "sheetBasedData", dataProviderClass = DataProviderUtils.class)
    public void myTest(Map<String, String> excelData) throws InterruptedException, IOException, ParseException, TimeoutException {
        System.out.println("Running test with: " + excelData);
	    String[] data = Getdata.getexceldata();
        String userName = data[0]; 
        String password = data[1];
        System.out.println(userName);
        System.out.println(password);
        number++;

    
       
        String origin = excelData.get("Origin");
        System.out.println(origin);
        String dest = excelData.get("Destination");
        System.out.println(dest);
//        int hotelindex = Integer.parseInt(excelData.get("HotelIndex"));

        
    
			String[] dates=GenerateDates.GenerateDatesToSelectFlights();
	        String fromDate=dates[0];
	        String fromMonthYear=dates[2];
	        String returnDate=dates[1]; 
	        String returnMonthYear=dates[3];
	        
	        //Login to application
	        NewDesign_Login NewDesignLogin= new NewDesign_Login(driver);
	        
        NewDesignLogin.enterUserName(userName);
        NewDesignLogin.enterPasswordName(password);
        NewDesignLogin.clickButton(); 
		Log.ReportEvent("PASS", "Enter UserName and Password is Successful");
		Thread.sleep(2000);
		NewDesignLogin.clickOnTravel();
		

		NewDesign_Buses_SerachPage NewDesignBusesSerachPage=new NewDesign_Buses_SerachPage(driver);
		NewDesign_Buses_ResultsPage NewDesignBusesResultsPage = new NewDesign_Buses_ResultsPage(driver);
		NewDesign_Buses_BookingPage NewDesignBusesBookingPage=new NewDesign_Buses_BookingPage(driver);
		
		NewDesignBusesSerachPage.clickOnBuses();
		Thread.sleep(3000);
		NewDesignBusesSerachPage.enterfromLocForBuses(origin, Log);
		Thread.sleep(2000);
		NewDesignBusesSerachPage.enterToLocForBuses(dest, Log);
		NewDesignBusesSerachPage.selectDate(fromDate, fromMonthYear, Log);
	
		NewDesignBusesSerachPage.SearchBus(Log, screenShots);
		Thread.sleep(1000);
		
		
		String[] checkindateResultPage = NewDesignBusesResultsPage.getCheckInDateTextFromResultPage();
		String[] ResultPgOrigin = NewDesignBusesResultsPage.getOriginFromresultPg();
		String[] ResultPgDest = NewDesignBusesResultsPage.getDestFromresultPg();
		
//		NewDesignBusesResultsPage.selectSortOption("Price: Low to High", Log);
//		NewDesignBusesResultsPage.validatePricesLowToHigh(Log);
//		Thread.sleep(2000);
//		NewDesignBusesResultsPage.selectSortOption("Price: High to Low", Log);
//		Thread.sleep(1000);
//		NewDesignBusesResultsPage.validatePricesHighToLow(Log);
//		
	//	NewDesignBusesResultsPage.selectSortOption("Departure Time", Log);
		
//		NewDesignBusesResultsPage.selectSortOption("Arrival Time", Log);
//		NewDesignBusesResultsPage.selectSortOption("Duration", Log);
	//	NewDesignBusesResultsPage.selectSortOption("Sort Recommended", Log);

	/*	NewDesignBusesResultsPage.filterByPriceAndValidate(Log, screenShots);
		NewDesignBusesResultsPage.clickAndValidatePolicyDropdown(Log, screenShots);*/
	//	NewDesignBusesResultsPage.clickAndValidateBusTypeOptions(Log, screenShots);
//	NewDesignBusesResultsPage.clickAndValidateSeatType(Log, screenShots);
	//	NewDesignBusesResultsPage.getAndValidatePolicyText(Log, screenShots);
		NewDesignBusesResultsPage.clickAndValidateSearchOperator(Log, screenShots);
	
		String[] Busdetails = NewDesignBusesResultsPage.getBusDetailsFromListingByIndex(0);
		
		String[] SeatType = NewDesignBusesResultsPage.getSeatTypeTextFromresultPgAfterSelect();
		String[] BoardingDetails = NewDesignBusesResultsPage.selectBoardingPoints(Log, screenShots);
		String[] DroppingPoint = NewDesignBusesResultsPage.selectDroppingPoint(Log, screenShots);
		NewDesignBusesResultsPage.clcikLowerBirth(Log, screenShots);
		Thread.sleep(1000);
		NewDesignBusesResultsPage.clickUpperBerth(Log, screenShots);
		String SeatSelectionPrice = NewDesignBusesResultsPage.getPriceAfterSeatSelection(Log, screenShots);
		NewDesignBusesResultsPage.clcikOnConfirmSeat();
		
		NewDesignBusesResultsPage.reasonForSelectionPopUp();
		
		String[] BookingPgArrival = NewDesignBusesBookingPage.getArrivalFromBookingPg();
		String[] BookingPgBoardingPoint = NewDesignBusesBookingPage.getBoardingPointFromBookingPg();
		String[] BookingPgBusName = NewDesignBusesBookingPage.getBusNameFromBookingPg();
		String[] BookingPgSeater = NewDesignBusesBookingPage.getBusSeaterTextFromBookingPg();
		String[] BookingPgDate = NewDesignBusesBookingPage.getDateFromBookingPg();
		String[] BookingPgDepartTime = NewDesignBusesBookingPage.getDepartureFromBookingPg();
		String[] BookingDest = NewDesignBusesBookingPage.getDestFromBookingPg();
		String[] BookingPgdroppingPoint = NewDesignBusesBookingPage.getDroppingPointFromBookingPg();
		String[] bookingPgDuration = NewDesignBusesBookingPage.getDurationFromBookingPg();
		String[] BookingPgorigin = NewDesignBusesBookingPage.getOriginFromBookingPg();
		String[] BookingPgPolicy = NewDesignBusesBookingPage.getPolicyFromBookingPg();
		String[] BookingPgPrice = NewDesignBusesBookingPage.getPriceFromBookingPg();
		
		
		NewDesignBusesBookingPage.validateOriginFromResultToBookingpage(BookingPgorigin, ResultPgOrigin, Log, screenShots);
		NewDesignBusesBookingPage.validateDestFromResultToBookingPage(BookingDest, ResultPgDest, Log, screenShots);
		NewDesignBusesBookingPage.validateBusOperatorNameFromListingToBookingPage(Busdetails, BookingPgBusName, Log, screenShots);
		NewDesignBusesBookingPage.validateBoardingPointLocationFromListingToBookingPage(BoardingDetails, BookingPgBoardingPoint, Log, screenShots);
		NewDesignBusesBookingPage.validateDroppingPointLocationFromListingToBookingPage(DroppingPoint, BookingPgdroppingPoint, Log, screenShots);
		NewDesignBusesBookingPage.validateSeaterTypeFromListingToBookingPage(SeatType, BookingPgSeater, Log, screenShots);
		NewDesignBusesBookingPage.validateCheckInDateBetweenResultAndBookingPages(checkindateResultPage, BookingPgDate, Log, screenShots);
		NewDesignBusesBookingPage.validateDepartureTimeFromBoardingToBookingPage(BoardingDetails, BookingPgDepartTime, Log, screenShots);
		NewDesignBusesBookingPage.validateArrivalTimeFromListingToBookingPage(Busdetails, BookingPgArrival, Log, screenShots);
		NewDesignBusesBookingPage.validateDurationFromListingToBookingPage(Busdetails, bookingPgDuration, Log, screenShots);
		NewDesignBusesBookingPage.validatePolicyFromListingToBookingPage(Busdetails, BookingPgPolicy, Log, screenShots);
		NewDesignBusesBookingPage.validatePriceFromresultToBookingPage(BookingPgPrice, SeatSelectionPrice, Log, screenShots);
		
		NewDesignBusesBookingPage.addPassengerDetails();
		NewDesignBusesBookingPage.clickSendForApproval();
		
       //Function to Logout from Application
    		//tripgainhomepage.logOutFromApplication(Log, screenShots);
    		//driver.quit();
         
       }
	
	 @BeforeMethod
	    @Parameters("browser")
	    public void launchApplication(String browser)
	    {
	       extantManager=new ExtantManager();
	       extantManager.setUpExtentReporter(browser);
	       className = this.getClass().getSimpleName();
	       String testName=className+"_"+number;
	       extantManager.createTest(testName);  // Get the ExtentTest instance
	       test=ExtantManager.getTest();
	       extent=extantManager.getReport();
	       test.log(Status.INFO, "Execution Started Successful"); 
	       driver=launchBrowser(browser);      
	       Log = new Log(driver, test);
	       screenShots=new ScreenShots(driver, test);
	    }

	    @AfterMethod
	    public void tearDown() {
	       if (driver != null) {
	       //  driver.quit();
	          extantManager.flushReport();
	       }
	    }
		
		
	}