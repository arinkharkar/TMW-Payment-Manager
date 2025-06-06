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


public class Main {
	
	private static final String password = "a";
	private static final String encryptedInformationPath = "";
	
	
	public static void main(String... args) throws IOException, GeneralSecurityException {

		SecretManager secretManager = new SecretManager(password, encryptedInformationPath); 
	  
		GoogleCalendarManager calendarManager;
		try {
			calendarManager = new GoogleCalendarManager();
		} catch (Exception e) {
			System.err.printf("Error with Google Calendar: %s", e.getMessage());
			return;
		}
		
		List<Event> events;
		
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
		
		// now go through the calendar data and make it 
		for (CalendarData cData : calendarData) {
			try {
				studentBillManager.addOwedBalance(cData.studentName, 0);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		for (CalendarData data : calendarData) {
			System.out.printf("Entry: student:%s time:%d tutor:%s\n", data.studentName, data.timeMinutes, data.tutorName);
		}
		
		
		ExcelManager excelManager;
		try {
		//	excelManager = new ExcelManager();
		} catch (Exception e) {
			System.err.printf("Error with excel: %s", e.getMessage());
		}
		
		
  }
}