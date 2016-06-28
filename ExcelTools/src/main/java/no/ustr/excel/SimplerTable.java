package no.ustr.excel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/*
 * A simple table able to store values
 * The table columns can be reached by name or index
 * the rows start at 0
 * 
 * 
 */
public class SimplerTable<C,V> implements Iterable<SimplerRow<C,V>> {
	private List<C> headers; 
	private List<SimplerRow<C,V>> rows;
	
	
	public SimplerTable(List<C> headers) {
		this.headers = headers;  
		rows = new ArrayList<SimplerRow<C,V>>(); 
	}
	
	
	/**
	 * Adds a value to a row. If rownr > maxRowNr+1, then rownr = maxRowNr+1
	 * Will carelessly overwrite existing values
	 */
	public void add(int rownr, int column, V value) {
		C key = headers.get(column); 
		set(rownr, key, value); 
	}
	
	
	
	public void set(int rownr, C column, V value) {
		if (rownr>= rows.size()) {
			throw new RuntimeException("No such row"); 
		}
		else {
			rows.get(rownr).set(column, value);
		}
	}
	
	/**
	 * 
	 * @param rownr
	 * @return the row deleted
	 */
	public SimplerRow removeRow(int rownr) {		
		return rows.remove(rownr);
	}


	public int width() {
		return headers.size();
	}


	public String[] getEmptyRowArray() {
		return new String[width()];
	}


	public SimplerRow<C,V> addNewRow() {
		// TODO Auto-generated method stub
		SimplerRow<C,V> sr = new SimplerRow<C,V>(headers);
		rows.add(sr);
		return sr; 
		
	}


	public Iterator<SimplerRow<C, V>> iterator() {
		// TODO Auto-generated method stub
		return rows.iterator();
	}


	public List<C> getHeaders() {
		// TODO Auto-generated method stub
		return Collections.unmodifiableList(headers); 
	}
	
	
	
	
	
}
