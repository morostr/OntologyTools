package no.ustr.ont.owl.filemapping;

import java.io.File;
import java.io.IOException;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;
import org.semanticweb.owlapi.util.AutoIRIMapper;

public class FlexibleIriMapper implements OWLOntologyIRIMapper {

	private OWLOntologyIRIMapper m; 
	private OWLOntologyIRIMapper backupM = null; 
	private boolean isIgnorant = false; 
	
	public FlexibleIriMapper(File baseFile) {
		if (!baseFile.exists()) {
			isIgnorant=true;
			m = new IgnorantIRIMapper();
		}
		else if (baseFile.getName().equals(ProtegeCatalogFile.defaultFileName))
				m = ProtegeCatalogFile.getIriMapperFromProtegeXml(baseFile);
			
		else if (!baseFile.isDirectory()) {
			//Basefile is not directory. Assume that it is a file in a directory containing the ontologies
			try {
				baseFile = baseFile.getCanonicalFile().getParentFile();
			} catch (IOException e) {
				throw new RuntimeException("Failed to find folder for iri mapping based on file:" + baseFile, e);					
			}
		}

		if (baseFile.isDirectory()) { //It really should be now
			m = new AutoIRIMapper(baseFile, false); 
		}
	}

	@Override
	public IRI getDocumentIRI(IRI ontologyIRI) {
		// TODO Auto-generated method stub
		return null;
	}
}
		
		
	/*	
		
	}

	@Override
	public IRI getDocumentIRI(IRI ontologyIRI) {
		IRI i = m.getDocumentIRI(ontologyIRI);
		
		if (!isIgnorant && backupM != null && i == null) {
			backupM = 
		}
		
	}
	
	
}*/
