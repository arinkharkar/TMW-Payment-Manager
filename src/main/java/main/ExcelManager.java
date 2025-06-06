package main;

import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelManager {
	
	private static final String excelResourceName = "/payments.xlsx";
	
	public ExcelManager() throws IOException {
		InputStream in = getClass().getResourceAsStream(excelResourceName);
		
		Workbook workbook;
		
		try {
			workbook = new XSSFWorkbook(in);
		} catch (IOException e) {
			throw new IOException(String.format("Error opening resource %s", excelResourceName));
		}
		
        Sheet sheet = workbook.getSheetAt(0);
        
        for (Row r : sheet) {
        	for (Cell c : r) {
        		System.out.printf("%s\n", c);
        	}
        }
		
		workbook.close();
	}
	
	
}
