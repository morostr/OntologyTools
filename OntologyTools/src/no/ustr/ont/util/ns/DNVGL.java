package no.ustr.ont.util.ns;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

public class DNVGL  {
	public static final String NS = "http://data.dnvgl.com/rdl/";
	public static final String prefix = "dnvgl"; 
	public static final String ontology = "http://data.dnvgl.com/ontology/"; 
	
	public final static Resource r( String localname ) { return ResourceFactory.createResource(NS +  localname );}
	public static final Property p( String localname ) { return ResourceFactory.createProperty(NS +  localname );}

	
}
