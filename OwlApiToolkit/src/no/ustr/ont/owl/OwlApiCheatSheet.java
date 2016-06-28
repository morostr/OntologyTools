package no.ustr.ont.owl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.FunctionalSyntaxDocumentFormat;
import org.semanticweb.owlapi.formats.PrefixDocumentFormat;
import org.semanticweb.owlapi.formats.TurtleDocumentFormat;
import org.semanticweb.owlapi.io.FileDocumentSource;
import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.MissingImportHandlingStrategy;
import org.semanticweb.owlapi.model.OWLDocumentFormat;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;


import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.UnknownOWLOntologyException;
import org.semanticweb.owlapi.util.AutoIRIMapper;

import no.ustr.ont.owl.filemapping.IgnorantIRIMapper;
import no.ustr.ont.owl.filemapping.ProtegeCatalogFile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class OwlApiCheatSheet {
	static Logger logger = LogManager.getLogger(OwlApiCheatSheet.class.getName());
	
		
	public static void writeOntology(OWLOntology ont, File outfile, OWLOntologyManager manage2r) throws UnknownOWLOntologyException, OWLOntologyStorageException, IOException {


		// Now the axioms are computed and added to the ontology, but we still have to save 
		// the ontology into a file. Since we cannot write to relative files, we have to resolve the 
		// relative path to an absolute one in an OS independent form. We do this by (virtually) creating a 
		// file with a relative path from which we get the absolute file.     
		OWLOntologyManager manager=OWLManager.createOWLOntologyManager();
		if (!outfile.exists())
			outfile.createNewFile();
		outfile=outfile.getAbsoluteFile();
		// Now we create a stream since the ontology manager can then write to that stream. 
		OutputStream outputStream = new FileOutputStream(outfile);
		// We use the same format as for the input ontology.
		System.out.println("Writing " +  outfile.getAbsolutePath());


		TurtleDocumentFormat oof = new TurtleDocumentFormat();

		manager.saveOntology(ont, oof , outputStream);
		// Now that ontology that contains the inferred axioms should be in the ontologies subfolder 
		// (you Java IDE, e.g., Eclipse, might have to refresh its view of files in the file system) 
		// before the file is visible.  
	}

	public static String cleanUri(String string) {
		return string
				.replaceAll("\\s+", "_")
				.replaceAll("\\.", "_")
				.replaceAll("/",  "")
				.replaceAll("\\\\", "")
				.replaceAll("%",""); 
	}

	private static OWLOntology getOntologyFromFileInternal(File f, OWLOntologyIRIMapper irimapper, boolean onlyManchester) {

		OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();

		ontologyManager.addIRIMapper(irimapper);
		OWLOntology ontology = null;  


		try {
			logger.debug("Loading: " + f.getAbsolutePath());
			ontology = ontologyManager.loadOntologyFromOntologyDocument(f)	; 		

		} catch (OWLOntologyCreationException e) {
			throw new RuntimeException(e); 
		}

		return ontology;
	}

	public static OWLOntology getOntologyFromFile(File f, OWLOntologyIRIMapper irimapper) {                
		return getOntologyFromFileInternal(f, irimapper, false);	
	}


	public static OWLOntology getOntologyFromFileOnlyManchester(File f, OWLOntologyIRIMapper irimapper) {                
		return getOntologyFromFileInternal(f, irimapper, true);		
	}

	public static OWLOntology getOntologyFromFile(File f) {	
		logger.debug("FIRST time here");
		return getOntologyFromFile(f, getIriMapper(f)); 

	}



	public static OWLOntologyIRIMapper getIriMapper(File f) {
		logger.debug("File f: " + f.getAbsolutePath());
		logger.debug("Ontology folder: " + f.getAbsoluteFile().getParentFile()); 

		File protegefile = new File(f.getAbsoluteFile().getParentFile().getAbsolutePath() + "/" + ProtegeCatalogFile.defaultFileName);
		logger.debug("Protegefile : " + protegefile);

		return ProtegeCatalogFile.getIriMapperFromProtegeXml(protegefile); 
	}
	
	public static OWLOntology getOntologyFromFileNoImports(File f) throws OWLOntologyCreationException {
		
		
		
		OWLOntologyLoaderConfiguration config = new OWLOntologyLoaderConfiguration();		
		config = config.setMissingImportHandlingStrategy(MissingImportHandlingStrategy.SILENT); 
		
		
		OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
		
		Set<OWLOntologyIRIMapper> a = new HashSet<OWLOntologyIRIMapper>();
		a.add(new IgnorantIRIMapper()); 
		ontologyManager.setIRIMappers(a );
		
		ontologyManager.setOntologyLoaderConfiguration(config);		
		
		
		OWLOntologyDocumentSource documentSource = new FileDocumentSource(f);
		OWLOntology ontology = ontologyManager.loadOntologyFromOntologyDocument(f);
		
		return ontology; 
		
	}
	
	public static void writeFunctionalSyntax(OWLOntology ontology, File out) throws OWLOntologyStorageException, FileNotFoundException {
		
		OWLOntologyManager ontologyManager = ontology.getOWLOntologyManager();
		
		PrefixDocumentFormat pm =  ontologyManager.getOntologyFormat(ontology).asPrefixOWLOntologyFormat();
		
		
		//Map<String, String> prefixName2PrefixMap = pm.getPrefixName2PrefixMap();
		
		
		
		/*for (String k : prefixName2PrefixMap.keySet()) 
			System.out.println(k + " : " + prefixName2PrefixMap.get(k));
			*/ 
		
		FunctionalSyntaxDocumentFormat oof = new FunctionalSyntaxDocumentFormat();
	//	oof.setDefaultPrefix("http://standards.iso.org/iso/15926/ontology/life-cycle-integration/");
		OutputStream outputStream = new FileOutputStream(out);

		
		ontologyManager.saveOntology(ontology, oof , outputStream);		
		
	}
	
	public static void main(String[] args) throws OWLOntologyCreationException, OWLOntologyStorageException, FileNotFoundException {
		/*
		OWLOntology o = getOntologyFromFileNoImports(new File("c:/Data/pcardl/projects/Part12/ISO 15926-12-CD/ISO 15926-12-CD/Assignment of a representation space-CD.ttl"));
		writeFunctionalSyntax(o, new File("c:/Data/pcardl/projects/Part12/ISO 15926-12-CD/ISO 15926-12-CD/testaaa.func"));
		System.out.println("Done"); 
		*/
		File a = new File("C:/tmp/hei.txt"); 
		
		File b  = getFileFromIRI(IRI.create(a));
		
		System.out.println(b.getAbsolutePath());
		
	}

	/**
	 * IF this is a file IRI, return a file
	 * @return
	 */
	public static File getFileFromIRI(IRI iri) {
//		if (iri == null)
//			throw new NullPointerException("Cant find file from iri with value null");
//		File f = new File(iri.toURI().getPath()); 
		
		
			return new File(iri.toURI().getPath());
//		} catch (MalformedURLException e) {
//			// TODO Auto-generated catch block
//			throw new RuntimeException("Error while making File from uri:" + iri.toString(), e); 
//		} 
		
	}
	
	
	
	
	

}
