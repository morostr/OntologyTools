package no.ustr.excel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SimplerRow<C,V> implements Iterable<V> {
	private List<C> headers; 
	private Map<C,V> values; 
	
	public SimplerRow(List<C> headers) {
		this.headers = headers;
		this.values = new HashMap<C,V>();  
	}
	
	
	public void set(int i, V value) {
		if (i<headers.size())		
			values.put(headers.get(i), value);
		
	}
	
	public void set(C column, V value) {
		values.put(column, value); 
	}


	public Iterator<V> iterator() {
		ArrayList<V> l = new ArrayList<V>();
		for (C col : headers) {
			l.add(values.get(col));
		}
		return l.iterator();
	}
	
	public V get(C column) {
		return values.get(column);
	}
	
}
