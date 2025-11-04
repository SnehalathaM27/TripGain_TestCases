package com.tripgain.common;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.util.ArrayList;
//
//import org.apache.poi.ss.usermodel.DataFormatter;
//import org.apache.poi.xssf.usermodel.XSSFCell;
//import org.apache.poi.xssf.usermodel.XSSFRow;
//import org.apache.poi.xssf.usermodel.XSSFSheet;
//import org.apache.poi.xssf.usermodel.XSSFWorkbook;


import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//
//
//public class Getdata {
//
//	    public static String[] getexceldata() throws IOException, InterruptedException {
//	        // Define the path to the Excel file
//	    	Thread.sleep(3000);
//	       // FileInputStream FileInputStream = new FileInputStream("C:\\Users\\LENOVO\\OneDrive - tripgain.com\\Documents\\EntireTestCases_automation\\Auto\\src\\test\\resources\\testdata\\Logincred.xlsx");
//	        File classpathRoot = new File(System.getProperty("user.dir"));
//	    // File app = new File(classpathRoot.getAbsolutePath() + "\\src\\test\\resources\\testdata\\Logincred.xlsx");
//	      File app = new File(classpathRoot.getAbsolutePath() + "\\src\\test\\resources\\testdata\\HotelsScripts_Skytravelers.xlsx");
//
//	       
//		    //   File app = new File(classpathRoot.getAbsolutePath() + "\\src\\test\\resources\\testdata\\NewDesignLoginCred.xlsx");
//
//	        String fileName = app.toString();
//	        
////	        FileInputStream FileInputStream = new FileInputStream(fileName);            
////	        // Open the Excel workbook
////	        XSSFWorkbook workbook = new XSSFWorkbook(fileName);
//	        
//	        FileInputStream fileInputStream = new FileInputStream(fileName);
//	        XSSFWorkbook workbook = new XSSFWorkbook(fileInputStream);
//
//	        
//	        // Get the sheet named "Sheet1"
//	        XSSFSheet sheet = workbook.getSheet("Sheet1");
//	        
//	        // Retrieve data from row 0 (username) and row 1 (password)
//	        XSSFRow row0 = sheet.getRow(0); // First row (username)
//	        XSSFCell cell0 = row0.getCell(1); // Second column (thilak@tripgain.com)
//	        String validUserName = cell0.getStringCellValue();
//	        
//	        
//	        XSSFRow row1 = sheet.getRow(1); // Second row (password)
//	        XSSFCell cell1 = row1.getCell(1); // Second column (Thanks@123)
//	        String validUserPassword = cell1.getStringCellValue();
//	       
//	        
//	        // Retrieve data from row 2 (invalid username) and row 3 (invalid password)
//	        XSSFRow row2 = sheet.getRow(2); // Third row (invalidusername)
//	        XSSFCell cell2 = row2.getCell(1); // Second column (tarun@tripgain.com)
//	        String inValidUserName = cell2.getStringCellValue();
//	        
//	        XSSFRow row3 = sheet.getRow(3); // Fourth row (inpwd)
//	        XSSFCell cell3 = row3.getCell(1); // Second column (thanks@123)
//	        String inValidUserPassword = cell3.getStringCellValue();
//	        
//	        // Close workbook and file input stream
//	        workbook.close();
//	        fileInputStream.close();
//
//	        
//	        // Return an array with the values for usn, pwd, inusn, inpwd
//	        return new String[] {validUserName, validUserPassword, inValidUserName, inValidUserPassword};
//	    }
//	    
//	    

public class Getdata {

    public static List<Map<String, String>> getexceldata(String sheetName) {
        List<Map<String, String>> dataList = new ArrayList<>();
        File classpathRoot = new File(System.getProperty("user.dir"));
        File app = new File(classpathRoot, "\\src\\test\\resources\\testdata\\HotelsScripts_Skytravelers.xlsx");
        String fileName = app.toString();

        try (FileInputStream fis = new FileInputStream(fileName);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                throw new RuntimeException("Sheet not found: " + sheetName);
            }

            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new RuntimeException("Header row is missing in sheet: " + sheetName);
            }

            int totalCols = headerRow.getLastCellNum();
            DataFormatter formatter = new DataFormatter();

            // Find the index of "TestRun" column
            int testRunColIndex = -1;
            for (int i = 0; i < totalCols; i++) {
                String header = formatter.formatCellValue(headerRow.getCell(i));
                if ("TestRun".equalsIgnoreCase(header)) {
                    testRunColIndex = i;
                    break;
                }
            }

            if (testRunColIndex == -1) {
                throw new RuntimeException("\"TestRun\" column not found in sheet: " + sheetName);
            }

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                // Check if TestRun column value is "SKIP"
                String testRunValue = formatter.formatCellValue(row.getCell(testRunColIndex));
                if ("SKIP".equalsIgnoreCase(testRunValue.trim())) {
                    continue; // Skip this row
                }

                Map<String, String> rowMap = new HashMap<>();
                boolean hasData = false;

                for (int j = 0; j < totalCols; j++) {
                    Cell headerCell = headerRow.getCell(j);
                    if (headerCell == null) continue;

                    String key = formatter.formatCellValue(headerCell);
                    String value = formatter.formatCellValue(row.getCell(j));

                    if (value != null && !value.trim().isEmpty()) {
                        hasData = true;
                    }

                    rowMap.put(key, value);
                }

                if (hasData) {
                    dataList.add(rowMap);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return dataList;
    }
	    
	}