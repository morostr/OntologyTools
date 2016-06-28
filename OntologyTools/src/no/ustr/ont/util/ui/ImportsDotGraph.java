package no.ustr.ont.util.ui;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jgrapht.alg.*;
import org.jgrapht.*;
import org.jgrapht.graph.*;

import org.semanticweb.owlapi.model.IRI;

import no.ustr.ont.owl.OwlApiCheatSheet;
import no.ustr.ont.util.OntologyTools;

public class ImportsDotGraph {

	public static void main(String[] args) {
		File ontologyFile = new File("c:/Data/RD/projects/newpart12/ontology/status-v-2.ttl");
		File oFile = new File("c:/Data/RD/projects/newpart12/ontology/document-v-1.ttl");
		
		ImportsDotGraph g = new ImportsDotGraph(oFile); 
		
	}

	
	
	public ImportsDotGraph(File ontologyFile) {
		
		OntologyTools t = new OntologyTools(OntologyTools.getSimpleMessageReceiver());
		
		Map<IRI,Set<IRI>> mp = t.getImportsMap(
				OwlApiCheatSheet.getOntologyFromFile(ontologyFile));
			
	
		DirectedGraph<IRI, DefaultEdge> directedGraph =
				new DefaultDirectedGraph<IRI, DefaultEdge>
					(DefaultEdge.class);
		
		for (IRI from : mp.keySet()) {
			directedGraph.addVertex(from);
			for (IRI to : mp.get(from)) {  				
				directedGraph.addVertex(to);
				directedGraph.addEdge(from, to);
			}
		}
		
		
		 
		/*
		String map=""; 
		for (IRI from : mp.keySet()) {
			for (IRI to : mp.get(from))  
				map += (getAbbrev(from) + "->" + getAbbrev(to)) +"\n"; 
		}
		
		String pre = ""; 
		for (IRI i: abbreviations.keySet()) {
			pre+=getAbbrev(i) + "[label=\"" + i.toString() + "\"]\n"; 
		}
		
		String graph = " digraph m {\n" + pre + map + "\n}";
		
		
		
		System.out.println(graph);  
		*/
		
	}	
	
	private Map<IRI, String> abbreviations = null;
	
	private int i = 0; 
	
	private  String getAbbrev(IRI from) {
		if (abbreviations==null)
			abbreviations = new HashMap<IRI, String>(); 
		
		if (!abbreviations.containsKey(from)) {
			i++;
			String v = "v" + i; 
			abbreviations.put(from, v); 
		}
		
		return abbreviations.get(from); 
	}

		
}
