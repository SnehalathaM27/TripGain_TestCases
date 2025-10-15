package NewDesign_Trips;

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

public class TCNDTR_3_CreateTripsForHotelsAndBuses extends BaseClass {
	WebDriver driver;
	ExtentReports extent;
	ExtentTest test;
	String className = "";
	Log Log; // Declare Log object
	ScreenShots screenShots; // Declare Log object
	ExtantManager extantManager;

	int number = 1;

	private WebDriverWait wait;

	@Test(dataProvider = "sheetBasedData", dataProviderClass = DataProviderUtils.class)
	public void myTest(Map<String, String> excelData)
			throws InterruptedException, IOException, ParseException, TimeoutException {
		System.out.println("Running test with: " + excelData);
		String[] data = Getdata.getexceldata();
		String userName = data[0];
		String password = data[1];
		System.out.println(userName);
		System.out.println(password);
		number++;

		String origin = excelData.get("Origin");
		System.out.println(origin);
		String destination = excelData.get("Destination");
		System.out.println(destination);
		
		String Busorigin = excelData.get("BusOrigin");
		System.out.println(Busorigin);
		String Busdestination = excelData.get("BusDestination");
		System.out.println(Busdestination);
		
		String hotelOrigin = excelData.get("HotelOrigin");
		System.out.println(hotelOrigin);

		String tripName = excelData.get("TripName");

		String[] dates = GenerateDates.GenerateDatesToSelectFlights();
		String fromDate = dates[0];
		String fromMonthYear = dates[2];
		String returnDate = dates[1];
		String returnMonthYear = dates[3];

		// Login to application
		NewDesign_Login NewDesignLogin = new NewDesign_Login(driver);

		SkyTravelers_Hotels_Login SkyTravelersHotelsLogin = new SkyTravelers_Hotels_Login(driver);
		NewDesignLogin.enterUserName(userName);
		NewDesignLogin.enterPasswordName(password);
		NewDesignLogin.clickButton();
		Log.ReportEvent("PASS", "Enter UserName and Password is Successful");
		Thread.sleep(2000);

		NewDesign_Trips NewDesignTrips = new NewDesign_Trips(driver);
		NewDesign_Hotels_ResultsPage NewDesignHotelsResultsPage=new NewDesign_Hotels_ResultsPage(driver);
		NewDesign_Hotels_DescPage NewDesignHotels_DescPage = new NewDesign_Hotels_DescPage(driver);
		NewDesign_Hotels_BookingPage NewDesign_HotelsBookingPage=new NewDesign_Hotels_BookingPage(driver);
		
		NewDesign_Buses_ResultsPage NewDesignBusesResultsPage = new NewDesign_Buses_ResultsPage(driver);
		NewDesign_Buses_BookingPage NewDesignBusesBookingPage=new NewDesign_Buses_BookingPage(driver);



		NewDesignTrips.clcikOnTrips();
		NewDesignTrips.createTrip();
		String EnteredtripName = NewDesignTrips.enterNameThisTrip(tripName, Log);
		String origindetails = NewDesignTrips.enterfrom(origin);
		String destdetails = NewDesignTrips.enterTo(destination);

		String journeydatedetails = NewDesignTrips.selectJourneyDate(fromDate, fromMonthYear);

		String returndatedetails = NewDesignTrips.selectReturnDate(returnDate, returnMonthYear);

		// List<String> servicesdetails = Trip_Planner.selectServices("Flight","Hotel");
		List<String> servicesdetails = NewDesignTrips.selectServices("Bus","Hotel");

		NewDesignTrips.clickCreateTripButton();

		List<String> servicesTextFromPopup = NewDesignTrips.getSelectedServicesTextFromPopup();

		NewDesignTrips.validateSelectedServicesInSelectedAndPopup(servicesdetails, servicesTextFromPopup, Log,screenShots);
		String[] tripIdFromPop = NewDesignTrips.getTripIdFromPopup(Log);
		NewDesignTrips.clickOnContinueToAddServicesBtn();

		String[] TripIdFromNextPage = NewDesignTrips.getTripIdFromTripDetailsPage();

		String[] TripDatesFromSearchPg = NewDesignTrips.getDatesFromTripDetailsPage();
		
		 List<String> selectedServicesFromDetailsPg = NewDesignTrips.getSelectedServicesTextFromDetailsPage();
		 NewDesignTrips.validateSelectedAndDetailsPageServices(servicesdetails, selectedServicesFromDetailsPg, Log, screenShots);

		 NewDesignTrips.clickOnServiceByText("Hotel", Log);
		 
		 String locationToSearch = NewDesignTrips.enterLocationForHotelsOndetailsPg(hotelOrigin);
		 String checkInDateFromDetailsPg = NewDesignTrips.selectJourneyDate(fromDate, fromMonthYear);
	   String checkOutDateFromDetailsPg = NewDesignTrips.selectReturnDate(returnDate, returnMonthYear);
	   NewDesignTrips.clickOnAddButton();
	   
	   String[] LocationNameAfterAdd = NewDesignTrips.getTripNameFromTripDetailsPageAfterAddForHotels();
	   String[] datesAfterAdd = NewDesignTrips.getCheckInAndOutDateFromTripDetailsPageForHotelsAfterAdd();
	   
	   NewDesignTrips.validateLocationFromsearchToAfterClickAddForHotels(locationToSearch, LocationNameAfterAdd, Log, screenShots);
	   NewDesignTrips.validateCheckInAndCheckOutDatesFromsearchToAdd(checkInDateFromDetailsPg, checkOutDateFromDetailsPg, datesAfterAdd, Log, screenShots);

	   NewDesignTrips.clickOnSearchHotelBut();
	   Thread.sleep(2000);
	   
	   String[] locNameFromTripResultPg = NewDesignTrips.getLocNAmeFromTripHotelResultsPg();
	   String[] checkindateResultPage = NewDesignHotelsResultsPage.getCheckInDateTextFromResultPage();
		String[] checkoutdateResultPage =NewDesignHotelsResultsPage.getCheckOutDateTextFromResultPage();
		
		NewDesignTrips.validateHotelLocationFromResultsAndAfterAddInDetailsPage(locNameFromTripResultPg, LocationNameAfterAdd, Log, screenShots);
		NewDesignTrips.validateCheckInAndOutDatesFromResultsAndDetailsAfterAdd(checkindateResultPage, checkoutdateResultPage, datesAfterAdd, Log, screenShots);
		
		Thread.sleep(3000);
		NewDesignHotelsResultsPage.clickOnSortOption("Price: Low to High", Log);
		NewDesignHotelsResultsPage.validatePricesLowToHigh(Log);
		Thread.sleep(2000);
		
		NewDesignHotelsResultsPage.clickOnSortOption("Price: High to Low", Log);
		NewDesignHotelsResultsPage.validatePricesHighToLow(Log);
		Thread.sleep(2000);

//			NewDesignHotelsResultsPage.clickOnSortOption("Star Rating: Ascending", Log);
//			NewDesignHotelsResultsPage.clickOnSortOption("Star Rating: Descending", Log);
			NewDesignHotelsResultsPage.clickOnSortOption("Distance: Ascending", Log);
			NewDesignHotelsResultsPage.validateDistanceAscending(Log);
			Thread.sleep(2000);

			NewDesignHotelsResultsPage.clickOnSortOption("Distance: Descending", Log);
			NewDesignHotelsResultsPage.validateDistanceDescendingSimple(Log);
			Thread.sleep(2000);


			NewDesignHotelsResultsPage.selectCurrencyFromDropdown("TND", Log);
			
			Thread.sleep(2000);

			
			
		//get all the hotel details from the selected hotel card
			String[] hotelDetails = NewDesignHotelsResultsPage.selectHotelAndGetDetails(1);
			
			//get all the details from desc page 
			String[] hotelAddressFromDesc = NewDesignHotels_DescPage.getAddressFromDescPg();
			String[] hotelAmenitiesFromDesc = NewDesignHotels_DescPage.getAmenitiesFromDescPg();
			String[] checkInDateFromDesc = NewDesignHotels_DescPage.getCheckInAfterFromDescPg();
			String[] checkOutFromDesc = NewDesignHotels_DescPage.getCheckOutTimeFromDescPg();
			String[] hotelNameFromDesc = NewDesignHotels_DescPage.getHotelNameFromDescPg();
		//	NewDesignHotels_DescPage.getHotelRatingFromDescPg();
			String[] perNightPriceFromDesc = NewDesignHotels_DescPage.getPerNightPriceFromDescPg();
			String[] hotelPriceFromDesc = NewDesignHotels_DescPage.getPriceFromDescPg();
			String[] PolicyFromDesc = NewDesignHotels_DescPage.getPolicyFromDescPg();
			String[] OtherCurrencyInDesc=NewDesignHotels_DescPage.getOtherCountryPriceFromDescPg();
			//NewDesignHotels_DescPage.getStarRatingCount(null);
			
			//validations b/w desc to result pages 
		NewDesignHotels_DescPage.validateHotelNameFromDescToResultPage(hotelNameFromDesc, hotelDetails, Log, screenShots);
			NewDesignHotels_DescPage.validateHotelAddressFromDescToResultPage(hotelAddressFromDesc, hotelDetails, Log, screenShots);
			NewDesignHotels_DescPage.validateHotelPriceFromDescToResultPage(hotelPriceFromDesc, hotelDetails, Log, screenShots);
			NewDesignHotels_DescPage.validateHotelPolicyFromDescToResultPage(PolicyFromDesc, hotelDetails, Log, screenShots);
			NewDesignHotels_DescPage.validatePerNightPriceFromDescToResultPage(perNightPriceFromDesc, hotelDetails, Log, screenShots);
			NewDesignHotels_DescPage.validateAmenitiesFromDescToResultPage(hotelAmenitiesFromDesc, hotelDetails, Log, screenShots);
			NewDesignHotelsResultsPage.validateOtherCurrencyPriceFromDescToResultPage(OtherCurrencyInDesc, hotelDetails, Log, screenShots);
			
			
			String[] selectRooms = NewDesignHotels_DescPage.selectRoomsFromDescPg();
			
			Thread.sleep(3000);
			String[] BookingPgcheckIn = NewDesign_HotelsBookingPage.getCheckInAfterFromBookingPg();
			String[] BookingPgCheckOut = NewDesign_HotelsBookingPage.getCheckOutTimeFromBookingPg();
			String[] BookingPgAddress = NewDesign_HotelsBookingPage.getHotelAddressFromBookingPg();
			String[] BookingPgHotelNm = NewDesign_HotelsBookingPage.getHotelNameFromBookingPg();
			String[] BookingPgLabel = NewDesign_HotelsBookingPage.getLabelTextFromBookingPg();
			String[] BookingPgMeals = NewDesign_HotelsBookingPage.getMealsTextFromBookingPg();
			String[] BookingPgPolicy = NewDesign_HotelsBookingPage.getPolicyTextFromBookingPg();
			String[] BookingPgRefundable = NewDesign_HotelsBookingPage.getRefundableTextFromBookingPg();
			String[] BookingPgSelectedRoomText = NewDesign_HotelsBookingPage.getSelectedRoomTextFromBookingPg();
			String[] BookingPgTotalfareAmount = NewDesign_HotelsBookingPage.getTotalFareAmountFromBookingPg();
			String[] BookingCheckIndate = NewDesign_HotelsBookingPage.getCheckInDateFromBookingPg();
			String[] BookingCheckOutdate = NewDesign_HotelsBookingPage.getCheckOutDateFromBookingPg();
			String[] BookingNightsStay = NewDesign_HotelsBookingPage.getHotelNightsStayFromBookingPg();
			
			NewDesign_HotelsBookingPage.validateCheckInAfterFromDescToBookingPage(checkInDateFromDesc, BookingPgcheckIn, Log, screenShots);
			NewDesign_HotelsBookingPage.validateCheckOutTimeFromDescToBookingPage(checkOutFromDesc, BookingPgCheckOut, Log, screenShots);
			NewDesign_HotelsBookingPage.validateHotelAddressFromDescToBookingPage(hotelAddressFromDesc, BookingPgAddress, Log, screenShots);
			NewDesign_HotelsBookingPage.validateHotelNameFromDescToBookingPage(hotelNameFromDesc, BookingPgHotelNm, Log, screenShots);
			NewDesign_HotelsBookingPage.validateLabelFromDescToBookingPage(selectRooms, BookingPgLabel, Log, screenShots);
			NewDesign_HotelsBookingPage.validateMealsTextFromDescToBookingPage(selectRooms, BookingPgMeals, Log, screenShots);
			NewDesign_HotelsBookingPage.validatePolicyTextFromDescToBookingPage(PolicyFromDesc, BookingPgPolicy, Log, screenShots);
			NewDesign_HotelsBookingPage.validateRefundableTextFromDescToBookingPage(selectRooms, BookingPgRefundable, Log, screenShots);
			NewDesign_HotelsBookingPage.validateSelectedRoomTextFromDescToBookingPage(selectRooms, BookingPgSelectedRoomText, Log, screenShots);
			//NewDesign_HotelsBookingPage.validatePriceFromDescWithBookingPage(selectRooms, BookingPgTotalfareAmount, Log, screenShots);
			NewDesign_HotelsBookingPage.validateCheckInDateBetweenResultAndBookingPage(checkindateResultPage, BookingCheckIndate, Log, screenShots);
			NewDesign_HotelsBookingPage.validateCheckOutDateBetweenResultAndBookingPage(checkoutdateResultPage, BookingCheckOutdate, Log, screenShots);
			//NewDesign_HotelsBookingPage.addTravellerDetails();
			NewDesign_HotelsBookingPage.enterLastNameForHotels("traveller");
			NewDesignTrips.clickOnAddHotelToTrip(Log);
			Thread.sleep(3000);
			
			NewDesignTrips.clcikOnDownButtonInHotelsDesclaimer();
			Thread.sleep(1000);
			
			String[] HotelnameFromDetailspg = NewDesignTrips.getHotelNameTextFromTripDetailsPage();
			String[] HoteladdressFromDetailspg = NewDesignTrips.getHotelAddressTextFromTripDetailsPage();
			String[] HotelPolicyFromDetailspg = NewDesignTrips.getHotelPolicyTextFromTripDetailsPage();
			String[] HotelroomsFromDetailspg = NewDesignTrips.gethotelRoomTextFromTripDetailsPage();
			String[] HotelMealsFromDetailspg = NewDesignTrips.getHotelMealsTextFromTripDetailsPage();
			String[] HotelCheckInFromDetailspg = NewDesignTrips.getHotelCheckInafterTextFromTripDetailsPage();
			String[] HotelCheckOutFromDetailspg = NewDesignTrips.getHotelCheckOutBeforeTextFromTripDetailsPage();
			String[] HotelStayFromDetailspg = NewDesignTrips.getHotelDaysStayTextFromTripDetailsPage();
			
			NewDesignTrips.validateHotelNameFromTripDetailsToHotelsBookingPage(HotelnameFromDetailspg, BookingPgHotelNm, Log, screenShots);
			NewDesignTrips.validateHotelAddressFromTripDetailsToHotelBookingPage(HoteladdressFromDetailspg, BookingPgAddress, Log, screenShots);
			NewDesignTrips.validateHotelPolicyFromTripDetailsToBookingPage(HotelPolicyFromDetailspg, BookingPgPolicy, Log, screenShots);
			NewDesignTrips.validateSelectedRoomFromTripDetailsToBookingPage(HotelroomsFromDetailspg, BookingPgSelectedRoomText, Log, screenShots);
			NewDesignTrips.validateHotelMealsFromTripDetailsToBookingPage(HotelMealsFromDetailspg, BookingPgMeals, Log, screenShots);
			NewDesignTrips.validateHotelCheckInTimeFromTripDetailsToBookingPage(HotelCheckInFromDetailspg, BookingPgcheckIn, Log, screenShots);
			NewDesignTrips.validateHotelCheckOutTimeFromTripDetailsToBookingPage(HotelCheckOutFromDetailspg, BookingPgCheckOut, Log, screenShots);
			NewDesignTrips.validateDaysStayFromTripDetailsToBookingPage(HotelStayFromDetailspg, BookingNightsStay, Log, screenShots);
			
			
			//---------------------------------------------------------------------------------------------------------
			
			 NewDesignTrips.clickOnServiceByText("Bus", Log);
			 
			 String enteredBusFromLOcSearch = NewDesignTrips.enterLocationForBusesOndetailsPg(Busorigin);
			 String enteredBusToLOcSearch = NewDesignTrips.enterToLocationForBusesOndetailsPg(Busdestination);
			 String enteredBusDatesToSearch = NewDesignTrips.selectBusesJourneyDate(fromDate, fromMonthYear);
			 NewDesignTrips.clickOnAddButton();
			 Thread.sleep(2000);
			 
			 String[] BusLocAfterAdd = NewDesignTrips.getTripBusFromLocFromTripDetailsPageAfterAdd();
			 String[] BusDatesAfterAdd = NewDesignTrips.getTripBusDatesFromTripDetailsPageAfterAdd();
			 NewDesignTrips.clcikOnSearchBus();
			 
			 NewDesignTrips.validateBusFromLocationOnDetailsPage(enteredBusFromLOcSearch, BusLocAfterAdd, Log, screenShots);
			 NewDesignTrips.validateBusToLocationOnDetailsPage(enteredBusToLOcSearch, BusLocAfterAdd, Log, screenShots);
			 NewDesignTrips.validateBusJourneyDateFromDetailsPageToAdd(enteredBusDatesToSearch, BusDatesAfterAdd, Log, screenShots);
			 
			 
			 
				String[] checkindateResultPageForBus = NewDesignBusesResultsPage.getCheckInDateTextFromResultPage();
				String[] ResultPgOrigin = NewDesignBusesResultsPage.getOriginFromresultPg();
				String[] ResultPgDest = NewDesignBusesResultsPage.getDestFromresultPg();
				
//				NewDesignBusesResultsPage.selectSortOption("Price: Low to High", Log);
//				NewDesignBusesResultsPage.validatePricesLowToHigh(Log);
//				Thread.sleep(2000);
//				NewDesignBusesResultsPage.selectSortOption("Price: High to Low", Log);
//				Thread.sleep(1000);
//				NewDesignBusesResultsPage.validatePricesHighToLow(Log);
//				
			//	NewDesignBusesResultsPage.selectSortOption("Departure Time", Log);
				
//				NewDesignBusesResultsPage.selectSortOption("Arrival Time", Log);
//				NewDesignBusesResultsPage.selectSortOption("Duration", Log);
			//	NewDesignBusesResultsPage.selectSortOption("Sort Recommended", Log);

			/*	NewDesignBusesResultsPage.filterByPriceAndValidate(Log, screenShots);
				NewDesignBusesResultsPage.clickAndValidatePolicyDropdown(Log, screenShots);*/
			//	NewDesignBusesResultsPage.clickAndValidateBusTypeOptions(Log, screenShots);
//			NewDesignBusesResultsPage.clickAndValidateSeatType(Log, screenShots);
			//	NewDesignBusesResultsPage.getAndValidatePolicyText(Log, screenShots);
				NewDesignBusesResultsPage.clickAndValidateSearchOperator(Log, screenShots);
			
				String[] Busdetails = NewDesignBusesResultsPage.getBusDetailsFromListingByIndex(0);
				
				String[] SeatType = NewDesignBusesResultsPage.getSeatTypeTextFromresultPgAfterSelect();
				String[] BoardingDetails = NewDesignBusesResultsPage.selectBoardingPoints(Log, screenShots);
				String[] DroppingPoint = NewDesignBusesResultsPage.selectDroppingPoint(Log, screenShots);
			//	NewDesignBusesResultsPage.clcikLowerBirth(Log, screenShots);
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
				String[] BookingPgBusPolicy = NewDesignBusesBookingPage.getPolicyFromBookingPg();
				String[] BookingPgPrice = NewDesignBusesBookingPage.getPriceFromBookingPg();
				String[] BookingPgSeat = NewDesignBusesBookingPage.getSelectedSeatFromBokingPg();
				
				
				NewDesignBusesBookingPage.validateOriginFromResultToBookingpage(BookingPgorigin, ResultPgOrigin, Log, screenShots);
				NewDesignBusesBookingPage.validateDestFromResultToBookingPage(BookingDest, ResultPgDest, Log, screenShots);
				NewDesignBusesBookingPage.validateBusOperatorNameFromListingToBookingPage(Busdetails, BookingPgBusName, Log, screenShots);
				NewDesignBusesBookingPage.validateBoardingPointLocationFromListingToBookingPage(BoardingDetails, BookingPgBoardingPoint, Log, screenShots);
				NewDesignBusesBookingPage.validateDroppingPointLocationFromListingToBookingPage(DroppingPoint, BookingPgdroppingPoint, Log, screenShots);
				NewDesignBusesBookingPage.validateSeaterTypeFromListingToBookingPage(SeatType, BookingPgSeater, Log, screenShots);
			//	NewDesignBusesBookingPage.validateCheckInDateBetweenResultAndBookingPages(checkindateResultPageForBus, BookingPgDate, Log, screenShots);
				NewDesignBusesBookingPage.validateDepartureTimeFromBoardingToBookingPage(BoardingDetails, BookingPgDepartTime, Log, screenShots);
				NewDesignBusesBookingPage.validateArrivalTimeFromListingToBookingPage(Busdetails, BookingPgArrival, Log, screenShots);
				NewDesignBusesBookingPage.validateDurationFromListingToBookingPage(Busdetails, bookingPgDuration, Log, screenShots);
				NewDesignBusesBookingPage.validatePolicyFromListingToBookingPage(Busdetails, BookingPgBusPolicy, Log, screenShots);
				NewDesignBusesBookingPage.validatePriceFromresultToBookingPage(BookingPgPrice, SeatSelectionPrice, Log, screenShots);
				
			//	NewDesignBusesBookingPage.enterEmailForBuses("abc@gmail.com");
			NewDesignBusesBookingPage.enterLastNameForBuses("traveller");
				//NewDesignBusesBookingPage.enterPhNoForBuses("98765432190");
				NewDesignBusesBookingPage.enetrIdCardNumber("1234567");
				NewDesignBusesBookingPage.clickOnIdCardType();
				NewDesignBusesBookingPage.clcikOnAddTripAndContinueButton();
				Thread.sleep(3000);
				NewDesignBusesBookingPage.clcikOnDownButtonInBusesDesclaimer();
				
				String[] BusNameAfterSelected = NewDesignBusesBookingPage.getBusNameFromSelectedInBusDetailsPg();
				String[] BusTypeAfterSelected = NewDesignBusesBookingPage.getBusTypeFromSelectedInBusDetailsPg();
				String[] BusFromLocAfterSelected = NewDesignBusesBookingPage.getBusFromLocFromSelectedInBusDetailsPg();
				String[] BusToLocAfterSelected = NewDesignBusesBookingPage.getBusToLocFromSelectedInBusDetailsPg();
				String[] BusJourneyDateAfterSelected = NewDesignBusesBookingPage.getBusJourneyDateFromSelectedInBusDetailsPg();
				String[] BusDepartTimeAfterSelected = NewDesignBusesBookingPage.getBusDepartTimeFromSelectedInBusDetailsPg();
				String[] BusArrivalTimeAfterSelected = NewDesignBusesBookingPage.getBusArrivalTimeFromSelectedInBusDetailsPg();
				String[] BusBoardingPointAfterSelected = NewDesignBusesBookingPage.getBusBoardingPointFromSelectedInBusDetailsPg();
				String[] BusDroppoingPOintAfterSelected = NewDesignBusesBookingPage.getBusDroppingPointFromSelectedInBusDetailsPg();
				String[] BusDurationAfterSelected = NewDesignBusesBookingPage.getBusDurationFromSelectedInBusDetailsPg();
				String[] BusSeatAfterSelected = NewDesignBusesBookingPage.getBusSeatTextFromSelectedInBusDetailsPg();
				String[] BusPolicyAfterSelected = NewDesignBusesBookingPage.getBusPolicyFromSelectedInBusDetailsPg();
				
				
				NewDesignBusesBookingPage.validateBusNameFromBusBookingToDetailspageAfterAdd(BookingPgBusName, BusNameAfterSelected, Log, screenShots);
				NewDesignBusesBookingPage.validateBusTypeFromBookingToDetailspageAfterAdd(BookingPgSeater, BusTypeAfterSelected, Log, screenShots);
				NewDesignBusesBookingPage.validateFromLocationBetweenBookingAndDetailsAfterAdd(BookingPgorigin, BusFromLocAfterSelected, Log, screenShots);
				NewDesignBusesBookingPage.validateToLocationBetweenBookingAndDetailsAfterAdd(BookingDest, BusToLocAfterSelected, Log, screenShots);
				NewDesignBusesBookingPage.validateJourneyDateBetweenBookingAndDetailsAfterAdd(BookingPgDate, BusJourneyDateAfterSelected, Log, screenShots);
				NewDesignBusesBookingPage.validateDepartTimeBetweenBookingAndDetailsAfterAdd(Busdetails, BusDepartTimeAfterSelected, Log, screenShots);
				NewDesignBusesBookingPage.validateArrivalTimeBetweenBookingAndDetailsAfterAdd(BookingPgArrival, BusArrivalTimeAfterSelected, Log, screenShots);
				NewDesignBusesBookingPage.validateBoardingPointBetweenBookingAndDetailsAfterAdd(BookingPgBoardingPoint, BusBoardingPointAfterSelected, Log, screenShots);
				NewDesignBusesBookingPage.validateDroppingPointBetweenBookingAndDetailsAfterAdd(BookingPgdroppingPoint, BusDroppoingPOintAfterSelected, Log, screenShots);
				NewDesignBusesBookingPage.validateDurationBetweenBookingAndDetailsAfterAdd(bookingPgDuration, BusDurationAfterSelected, Log, screenShots);
				NewDesignBusesBookingPage.validateSelectedSeatBetweenBookingAndDetailsAfterAdd(BookingPgSeat, BusSeatAfterSelected, Log, screenShots);
				NewDesignBusesBookingPage.validateBusPolicyBetweenBookingAndDetailsAfterAdd(BookingPgPolicy, BusPolicyAfterSelected, Log, screenShots);
				
				
				NewDesignBusesBookingPage.clickOnSubmitTripButton(Log);
				
				List<String> DataAfterSubmit = NewDesignBusesBookingPage.getDataInTripReqAfterClickOnSubmit();
				
				NewDesignBusesBookingPage.validateTripNameAfterSubmit(EnteredtripName, DataAfterSubmit, Log, screenShots);
				NewDesignBusesBookingPage.validateFromLocationAfterSubmit(origindetails, DataAfterSubmit, Log, screenShots);
				NewDesignBusesBookingPage.validateToLocationAfterSubmit(destdetails, DataAfterSubmit, Log, screenShots);
				NewDesignBusesBookingPage.validateTripDatesAfterSubmit(journeydatedetails, returndatedetails, DataAfterSubmit, Log, screenShots);
				NewDesignBusesBookingPage.validateServicesMatchAfterSubmit(servicesdetails, DataAfterSubmit, Log, screenShots);
				NewDesignBusesBookingPage.validateTripIdBetweenSubmitAndDetailsPage(DataAfterSubmit, TripIdFromNextPage, Log, screenShots);
				
		// Function to Logout from Application
		// tripgainhomepage.logOutFromApplication(Log, screenShots);
		driver.quit();

	}

	@BeforeMethod
	@Parameters("browser")
	public void launchApplication(String browser) {
		extantManager = new ExtantManager();
		extantManager.setUpExtentReporter(browser);
		className = this.getClass().getSimpleName();
		String testName = className + "_" + number;
		extantManager.createTest(testName); // Get the ExtentTest instance
		test = ExtantManager.getTest();
		extent = extantManager.getReport();
		test.log(Status.INFO, "Execution Started Successful");
		driver = launchBrowser(browser);
		Log = new Log(driver, test);
		screenShots = new ScreenShots(driver, test);
	}

	@AfterMethod
	public void tearDown() {
		if (driver != null) {
			// driver.quit();
			extantManager.flushReport();
		}
	}

}