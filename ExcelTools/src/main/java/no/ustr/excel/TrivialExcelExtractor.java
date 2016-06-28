package no.ustr.excel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.streaming.SXSSFRow.CellIterator;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class TrivialExcelExtractor {
	
	
	private DataFormatter formatter; 
	private FormulaEvaluator evaluator; 
	
	private static String unnamedColumnNameBase = "unnamed_";
	private int unnamedColIdx=0;
	public TrivialExcelExtractor() {
		formatter = new DataFormatter(); //creating formatter using the default locale		 
		
		
	}
	
		
	public SimplerTable<String,String> parse(File file, String sheet) throws IOException {
	    
	    FileInputStream fis = new FileInputStream(file);
	    XSSFWorkbook wb = new XSSFWorkbook(fis);

	    evaluator = wb.getCreationHelper().createFormulaEvaluator();
	    
	    XSSFSheet sh = wb.getSheet(sheet);

	    //Assuming first row contains headers
	    List<String> headers = new ArrayList<String>(); 
	    Iterator<Row> rowit = sh.iterator(); 
	    
	    Row row = rowit.next(); 
	    Iterator<Cell> ci = row.iterator();
	    while (ci.hasNext()) {
	    	Cell c = ci.next(); 
	    	String cellValue = forceGetCellValue(c);
	    	
	    	if (cellValue == "") {
	    		cellValue = unnamedColumnNameBase + unnamedColIdx;
	    		unnamedColIdx++;
	    	}
	    		
	    	headers.add(cellValue);
	    }
	    	    	    
	    SimplerTable<String,String> st = new SimplerTable<String,String>(headers); 
	    
	    
	    while (rowit.hasNext()) {
	    	row = rowit.next(); 
	    	ci = row.iterator();
	    		    	
	    	SimplerRow<String,String> sr = st.addNewRow();
	    	for (int idx = 0; ci.hasNext(); idx++) {	    	
	    		Cell c = ci.next();
	    		String cellValue = forceGetCellValue(c);	    		
	    		sr.set(idx,cellValue);	    		
	    	}	    		    	
	    }	    	

	    return st; 
	}

	private String forceGetCellValue(Cell cell) {
		
		String value = formatter.formatCellValue(cell,evaluator); //Returns the formatted value of a cell as a String regardless of the cell type
		return value;
	}
	
	
	
	public static void main(String[] args) throws IOException {
		File f = new File("C:/Data/maritim/modam/doc/ABB Signal list.xlsx");
		
		SimplerTable<String, String> st = new TrivialExcelExtractor().parse(f, "RDS TAG NAMES");
		
		for (String v : st.getHeaders()) {
			System.out.print("| " + v + "|");
		}
		
		for (SimplerRow<String, String> row : st) {
			for (String v : row) {
				System.out.print("| " + v + "|");
			}
			System.out.println("");
		}	
				
	}
	
}
