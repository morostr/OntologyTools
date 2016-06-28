package no.ustr.ont.owl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.Reasoner.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.UnknownOWLOntologyException;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.ReasonerProgressMonitor;
import org.semanticweb.owlapi.util.InferredAxiomGenerator;
import org.semanticweb.owlapi.util.InferredClassAssertionAxiomGenerator;
import org.semanticweb.owlapi.util.InferredEquivalentDataPropertiesAxiomGenerator;
import org.semanticweb.owlapi.util.InferredObjectPropertyCharacteristicAxiomGenerator;
import org.semanticweb.owlapi.util.InferredOntologyGenerator;
import org.semanticweb.owlapi.util.InferredPropertyAssertionGenerator;
import org.semanticweb.owlapi.util.InferredSubClassAxiomGenerator;
import org.semanticweb.owlapi.util.InferredSubObjectPropertyAxiomGenerator;

import no.ustr.ont.owl.filemapping.ProtegeCatalogFile;



/**
 * This class will produce a model that contains only the infered axioms from
 * a set of input models 
 * @author MRST
 *
 */
public class OwlInferenceMachine {
	
	private boolean onlyDirectSubClassOf = true; 
	
	public void setOnlyDirectSubClassesOf(boolean a) {
		this.onlyDirectSubClassOf = a;
	}
	
	public boolean getOnlyDirectSubClassesOf() {
		return this.onlyDirectSubClassOf;
	}
	
	public OwlInferenceMachine(ReasonerProgressMonitor reasonerProgressMonitor) {
		this.reasonerProgressMonitor = reasonerProgressMonitor;
	}

	public static void main(String[] args) throws UnknownOWLOntologyException, OWLOntologyStorageException, IOException {
		System.out.println("Start"); 
		Map<IRI,IRI> ans = new HashMap<IRI,IRI>(); 
		
		String folder = "C:/Data/ahus/ontologi/release/"; 
		String filename = folder + "ahus-dks.ttl";
		     
		File file = new File(filename);
		
		File outFile = new File(folder + "inferred.ttl"); 
		                       
		
		OWLOntologyIRIMapper irimapper = ProtegeCatalogFile.getIriMapperFromProtegeXml(
				new File(file.getParentFile().getAbsolutePath() + "/" + ProtegeCatalogFile.defaultFileName));
				
		
		OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();	
		ontologyManager.addIRIMapper(irimapper); 

		OwlInferenceMachine mchnine = new OwlInferenceMachine(OwlInferenceMachine.getReasonerProgressMonitor()); 
				
		OWLOntology inferedOntology = null; 
		try {
			
			OWLOntology ontology = ontologyManager.loadOntologyFromOntologyDocument(file);
			inferedOntology = mchnine.createInference(ontology);
			OwlApiCheatSheet.writeOntology(inferedOntology, outFile, ontologyManager);
			
			
		} catch (OWLOntologyCreationException e) {
			throw new RuntimeException(e); 
		}
		
	}
	
	
	
	public static ReasonerProgressMonitor getReasonerProgressMonitor() {
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
	
	private ReasonerProgressMonitor reasonerProgressMonitor; 
	public void setProgressMonitor(ReasonerProgressMonitor r) {
		this.reasonerProgressMonitor = r;  
	}
	
	
	public OWLOntology createInference(OWLOntology inputOnt) throws OWLOntologyCreationException, UnknownOWLOntologyException, OWLOntologyStorageException, IOException {

        OWLOntologyManager manager=OWLManager.createOWLOntologyManager();
        Date d = new Date(); 
        
		//Assume inputOnt is already loaded and all is fine with 
		//imports and stuff. 
 
		//This code is gotten from here: https://github.com/phillord/hermit-reasoner/blob/master/examples/org/semanticweb/HermiT/examples/MaterialiseInferences.java
		
        ReasonerFactory factory = new ReasonerFactory();
        // The factory can now be used to obtain an instance of HermiT as an OWLReasoner. 
        Configuration c=new Configuration();
        
    
       
        c.reasonerProgressMonitor=reasonerProgressMonitor; 
        
        
        
        OWLReasoner reasoner=factory.createReasoner(inputOnt, c);
        // The following call causes HermiT to compute the class, object, 
        // and data property hierarchies as well as the class instances. 
        // Hermit does not yet support precomputation of property instances. 
        //reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY, InferenceType.CLASS_ASSERTIONS, InferenceType.OBJECT_PROPERTY_HIERARCHY, InferenceType.DATA_PROPERTY_HIERARCHY, InferenceType.OBJECT_PROPERTY_ASSERTIONS);
        // We now have to decide which kinds of inferences we want to compute. For different types 
        // there are different InferredAxiomGenerator implementations available in the OWL API and 
        // we use the InferredSubClassAxiomGenerator and the InferredClassAssertionAxiomGenerator 
        // here. The different generators are added to a list that is then passed to an 
        // InferredOntologyGenerator. 
        List<InferredAxiomGenerator<? extends OWLAxiom>> generators
        	= new ArrayList<InferredAxiomGenerator<? extends OWLAxiom>>();
        generators.add(new InferredSubClassAxiomGenerator());
        generators.add(new InferredClassAssertionAxiomGenerator());
        generators.add(new InferredPropertyAssertionGenerator()); 
        generators.add(new InferredEquivalentDataPropertiesAxiomGenerator()); 
        generators.add(new InferredSubObjectPropertyAxiomGenerator()); 
        generators.add(new InferredObjectPropertyCharacteristicAxiomGenerator()); 
        if (!onlyDirectSubClassOf) 
        	generators.add(new InferredSubClassAxiomGenerator() {
        		protected void addAxioms(
        				OWLClass entity, 
        				OWLReasoner reasoner, 
        				OWLDataFactory dataFactory, 
        				Set<OWLSubClassOfAxiom> result) {
        		
        		for (OWLClass superclass : reasoner.getSuperClasses(entity, false).getFlattened())
                	result.add(dataFactory.getOWLSubClassOfAxiom(entity, superclass));
        	}
        });
        
        
        // We dynamically overwrite the default disjoint classes generator since it tries to 
        // encode the reasoning problem itself instead of using the appropriate methods in the 
        // reasoner. That bypasses all our optimisations and means there is not progress report :-( 
        // We don't want that!
        /*
        generators.add(new InferredDisjointClassesAxiomGenerator() {
            boolean precomputed=false;
            protected void addAxioms(
            					OWLClass entity, 
            					OWLReasoner reasoner, 
            					OWLDataFactory dataFactory, 
            					Set<OWLDisjointClassesAxiom> result) {
                if (!precomputed) {
                    reasoner.precomputeInferences(InferenceType.DISJOINT_CLASSES);
                    precomputed=true;
                }
                for (OWLClass cls : reasoner.getDisjointClasses(entity).getFlattened()) {
                    result.add(dataFactory.getOWLDisjointClassesAxiom(entity, cls));
                }
            }
        });
        */
        // We can now create an instance of InferredOntologyGenerator. 
        InferredOntologyGenerator iog=new InferredOntologyGenerator(reasoner,generators);
        
        
      
        // Before we actually generate the axioms into an ontology, we first have to create that ontology. 
        // The manager creates the for now empty ontology for the inferred axioms for us. 
        OWLOntology inferredAxiomsOntology=manager.createOntology();
        // Now we use the inferred ontology generator to fill the ontology. That might take some 
        // time since it involves possibly a lot of calls to the reasoner.
        
        
        iog.fillOntology(manager.getOWLDataFactory(), inferredAxiomsOntology);
        long seconds = new Date().getTime() - d.getTime(); 
       seconds = seconds /60;
        
		return inferredAxiomsOntology;
    }
		
		
		
		
	}

	
	
	