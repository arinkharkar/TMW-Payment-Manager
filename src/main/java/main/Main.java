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
		for (Event e : events) {
			System.out.printf("Entry: %s (%s)\n", e.getSummary(), e.getStart().getDateTime());
		}
  }
}