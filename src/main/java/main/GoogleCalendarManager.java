package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;

public class GoogleCalendarManager {
	private static final String APPLICATION_NAME = "TMW Payment Manager";
	private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
	
	private static final String TOKENS_DIRECTORY_PATH = "tokens";
	
	private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
	
	private static final String REGEX_PATTERN = "^([A-Za-z]+)[A-Za-z\\s']*\\((\\d+)\\s*(?:minutes|mins|min) with (.*?)\\)$";
	
	private static final String VALID_NAME_REGEX = "^[A-Z][a-z]+(?:[ '-][A-Z]?[a-z]+)*$";
	
	// We only need to read from the calendar
	private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR_READONLY);

	private final static int PORT = 8887; 
	
	Calendar calendar;
	
	
	
	
	/**
	 * Creates an instance of GoogleCalendarManager, used to get events from the Google Calendar
	 * @throws Exception if any credentials were invalid or failed to connect to google API services
	 */
	public GoogleCalendarManager() throws Exception {
		NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		
		calendar = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT, CREDENTIALS_FILE_PATH))
		                       .setApplicationName(APPLICATION_NAME)
		                       .build();	
	}
	
	/**
	 * Gets all calendar events from the start time to end time
	 * @param start The DateTime object representing the beginning of objects to get
	 * @param end The DateTime object representing the end of objects to get
	 * @return a list of all events in that time
	 * @throws IOException
	 */
	public List<Event> getEvents(DateTime start, DateTime end) throws IOException {
	    List<Event> events = calendar.events().list("primary")
				        .setTimeMin(start)
				        .setTimeMax(end)
				        .setOrderBy("startTime")
				        .setSingleEvents(true)
				        .execute()
				        .getItems();
	    return events;
	}
	
	/**
	 * Gets all calendar events from the Monday of the week to Sunday
	 * @param mondayOfWeek the Monday to start at
	 * @return a list of all events in that time
	 * @throws Exception
	 */
	public List<Event> getWeekEvents(DateTime mondayOfWeek) throws Exception {
		
		if (Instant.ofEpochMilli(mondayOfWeek.getValue()).atZone(ZoneId.systemDefault()).toLocalDate().getDayOfWeek() != DayOfWeek.MONDAY) {
			throw new Exception(String.format("Invalid Date given %s", Instant.ofEpochMilli(mondayOfWeek.getValue()).atZone(ZoneId.systemDefault()).toLocalDate().format(DateTimeFormatter.ISO_DATE)));
		}
		
		DateTime startDate = mondayOfWeek;
		DateTime endDate   = new DateTime (Date.from(Instant.ofEpochMilli(mondayOfWeek.getValue()).plus(Duration.ofDays(7))));
		
		return getEvents(startDate, endDate);
	}
	
	/**
	 * Converts a list of raw Google calendar data into a list of CalendarData
	 * @param events The events to convert
	 * @return The bill data list
	 * @throws Exception 
	 */
	public List<CalendarData> eventToCalendarData(List<Event> events) throws Exception {
		/* FORMAT:
		 * [Name] ([Time spent] minutes with [Tutor])
		 * We use a regex to extract this data from the list
		 */
		Pattern calendarValidationPattern = Pattern.compile(REGEX_PATTERN);
		
		List<CalendarData> billDatas = new ArrayList<CalendarData>();
		
		for (Event event : events) {
			// Match the regex with the event title (contains the details)

			Matcher matcher = calendarValidationPattern.matcher(event.getSummary());
			if (!matcher.matches()) {
				throw new Exception(String.format("Failed to parse \"%s\", the calendar event has an invalid format!", event.getSummary()));
			}
			
			String studentName = matcher.group(1);
			
			// make sure the timeSpent is an actual number
			int timeSpent;
			try {
				timeSpent = Integer.parseInt(matcher.group(2));
			} catch (NumberFormatException e) {
				throw new Exception(String.format("Error, couldnt parse %s, invalid format!", event.getSummary()));
			}
            String tutorName = matcher.group(3);
            
            // now we check if the names are valid based on another regex
            Pattern namePattern = Pattern.compile(VALID_NAME_REGEX);
            
            
            matcher = namePattern.matcher(tutorName);
            
            if (!matcher.matches()) {
            	throw new Exception(String.format("Failed to parse %s, the tutor name has an invalid format! (enable no-check-names if this is an error)", event.getSummary()));
            }
            
            matcher = namePattern.matcher(studentName);
            
            if (!matcher.matches()) {
            	throw new Exception(String.format("Failed to parse %s, the student name has an invalid format! (enable no-check-names if this is an error)", event.getSummary()));
            }
            
            // now lets get the date of the event
            DateTime eventStartDateTime = event.getStart().getDateTime();
            
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm a");
            
            String finalDateTimeString;
            
            try {
            	 OffsetDateTime odt = OffsetDateTime.parse(eventStartDateTime.toStringRfc3339());

                 // Convert to system default timezone
                 odt = odt.atZoneSameInstant(ZoneId.systemDefault()).toOffsetDateTime();
                 
                 finalDateTimeString = odt.format(outputFormatter);
                 
            } catch (Exception e) {
            	throw new Exception("Error: Could not get/parse the starting date/time of a calendar event");
            }
            
            CalendarData billData = new CalendarData();
            
            billData.timeMinutes = timeSpent;
            billData.studentName = studentName;
            billData.tutorName = tutorName;
            billData.dateTimeOfTutoring = finalDateTimeString;
            
            System.out.println("Final time: " + billData.dateTimeOfTutoring);
            
            billDatas.add(billData);
            
		}
		
		
		return billDatas;
		
	}
	
	
	/**
	 * Gets the previous Monday from the current system date & time
	 * @return DateTime object of the previous monday
	 */
	public static DateTime getPrevMonday() {
		return new DateTime(Date.from(LocalDate.now().with(DayOfWeek.MONDAY).minusWeeks(LocalDate.now().getDayOfWeek() == DayOfWeek.MONDAY ? 1 : 0).atStartOfDay(ZoneId.systemDefault()).toInstant()));
	}
	
	private HttpRequestInitializer getCredentials(NetHttpTransport HTTP_TRANSPORT, String credentialsFilePath) throws FileNotFoundException, IOException {
		// Load client secrets.
	    InputStream in = getClass().getResourceAsStream(credentialsFilePath);
	    if (in == null) { 
	      throw new FileNotFoundException(String.format("Resource not found: %s", credentialsFilePath));
	    }
	    
	    GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

	    // Build flow and trigger user authorization request.
	    GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
	        HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
	        .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
	        .setAccessType("offline")
	        .build();
	    
	    
	    LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(PORT).build();
	    Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
	    //returns an authorized Credential object.
	    return credential;
	}
	
	

}
