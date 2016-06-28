package no.ustr.ont.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FileUtilities {

	
	public static List<File> getListOfFilesFromFileWithHash(File inputfile, String basePath) {
		List<File> files = new ArrayList<File>(); 	
		
		//Find the list of ontologies (The single case will be created later)
		try (BufferedReader br = new BufferedReader(new FileReader(inputfile))) {
		    String line;
		    
		    String base = (basePath != null) ? basePath + "/" : ""; 
		    
		    while ((line = br.readLine()) != null) {
		    	line = line.trim(); 
		    	if (line.charAt(0) != '#') { 
		    		File newFile = new File(base + line); 
		    		files.add(newFile);
		    	}
		    }
		}		
		catch (IOException e) {
			RuntimeException ee = new RuntimeException("error while reading file: " + inputfile); 
			ee.addSuppressed(e);; 
			throw ee; 
		}
		return files;
	}
	
	public static List<String> getLinesFromFileWithHash(File inputFile) {
		List<String> lines = new ArrayList<String>(); 
		try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
		    String line;
		    
		    while ((line = br.readLine()) != null) {
		    	line = line.trim(); 
		    	if (line.charAt(0) != '#' && !line.isEmpty()) {		    		 
		    		lines.add(line);
		    	}
		    }
		}		
		catch (IOException e) {
			RuntimeException ee = new RuntimeException("error while reading file: " + inputFile); 
			ee.addSuppressed(e);; 
			throw ee; 
		}
		return lines; 
	}
	
	
	/**
	 * Takes file with file-names and a list of files L, and removes from L all files with names matching one of the files on the list. 
	 * Returns a new list with these actions performed. Leaves the provided list unaltered. 
	 * from a provided list of 
	 * @param returnSet
	 * @param filenames
	 */

	public static List<File> filterListOfFiles(List<File> returnSet, File filenames) {
		List<File> myList = new ArrayList<>(returnSet); 
		
		Set<String> set = new HashSet<>(getLinesFromFileWithHash(filenames)); 
		for (File f : returnSet)
			if (!set.contains(f.getName()))
				myList.add(f); 
				
		return myList; 
		
	}
}
