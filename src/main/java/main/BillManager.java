package main;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class BillManager {
	Map<String, Integer> owedBalance;
	
	// if a transaction is more than this, lets have manual verification, it may be a bug
	public static final int suspicousAmount = 1800;
	
	public BillManager() {
		// A TreeMap stores values alphabetically, perfect for iterating through people's names alphabetically
		owedBalance = new TreeMap<>();
	}
	
	/** 
	 * Finds the current owed balance of the student
	 * @param name The name of the student
	 * @return The owed balance of the student
	 * @throws NullPointerException if the student wasn't found
	 */
	int getOwedBalance(String name) throws NullPointerException {
		Integer balance = owedBalance.get(name);
		// if the name doesn't exist
		if (balance == null) {
			throw new NullPointerException(String.format("Name %s does not exist!", name));
		}
		return balance;
	}
	
	
	
	/** 
	 * Sets the current owed balance of the student
	 * @param name The name of the student
	 * @param balance The balance to set
	 * @throws Exception if the student wasn't found or if the amount was too much
	 */
	void setOwedBalance(String name, int balance) throws Exception {
		if (owedBalance.get(name) == null) {
			throw new NullPointerException(String.format("Name %s does not exist!", name));	
		}
		
		if (Math.abs(balance) > suspicousAmount) {
			throw new Exception(String.format("Refusing to set %s owed balance to %d, the amount seems suspicious (defined as greater/less than %d)", name, balance, suspicousAmount));
		}
		
		owedBalance.put(name, balance);
	}
	
	/** 
	 * Adds to the current owed balance of the student, creating the student if he/she weren't found
	 * @param name The name of the student
	 * @param balance The balance to add
	 * @throws Exception if the amount was too much
	 */
	void addOwedBalance(String name, int balance) throws Exception {
		Integer startingBalance = owedBalance.get(name);
		
		// if the student didnt exist, start the balance at 0
		if (startingBalance == null) {
			startingBalance = 0;
		}
		
		if (Math.abs(balance) > suspicousAmount) {
			throw new Exception(String.format("Refusing to add %s owed balance to %d, the amount seems suspicious (defined as greater/less than %d)", name, balance, suspicousAmount));
		}
		
		owedBalance.put(name, balance + startingBalance);
	}
	
	/** 
	 * Subtracts from the current owed balance of the student, creating the student if he/she weren't found
	 * @param name The name of the student
	 * @param balance The balance to subtract
	 * @throws Exception if the amount was too much
	 */
	void subtractOwedBalance(String name, int balance) throws Exception {
		Integer startingBalance = owedBalance.get(name);
		if (startingBalance == null) {
			startingBalance = 0;
		}
		
		if (Math.abs(balance) > suspicousAmount) {
			throw new Exception(String.format("Refusing to subtract %s owed balance to %d, the amount seems suspicious (defined as greater/less than %d)", name, balance, suspicousAmount));
		}
		
		owedBalance.put(name, balance - startingBalance);
	}
}
