package no.ustr.ont.rdf;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.RDFS;

public class FusekiConfigFile {
	private static final String fuseki = "http://jena.apache.org/fuseki#";
	private static final String ja = "http://jena.hpl.hp.com/2005/11/Assembler#";
	private static final String tdb = "http://jena.hpl.hp.com/2008/tdb#";

	private static final Property p( String ns , String local)
	{ return ResourceFactory.createProperty( ns, local ); }
	private static final Resource r( String ns , String local)
	{ return ResourceFactory.createResource( ns+ local ); }

	
	private static final Property fusekiServices = p(fuseki, "services");
	private static final Resource fusekiServer = r(fuseki,"Server");
	private static final Resource fusekiService = r(fuseki,"Service");
	private static final Property fusekiname = p(fuseki,"name");
	private static final Property fusekiserviceQuery = p(fuseki,"serviceQuery");
	private static final Property fusekidataset = p( fuseki,"dataset");
	private static final Resource jaRDFDataset = r( ja,"RDFDataset");
	private static final Property jadefaultGraph = p(ja,"defaultGraph");
	private static final Resource jaMemoryModel = r( ja,"MemoryModel");
	private static final Property jagraphName = p( ja,"graphName");
	private static final Property jacontent = p(ja,"content");
	private static final Property jaGraph = p(ja,"graph");
	private static final Property janamedGraph = p(ja,"namedGraph");
	private static final Property tdbunionDefaultGraph = p(tdb,"unionDefaultGraph");
	
	private static final Property jaexternalContent = p(ja,"externalContent");
	
	public static Map<String,String> getFusekiPrefixMapping() {
		HashMap<String, String> a = new HashMap<String,String>();
		a.put("","http://d/");
		a.put("fuseki","http://jena.apache.org/fuseki#");
		a.put("rdf","http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		a.put("rdfs","http://www.w3.org/2000/01/rdf-schema#");
		a.put("tdb","http://jena.hpl.hp.com/2008/tdb#");
		a.put("ja","http://jena.hpl.hp.com/2005/11/Assembler#");
		return a; 		
	}
	
	public static Model getFusekiConfigFile(Collection<File> files, Collection<File> auxfiles, String datasetname)  {
		Model m = ModelFactory.createDefaultModel();
		
		m.setNsPrefixes(getFusekiPrefixMapping());
		String path = "file:///";
		//server
		/*
[] rdf:type fuseki:Server ;
   fuseki:services <#service1> .
		 */
		
		Resource server = m.createResource(null, fusekiServer);		
		Resource serviceOne = m.createResource(m.expandPrefix(":service1"), fusekiService);
		Resource dataset = m.createResource(m.expandPrefix(":#dataset"), jaRDFDataset);
	
		Resource defaultGraphContent =m.createResource();
		
		server.addProperty(fusekiServices, serviceOne);
		
		
		serviceOne.addProperty(fusekiname, (datasetname!=null) ? datasetname : "serviceOne" );
		serviceOne.addProperty(fusekiserviceQuery, "sparql");
		serviceOne.addProperty(fusekiserviceQuery, "query");
		serviceOne.addProperty(fusekidataset, dataset);
		
		dataset.addProperty(RDFS.label, "ds");		
	
		Resource defaultGraph = m.createResource(null, jaMemoryModel);
		dataset.addProperty(jadefaultGraph, defaultGraph); 		
		defaultGraph.addProperty(jacontent, defaultGraphContent);
		
		
		
		//dataset.addLiteral(tdbunionDefaultGraph,  true);
		for (File f : files ) {
			Resource fileResource = addGraphToDataset(m, path, dataset, f);				
			
			defaultGraphContent.addProperty(jaexternalContent, fileResource);
		}
		
		if (auxfiles != null) {
			for (File f : auxfiles) {
				addGraphToDataset(m, path, dataset, f);				
			}
		}
		
		return m;
		
	}
	private static Resource addGraphToDataset(Model m, String path,
			Resource dataset, File f) {
		Resource mmodel = m.createResource(m.expandPrefix(":memModel_" + f.getName()), jaMemoryModel);
		Resource content = m.createResource();			
		Resource fileResource;
		try {
			fileResource = m.createResource(path + f.getCanonicalPath().replaceAll("\\\\", "/"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException("Problems creating canonical path from file:" + f.getAbsolutePath());
		}
		Resource namedGraph = m.createResource();
		
		dataset.addProperty(janamedGraph, namedGraph);
		namedGraph.addProperty(jagraphName, m.expandPrefix(":graph_" + f.getName()));
		namedGraph.addProperty(jaGraph, mmodel);
		mmodel.addProperty(jacontent, content);			
		content.addProperty(jaexternalContent, fileResource);
		return fileResource;
	}
	
	
}
