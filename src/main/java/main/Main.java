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
import java.util.List;
import java.util.Map;


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
		
		// now go through the calendar data and add the student billings to it all
		for (CalendarData cData : calendarData) {
			try {
				
				// get the rate of the tutor
				double tutorRateCentsPerMin;
				double tutorCutAsDecimal;
				try {
					tutorRateCentsPerMin = tutorCostManager.getTutorRateCentsPerMinute(cData.tutorName, cData.studentName);
				} catch(Exception e) {
					System.err.printf("Error finding rate of %s, %s", cData.tutorName, e.getMessage());
					return;
				}
				
				// get the cut the tutor makes
				try {
					tutorCutAsDecimal = tutorCostManager.getTutorCut(cData.tutorName, cData.studentName);
				} catch (Exception e) {
					System.err.printf("Error finding cut of %s, %s", cData.tutorName, e.getMessage());
					return;
				}
				
				studentBillManager.addOwedBalance(cData.studentName, tutorRateCentsPerMin * cData.timeMinutes);
				// add the total bill to the students owed balance, times the cut (should be 0.8)
				tutorBillManager.addOwedBalance(cData.tutorName, tutorRateCentsPerMin * cData.timeMinutes * tutorCutAsDecimal);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		Map<String, Double> billMap = studentBillManager.getBillMap();
		Map<String, Double> tutorOwedMap = tutorBillManager.getBillMap();
		
		for (Map.Entry<String, Double> entry : billMap.entrySet()) {
			System.out.printf("%s owes $%.2f\n", entry.getKey(), entry.getValue() / 100.0);
		}
		
		for (Map.Entry<String, Double> entry : tutorOwedMap.entrySet()) {
			System.out.printf("You owe %s $%.2f\n", entry.getKey(), entry.getValue() / 100.0);
		}

		
		
		ExcelManager excelManager;
		try {
			excelManager = new ExcelManager();
		} catch (Exception e) {
			System.err.printf("Error with excel: %s", e.getMessage());
		}
	
		
  }
}