package no.ustr.ont.util;

import org.semanticweb.owlapi.reasoner.ReasonerProgressMonitor;

/**
 * A message receiver is a class that retreives messages from the ontology tool 
 * processes, and displays them to the user. 
 * 
 * As these messages often will have a reasoning aspect, every message receiver
 * must provide a reasoner progress monitor when requested. 
 * @author MRST
 *
 */
public interface MessageReceiver {
	public void msg(String msg);

	public ReasonerProgressMonitor getProgressMonitor();

	void msgnnl(String msg);	
}
