package main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class BillManager {
	

	List<BillData> owedBalance;
	
	
	public static final double CENTS_PER_DOLLAR = 100.0;
	// if a transaction is more than this, lets have manual verification, it may be a bug
	public static final double suspicousAmount = 1800.0 * CENTS_PER_DOLLAR;
	
	public BillManager() {
		
		owedBalance = new ArrayList<BillData>();
	}
	
	public void addBillData(CalendarData calendarData, double rateCentsPerMin, double tutorCutAsDecimal) throws Exception {
		BillData newBillData = new BillData();
		
		newBillData.tutorName = calendarData.tutorName;
		newBillData.studentName = calendarData.studentName;
		newBillData.dateTimeOfTutoring = calendarData.dateTimeOfTutoring;
		
		newBillData.amount = rateCentsPerMin * calendarData.timeMinutes / CENTS_PER_DOLLAR;
		newBillData.tutorCut = newBillData.amount * tutorCutAsDecimal;
		
		if (newBillData.amount > suspicousAmount || newBillData.amount <= 0)
			throw new Exception(String.format("Error amount %d charged to %s seems invalid", newBillData.amount, newBillData.studentName));
		
		owedBalance.add(newBillData);
	}
	
	Map<String, Double> getTotalBillForAllStudents() {
		// lets us iterate alphabetically
		Map<String, Double> map = new TreeMap<String, Double>();
		for (BillData billData : owedBalance) {
			double currentTotal;
			if (map.get(billData.studentName) == null)
				currentTotal = 0;
			else
				currentTotal = map.get(billData.studentName);
			
			currentTotal += billData.amount;
			
			map.put(billData.studentName, currentTotal);
		}
		return map;
	}
	
	Map<String, Double> getTotalBillForAllTutors() {
		// lets us iterate alphabetically
		Map<String, Double> map = new TreeMap<String, Double>();
		for (BillData billData : owedBalance) {
			double currentTotal;
			if (map.get(billData.tutorName) == null)
				currentTotal = 0;
			else
				currentTotal = map.get(billData.tutorName);
			
			currentTotal += billData.tutorCut;
			
			map.put(billData.tutorName, currentTotal);
		}
		return map;
	}
	
	Map<String, String> getTotalItemizedBillForAllStudents() {
		// lets us iterate alphabetically
		Map<String, String> map = new TreeMap<String, String>();
		for (BillData billData : owedBalance) {
			String curEntry = map.get(billData.studentName);
			if (curEntry == null) 
				curEntry = "";
			
			String newEntry = String.format("%s worked with %s on %s, total : $%.2f\n", billData.studentName, billData.tutorName, billData.dateTimeOfTutoring, billData.amount);
			map.put(billData.studentName, curEntry + newEntry);
		}
		return map;
	}
	
	Map<String, String> getTotalItemizedBillForAllTutors() {
		// lets us iterate alphabetically
		Map<String, String> map = new TreeMap<String, String>();
		for (BillData billData : owedBalance) {
			String curEntry = map.get(billData.tutorName);
			if (curEntry == null) 
				curEntry = "";
			
			String newEntry = String.format("%s worked with %s on %s, total : $%.2f\n", billData.tutorName, billData.studentName, billData.dateTimeOfTutoring, billData.tutorCut);
			map.put(billData.tutorName, curEntry + newEntry);
		}
		return map;
	}
	
}
