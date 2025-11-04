package NewDesign_Buses;

import java.awt.AWTException;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import java.lang.reflect.Method;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.tripgain.collectionofpages.NewDesignHotelsSearchPage;
import com.tripgain.collectionofpages.NewDesign_AwaitingApprovalScreen;
import com.tripgain.collectionofpages.NewDesign_Buses_BookingPage;
import com.tripgain.collectionofpages.NewDesign_Buses_ResultsPage;
import com.tripgain.collectionofpages.NewDesign_Buses_SerachPage;
import com.tripgain.collectionofpages.NewDesign_EmulateProcess;
import com.tripgain.collectionofpages.NewDesign_Hotels_BookingPage;
import com.tripgain.collectionofpages.NewDesign_Hotels_DescPage;
import com.tripgain.collectionofpages.NewDesign_Hotels_ResultsPage;
import com.tripgain.collectionofpages.NewDesign_Login;
import com.tripgain.collectionofpages.NewDesign_Trips;
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
  
 // ThreadLocal to store Excel data per test thread
 	static ThreadLocal<Map<String, String>> excelDataThread = new ThreadLocal<>();
    int number=1;


    @Test(dataProvider = "sheetBasedData", dataProviderClass = DataProviderUtils.class)
    public void myTest(Map<String, String> excelData) throws InterruptedException, IOException, ParseException, TimeoutException {
        System.out.println("Running test with: " + excelData);
try {	    
        String username = excelData.get("UserName");
        String pwd = excelData.get("Password");
        
        System.out.println(username);
        System.out.println(pwd);
        
        
        String username1 = excelData.get("UserName1");
        String pwd1 = excelData.get("Password1");
        number++;


	    
//        String userName = data[0]; 
//        String password = data[1];
//        System.out.println(userName);
//        System.out.println(password);

    
       
        String origin = excelData.get("Origin");
        System.out.println(origin);
        String dest = excelData.get("Destination");
        System.out.println(dest);
//        int hotelindex = Integer.parseInt(excelData.get("HotelIndex"));

        
        String searchby=excelData.get("SearchBy");
        String searchvalue=excelData.get("SearchValue");
        String remarks=excelData.get("Remarks");
        String status=excelData.get("Status");
        String travellerSearchValue=excelData.get("TravellerSearchValue");
        String travellerSearchValue2=excelData.get("TravellerSearchValue2");
        String status2=excelData.get("Status2");
        





        
    
			String[] dates=GenerateDates.GenerateDatesToSelectFlights();
	        String fromDate=dates[0];
	        String fromMonthYear=dates[2];
	        String returnDate=dates[1]; 
	        String returnMonthYear=dates[3];
	        
	        //Login to application
	        NewDesign_Login NewDesignLogin= new NewDesign_Login(driver);
	        
        NewDesignLogin.enterUserName(username);
        NewDesignLogin.enterPasswordName(pwd);
        NewDesignLogin.clickButton(); 
		Log.ReportEvent("PASS", "Enter UserName and Password is Successful");
		Thread.sleep(2000);
		NewDesignLogin.clickOnTravel();
		

		NewDesign_Buses_SerachPage NewDesignBusesSerachPage=new NewDesign_Buses_SerachPage(driver);
		NewDesign_Buses_ResultsPage NewDesignBusesResultsPage = new NewDesign_Buses_ResultsPage(driver);
		NewDesign_Buses_BookingPage NewDesignBusesBookingPage=new NewDesign_Buses_BookingPage(driver);
		NewDesign_AwaitingApprovalScreen NewDesign_Awaiting_ApprovalScreen=new NewDesign_AwaitingApprovalScreen(driver);
		NewDesign_EmulateProcess NewDesign_Emulate_Process=new NewDesign_EmulateProcess(driver);
		NewDesign_Trips NewDesignTrips = new NewDesign_Trips(driver);
		
		NewDesign_Hotels_ResultsPage NewDesignHotelsResultsPage=new NewDesign_Hotels_ResultsPage(driver);
		NewDesign_Hotels_DescPage NewDesignHotels_DescPage = new NewDesign_Hotels_DescPage(driver);
		NewDesign_Hotels_BookingPage NewDesign_HotelsBookingPage=new NewDesign_Hotels_BookingPage(driver);
		


		
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
		//NewDesignBusesResultsPage.clickUpperBerth(Log, screenShots);
		//NewDesignBusesResultsPage.clickUpperBerth(Log, screenShots);

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
		NewDesignBusesBookingPage.clickSendForApproval(Log, screenShots);
		
		NewDesign_Awaiting_ApprovalScreen.waitUntilAwaitingPageLoads(Log, screenShots);
		
		//--------------------------------Awaiting page-------------------------------------------
		
		String[] LocationFromAwaitingScreen = NewDesign_Awaiting_ApprovalScreen.getLocationDetailsFromAwaitingScreen();
		String[] DateFromAwaitingScreen = NewDesign_Awaiting_ApprovalScreen.getdateFromAwaitingPg();
		String[] ApproveridFromAwaitingScreen = NewDesign_Awaiting_ApprovalScreen.getApproverIdFromAwaitingPg(Log);
		
		NewDesign_Awaiting_ApprovalScreen.clickOnViewTripInAwaitingScreen();
		String[] LocationFromViewtripScreen = NewDesign_Awaiting_ApprovalScreen.getLocationDetailsFromViewTripInAwaitingScreen();
		String[] BusNameFromViewtripScreen = NewDesign_Awaiting_ApprovalScreen.getBusNameFromViewTripInAwaitingScreen();
		String[] BusTypeFromViewtripScreen = NewDesign_Awaiting_ApprovalScreen.getBustypeFromViewTripInAwaitingScreen();
		String[] BusOriginFromViewtripScreen = NewDesign_Awaiting_ApprovalScreen.getBusoriginLocFromViewTripInAwaitingScreen();
		String[] BusDestFromViewtripScreen = NewDesign_Awaiting_ApprovalScreen.getBusDestLocFromViewTripInAwaitingScreen();
		String[] BusDepartTimeFromViewtripScreen = NewDesign_Awaiting_ApprovalScreen.getBusDepartTimeFromViewTripInAwaitingScreen();
		String[] BusArrivalTimeFromViewtripScreen = NewDesign_Awaiting_ApprovalScreen.getBusArrTimeFromViewTripInAwaitingScreen();
		String[] BusDepartDateFromViewtripScreen = NewDesign_Awaiting_ApprovalScreen.getBusDepartDateFromViewTripInAwaitingScreen();
		String[] BusDurationFromViewtripScreen = NewDesign_Awaiting_ApprovalScreen.getBusDurationFromViewTripInAwaitingScreen();
		String[] BusPolicyFromViewtripScreen = NewDesign_Awaiting_ApprovalScreen.getBusPolicyFromViewTripInAwaitingScreen();
		String[] BusApproverIdFromViewtripScreen = NewDesign_Awaiting_ApprovalScreen.getApproverIdFromViewTripInViewTripScreen();
		String[] BusPriceFromViewtripScreen = NewDesign_Awaiting_ApprovalScreen.getPriceFromViewTripInViewTripScreen();
		NewDesign_Awaiting_ApprovalScreen.getStatusFromViewTripInViewTripScreen(Log);
		
		NewDesign_Awaiting_ApprovalScreen.validateLocationsDetailsFromAwaitingApprovalToViewTrippage(LocationFromAwaitingScreen, LocationFromViewtripScreen, Log, screenShots);
		NewDesign_Awaiting_ApprovalScreen.validateApproverIdFromAwaitingApprovalToViewTrippage(ApproveridFromAwaitingScreen, BusApproverIdFromViewtripScreen, Log, screenShots);
		NewDesign_Awaiting_ApprovalScreen.validateOriginFromBusBookingToViewTrippage(BookingPgorigin, BusOriginFromViewtripScreen, Log, screenShots);
		NewDesign_Awaiting_ApprovalScreen.validateDestFromBusBookingToViewTrippage(BookingDest, BusDestFromViewtripScreen, Log, screenShots);
		NewDesign_Awaiting_ApprovalScreen.validateBusNameFromBusBookingToViewTrippage(BookingPgBusName, BusNameFromViewtripScreen, Log, screenShots);
		NewDesign_Awaiting_ApprovalScreen.validateBusTypeFromBusBookingToViewTrippage(BookingPgSeater, BusTypeFromViewtripScreen, Log, screenShots);
		NewDesign_Awaiting_ApprovalScreen.validateBusDepartTimeFromBusBookingToViewTrippage(Busdetails, BusDepartTimeFromViewtripScreen, Log, screenShots);
		NewDesign_Awaiting_ApprovalScreen.validateBusArrivalTimeFromBusBookingToViewTrippage(Busdetails, BusArrivalTimeFromViewtripScreen, Log, screenShots);
		NewDesign_Awaiting_ApprovalScreen.validateBusdurationFromBusBookingToViewTrippage(bookingPgDuration, BusDurationFromViewtripScreen, Log, screenShots);
		NewDesign_Awaiting_ApprovalScreen.validateBusPolicyFromBusBookingToViewTrippage(BookingPgPolicy, BusPolicyFromViewtripScreen, Log, screenShots);
		NewDesign_Awaiting_ApprovalScreen.validateBusPriceFromBusBookingToViewTrippage(BookingPgPrice, BusPriceFromViewtripScreen, Log, screenShots);
		
		NewDesign_Awaiting_ApprovalScreen.clickApprovalDetailsButtonInViewtrip();
		NewDesign_Awaiting_ApprovalScreen.getApproverNameInAwaitingScreen(Log);
		NewDesign_Awaiting_ApprovalScreen.getApproverTimeInAwaitingScreen(Log);
		
		//-------------------logout---------------------------------------------------------
		NewDesign_Awaiting_ApprovalScreen.clickOnLogout();
		
		//emulate to approver screen through admin------------------------------------
		
		  NewDesignLogin.enterUserName(username1);
	        NewDesignLogin.enterPasswordName(pwd1);
	        NewDesignLogin.clickButton(); 
	        
	        NewDesign_Emulate_Process.clcikOnAdmin();
	        NewDesign_Emulate_Process.clickOnSearchByThroughUser(searchby);
	        NewDesign_Emulate_Process.clickSearchValueThroughUser(searchvalue);
	        NewDesign_Emulate_Process.clcikOnCorpTravellerSearchButton();
	        NewDesign_Emulate_Process.clickOnEmulmateUserOption();
	        NewDesign_Emulate_Process.waitUntilApproverScreenDisplay(Log, screenShots);
	        NewDesign_Emulate_Process.clcikOnAdmin();
	        Thread.sleep(1000);
	        NewDesign_Emulate_Process.searchApproverIdInApprovalReqScreen(ApproveridFromAwaitingScreen, Log, screenShots);
	        
	        //-----------------------get all the details from the approver screen--------------------------
	        Thread.sleep(2000);
	        String[] LocationFromApproverScreen = NewDesign_Emulate_Process.getLocationDetailsFromApproverApprovalReqScreen();
	        String[] BusNameFromApproverScreen = NewDesign_Emulate_Process.getBusNameFromApproverApprovalReqScreen();
	        String[] BusTypeFromApproverScreen = NewDesign_Emulate_Process.getBustypeFromApproverApprovalReqScreen();
	        String[] BusDepartTimeFromApproverScreen = NewDesign_Emulate_Process.getBusDepartTimeFromApproverApprovalReqScreen();
	        String[] BusArrTimeFromApproverScreen = NewDesign_Emulate_Process.getBusArrTimeFromApproverApprovalReqScreen();
	        String[] BusDepartdateFromApproverScreen = NewDesign_Emulate_Process.getBusDepartDateFromApproverApprovalReqScreen();
	        String[] BusoriginFromApproverScreen = NewDesign_Emulate_Process.getBusoriginLocFromApproverApprovalReqScreen();
	        String[] BusDestFromApproverScreen = NewDesign_Emulate_Process.getBusDestLocFromApproverApprovalReqScreen();
	        String[] BusDurationFromApproverScreen = NewDesign_Emulate_Process.getBusDurationFromApproverApprovalReqScreen();
	        String[] BusPolicyFromApproverScreen = NewDesign_Emulate_Process.getBusPolicyFromApproverApprovalReqScreen();
	        String[] BusPriceFromApproverScreen = NewDesign_Emulate_Process.getPriceFromApproverApprovalReqScreen();
	        String[] BusStatusFromApproverScreen = NewDesign_Emulate_Process.getStatusFromApproverApprovalReqScreen(Log);
	        String[] ApproverIdFromApproverScreen = NewDesign_Emulate_Process.getApproverIdFromApproverApprovalReqScreen();
	        
	        
	        NewDesign_Emulate_Process.validateLocationsDetailsFromViewTripToApproverScreenForBuses(LocationFromViewtripScreen, LocationFromApproverScreen, Log, screenShots);
	        NewDesign_Emulate_Process.validateApproverIdFromViewTripToApproverScreenForBuses(BusApproverIdFromViewtripScreen, ApproverIdFromApproverScreen, Log, screenShots);
	        NewDesign_Emulate_Process.validateBusOriginLocFromViewTripToApproverScreenForBuses(BusOriginFromViewtripScreen, BusoriginFromApproverScreen, Log, screenShots);
	        NewDesign_Emulate_Process.validateDestFromViewTripToApproverScreenForBuses(BusDestFromViewtripScreen, BusDestFromApproverScreen, Log, screenShots);
	        NewDesign_Emulate_Process.validateBusNameFromViewTripToApproverScreenForBuses(BusNameFromViewtripScreen, BusNameFromApproverScreen, Log, screenShots);
	        NewDesign_Emulate_Process.validateBusTypeFromViewTripToApproverScreenForBuses(BusTypeFromViewtripScreen, BusTypeFromApproverScreen, Log, screenShots);
	        NewDesign_Emulate_Process.validateBusDepartTimeFromViewTripToApproverScreenForBuses(BusDepartTimeFromViewtripScreen, BusDepartTimeFromApproverScreen, Log, screenShots);
	        NewDesign_Emulate_Process.validateBusArrivalTimeFromViewTripToApproverScreenForBuses(BusArrivalTimeFromViewtripScreen, BusArrTimeFromApproverScreen, Log, screenShots);
	        NewDesign_Emulate_Process.validateBusdurationFromViewTripToApproverScreenForBuses(BusDurationFromViewtripScreen, BusDurationFromApproverScreen, Log, screenShots);
	        NewDesign_Emulate_Process.validateBusPolicyFromViewTripToApproverScreenForBuses(BusPolicyFromViewtripScreen, BusPolicyFromApproverScreen, Log, screenShots);
	        NewDesign_Emulate_Process.validateBusPriceFromViewTripToApproverScreenForBuses(BusPriceFromViewtripScreen, BusPriceFromApproverScreen, Log, screenShots);
	        
	        NewDesign_Emulate_Process.clcikOnProcessButton();
	        NewDesign_Emulate_Process.enterRemarks(remarks);
	        NewDesign_Emulate_Process.clickOnStatus(status);
	        NewDesign_Emulate_Process.clickOnUpdateBtn();
	        
	        //----------------switch back to traveller -------------------------------------------
	        Thread.sleep(3000);
	        NewDesign_Emulate_Process.clcikOnSwitchBack();
	        NewDesign_Emulate_Process.waitUntilApproverScreenDisplay(Log, screenShots);
	        NewDesign_Emulate_Process.clcikOnAdmin();
	        NewDesign_Emulate_Process.clickOnSearchByThroughUser(searchby);
	        NewDesign_Emulate_Process.clickSearchValueThroughUser(travellerSearchValue);
	        NewDesign_Emulate_Process.clcikOnCorpTravellerSearchButton();
	        NewDesign_Emulate_Process.clickOnEmulmateUserOption();
	        NewDesign_Emulate_Process.waitUntilApproverScreenDisplay(Log, screenShots);
	        
			NewDesignTrips.clcikOnTrips();
			NewDesignTrips.clickOnAwaitingApproval();
			NewDesignTrips.clickOnsearchTripsInAwaitingApprovalPg(ApproveridFromAwaitingScreen[0], Log, screenShots);
			String[] travellerStatus = NewDesignTrips.getStatusInAwaitingApprovalForHotels(Log);
			NewDesign_Awaiting_ApprovalScreen.clickOnViewTripInAwaitingScreen();
			NewDesignTrips.getStatusInAwaitingApprovalForBusesInPendingStatys(Log);
			
			//---------------If Two levels of approver--------------------------- 
	        NewDesign_Emulate_Process.clcikOnSwitchBack();
	        NewDesign_Emulate_Process.waitUntilApproverScreenDisplay(Log, screenShots);
	        NewDesign_Emulate_Process.clcikOnAdmin();
	        NewDesign_Emulate_Process.clickOnSearchByThroughUser(searchby);
	        NewDesign_Emulate_Process.clickSearchValueThroughUser(travellerSearchValue2);
	        NewDesign_Emulate_Process.clcikOnCorpTravellerSearchButton();
	        NewDesign_Emulate_Process.clickOnEmulmateUserOption();
	        NewDesign_Emulate_Process.waitUntilApproverScreenDisplay(Log, screenShots);
	      
	        NewDesign_Emulate_Process.clcikOnAdmin();
	        NewDesign_Emulate_Process.clickOnApprovalReqIn2ndApproverScreen();
	        NewDesign_Emulate_Process.searchApproverIdInApprovalReqScreen(ApproveridFromAwaitingScreen, Log, screenShots);
			String[] ApproverStatus = NewDesignTrips.getStatusInAwaitingApprovalForBuses(Log);
			NewDesignTrips.validateStatusFromTravellerToApprover(travellerStatus, ApproverStatus, Log, screenShots);
		//	NewDesignTrips.clickArrowToOpenTrip();
			 NewDesign_Emulate_Process.clcikOnProcessButton();
		        NewDesign_Emulate_Process.enterRemarks(remarks);
		        NewDesign_Emulate_Process.clickOnStatus(status2);
		        NewDesign_Emulate_Process.clickOnUpdateBtn();
		        
		        
		        NewDesign_Emulate_Process.clcikOnSwitchBack();
		        NewDesign_Emulate_Process.waitUntilApproverScreenDisplay(Log, screenShots);
		        NewDesign_Emulate_Process.clcikOnAdmin();
		        NewDesign_Emulate_Process.clickOnSearchByThroughUser(searchby);
		        NewDesign_Emulate_Process.clickSearchValueThroughUser(travellerSearchValue);
		        NewDesign_Emulate_Process.clcikOnCorpTravellerSearchButton();
		        NewDesign_Emulate_Process.clickOnEmulmateUserOption();
		        NewDesign_Emulate_Process.waitUntilApproverScreenDisplay(Log, screenShots);
		        

		        NewDesignTrips.clcikOnTrips();
				NewDesignTrips.clickOnAwaitingApproval();
				NewDesignTrips.clickOnsearchTripsInAwaitingApprovalPg(ApproveridFromAwaitingScreen[0], Log, screenShots);
				 NewDesignTrips.getStatusInAwaitingApprovalForBuses(Log);
				NewDesign_Awaiting_ApprovalScreen.clickOnViewTripInAwaitingScreen();
				NewDesign_Awaiting_ApprovalScreen.getStatusFromViewTripInViewTripScreen(Log);

              System.out.println("Completed");

			

			
			
			
	        
	        

	        
	        
		
		
		
		
		
		
       //Function to Logout from Application
    		//tripgainhomepage.logOutFromApplication(Log, screenShots);
    		//driver.quit();
		
		
		}catch (Exception e)
		{
			String errorMessage = "Exception occurred: " + e.toString();
			Log.ReportEvent("FAIL", errorMessage);
			screenShots.takeScreenShot();
			e.printStackTrace();  // You already have this, good for console logs
			Assert.fail(errorMessage);
		}
         
       }
	
    
    @BeforeMethod(alwaysRun = true)
	@Parameters("browser")
	public void launchApplication(String browser, Method method, Object[] testDataObjects) {
		// Get test data passed from DataProvider
		@SuppressWarnings("unchecked")
		Map<String, String> testData = (Map<String, String>) testDataObjects[0];
		excelDataThread.set(testData);  // Set it early!

		String url = (testData != null && testData.get("URL") != null) ? testData.get("URL") : "https://defaulturl.com";

		extantManager = new ExtantManager();
		extantManager.setUpExtentReporter(browser);
		className = this.getClass().getSimpleName();
		String testName = className + "_" + number;
		extantManager.createTest(testName);
		test = ExtantManager.getTest();
		extent = extantManager.getReport();
		test.log(Status.INFO, "Execution Started Successfully");

		driver = launchBrowser(browser, url);
		Log = new Log(driver, test);
		screenShots = new ScreenShots(driver, test);
	}

	@AfterMethod
	public void tearDown() {
		if (driver != null) {
			driver.quit();
			extantManager.flushReport();
		}
	}


		
		
	}