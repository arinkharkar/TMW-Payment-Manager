package main;	


import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;


public class Main {
	
	private static final String password = "a";
	private static final String encryptedInformationPath = "";
	
	
	public static void main(String... args) throws IOException, GeneralSecurityException {
		// TODO: implement secrets, currently does nothing
		SecretManager secretManager = new SecretManager(password, encryptedInformationPath); 
	  
		// Manages the Google Calendar stuff (getting events)
		GoogleCalendarManager calendarManager;
		try {
			calendarManager = new GoogleCalendarManager();
		} catch (Exception e) {
			System.err.printf("Error with Google Calendar: %s", e.getMessage());
			return;
		}
		
		
		List<Event> events;
		
		// Get the events for the current week from Monday - Sunday
		try {
			events = calendarManager.getWeekEvents(GoogleCalendarManager.getPrevMonday());
		} catch (Exception e) {
			System.err.printf("Error getting previous Monday: %s", e.getMessage());
			return;
		}
		
		// turn the raw Google API events into a list of BillData
		List<CalendarData> calendarData;
		try {
			calendarData = calendarManager.eventToCalendarData(events);
		} catch (Exception e) {
			System.err.printf("Error parsing Google Calendar: %s", e.getMessage());
			return;
		}
		
		BillManager studentBillManager = new BillManager();
		BillManager tutorBillManager = new BillManager();
		
		TutorCostManager tutorCostManager;
		// This manages how much each tutor makes per hour
		try {
			tutorCostManager = new TutorCostManager();
		} catch (Exception e) {
			System.err.printf("Error finding tutor rates: %s", e.getMessage());
			return;
		}

		
		for (CalendarData cData : calendarData) {
				
				// get the rate of the tutor
				double tutorRateCentsPerMin;
				double tutorCutAsDecimal;
				try {
					tutorRateCentsPerMin = tutorCostManager.getTutorRateCentsPerMinute(cData.tutorName, cData.studentName);
					tutorCutAsDecimal    = tutorCostManager.getTutorCut(cData.tutorName, cData.studentName);
				} catch(Exception e) {
					System.err.printf("Error finding rate of %s, %s", cData.tutorName, e.getMessage());
					return;
				}
				
				try {
					studentBillManager.addBillData(cData, tutorRateCentsPerMin, tutorCutAsDecimal);
				} catch (Exception e) {
					System.err.printf("Error adding bill data to %s", cData.studentName);
					return;
				}
				

		}
		
		for (Map.Entry<String, String> entry : studentBillManager.getTotalItemizedBillForAllStudents().entrySet()) {
			System.out.printf("%s\nTotal: $%.2f\n\n", entry.getValue(), studentBillManager.getTotalBillForAllStudents().get(entry.getKey()));
			
		}
		
		System.out.println("----------------------------------------------------------------------------------");
		
		for (Map.Entry<String, String> entry : studentBillManager.getTotalItemizedBillForAllTutors().entrySet()) {
			System.out.printf("%s\nTotal: $%.2f\n\n", entry.getValue(), studentBillManager.getTotalBillForAllTutors().get(entry.getKey()));
			
		}
		
		ExcelManager excelManager;
		try {
			excelManager = new ExcelManager();
		} catch (Exception e) {
			System.err.printf("Error with excel: %s", e.getMessage());
		}
	
		
  }
}