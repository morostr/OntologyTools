package no.ustr.ont.util.ns;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

public class VIS {
	public static final String NS = "http://data.dnvgl.com/vis/";
	static final Resource r( String localname ) { return ResourceFactory.createResource(NS +  localname );}
	static final Property p( String localname ) { return ResourceFactory.createProperty(NS +  localname );}
	
	
	public static final String funcPrefix = "func_";
	public static final String prefix = "vis"; 
	public static final Resource getFunction(String id) {return r(funcPrefix + id); }
	
}
