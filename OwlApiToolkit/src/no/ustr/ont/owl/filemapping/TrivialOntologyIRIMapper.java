package no.ustr.ont.owl.filemapping;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import no.ustr.ont.owl.OwlApiCheatSheet;



public class TrivialOntologyIRIMapper implements OWLOntologyIRIMapper {
	static Logger logger = LogManager.getLogger(OwlApiCheatSheet.class.getName());
	private Map<IRI, IRI> irimap ; 
		
	public TrivialOntologyIRIMapper(Map<String,String> ontologyToFileMap) {
		irimap = new HashMap<IRI,IRI>(); 
		for (String ontologyName : ontologyToFileMap.keySet()) {
			IRI ontologyIri = IRI.create(ontologyName);
			IRI fileIri = IRI.create(new File(ontologyToFileMap.get(ontologyName)));
			irimap.put(ontologyIri, fileIri); 
		}		
	}

	@Override
	public IRI getDocumentIRI(IRI ontologyIRI) {
		logger.debug("Request location for iri:" + ontologyIRI);
		return irimap.get(ontologyIRI);
	}

}
