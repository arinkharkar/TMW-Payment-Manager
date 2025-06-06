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
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;
import java.util.List;

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
import com.google.api.services.calendar.model.Events;

public class GoogleCalendarManager {
	private static final String APPLICATION_NAME = "TMW Payment Manager";
	private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
	
	private static final String TOKENS_DIRECTORY_PATH = "tokens";
	
	private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
	
	// We only need to read from the calendar
	private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR_READONLY);

	private final static int PORT = 8888; 
	
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
	
	public List<Event> getWeekEvents(DateTime mondayOfWeek) throws Exception {
		
		if (Instant.ofEpochMilli(mondayOfWeek.getValue()).atZone(ZoneId.systemDefault()).toLocalDate().getDayOfWeek() != DayOfWeek.MONDAY) {
			throw new Exception(String.format("Invalid Date given %s", Instant.ofEpochMilli(mondayOfWeek.getValue()).atZone(ZoneId.systemDefault()).toLocalDate().format(DateTimeFormatter.ISO_DATE)));
		}
		
		DateTime startDate = mondayOfWeek;
		DateTime endDate   = new DateTime (Date.from(Instant.ofEpochMilli(mondayOfWeek.getValue()).plus(Duration.ofDays(7))));
		
		return getEvents(startDate, endDate);
	}
	
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
