package no.ustr.ont.owl.filemapping;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ProtegeCatalogFile {
	public static final String defaultFileName = "catalog-v001.xml";
	private Map<String,String> ontologyToFileMap; 
	
	
	public static TrivialOntologyIRIMapper getIriMapperFromProtegeXml(File protegeXml) {
		ProtegeCatalogFile f = new ProtegeCatalogFile(protegeXml);
		TrivialOntologyIRIMapper i = new TrivialOntologyIRIMapper(f.getOntologyToFileMap()); 
		return i; 
	}
	
	public ProtegeCatalogFile(File protegeCatalogFile) {
		
		Document dom = null;
		
		//get the factory
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		
		File protegeContainerFolder = protegeCatalogFile.getParentFile(); 
		try {

			//Using factory get an instance of document builder
			DocumentBuilder db = dbf.newDocumentBuilder();

			//parse using builder to get DOM representation of the XML file
			dom = db.parse(protegeCatalogFile.getAbsoluteFile());


		}catch(ParserConfigurationException pce) {
			pce.printStackTrace();
		}catch(SAXException se) {
			se.printStackTrace();
		}catch(IOException ioe) {
			ioe.printStackTrace();
		}

		Element docEle = dom.getDocumentElement();
		ontologyToFileMap = new HashMap<String,String>();  
		NodeList nl = docEle.getElementsByTagName("uri");
		if(nl != null && nl.getLength() > 0) {
			for(int i = 0 ; i < nl.getLength();i++) {
				Element el = (Element)nl.item(i);
				String name = el.getAttribute("name");
				String uri = el.getAttribute("uri");
				ontologyToFileMap.put(name,  protegeContainerFolder + "/" + uri); 
			}
		}		
	}
	
	public Map<String,String> getOntologyToFileMap() { 
		return Collections.unmodifiableMap(ontologyToFileMap); 
	}
}
