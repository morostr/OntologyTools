package no.ustr.ont.owl.filemapping;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;


public class IgnorantIRIMapper implements OWLOntologyIRIMapper {

	@Override
	public IRI getDocumentIRI(IRI ontologyIRI) {
		// TODO Auto-generated method stub
		//System.out.println("asked for:" + ontologyIRI);
		//throw new RuntimeException("YO"); 
		return null;
	}

}
