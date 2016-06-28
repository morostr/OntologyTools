package no.ustr.ont.util.ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;


import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.UnknownOWLOntologyException;

import no.ustr.ont.owl.OwlApiCheatSheet;
import no.ustr.ont.rdf.JenaTools;
import no.ustr.ont.util.FileUtilities;
import no.ustr.ont.util.MessageReceiver;
import no.ustr.ont.util.OntologyTools;


public class CommandLineInterfaceClient {
	public static final String helpString = "Usage : \nFor arguments to imports graph: \n'n' => show ontology name."
			+ "'f' => show filename.\nAny occurence of 'f' and 'n' are removed, and the remainder used as delimter between ontology name and filename"
			+ "\n's' is short for space, and 't' is short for tabulator. Try it once and you'll get it ."; 
	private static final String VERBOSE = "verbose";
	private static final String HELP = "help";
	private static final String OUTPUT = "output";
	private static final String INFERENCE = "infer";
	private static final String FUSEKICONFIG = "fconfig";
	private static final String FILE = "file";
	private static final String INDIRECT_SUBCLASSOF_AXIOMS = "iind";
	private static final String NAME = "name";
	private static final String AUX = "aux";
	private static final String IMPORTS_PRINT = "importsgraph";
	private static final String IMPORTS= "imports"; 
	private static final String CLOSURELIST = "genlist";
	private static final String ONLYMANCHESTER = "onlymanchester";
	private static final String VERY_VERBOSE = "veryverbose";
	private static final String QUIET = "quiet";
	private static final String SHOW_ALL_ERRORS = "showallerrors";
	private static final String COMBINE = "combine";
	private static final String LIST_FILE = "readlist";
	private static final String EXCLUDE_LIST = "exclude";
	private static final String FOLDER = "folder";
	private static final String ONLY_PARSE = "onlyparse"; 
	
	static Logger log = Logger.getLogger(CommandLineInterfaceClient.class.getName());
	
	private boolean quiet = false;
	private CommandLine line;	
	private Options options; 
	
	private static FilenameFilter ttlFilter = new FilenameFilter() {
		public boolean accept(File dir, String name) {
			return name.endsWith(".ttl")
					&& !(
							name.startsWith(".#")
							||  name.startsWith("~")
							);
			}
	};
	
	public void execute() throws Exception  {					
			
		try {
			
			if (line.hasOption(VERBOSE)) {				
				//Logger.getRootLogger().setLevel(Level.DEBUG);
				Logger.getLogger("com.dnvgl.sirm").setLevel(Level.DEBUG);				
				log.debug("DEBUG ON"); 
			}
			if (line.hasOption(VERY_VERBOSE)) {				
				Logger.getRootLogger().setLevel(Level.DEBUG);	
				Logger.getLogger("org.semanticweb.owlapi").setLevel(Level.DEBUG);
				log.debug("VERY VERBOSE DEBUG ON"); 
			}			
			if ( line.hasOption(HELP) ) {
				doHelpMessage();		
			}
			else {
				
				//TODO: Fix AUX
				if (line.hasOption(AUX))
					throw new IllegalArgumentException("AUX does not work in this version"); 
				
				MessageReceiver mr = getMessageReceiver(line.hasOption(QUIET)); 
				OntologyTools ontologyTools = new OntologyTools(mr);

				if (line.hasOption(INFERENCE)) 
					doInference(mr, ontologyTools);				
				else if (line.hasOption(IMPORTS_PRINT)) 
					doImportsGraph(ontologyTools);
				else if (line.hasOption(FUSEKICONFIG)) 	
					doFusekiConfig(mr, ontologyTools);
				else if (line.hasOption(COMBINE)) 
					doCombine(mr, ontologyTools); 
				// no other options, check for file option
				else if (line.hasOption(ONLY_PARSE)) {					
					doParseFile(mr, ontologyTools); 
				}
				else if (line.hasOption(FILE)
						|| line.hasOption(FOLDER)
						|| line.hasOption(LIST_FILE))
					doShowListFile(mr, ontologyTools); 
				else 
					doHelpMessage();						
			}
		}
		catch (Exception e) {
			if (line.hasOption(SHOW_ALL_ERRORS))
				throw e; 
			System.out.println("Error: " + e.getMessage());
			System.exit(1);
		}
	}




	private void doShowListFile(MessageReceiver mr, OntologyTools ontologyTools) {
		List<File> files = getFilesFromArg(mr, ontologyTools);
		mr.msg("All files selected with the current options:");
		for (File f : files) 
			mr.msg(f.getName());
			
		
	}




	private void doCombine(MessageReceiver mr, OntologyTools ontologyTools) throws FileNotFoundException  {
		File outputFile = requireExistingFile(COMBINE);
		
		List<File> files = getFilesFromArg(mr, ontologyTools); 
		Model m = ModelFactory.createDefaultModel(); 
		
		for (File f : files)
			JenaTools.addFileToJenaModel(f, m);   	
		
		JenaTools.writeTtl(m, outputFile);
	}




	private void doParseFile(MessageReceiver mr, OntologyTools ontologyTools) {
		List<File> files = getFilesFromArg(mr, ontologyTools); 
		
		for (File file : files) {
		if (line.hasOption(ONLYMANCHESTER))  
			OwlApiCheatSheet.getOntologyFromFileOnlyManchester(file, OwlApiCheatSheet.getIriMapper(file));		
		else 
			OwlApiCheatSheet.getOntologyFromFile(file);
		
		mr.msg("File " + file.getName() + " parsed successfully.");
		
		}
	
		mr.msg("Files finished loading.");
	}




	private void doHelpMessage() {
		HelpFormatter f = new HelpFormatter();
		f.printHelp(helpString, options);
	}




	private MessageReceiver getMessageReceiver(boolean quiet) {
		return (quiet) ?
				OntologyTools.getQuietMessageReceiver() :  
					OntologyTools.getSimpleMessageReceiver();
	}




	private void doInference(MessageReceiver mr, OntologyTools ontologyTools)
			throws OWLOntologyStorageException, IOException {
		mr.msg("Will create infered triples from " + require(INFERENCE) + " and dependants, and output results in " + require(OUTPUT));
		File output = new File(require(OUTPUT));
		File ontologyFile = new File(require(INFERENCE));
		ontologyTools.createInferedTriplesFromFile(ontologyFile, output, !line.hasOption(INDIRECT_SUBCLASSOF_AXIOMS));
	}




	private void doImportsGraph(OntologyTools ontologyTools) {
		File baseOntologyFile = new File(require(FILE));
		
		String mode_str= require(IMPORTS_PRINT); 
		int mode = 0; 
		if (mode_str.contains("f"))
			mode+=1;
		if (mode_str.contains("n"))
			mode+=2;
		
		String delimiter = mode_str.replace("f","").replaceAll("n", "").replaceAll("s", " ").replaceAll("t", "\t");
		
		if (delimiter.isEmpty())
			delimiter = ";"; 
		
		
		Map<String, File> map = ontologyTools.getImportsClosureMap(baseOntologyFile);
		for (String ontName : map.keySet()) {
			File f = map.get(ontName); 
			if (mode==2 || mode == 3) 						
				System.out.print(ontName); 
			if (mode==3)
				System.out.print(delimiter);
			if (mode==1 || mode == 3)
				System.out.print(f.getName());
			System.out.println(""); 
		}
	}


	/**
	 * For new methods only. Create a list of files from the arguments in line
	 * The default operation is as follows: 
	 *  1. Get the initial set of files from either 
	 *     * -f    : One actual file
	 *     * -l    : One file that contains a list of files
	 *     * -l -F : One file (-l) that contains a list of files with (-F) as base folder
	 *  2. Either 
	 *     * Return that file/list of files directly
	 *     * Build a new list of files from the following, and return that
	 *        * -m                : Take the input closure of all the files
	 *        * -x                : Remove the files in -x from all the files. This file will only match filenames, not paths. 
	 *        * -m -x : Do -m, and then remove all files in -x 
	 * @param mr
	 * @param ontologyTools
	 * @return
	 */
	private List<File> getFilesFromArg(MessageReceiver mr, OntologyTools ontologyTools) {
		
		mr.msgnnl("Input is ");
		String areIs = "are"; 
		//First get the set of initial files
		List<File> initFiles = new ArrayList<File>(); 
		if (line.hasOption(LIST_FILE)) {	
			mr.msgnnl(" a list of files from the file: " + line.getOptionValue(LIST_FILE));  
			initFiles = FileUtilities.getListOfFilesFromFileWithHash(
					requireExistingFile(LIST_FILE), 
					line.getOptionValue(FOLDER));  			
		}
		else if (line.hasOption(FILE)) { //if we dont have LIST_FILE, check for FILE
			mr.msgnnl("the file:" + line.getOptionValue(FILE));   
			areIs = "is"; 
			initFiles = Arrays.asList(requireExistingFile(FILE));			
		}
		else if (line.hasOption(FOLDER)) {
			mr.msgnnl("the files in folder :" + line.getOptionValue(FOLDER));   
			initFiles = Arrays.asList(getTtlFileList(requireExistingFile(FOLDER)));  
		}
		else {
			throw new RuntimeException("You must have either a file (-f), folder (-F), or a list of files read from a file (-l) as an argument to this command"); 
		}
		
		//Then build the return set
		List<File> returnSet = new ArrayList<>(); 

		
		if (line.hasOption(IMPORTS)) {
			mr.msgnnl(" which " + areIs + " used as basis for an imports closure. ");
			for (File baseOntologyFile : initFiles) {
				Map<String, File> map = ontologyTools.getImportsClosureMap(baseOntologyFile);								
				for (String ontName : map.keySet())	returnSet.add(map.get(ontName)); 				
			}
		}
		else {
			mr.msgnnl(".");
			returnSet = initFiles; 
		}
		
		 
		if (line.hasOption(EXCLUDE_LIST)) {
			mr.msg(" The file names in the file:" + line.getOptionValue(EXCLUDE_LIST) + " are excluded.");
			returnSet = FileUtilities.filterListOfFiles(
					returnSet, 
					requireExistingFile(EXCLUDE_LIST));   
					
		}
		mr.msgnnl(""); 
		
		return returnSet; 
		
	}


	




	private void doFusekiConfig(MessageReceiver mr, OntologyTools t)
			throws IOException, FileNotFoundException {
		
		File output = new File(require(FUSEKICONFIG));
		String datasetName = line.getOptionValue(NAME);		
		
		File[] files = null; 
		File auxFolder = null; 
		
		if (line.hasOption(IMPORTS)) {
			File baseOntologyFile = new File(require(FILE));			
			Map<String, File> map = t.getImportsClosureMap(baseOntologyFile);
			files = new File[map.size()];
			int i=0; 
			for (String ontName : map.keySet()) {
				files[i++] = map.get(ontName);
			}
			if (line.hasOption(AUX)) auxFolder = new File(require(AUX)); 
			
		}
		else {
			File folder = new File(require(FILE));		
			files = getTtlFileList(folder); 
			if (line.hasOption(AUX)) auxFolder = new File(require(AUX));			
				auxFolder =  requireExistingFile(AUX, folder.getCanonicalPath()); 
		}		
		
		String config;		
		
		if (line.hasOption(AUX)) {						
			
			File[] auxfiles = 	getTtlFileList(auxFolder);				
			config = t.getFusekiConfigFile(Arrays.asList(files), Arrays.asList(auxfiles), datasetName); 
		}
		else {				
			config = t.getFusekiConfigFile(Arrays.asList(files), datasetName);
		}
		
		PrintWriter out = new PrintWriter(output);
		out.write(config);
		out.close();
		mr.msg("Wrote fuseki-config file to " + output.getAbsolutePath());
	}	
	
	private File[] getTtlFileList(File folder) {
		File[] files = folder.listFiles(ttlFilter); 		

		if (!folder.exists())
			throw new RuntimeException("Folder " + folder.getAbsolutePath() + " does not exist"); 
		
		if (files.length==0) 
			throw new RuntimeException("Folder " + folder.getAbsolutePath() + " does not contain any .ttl files"); 
		
		return files; 
	}


	private File requireExistingFile(String varname) {
		
		return requireExistingFile(varname, null);
	}
	
	
	private File requireExistingFile(String varname, String suggestedPath) {
		File f = null;
		
		String filename = line.getOptionValue(varname); 
		if (filename==null)
			throw new RuntimeException("Could not find value for argument " + varname + ". This is required for the actions you requested."); 
		
		f = new File(filename);

		if (!f.exists() && suggestedPath != null) {
			f = new File(suggestedPath + 
					File.separator + 
					line.getOptionValue(AUX));
		}

		if (!f.exists()) {
			String msg = (suggestedPath != null)
					? "Did not find file with name " + filename + "  and suggested path " + suggestedPath
					: "Did not find file with name " + filename + "."; 
			throw new RuntimeException(msg);
		}
		
		return f; 
	}
	

	private String require(String output2) {
		if (line.hasOption(output2)) {
			String val = line.getOptionValue(output2);
			if (val!=null)
				return val; 				
		}
		
		throw new RuntimeException("Option " + output2 + " is required for this operation."); 
		
	}

	public CommandLineInterfaceClient(String[] args) throws ParseException {
		CommandLineParser parser = new DefaultParser();		
	
		// Options
		options = new Options();
		options.addOption("q", QUIET, false, "Turn all messages off");
		options.addOption("v", VERBOSE, false, "Turn debug on");
		options.addOption("V", VERY_VERBOSE, false, "Turn all debug on");
		options.addOption("o", OUTPUT, true, "Output file");
		options.addOption("i", INFERENCE, true, "Find infered triples from file."); 
		options.addOption("f", FILE, true, "Provide a file. Exact use varies with option. If no action is given, simply open the file.");
		options.addOption("F", FOLDER, true, "Provide a folder. Exact use varies with option. If no action is given, simply take the contents of the folder.");
		options.addOption("c", FUSEKICONFIG, true, "Generate a fuseki config file. Output to arg. --" + FILE + " designates the source folder");
		options.addOption("n", NAME, true, "The name of a thing, for instance the dataset name");
		options.addOption("I", INDIRECT_SUBCLASSOF_AXIOMS, false, "Also include indirect subclasses when calculating inferred triples");
		options.addOption("h", HELP, false, "Also include indirect subclasses when calculating inferred triples");
		options.addOption("a", AUX, true, "NOTE: DOES NOT WORK IN THIS VERSION. Exclude files in this folder from the default graph, but put them in their own graphs.") ;
		options.addOption("d", IMPORTS_PRINT, true, "List all direct and indirect imports of one ontology given in --file. The argument decides how the list is printed.");
		options.addOption("m", IMPORTS, false, "Toggle to make -f be a list of files based on owl:imports, instead of simply one file or folder. Mainly for use with other arguments.");
		options.addOption("M", ONLYMANCHESTER, false, "Only try Manchester Syntax (for debugging omn files)"); 
		options.addOption("E", SHOW_ALL_ERRORS, false, "Show stacktrace on error."); 
		options.addOption("C", COMBINE, true, "Combine files listed in -f to one file, the argument of this command. If --" + IMPORTS + " is on, take imports closure instead"); 
		options.addOption("l", LIST_FILE, true, "Read a file for a list of files to include. Can be combined with -f (providing a folder for these files), and -m /-x");
		options.addOption("x", EXCLUDE_LIST, true, "Exclude the files in the file provided from the result set.");
		options.addOption("p", ONLY_PARSE, false, "Parse selected file(s)"); 
		this.line = parser.parse( options, args );				
	}	
	
	
	public static String[] addDebugArgs(String type) {
		System.out.println("========================================================\n"
				+ "         TEST MODE IS ON\n"
				+"========================================================\n"
				+ "if you dont know what this message means, something \nwent wrong when uploading the tool."
				+ " The program is \nuseless in its present form and you need a new copy. \n=======================================================\n"); 

		String s = null;		
		if (type.equals(INFERENCE)) {
			s = "--" + INFERENCE + " C:/Data/ahus/ontologi/release/ahus-dks.ttl --" + OUTPUT + " C:/Data/ahus/ontologi/release/inference.ttl";
		}
		else if(type.equals(FUSEKICONFIG)) {
			s = "-v -E --" + FUSEKICONFIG + " test.ttl --" + FILE + " C:/Data/maritim/modam/ontology/ttl/compass.ttl --" + IMPORTS + " -n name";
		}
//		else if(type.equals(FILE)) {
//			s = "-v --" + FILE + " C:/Data/aibel-bckp/transforms/RDF/skos-MMD.ttl";  //+ " --" + ONLYMANCHESTER; 
//		}
		else if (type.equals(IMPORTS_PRINT)) {
			s = "-v -E --" + IMPORTS_PRINT + " fns;s --" + FILE + " C:/Data/aibel-bckp/transforms/RDF/product.ttl"; 
		}
		else if (type.equals(FILE)) {
			s = "-v -E --" + IMPORTS + " --" + FILE + 
					" \'C:/Data/maritim/modam/ontology/N3072_ISO_CD_15926-12_CD_Ballot/ISO 15926-12 ontology and examples/ontology-v-0.1/turtle/collector-DL-native-v-0.1.ttl\'"
					; 
		}
		else if(type.equals(HELP)) {
			s = "--" + HELP; 
		}
		else { 
			System.out.println("Illegal debug type: " + type); 
			System.exit(1); 
		}
		System.out.println("All arguments replaced by these:\n" + s);
		return s.split(" ");		
	}
	
	public static void main(String[] args) throws Exception {
		
		args = addDebugArgs(FILE);
		
		
		CommandLineInterfaceClient client = new CommandLineInterfaceClient(args); 		
		client.execute();	
	}
	
}


