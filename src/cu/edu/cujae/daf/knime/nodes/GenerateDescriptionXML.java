/**
package cu.edu.cujae.daf.knime.nodes;

import cu.edu.cujae.daf.core.Algorithm;
import cu.edu.cujae.daf.core.DiscoveryMode;
import cu.edu.cujae.daf.context.dataset.Dataset.ClassType;
import cu.edu.cujae.daf.context.dataset.Dataset.DiscreteClass$;
import cu.edu.cujae.daf.context.dataset.Dataset.NumericClass$;
import cu.edu.cujae.daf.context.dataset.Dataset.SurvivalClass$;
import cu.edu.cujae.daf.formatter.FormatSymbols;
import scala.Tuple2;
import scala.Tuple4;
import cu.edu.cujae.daf.IO;

import java.util.Map;

public class GenerateDescriptionXML {

	private static final String dinosName = "dinos";

	private static final String dinosAllUpper = dinosName.toUpperCase();

	private static final String dinosFirstUpper = capitalizeFirst(dinosName);

	private static final String lineBreak = "<br/>";

	private static final String doubleLineBreak = lineBreak + lineBreak;

	private enum DINOS_TYPES {
		DISCOVERY,
		EXTRACTOR,
		PARSER
	}

	;

	private scala.collection.immutable.Map<ClassType, DiscoveryMode> modes = Algorithm.modesMap_type();

	// XML HEADER

	private static final String xmlHeader = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";

	private static void addXMLHeader(Tuple4<ClassType, DINOS_TYPES, StringBuilder, DiscoveryMode> info) {
		info._3().append(xmlHeader);
	}

	static Map<DINOS_TYPES, String> knimeNodeType = Map.of(
			DINOS_TYPES.DISCOVERY, "Learner",
			DINOS_TYPES.EXTRACTOR, "Manipulator",
			DINOS_TYPES.PARSER, "Predictor"
	);

	// KNIME HEADER

	private static final String beginKnimeHeader = "<knimeNode icon=\"./default.png\" type=\"";
	private static final String endKnimeHeader = "\" xmlns=\"http://knime.org/node/v2.8\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://knime.org/node/v2.10 http://knime.org/node/v2.10.xsd\">";

	private static void addKnimeHeader(Tuple4<ClassType, DINOS_TYPES, StringBuilder, DiscoveryMode> info) {
		String typeString = knimeNodeType.get(info._2());
		var builder = info._3();
		builder.append(beginKnimeHeader).append(typeString).append(endKnimeHeader);
		builder.append('\n');
	}

	// NAME

	static Map<DINOS_TYPES, String> knimeNodeName = Map.of(
			DINOS_TYPES.DISCOVERY, "Subgroup Discovery",
			DINOS_TYPES.EXTRACTOR, "Dataset As Subgroup Extractor",
			DINOS_TYPES.PARSER, "Subgroup Parser"
	);

	public static String capitalizeFirst(String string) {
		return string.substring(0, 1).toUpperCase() + string.substring(1);
	}

	private static void addName(Tuple4<ClassType, DINOS_TYPES, StringBuilder, DiscoveryMode> info) {
		String nameString = knimeNodeName.get(info._2());
		String newName = capitalizeFirst(info._4().alternateName());
		info._3().append("\n<name>")
				.append(dinosAllUpper)
				.append(" ")
				.append(newName)
				.append(" ")
				.append(nameString)
				.append("</name>\n");
	}

	// SHORT DESCRIPTION

	static Map<ClassType, String> classShortDesc = Map.of(
			DiscreteClass$.MODULE$, "Single target nominal (String or Boolean columns)",
			NumericClass$.MODULE$, "Single target numeric (Integer or Double columns)",
			SurvivalClass$.MODULE$, "Survival Analysis"
	);

	private static void addShortDescription(Tuple4<ClassType, DINOS_TYPES, StringBuilder, DiscoveryMode> info) {

		String typeString = classShortDesc.get(info._1());
		String text = "";
		DINOS_TYPES type = info._2();
		var builder = info._3();

		if (type == DINOS_TYPES.DISCOVERY) {
			text = typeString + "Subgroup Discovery with the DINOS algorithm";
		} else if (type == DINOS_TYPES.EXTRACTOR) {
			text = "Extract the entire dataset as one big DINOS subgroup for " + text;
		} else if (type == DINOS_TYPES.PARSER) {
			text = "Create DINOS subgroups for " + text + " given a dataset and a text descriptions of said subgroups";
		}

		builder.append("\n<shortDescription>")
				.append(text)
				.append("</shortDescription>\n");
	}

	private static final String endTag = "</knimeNode>";

	// INTRO

	private static final String modeDescriptionIntro = "This specific node is for use in tables ";
	private static final String modeDescriptionDiscrete = "with a single nominal / discrete target variable among the String or Boolean columns.";
	private static final String modeDescriptionNumeric = "with a single numeric target variable among the Integer or Double columns (not Long).";
	private static final String modeDescriptionSurvival = "describing Survival Analysis data. SA is a branch of statistics which uses specific techniques to analyze datasets with a particular kind of heterogeneous data with a measured time as target variable: The dataset contains both rows that have suffered an event of interest while others haven't (in this last case the data is said to be \"censored\")\n" +
			lineBreak + "The time to the event must be a Double or Integer column and also cannot contain zero or negative values." +
			lineBreak + "The censoring indicator can be either String, Boolean or Integer.";

	private static void addDinosModeDescription(Tuple4<ClassType, DINOS_TYPES, StringBuilder, DiscoveryMode> info) {
		var builder = info._3();
		var mode = info._1();
		builder.append(modeDescriptionIntro);
		String appendThis = null;

		if (mode == DiscreteClass$.MODULE$) {
			appendThis = modeDescriptionDiscrete;
		} else if (mode == NumericClass$.MODULE$) {
			appendThis = modeDescriptionNumeric;
		} else if (mode == SurvivalClass$.MODULE$) {
			appendThis = modeDescriptionSurvival;
		}

		builder.append(appendThis);
	}

	private static final String discoveryIntro = "\t\t\tFinds subgroups in the given data using the DINOS algorithm. Subgroup discovery is a data mining technique for extracting easily understandable \"IF-THEN\" rules with respect to a chosen target variable. Said rules are supposed to be easily understandable for the final user, distinct from one another with the minimum possible amount of overlap, and taking all rules as a whole they should describe and cover as much of the data as possible.\n\t\t\t" + doubleLineBreak;

	private static final String dinosPapers = doubleLineBreak + "Papers describing the algorithm:\n" +
			"\t\t\t" + lineBreak + "\n" +
			"\t\t\t<a href=\"https://www.researchgate.net/publication/348407181_Nuevo_metodo_para_el_descubrimiento_de_subgrupos_no_redundantes\">\n" +
			"\t\t\tOriginal Paper (in Spanish)</a>";

	private static void addDiscoveryIntro(Tuple4<ClassType, DINOS_TYPES, StringBuilder, DiscoveryMode> info) {
		var builder = info._3();
		builder.append(discoveryIntro);
		addDinosModeDescription(info);
		builder.append(dinosPapers);
	}

	private static final String extractorIntro = "Takes the given table to create a DINOS subgroup from it. Class value will be empty. Mostly useful for Survival Analysis, where the resulting subgroup can be appended to Discovery or Parser Results to make a Kaplan-Meier graph comparing subgroups and the dataset's instances." + doubleLineBreak;

	private static void addExtractorIntro(Tuple4<ClassType, DINOS_TYPES, StringBuilder, DiscoveryMode> info) {
		var builder = info._3();
		builder.append(extractorIntro);
		addDinosModeDescription(info);
	}

	private static final String parserIntro = "From textual description of subgroups, find the instances in the data that belongs to said subgroups. DINOS Parser Nodes accept both rules without the \"THEN\" part (which will be calculated on run time) or the full \"IF-THEN\". Useful for either recreating DINOS runs, evaluating arbitrary hand-made subgroups or seeing how different metrics evaluate the same subgroups.";
	private static final String nominalParserRules = "Rules for this mode can be input as either:" + lineBreak +
			"1 - Just a String Column with the IF part: " + "column1 = {\"petalLength in [1.0 ; 1.9]\"}" + lineBreak +
			"2 - A String Column with both the IF and the \"THEN\" part: " + "column1 = {\"petalLength in [1.0 ; 1.9] --> class = \"Iris-setosa\" \"}" + lineBreak;
	private static final String numericParserRules = "Rules for this mode can be input as either:" + lineBreak +
			"1 - Just a String Column with the IF part: " + "column1 = {temperature in [97.2; 97.8]}" + lineBreak +
			"2 - A String Column with both the IF and the \"THEN\" part: " + "column1 = {\"temperature in [97.2; 97.8] --> heart_rate in [58.0; 78.0]}\"" + lineBreak;
	private static final String survivalParserRules = lineBreak + "Unlike other modes, this one doesn't need an \"THEN\" part and just needs a String column with the description:" + lineBreak +
			"column 1 = {\"sex equals M\"}" + lineBreak +
			"This mode can be specially useful for getting segments of populations not obtained by the subgroups and then concatenate it with instance results to make a Kaplan Meier graph with both. Using the previous example, with the Table Creator a \"sex equals F\" not found by DINOS and then concatenate it with the \"sex equals M \" instances";

	static Map<ClassType, String> parserModeRules = Map.of(
			DiscreteClass$.MODULE$, nominalParserRules,
			NumericClass$.MODULE$, numericParserRules,
			SurvivalClass$.MODULE$, survivalParserRules
	);

	private static void addParserIntro(Tuple4<ClassType, DINOS_TYPES, StringBuilder, DiscoveryMode> info) {
		var builder = info._3();
		builder.append(parserIntro);
		builder.append(lineBreak);
		builder.append(lineBreak);
		builder.append( FormatSymbols.whiteSpace() );

		addDinosModeDescription(info);

		String modeIntro = parserModeRules.get( info._1() );
		builder.append(modeIntro);
	}

	private static void addIntro(Tuple4<ClassType, DINOS_TYPES, StringBuilder, DiscoveryMode> info) {

		var currentNodeType = info._2();
		var builder = info._3();

		builder.append("\n<intro>\n");
		if (currentNodeType == DINOS_TYPES.DISCOVERY) {
			addDiscoveryIntro(info);
		} else if (currentNodeType == DINOS_TYPES.EXTRACTOR) {
			addExtractorIntro(info);
		} else if (currentNodeType == DINOS_TYPES.PARSER) {
			addParserIntro(info);
		}
		builder.append("\n</intro>\n");

	}

		// VISUAL CONFIGURATION OPTIONS
	private static void addTabStart(Tuple4<ClassType, DINOS_TYPES, StringBuilder, DiscoveryMode> info, String tab) {
		info._3().append("\n" + "<tab name=\"").append(tab).append("\">");
	}

	private static void addTabEnd(Tuple4<ClassType, DINOS_TYPES, StringBuilder, DiscoveryMode> info) {
		info._3().append("\n</tab>\n");
	}

	private static String classColumnTag = "Class Column";
	private static String columnToDefine = "The column to define as target for subgroup discovery. The node refuses to execute or open the dialogue window If it can't find a column of the supported types for this mode";

	static Map<ClassType, String> classColumnName = Map.of(
			DiscreteClass$.MODULE$,
				createOptionsTag(classColumnTag , columnToDefine + " (String)."),
			NumericClass$.MODULE$,
				createOptionsTag(classColumnTag , columnToDefine + " (Integer or Double)."),
			SurvivalClass$.MODULE$,
				( createOptionsTag(classColumnTag , columnToDefine + " (Integer or Double, positive for survivial times).") +
				createOptionsTag("Status/Censoring column" , "The column for indicating if the row is censored or not. Must be Integer, String or Boolean. Cannot be the same as the class column.") +
				createOptionsTag("Censored Row Indication" , "Which value of the censoring column use to indicate censoring. If the value is outside the allowed ranges for the column, the node will issue an error on execution.") )
	);

	private static void addClassOptionsToGeneralTab(Tuple4<ClassType, DINOS_TYPES, StringBuilder, DiscoveryMode> info) {
		info._3().append( classColumnName.get( info._1() ) );
	}

	static Map<DINOS_TYPES, String> otherOptionsGeneralTab = Map.of(
			DINOS_TYPES.DISCOVERY,
				createOptionsTag("Seed To Use" , "Define the seed for random generation, if in doubt leave the checkbox unmarked to always use a new seed in every execution. Notice that even with the same seed it is not guaranteed that execution results will be identical among rules."),
			DINOS_TYPES.EXTRACTOR,
				"",
			DINOS_TYPES.PARSER,
				createOptionsTag("Subgroup Body Description" , "The column with either the IF part of the subgroup description or the whole \"IF-THEN\"")
	);

	private static void addOtherOptionsToGeneralTab(Tuple4<ClassType, DINOS_TYPES, StringBuilder, DiscoveryMode> info) {
		info._3().append( otherOptionsGeneralTab.get( info._2() ) );
	}

	private static void addGeneralTab(Tuple4<ClassType, DINOS_TYPES, StringBuilder, DiscoveryMode> info) {
		addTabStart(info, "General");

		addClassOptionsToGeneralTab(info);

		addOtherOptionsToGeneralTab(info);

		addTabEnd(info);
	}

	private static String commonMetrics =
			createOptionsTag("NOTE: " , "Hover the mouse pointer over components to read a description of them")+
			createOptionsTag("Metric (metrics)" , "Method or formulas to get the quality of a subgroup, with quality defined as a numerical value to be maximized that the larger it is shows the subgroup is more different from the rest of the data") +
			createOptionsTag("Objectives (objs)" , "This uses the same methods for the metrics, but are instead used in the solution generator (see below)") +
			createOptionsTag("Class Selector (classSel)" , "Criteria to select the value of the target variable in each subgroup") +
			createOptionsTag("Feasibility Evaluator (feas)" , "Determine if a candidate subgroup is feasible") +
			createOptionsTag("Feasibility Operator (feasOp)" , "Feasibility criteria of an individual, which modifies previously defined candidate subgroups as unfeasible until it is marked as feasible") +
			createOptionsTag("Tuners (tuners)" , "Modifies candidate subgroup to a certain criteria after creation") +
			createOptionsTag("Dominance (dom)" , "Dominance criteria between two candidate subgroups to use in the genetic algorithm, dominance determines which of two subgroups to be compared is the best") +
			createOptionsTag("Redundancy (red)" , "Redundancy criteria between two candidate subgroups to use in the genetic algorithm, which determines if the subgroups to be compared cover the same instances i.e: their results overlap each other") +
			createOptionsTag("External Population Updater (upd)" , "After each algorithm iteration, which filter to apply to determine if any of the newly generated subgroups is better than the ones previously defined as the best") +
			createOptionsTag("Individual (ind)" , "An individual is an internal representation of a candidate subgroup, not final ones as presented to the user") +
			createOptionsTag("Initial Population Generator (popGen)" , "At the beginning of each iteration, which method to use to create new candidate subgroups") +
			createOptionsTag("Solution Generator (solGen)" , "Method to use to determine the best candidates among the initial population") +
			createOptionsTag("Crossover Operator (cross)" , "Crossover method for the genetic algorithm, which creates a new candidate subgroup from a list of other candidates") +
			createOptionsTag("Mutate Operator (mut)", "Mutation method for the genetic algorithm, which alters the given candidate based on a certain criteria");


	private static void addComponentsTab(Tuple4<ClassType, DINOS_TYPES, StringBuilder, DiscoveryMode> info) {
		addTabStart(info, "Components");

		info._3().append( createOptionsTag("Use Default Components" , "If true, ignore all custom components configuration and use the default one") );
		info._3().append( commonMetrics );

		addTabEnd(info);
	}

	static String commonHyperParams =
			createOptionsTag("Component Specific Configuration Parameters" , "The components of the algorithm can use variables to fine tune their behavior. All available settings are collected here, notice that some components may use the same setting.") +
			createOptionsTag("NOTE: " , "Hover the mouse pointer over settings to read a description of them");


	private static void addSettingsTab(Tuple4<ClassType, DINOS_TYPES, StringBuilder, DiscoveryMode> info) {
		addTabStart(info, "Settings");

		StringBuilder builder = info._3();
		addOptionTag(info, "Maximum Amount of Evaluations" , "The algorithm will make no more evaluations and stop after doing the specified amount. Due to the iterative cycle of the genetic algorithm where it replaces subgroups previously defined as the best as it finds better ones the higher this number is, the more chances and time the algorithm will have to find better subgroups. However, making it larger will not magically find better subgroups for the given data, there being a limit depending on both the component configuration and the distribution of the data.");
		if( info._2() == DINOS_TYPES.DISCOVERY )
			addOptionTag(info, "Choose Fixed Variables" , "Fixing a variable means to force it to always appears in a subgroup. Useful when wanting to study a specific subset of data. Fixed variables are the ones on the LEFT box, outlined in red");
		builder.append(commonHyperParams);
		addTabEnd(info);
	}
	private static String createOptionsTag(String name, String description){
		return "\n\t<option name=\"" + name + "\">" + description + " </option>";
	}

	private static void addOptionTag(Tuple4<ClassType, DINOS_TYPES, StringBuilder, DiscoveryMode> info, String name, String description){
		info._3().append( createOptionsTag(name, description) );
	}

	private static void addOptions(Tuple4<ClassType, DINOS_TYPES, StringBuilder, DiscoveryMode> info) {

		addGeneralTab(info);
		if (info._2() != DINOS_TYPES.EXTRACTOR) {
			addComponentsTab(info);
			addSettingsTab(info);
		}
		info._3().append("\n");
	}

		// FULL DESCRIPTION

	private static void addFullDescription(Tuple4<ClassType, DINOS_TYPES, StringBuilder, DiscoveryMode> info) {
		info._3().append("\n<fullDescription>\n");

		addIntro(info);

		addOptions(info);

		info._3().append("\n</fullDescription>\n");
	}

		// PORTS

	private static final Tuple2<String, String>[] inPortsDiscovery = new Tuple2[]{
			new Tuple2<>("Input data table", "The table in which to discover subgroups")};

	private static final Tuple2<String, String>[] inPortsExtractor = new Tuple2[]{
			new Tuple2<>("Input data table", "The table which to discover subgroups")};

	private static final Tuple2<String , String>[] inPortsParser  = new Tuple2[] {
			new Tuple2<>("Input data table", "The table which to use as dataset") ,
			new Tuple2<>("Subgroup Descriptions", "The table with subgroup descriptions for parsing")};

	private static final Map <DINOS_TYPES , Tuple2<String , String>[]> inPortsMap = Map.of(
			DINOS_TYPES.DISCOVERY , inPortsDiscovery,
			DINOS_TYPES.EXTRACTOR , inPortsExtractor,
			DINOS_TYPES.PARSER , inPortsParser
	);

	private static final Tuple2<String , String>[] outPorts  = new Tuple2[] {
			new Tuple2<>("Subgroups", "Subgroups Description With Metrics") ,
			new Tuple2<>("Instances", "Description of each instance and which subgroup it belongs") ,
			new Tuple2<>("Runtime Information", "Execution Information and Configuration variables")};

	private static final Map <DINOS_TYPES , Tuple2<String , String>[]> outPortsMap = Map.of(
			DINOS_TYPES.DISCOVERY , outPorts,
			DINOS_TYPES.EXTRACTOR , outPorts,
			DINOS_TYPES.PARSER , outPorts
	);

	private static void addPortsHelper(String tag , StringBuilder builder, Tuple2<String , String>[] data) {
		for(int count = 0 ; count < data.length ; ++ count) {
			var current = data[count];
			builder.append("\t<");
			builder.append(tag);
			builder.append(" index=\"");
			builder.append(count);
			builder.append("\" name=\"");
			builder.append(current._1);
			builder.append("\">");
			builder.append(current._2);
			builder.append("</");
			builder.append(tag);
			builder.append(">\n");
		}

		builder.append('\n');
	}

	private static void addPorts(Tuple4<ClassType, DINOS_TYPES, StringBuilder, DiscoveryMode> info ) {
		var builder = info._3();

		builder.append("\n<ports>\n\n");

		var inPortsToAdd = inPortsMap.get(info._2());
		addPortsHelper("inPort" , builder, inPortsToAdd);

		var outPortsToAdd = outPortsMap.get(info._2());
		addPortsHelper("outPort" , builder, outPortsToAdd);

		builder.append("</ports>");
	}

	// END TAG

	private static void addKnimeEndTag(Tuple4<ClassType, DINOS_TYPES, StringBuilder, DiscoveryMode> info ) {
		info._3().append( "\n\n" + endTag );
	}


	// CREATE FILE NAME

	private static final String fileNameEnd = "NodeFactory.xml";

	private static final Map <DINOS_TYPES , String> knimeNodeFileNameMiddle = Map.of(
			DINOS_TYPES.DISCOVERY , "SubgroupDiscovery",
			DINOS_TYPES.EXTRACTOR , "DatasetAsSubgroupExtractor",
			DINOS_TYPES.PARSER , "Parser"
	);

	private static String createFileName(Tuple4<ClassType, DINOS_TYPES, StringBuilder, DiscoveryMode> info , String path) {
		String alternate =  info._4().alternateName();
		String middle = knimeNodeFileNameMiddle.get( info._2() );
		return path + "/" + alternate + "/" + dinosFirstUpper + capitalizeFirst( info._4().alternateName() ) + middle + fileNameEnd;
	}

		// THE ACTION

	public static void generateDescriptionXML(String path) {

		for( DINOS_TYPES currentType : DINOS_TYPES.values() ) {

			DiscoveryMode[] modesArray = Algorithm.modesArray();

			Tuple4<ClassType , DINOS_TYPES , StringBuilder , DiscoveryMode>[] builders
					= new Tuple4[ Algorithm.modesArray().length ] ;

			for(int count = 0 ; count < builders.length  ; ++count)
				builders[count] = new Tuple4<>(
							modesArray[count].datasetType(),
							currentType,
							new StringBuilder() ,
							Algorithm.modesMap_type().get(modesArray[count].datasetType()).get()
						);

			for(int count = 0 ; count < builders.length ; ++count) {
				var current = builders[count];
				addXMLHeader(current);
				addKnimeHeader(current);
				addName(current);
				addShortDescription(current);
				addFullDescription(current);
				addPorts(current);
				addKnimeEndTag(current);

				IO.writeFile(
						createFileName( current , path )
						, current._3() );

			}
		}
	}
}
*/