package no.ustr.ont.util.ns;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

public class PCA  {
	
	public static final String NS = "http://data.posccaesar.org/rdl/"; 
	static final Resource r( String localname ) { return ResourceFactory.createResource(NS +  localname );}
	static final Property p( String localname ) { return ResourceFactory.createProperty(NS +  localname );}

	
	public static final Resource System = r("RDS316259");  
}
