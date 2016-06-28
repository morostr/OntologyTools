package no.ustr.ont.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.UnknownOWLOntologyException;
import org.semanticweb.owlapi.reasoner.ReasonerProgressMonitor;

import com.google.common.base.Optional;

import no.ustr.ont.owl.OwlApiCheatSheet;
import no.ustr.ont.owl.OwlInferenceMachine;
import no.ustr.ont.owl.filemapping.ProtegeCatalogFile;
import no.ustr.ont.rdf.FusekiConfigFile;
import no.ustr.ont.rdf.JenaTools;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Selector;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.OWL;
import org.apache.log4j.Logger;

/*
 * Contains methods for performing a set of commonly done ontology tasks
 * Is intended to be the thing that performs the actions directed by an UI. 
 * The MessageReceiver is intended to be a way of communicating with the UI. 
 * 
 * The ontology tools will use both Jena and OwlApi where convenient. 
 * 
 */
public class OntologyTools {	
	static Logger log = Logger.getLogger(OntologyTools.class.getName());

	private MessageReceiver mr; 
	/*
	 * Default constructor.
	 */
	public OntologyTools(MessageReceiver mr) {
		this.mr = mr;
	} 
	
	public static void main(String[] args) throws UnknownOWLOntologyException, OWLOntologyStorageException, IOException {


	}
	
	/**
	 * Creates a fuseki config file based on the files in files. 
	 * @param files
	 * @param datasetname
	 * @return
	 */
	public String getFusekiConfigFile(Collection<File> files, String datasetname) {
		Model m = FusekiConfigFile.getFusekiConfigFile(files, null, datasetname);
		return getStringFromModel(m);
	}
	
	
	/**
	 * Creates fuseki config file based on the files in files. The auxfiles will be 
	 * added to separate graphs, but will not be in the default graph
	 * 
	 * @param files
	 * @param auxfiles
	 * @param datasetname
	 * @return
	 */
	public String getFusekiConfigFile(Collection<File> files, Collection<File> auxfiles,
			String datasetname) {
		Model m = FusekiConfigFile.getFusekiConfigFile(files, auxfiles, datasetname);  
		return getStringFromModel(m) ;
	}	
	
	public static void writeModel(Model model, File file) throws FileNotFoundException {
		FileOutputStream o = new FileOutputStream(file);		
		model.write(o, "TTL");
	}	

	public static String getStringFromModel(Model model) {				
		StringWriter out = new StringWriter();
		model.write(out, "TTL"); 
		String result = out.toString();
		return result; 
	}

	public void createInferedTriplesFromFile(File ontologyFile, File outputFile, boolean directOnly) throws UnknownOWLOntologyException, OWLOntologyStorageException, IOException {		                       
		mr.msg("Creating ontology <=> file mapping");
		
		
		
		OWLOntologyIRIMapper irimapper = ProtegeCatalogFile.getIriMapperFromProtegeXml(
				new File(ontologyFile.getParentFile().getAbsolutePath() + "/" + ProtegeCatalogFile.defaultFileName));
				
		log.debug("Finished creating irimapper"); 
		
		OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();	
		ontologyManager.addIRIMapper(irimapper);
		
		log.debug("Created ontologyManager"); 
		
		OwlInferenceMachine mchn = new OwlInferenceMachine(mr.getProgressMonitor());
		mchn.setOnlyDirectSubClassesOf(directOnly);

		
		OWLOntology inferedOntology = null; 
		try {
			mr.msg("Loading ontology and imports");
			OWLOntology ontology = ontologyManager.loadOntologyFromOntologyDocument(ontologyFile);
			inferedOntology = mchn.createInference(ontology);
			OwlApiCheatSheet.writeOntology(inferedOntology, outputFile, ontologyManager);
			
			
		} catch (OWLOntologyCreationException e) {
			throw new RuntimeException(e); 
		}
		
	}

	public static MessageReceiver getSimpleMessageReceiver() {
		return new MessageReceiver() {
			@Override
			public void msg(String msg) {						
				System.out.println(msg);
			}
			@Override
			public void msgnnl(String msg) {						
				System.out.print(msg);
			}
			@Override
			public ReasonerProgressMonitor getProgressMonitor() {
				  ReasonerProgressMonitor r = new ReasonerProgressMonitor() {
			    	   int treshold=0; 
					@Override
					public void reasonerTaskStarted(String taskName) {
						System.out.print(taskName + " "); 			
					}

					@Override
					public void reasonerTaskStopped() {
						// TODO Auto-generated method stub
						System.out.println(" done"); 
					}

					@Override
					public void reasonerTaskProgressChanged(int value, int max) {
						// TODO Auto-generated method stub
						int a = (value*100)/max;
					
						treshold = (treshold+20)<a ? a : treshold;
						
						if (a==treshold)
						System.out.print("."); 
					}

					@Override
					public void reasonerTaskBusy() {
						// TODO Auto-generated method stub
						System.out.println("."); 
					}
			    	   
			       };
			       return r;
			}
			
		}; 
	}

	public String getFusekiConfigFile(List<File> asList) {
		// TODO Auto-generated method stub
		return null;
	}

	
	
	/**
	 * Create a map of imports based on one ontology
	 */
	public Map<IRI, Set<IRI>> getImportsMap(OWLOntology ontology) {
		Map<IRI,Set<IRI>> ans = new HashMap<IRI,Set<IRI>>();
		for (OWLOntology o: ontology.getOWLOntologyManager().getImportsClosure(ontology)) {
			Set<IRI> s = new HashSet<IRI>();  
			for (OWLImportsDeclaration ooimp : o.getImportsDeclarations()) {
				s.add(ooimp.getIRI());
			}
			
			Optional<IRI> f = o.getOntologyID().getOntologyIRI();
			if (f.isPresent())			
				ans.put(f.get(), s);
			
		}
		return ans;
	}
	
	
	
	
	/**
	 * Collects the ontologies that make up the imports closure based on the provided file. 
	 * 
	 */
	public Set<OWLOntology> getImportsClosure(File ontologyFile) {
		mr.msg("Creating ontology <=> file mapping");
		
		log.debug("Loading ontology "  + ontologyFile.getName() + " ...");  
		OWLOntology o = OwlApiCheatSheet.getOntologyFromFile(ontologyFile);		
		
		log.debug("Loaded");
		OWLOntologyManager ontologyManager = o.getOWLOntologyManager();		
				
		log.debug("Loaded ontologyManager. Getting imports ... ");
		
		Set<OWLOntology> ontologies	= ontologyManager.getImports(o);			
		log.debug("Done getting imports. ");
		return ontologies; 
	}
	
	
	/**
	 * Finding the import hierarchy through RDF, not OWL
	 * @param ontologyFile
	 * @return
	 */
	public Map<String, File> getImportsClosureMap(File ontologyFile) {
		Model m = JenaTools.loadModel(ontologyFile); 
		Map<String, File> map = new HashMap<String,File>(); 
		
		StmtIterator stmtit = m.listStatements(null, RDF.type, OWL.Ontology); //TODO: Wrong
		
		//We should only have one such resource
		Resource thisOntologyResource = null; 
		{
			if (!stmtit.hasNext()) 
				throw new RuntimeException("No ontology statement found in file : " + ontologyFile.getName()); 

			thisOntologyResource = stmtit.next().getSubject(); 
			if (stmtit.hasNext())
				throw new RuntimeException("Multiple ontology statements found in file : " + ontologyFile.getName());
			
		}
		
		log.debug("Found first ontology:" + thisOntologyResource.getLocalName()); 

		//Create IRI mapper so we find the imported ontologies
		OWLOntologyIRIMapper irimap = ProtegeCatalogFile.getIriMapperFromProtegeXml(
				new File(ontologyFile.getParentFile().getAbsolutePath() + "/" + ProtegeCatalogFile.defaultFileName));
		
		
		getImportsClosureMapInternal(irimap, ontologyFile, thisOntologyResource, map); 		
		
		return map; 
	
	}
	
	public void getImportsClosureMapInternal(OWLOntologyIRIMapper irimapper, File ontologyFile, Resource ontologyResource, Map<String, File> map) {
		
		Model m = JenaTools.loadModel(ontologyFile); 		
		
		//		Ground the resource in the current ontology
		Resource r = m.createResource(ontologyResource.getURI()); 
		
		StmtIterator stmtit = r.listProperties(OWL.imports); 
		
		if (map.containsKey(ontologyResource.getURI()))
			return; //We have already checked this ontology, no need to redo it. 
		
		map.put(ontologyResource.getURI(), ontologyFile); 		
		
		if (!stmtit.hasNext()) 
			log.debug("No imports for ontology     : " + ontologyResource.getURI() + "  in file: " + ontologyFile.getAbsolutePath());
		else {
			log.debug("Reading imports in ontology :" + ontologyResource.getLocalName()); 
		}
		
		while (stmtit.hasNext()) {			
			Resource newOntologyResource = stmtit.next().getResource(); 
			String newOntologyUri = newOntologyResource.getURI();			
			IRI newOntologyFileIri = irimapper.getDocumentIRI(IRI.create(newOntologyUri));
			if (newOntologyFileIri == null)
				throw new NullPointerException("no match for " + newOntologyUri + ". Can't find ontology in catalogue"); 
			File newOntologyFile = OwlApiCheatSheet.getFileFromIRI(newOntologyFileIri);
			
			getImportsClosureMapInternal(irimapper, newOntologyFile, newOntologyResource, map); 
		}
	
	
	}
	
	
	private Map<String, File> getImportsClosureMapLegacyImpl(File ontologyFile) {
		
		
		Set<OWLOntology> ontologies = getImportsClosure(ontologyFile); 
		
		log.debug("Got imports closure, parsing ..."); 
		
		//TODO: Find a better way to do this (!)
		OWLOntologyIRIMapper irimapper = ontologies.iterator().next().getOWLOntologyManager().getIRIMappers().iterator().next(); 
		
		
		Map<String, File> importMap = new HashMap<String, File>(); 
		for (OWLOntology o: ontologies) {
			Optional<IRI> ontologyIRI = o.getOntologyID().getOntologyIRI();
			if (ontologyIRI.isPresent()) {			
				IRI oiri = irimapper.getDocumentIRI(ontologyIRI.get());			
				File f = new File(oiri.toURI());
				importMap.put(ontologyIRI.get().toString(), f); 
			}
		}
		
		return importMap;
		
	}
	
	
	/** 
	 * Collects files in the same folder that are in the imports closure of the provided file
	 * Ignores files not in the same folder. 
	 * @param ontologyFile
	 * @return
	 */
	public File[] getImportsClosureAsFileList(File ontologyFile) {
		mr.msg("Creating ontology <=> file mapping");
		OWLOntologyIRIMapper irimapper = ProtegeCatalogFile.getIriMapperFromProtegeXml(
				new File(ontologyFile.getParentFile().getAbsolutePath() + "/" + ProtegeCatalogFile.defaultFileName));
				
		
		OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();	
		ontologyManager.addIRIMapper(irimapper);
				
		Set<OWLOntology> ontologies = null; 
		try {
			mr.msg("Loading ontology and imports");
			OWLOntology ontology = ontologyManager.loadOntologyFromOntologyDocument(ontologyFile);
			ontologies = ontologyManager.getImports(ontology); 
			
		} catch (OWLOntologyCreationException e) {
			throw new RuntimeException(e); 
		}
		
		List<File> files = new ArrayList<File>(); 
		for (OWLOntology o : ontologies) {		
			
			Optional<IRI> ontologyIRI = o.getOntologyID().getOntologyIRI();
			if (ontologyIRI.isPresent()) {			
				IRI oiri = irimapper.getDocumentIRI(ontologyIRI.get());			
				File f = new File(oiri.toURI());
				files.add(f); 
			}
			
		}
		
		return files.toArray(new File[files.size()]); 
	}

	
	public void convertOntologyToOWLFunctionalSyntax(File infile, File outfile) throws OWLOntologyCreationException, OWLOntologyStorageException, FileNotFoundException {
		OWLOntology o = OwlApiCheatSheet.getOntologyFromFileNoImports(new File("c:/Data/pcardl/projects/Part12/ISO 15926-12-CD/ISO 15926-12-CD/Assignment of a representation space-CD.ttl"));
		OwlApiCheatSheet.writeFunctionalSyntax(o, new File("c:/Data/pcardl/projects/Part12/ISO 15926-12-CD/ISO 15926-12-CD/testaaa.func"));	
	}

	public static MessageReceiver getQuietMessageReceiver() {
		return new MessageReceiver() {

			@Override
			public void msg(String msg) {
							
			}

			@Override
			public ReasonerProgressMonitor getProgressMonitor() {
				
				return null;
			}

			@Override
			public void msgnnl(String msg) {
								
			}};
		
	}
	
	
}
