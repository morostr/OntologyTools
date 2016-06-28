package no.ustr.ont.rdf;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Properties;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.util.FileManager;
import org.apache.jena.vocabulary.OWL;


public class JenaTools {

	/**
	 * Removes periods, dashes and so on from the URI. 
	 * This method will be in constant development, the only promise given is that the new URI is
	 * better than the one provided, not that it produces the same URI each time. 
	 * 
	 * @param string
	 * @return A string that follows best practice for URIs. 
	 * 
	 */
	public static String cleanUri(String string) {
		return string
				.replaceAll("\\s+", "_")
				.replaceAll("\\.", "_")
				.replaceAll("/",  "")
				.replaceAll("\\\\", "")
				.replaceAll("%",""); 
	}
	
	
	 protected static final Property property( String uri, String local )
     { return ResourceFactory.createProperty( uri, local ); }


	public static void writeTtl(Model m, File outputFile) throws FileNotFoundException {
		FileOutputStream fos= new FileOutputStream(outputFile); 		
		m.write(fos, "TTL");		
	}


	public static Model loadModel(File ontologyFile) {
		return FileManager.get().loadModel(ontologyFile.getAbsolutePath()); 
	}


	public static void addOntologyMetadata(Model m, String ontologyUri,  List<String> importsList) {
		
		Resource ont = m.createResource(ontologyUri);
		importsList.forEach(l -> ont.addProperty(OWL.imports, m.createResource(l)));
		
		
	}
	
	
	public static Model addFileToJenaModel(File file, Model model) {
		
		String filename = "file:///" + file.getAbsolutePath(); 

		// read the RDF/XML file

		int dotPos = filename.lastIndexOf(".") + 1;
		String extension = filename.substring(dotPos);


		if (extension.toUpperCase().equals("TTL")) {
			model.read(filename, null, "TTL");
		}
		else if (extension.toUpperCase().equals("N3")) {
			model.read(filename, null, "N3");
		}
		else if (extension.toUpperCase().equals("OWL")) {
			model.read(filename,null, "RDF/XML");
		}
		else if (extension.toUpperCase().equals("RDF")) {
			model.read(filename, null, "RDF/XML");
		}
		else return null; 


		return model;
	}


	
	
	
}
