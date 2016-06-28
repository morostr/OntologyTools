package no.ustr.ont.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringReplacer {
	private String source;
	private HashMap<String, String> values; 
	private String prefix ="#";
	private String postfix = "";
	private boolean allowEmptyValues = false; 
	
	public static void main(String[] args) {
		StringReplacer sr = new StringReplacer("this is \\var1\\ and here are \\var2\\", "\\", "\\"); 
		sr.setVariable("var1",  "REPLACED");
		sr.setVariable("var2.",  "REPLACED2");
		
		sr.setAllowEmptyValues(true);;
		System.out.println(sr.getString()); 
	}
	
	public StringReplacer(String source) {
		this.source = source;
		this.values = new HashMap<>(); 
	}
	
	public StringReplacer(String source, String prefix) {
		this(source);
		this.prefix = prefix; 
	}
	
	public StringReplacer(String source, String prefix, String postfix) {
		this(source, prefix); 
		this.postfix = postfix; 
	}
	
	public void setAllowEmptyValues(boolean allowEmptyValues) {
		this.allowEmptyValues = allowEmptyValues; 				
	}
	
	public String getString(Map<String,String> values) {
		Set<String> missingValues = getMissingValues(values); 
		if (allowEmptyValues || missingValues.isEmpty() ) {
			String currentString = source; 
			for (String varname : values.keySet()) {
				currentString = replaceVariable(varname, values.get(varname), currentString); 
			}
			return currentString; 
		}
		else 
			throw new RuntimeException("Not all variables have values, and allowEmptyValues is false."); 		 
	}
	
	public String getString() {
		return getString(values); 
	}
	

	private Set<String> getMissingValues(Map<String, String> values2) {
		Set<String> varSet = getVariableSet();
		for (String var : values2.keySet()) {
			varSet.remove(var);
			//System.out.println("Removed:" + var);
		}
		return varSet; 
	}

	private Set<String> getVariableSet() {
		
		String fullRegexp = "" + Pattern.quote(prefix) + "(\\w+)" + Pattern.quote(postfix); 
		// System.out.println(fullRegexp); 
		Matcher m = Pattern.compile(fullRegexp )
					.matcher(source);	
		 
		Set<String> variableSet = new HashSet<>(); 
		while (m.find()) {
			String var = m.group(); 
			//System.out.println("Found:" + var);
			variableSet.add(var); 
		}
		return variableSet;
	}

	private String replaceVariable(String varname, String value, String currentString) {
		String varInSource = Pattern.quote(prefix) + varname + Pattern.quote(postfix); 
		// System.out.println(varname + "  " + value);
		return currentString.replaceAll(varInSource, value); 
	}
	
	
	public void setVariable(String varname, String value) {		
		values.put(cleanVarName(varname), value);
	}

	private String cleanVarName(String varname) {
		varname = varname.trim();
		if (varname.startsWith(prefix))
			varname = varname.substring(prefix.length());
		if (varname.endsWith(postfix))
			varname = varname.substring(0, varname.length()-postfix.length());
		if (varname.matches("\\w+"))
			return varname;
		
		throw new IllegalArgumentException("Illegal format for variablename :\"" + varname + "\""); 
	}
}
