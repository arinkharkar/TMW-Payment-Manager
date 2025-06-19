package main;

import java.util.Map;
import java.util.TreeMap;

public class TutorCostManager {
	
	// maps tutors' names to their rate in cents / minute
	Map<String, Double> tutorCostMap;
	
	// maps tutors' names to the cut they get (as a decimal not percent)
	Map<String, Double> tutorCutMap;
 	
	private static final double HOURS_PER_MIN = 1.0/60.0;
	private static final double CENTS_PER_DOLLAR = 100.0/1.0;
	
	double fromDollarsPerHourToCentsPerMinute(double dollarsPerHour) {
		return (dollarsPerHour * HOURS_PER_MIN * CENTS_PER_DOLLAR);
	}
	
	// TODO: regex to validate tutorName
	// Adds the rate of the tutor to the map, if we need to overload the students rate (if the have a discount or something else then add their name here)
	void addTutorRate(String tutorName, String studentName, double rateDollarsPerHour) throws Exception {
		// if we have a custom rate for the student, lets just append the name directly to store this in the map
		addTutorRate(tutorName + studentName, rateDollarsPerHour);
		
	}
	
	void addTutorRate(String tutorName, double rateDollarsPerHour) throws Exception {
		if (rateDollarsPerHour <= 0)
			throw new Exception(String.format("Error, invalid rate: %0.2f given for %s", rateDollarsPerHour, tutorName));
		
		tutorCostMap.put(tutorName, fromDollarsPerHourToCentsPerMinute(rateDollarsPerHour));
		
	}
	
	void addTutorCut(String tutorName, String studentName, double cutAsDecimal) throws Exception {
		addTutorCut(tutorName + studentName, cutAsDecimal);
	}
	
	// for an 80% cut, input 0.8
	void addTutorCut(String tutorName, double cutAsDecimal) throws Exception {
		if (cutAsDecimal <= 0 || cutAsDecimal > 1)
			throw new Exception(String.format("Error, invalid rate: %0.2f given for %s", cutAsDecimal, tutorName));
		
		tutorCutMap.put(tutorName, cutAsDecimal);
		
	}
	
	double getTutorCut(String tutorName, String studentName) throws Exception {
		// first, lets check if the student has an overriden cut, if they do then return this cut
		if (tutorCutMap.get(tutorName + studentName) != null) {
			return tutorCutMap.get(tutorName + studentName);
		}
		
		if (tutorCutMap.get(tutorName) == null) 
			throw new Exception(String.format("Error, could not find cut for %s", tutorName));
		return tutorCutMap.get(tutorName);
	}
	
	double getTutorRateCentsPerMinute(String tutorName, String studentName) throws Exception {
		// first, lets check if the student has an overriden rate, if they do then return this rate
		if (tutorCostMap.get(tutorName + studentName) != null)
			return tutorCostMap.get(tutorName + studentName);
		
		if (tutorCostMap.get(tutorName) == null)
			throw new Exception(String.format("Error, could not find rate for %s", tutorName));
		return tutorCostMap.get(tutorName);
	}
	
	public TutorCostManager() throws Exception {
		tutorCostMap = new TreeMap<String, Double>();
		tutorCutMap = new TreeMap<String, Double>();
		// for now, hardcode the tutor owed values, later we need to read from the excel
		
		addTutorRate("Malini", 90);
		addTutorRate("Arin", 60);
		addTutorRate("Arin", "Nikhil", 80);
		
		addTutorCut("Malini", 1);
		addTutorCut("Arin", 0.8);
		
	}
	

	
}
