package cu.edu.cujae.daf.knime.nodes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.knime.core.data.BooleanValue;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataValue;
import org.knime.core.data.MissingCell;
import org.knime.core.data.StringValue;
import org.knime.core.data.container.CloseableRowIterator;
import org.knime.core.data.def.BooleanCell;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.defaultnodesettings.DialogComponent;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelSeed;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.defaultnodesettings.SettingsModelStringArray;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.flowvariable.FlowVariablePortObject;
import org.knime.core.node.port.inactive.InactiveBranchPortObject;
import org.knime.core.node.workflow.VariableType.BooleanType;
import org.knime.core.node.workflow.VariableType.IntType;
import org.knime.core.node.workflow.VariableType.LongType;
import org.knime.core.node.workflow.VariableType.StringArrayType;
import org.knime.core.node.workflow.VariableType.StringType;

import cu.edu.cujae.daf.codification.Gene;
import cu.edu.cujae.daf.codification.individual.Individual;
import cu.edu.cujae.daf.context.Configuration;
import cu.edu.cujae.daf.context.dataset.Attribute;
import cu.edu.cujae.daf.context.dataset.AttributeType;
import cu.edu.cujae.daf.context.dataset.Dataset;
import cu.edu.cujae.daf.context.dataset.Dataset.ClassType;
import cu.edu.cujae.daf.context.dataset.INTEGER$;
import cu.edu.cujae.daf.context.dataset.Instance;
import cu.edu.cujae.daf.context.dataset.NOMINAL$;
import cu.edu.cujae.daf.context.dataset.REAL$;
import cu.edu.cujae.daf.context.dataset.SURVIVAL$;
import cu.edu.cujae.daf.context.dataset.SurvClassDataset;
import cu.edu.cujae.daf.core.Algorithm;
import cu.edu.cujae.daf.core.ComponentInfo;
import cu.edu.cujae.daf.core.DiscoveryMode;
import cu.edu.cujae.daf.core.Subgroup;
import cu.edu.cujae.daf.evaluation.metric.Metric;
import cu.edu.cujae.daf.knime.dialogcomponents.DialogComponentCheckBoxGroupReferenced;
import cu.edu.cujae.daf.knime.dialogcomponents.DialogComponentStringSelectionReferenced;
import cu.edu.cujae.daf.randomize.Randomize;
import cu.edu.cujae.daf.utils.SubgroupParser;
import scala.Option;
import scala.Tuple2;
import scala.Tuple4;
import scala.collection.JavaConverters;
import scala.collection.mutable.Set;
import scala.jdk.CollectionConverters;

/**
 * Contains all methods and constants for interfacing with the DAF library
 * Must be overriden for specific modes
 * 
 * @author Jonathan David González Pereda, CUJAE
 */

	@SuppressWarnings("static-access")
public abstract class GenericDinosKnimeWorkflow {

	
	
		/*
		 *  CONSTANTS
		 */
	
	public static final scala.collection.mutable.Map<String, scala.collection.mutable.Map<String, Set<ComponentInfo>>>
		TREE_COMPONENTS = getComponentsTree();
	
	public static final Map<String, scala.collection.mutable.Map<String, Set<ComponentInfo>>>
		TREE_COMPONENTS_CONVERTED = JavaConverters.mapAsJavaMapConverter(TREE_COMPONENTS).asJava();
		
		// Strings for configuration keys
	public static final String KEY_DEFAULTCOMPONENTS = "useDefault";
	public static final String KEY_TARGET = "targetClass";
	public static final String KEY_DESC = "descClass";
	public static final String KEY_IN = "inClass";
	public static final String KEY_THEN = "thenClass";
	public static final String KEY_SEED = "seedAlgorithm";
	public static final String KEY_TRIALS = "trials";
	public static final String KEY_COLLECTITERATIONS = "collectIterations";
	public static final String KEY_SETTINGS = "settings";
 
	public enum NODES_TYPES {
			DISCOVERY,
			EXTRACTOR,
			PARSER
	};
	
		// Default values
	public static final boolean DEFAULT_DEFAULTCOMPONENTS = true;
	public static final long DEFAULT_SEED = 0;
	public static final String DEFAULT_TARGET = null;
	public static final String DEFAULT_DESC = null;
	public static final String DEFAULT_BODY = null;
	public static final String DEFAULT_IN = null;
	public static final String DEFAULT_THEN = null;
	public static final boolean DEFAULT_GENERATESEED = false;
	public static final int DEFAULT_CHECKBOXGROUPELEMENTSPERROW = 2;
	public static final int DEFAULT_CHECKBOXMETRICSMINIMUM = 1;
	public static final int DEFAULT_TRIALS = Configuration.getDefaultMaxTrials();
	public static final int DEFAULT_TRIALS_MIN = 1;
	public static final int DEFAULT_TRIALS_MAX = Integer.MAX_VALUE;
	public static final int DEFAULT_TRIALSTEPSIZE = 1000;
	public static final boolean DEFAULT_COLLECTITERATIONS = true;
	public static final int DEFAULT_UPDATE_INITIALDELAY = 0;
	public static final int DEFAULT_UPDATE_EVERY = 100;	// Tenth of second
	public static final TimeUnit DEFAULT_UPDATE_UNIT = TimeUnit.MILLISECONDS;
	public static final String DEFAULT_PREDICTION = "Prediction";
	public static final Class<? extends DataValue>[] TARGETS_DESCTYPES = new Class[] {StringValue.class};
	public static final Class<? extends DataValue>[] TARGETS_INTYPES = new Class[] {BooleanValue.class};
	public static final Class<? extends DataValue>[] TARGETS_THENYPES = new Class[] {StringValue.class};
	
		// Boolean Text
	public static final String BOOL_TRUE_TEXT = Boolean.toString(true);
	public static final int BOOL_FALSE_NUM = 0;
	public static final String BOOL_FALSE_TEXT = Boolean.toString(false);
	public static final int BOOL_TRUE_NUM = 1;
	
		// Name of Dialog Components and column names
	public static final String NAME_OPTIONAL = " (Optional)";
	public static final String NAME_CLASS = "Define target class";
	public static final String NAME_CLASSCOLUMN = "Class Column";
	public static final String NAME_SEED = "Seed for Random Generation";
	public static final String NAME_SEEDLABEL = "Seed to use (disable to always generate a random one)";
	public static final String NAME_DEFAULTCOMPONENTS = "Use Default Components";
	public static final String NAME_OVERALCONFIGS = "Overall configuration parameters";
	public static final String NAME_SPECIFICCONFIGS = "Component specific configuration parameters";
	public static final String NAME_TRIALS = "Maximum amount of evaulations";	
	public static final String NAME_TRIALS_ERROR = "Amount of trials must be a positive value";
	public static final String NAME_COLLECTITERATIONS = "Save the metrics of interest after each generator run";
	public static final String NAME_RESETCOMPONENTS = "Reset All";
	public static final String NAME_CENSORCOL = "Status/Censoring column";
	public static final String NAME_CENSORIND = "Censored row indication";
	public static final String NAME_PARSER = "Define columns where to find subgroup descriptions";
	public static final String NAME_PARSER_DESCRIPTION = "Subgroup Body Description";
	public static final String NAME_PARSER_IN = "Subgroup included or not" + NAME_OPTIONAL;
	public static final String NAME_PARSER_THEN = "Subgroup Class" + NAME_OPTIONAL;
	public static final String TAB_PARSER = "Parsing options";
	
		// Messages for the node's warning, error and progress bar
	public static final String MESSAGE_PROCESSCOLUMNS = "Processing Attributes (Columns of the Table)";
	public static final String MESSAGE_EXECUTION = "Discovering Subgroups";
	public static final String MESSAGE_CREATINGINSTANCES = "Creating instances for use in the Algorithm";
	public static final String MESSAGE_CREATINGATTRIBUTES = "Creating attributes as used in the algorithm";
	public static final String MESSAGE_READY = "Dataset ready";
	public static final String MESSAGE_TRIAL = MESSAGE_EXECUTION + ", trial:";
	public static final String MESSAGE_EMPTYTABLE = "The provided table is empty";
	public static final String MESSAGE_NOCLASSVALIDCONFIG = "No class column set but valid configuration";
	public static final String MESSAGE_NOCLASS = "Table contains no suitable column to set as target";
	public static final String MESSAGE_GUESING = "Guessing target column: ";
	public static final String MESSAGE_CONFIGCLASSNOTFOUND = "Previously configured class column not found or incompatible: ";
	public static final String MESSAGE_FINISHED = "Finished Discovering Subgroups, parsing results";
	private static final String MESSAGE_PARSER = "Parsing Subgroups";
	public static final String ERROR_PARSER_SAMECLASSTHEN = "The subgroup body and class columns cannot be the same";

		// Cell types identifiers
	public static final String ID_STRING = "org.knime.core.data.def.StringCell";
	public static final String ID_BOOL = "org.knime.core.data.def.BooleanCell";
	public static final String ID_DOUBLE = "org.knime.core.data.def.DoubleCell";
	public static final String ID_INT = "org.knime.core.data.def.IntCell";

		// Order of input ports for the nodes
	public static final int PORT_INPUT_DATASET = 0;
	public static final int PORT_INPUT_DESCRIPTIONS = 1;
	public static final PortType[] PORT_INPUT_USUAL = new PortType[] {
			BufferedDataTable.TYPE};
	
	public static final PortType[] PORT_INPUT_PARSER = new PortType[] {
			BufferedDataTable.TYPE,
			BufferedDataTable.TYPE
			};
	
		// Port types (for model constructor)
	public static final int PORT_OUTPUT_SUBGROUPS = 0;
	public static final int PORT_OUTPUT_INSTANCES = 0;
	public static final int PORT_OUTPUT_VARIABLES = 0;
	public static final PortType[] PORT_OUTPUT_TYPES = new PortType[]{
			BufferedDataTable.TYPE,
			BufferedDataTable.TYPE,
			FlowVariablePortObject.TYPE};
	
		// Name of Tabs for Nodes Dialogues
	public static final String TAB_DEFAULT = "General";
	public static final String TAB_COMPONENTS = "Components";
	public static final String TAB_SETTINGS = "Settings";

		// Names of Available Components types
	public static final Tuple2<String, String>[] INFO_COMPONENTS = Algorithm.componentNames();
	public static final Tuple4<String, String, String, Object>[] INFO_CONFIGURABLES = Algorithm.configurableNames();


	
	
	
			/*
			 * SETTINGS MODEL CREATORS
			 */

		/**
		 * This returns the type of datacels supported, mainly as use 
		 * for filtering which can be used as classes in node dialogs
		 * 
		 *  @return The kinds of inputs cels supported by the current mode
		 */
	public abstract Class<? extends DataValue>[] getAceptedTargetTypes();
	
	public abstract Class<? extends DataValue> getThenTargetType();

	private static scala.collection.mutable.Map<String, scala.collection.mutable.Map<String, Set<ComponentInfo>>> getComponentsTree() {	
			var tree = Algorithm.allAvailable();
				tree.remove("format");
				return tree;
			}

		/**
		 * Each dataset have an identifier,
		 * this can be used to quickly get it
		 * 
		 *  @return The identifier of the supported dataset
		 */
	public ClassType getClassType() {
		return getModeHelper().datasetType();
	}
	
	public abstract DiscoveryMode getModeHelper();
	
		/**  @return A model for use in storing the information of the target class*/
	public static SettingsModelString createTargetClass() {
		return new SettingsModelString( KEY_TARGET , DEFAULT_TARGET );
		}
	
	public static SettingsModelString createDescClass() {
		return new SettingsModelString( KEY_DESC, DEFAULT_DESC);
	}
	
	public static SettingsModelString createInClass() {
		return new SettingsModelString( KEY_IN , DEFAULT_IN );
	}
	
	public static SettingsModelString createThenClass() {
		return new SettingsModelString( KEY_THEN , DEFAULT_THEN);
	}
		
		/**  @return A model for use in storing the information of the randomization seed*/
	public static SettingsModelSeed createGeneratorSeed() {
		return new SettingsModelSeed( KEY_SEED, DEFAULT_SEED, DEFAULT_GENERATESEED);
		}
	
		/**  @return A model for use in storing the amount of trials to use*/
	public static SettingsModelIntegerBounded createTrialModel() {
		return new SettingsModelIntegerBounded(KEY_TRIALS, DEFAULT_TRIALS, DEFAULT_TRIALS_MIN, DEFAULT_TRIALS_MAX);
		}
	
		/**  @return A model for use in storing whether to use or not the default settings*/
	public static SettingsModelBoolean createUseDefaultOrNot() {
		return new SettingsModelBoolean( KEY_DEFAULTCOMPONENTS , DEFAULT_DEFAULTCOMPONENTS );
		}
	
		/**  @return A model for use in storing if to collect or not metrics info after each algorithm iteration*/
	public static SettingsModelBoolean createCollectItarationsModel() {
		return new SettingsModelBoolean( KEY_COLLECTITERATIONS, DEFAULT_COLLECTITERATIONS);
	}
	


		/*
		 *  CLASSES FOR EACH COMPONENT
		 */
	
		/**
		 * This returns the available classes for each component
		 * TODO Currently hardcoded, pending change in DINOS to dinamically get classes
		 * 
		 *  @return A map where each key refers to an available classes, and in turn each value is another map with short name as key and verbose human readable name as value
		 */
	private static final Map< String , Map < String , String > > getAvailabeClasses() {

		return getClassesSpecifiedMode("");
	}
	
	public static final Map<String, Map<String, String>> getClassesSpecifiedMode(String mode) {

		Map< String , Map < String , String > > result = new LinkedHashMap<String, Map<String, String>>();
	
		for(int count = 0 ; count < INFO_COMPONENTS.length; ++count) {
			Tuple2<String, String> currentComponent = INFO_COMPONENTS[count];
			var currentInfo1 = JavaConverters.mapAsJavaMapConverter( TREE_COMPONENTS_CONVERTED.get(currentComponent._1) ).asJava();
			
			if(currentInfo1 != null) {
				var currentInfo2 = currentInfo1.getOrDefault(mode , null);
				
				if(currentInfo2 != null) {
				var iterator = currentInfo2.iterator();
				Map < String , String > insideMap = new LinkedHashMap<String, String>();
				
				while( iterator.hasNext() ) {
					ComponentInfo next = iterator.next();
					insideMap.put( next.shortName(), next.verboseName() );
				}
				
				result.put(currentComponent._1, insideMap);
				int hello = 45;
				}
			}
		}
		
		return result;
	}
	
		/**  @return The classes that are specific to this mode, same structure as getAvailabeClasses*/
	public final Map< String , Map < String , String > >getExclusiveClasses() {
	
		return getClassesSpecifiedMode( this.getModeHelper().identifier() );
		
	}
	
	public final Map<String, String[][]> combinedClasses = getCombinedClasses();
	
		/**  @return A map with the the short name of component as key and as value two arrays as a 2d array: first have the short name of each class and in the same position in the second the verbose name*/
	private Map<String, String[][]> getCombinedClasses() {

		Map<String, Map<String, String>> generalClasses = this.getAvailabeClasses();
		Map<String, Map<String, String>> exclusiveClasses = this.getExclusiveClasses();

		Map<String, String[][] > answer = new LinkedHashMap <String, String[][]> ();
		
		for(int count = 0 ; count < INFO_CONFIGURABLES.length ; ++count) {
			
			String keyToAdd = INFO_CONFIGURABLES[count]._1();
			String keyToFind = INFO_CONFIGURABLES[count]._3();
			int newSize = 0;
			
			Map<String, String> valueGeneral = generalClasses.getOrDefault(keyToFind, null);
			if (valueGeneral != null) newSize += valueGeneral.size();
			
			Map<String, String> valueExclusive = exclusiveClasses.getOrDefault(keyToFind, null);
			if (valueExclusive != null) newSize += valueExclusive.size();
				// First key then label
			String[][] toAdd = new String[2][newSize];
			int positionToAdd = 0;
			
				// First Add exclusives
			if (valueExclusive != null) {
				Iterator<String> iterator = valueExclusive.keySet().iterator();
				while( iterator.hasNext() ) {
					String next = iterator.next();
					toAdd[0][positionToAdd] = next;
					toAdd[1][positionToAdd] = valueExclusive.get(next);
					++positionToAdd;
				}
			}
				// Second, add general
			if (valueGeneral != null) {
				Iterator<String> iterator = valueGeneral.keySet().iterator();
				while( iterator.hasNext() ) {
					String next = iterator.next();
					toAdd[0][positionToAdd] = next;
					toAdd[1][positionToAdd] = valueGeneral.get(next);
					++positionToAdd;
				}
			}
			
			answer.put(keyToAdd, toAdd);
		}
		
		return answer;
	}
	
		/** @return A map where each key is one of the the available components types and the value is an array with the default values */
	public scala.collection.immutable.Map<String, String[] > getDefaultClasses() {
		return getModeHelper().getCombinedDefaultParameters();
	}

		/**
		 * This returns the visual components for selecting which classes to use for each component
		 * 
		 *  @return The visual components ready to be added to a DefaultNodeSettingsPane
		 */
	public DialogComponent[] createDialogComponentForAllComponentes() {
		
		DialogComponent[] answer = new DialogComponent[ INFO_CONFIGURABLES.length ];
		Map<String, String[]> defaults = defaultParameters;
		Map<String, String[][]> combinedClasses = this.combinedClasses;

		for(int count = 0 ; count < this.INFO_CONFIGURABLES.length ; ++count) {
			
			Tuple4<String, String, String, Object> currentDescription = INFO_CONFIGURABLES[count];
			
			switch ( (int) currentDescription._4() ) {
			case 0: {
				String[][] referencesAndLabels = combinedClasses.get( currentDescription._1() );
				answer[count] = new DialogComponentStringSelectionReferenced(
						(SettingsModelString) createSettingsModelFromComponentDescription(currentDescription),
						currentDescription._2(),
						referencesAndLabels[0],
						defaults.get( currentDescription._1() )[0],
						referencesAndLabels[1]);
				break;  } 
			
			case 1, 2: {
				String[][] referencesAndLabels = combinedClasses.get( currentDescription._1() );
				answer[count] = new DialogComponentCheckBoxGroupReferenced(
						(SettingsModelStringArray) createSettingsModelFromComponentDescription(currentDescription),
						currentDescription._2(),
						referencesAndLabels[1],
						referencesAndLabels[0],
						DEFAULT_CHECKBOXGROUPELEMENTSPERROW,
						DEFAULT_CHECKBOXMETRICSMINIMUM,
						defaults.get( currentDescription._1() ) );
				break;  } 
			default:
				throw new IllegalArgumentException("Unexpected value for component type" +  (int) currentDescription._4() );
			}
		}
		
		return answer;
	}

		/**
		 * This returns a settings model for selecting a class given a description
		 * 
		 *  @return The visual components ready to be added to a DefaultNodeSettingsPane
		 */
	private SettingsModel createSettingsModelFromComponentDescription( Tuple4<String, String, String, Object> componentDescription ) {

		SettingsModel answer = null;
		Map<String, String[]> defaults = defaultParameters;
		

		switch ( (int) componentDescription._4() ) {
		case 0: { answer = new SettingsModelString(componentDescription._1() , defaults.get( componentDescription._1() )[0]); break;  } 
		
		case 1, 2: { answer = new SettingsModelStringArray(componentDescription._1() , defaults.get( componentDescription._1() )); break;  } 
		default:
			throw new IllegalArgumentException("Unexpected value for component type" +  (int) componentDescription._4() );
		}
		
		return answer;
	}
	
	public final Map<String, String[]> defaultParameters = JavaConverters.mapAsJavaMapConverter(getDefaultClasses()).asJava() ;

		/** @return A map where the key is the short name for each configurable available and the value is it's corresponding seeting model*/
	public LinkedHashMap<String, SettingsModel> createSettingsModelForAllComponents() {
		
		LinkedHashMap< String , SettingsModel> answer = new LinkedHashMap<String, SettingsModel>();
		
		for(int count = 0 ; count < this.INFO_CONFIGURABLES.length ; ++count) {
			answer.put( this.INFO_CONFIGURABLES[count]._1() , createSettingsModelFromComponentDescription( this.INFO_CONFIGURABLES[count]));
		}
		
		return answer;
	}
	

		/*
		 *  SETTINGS FOR EACH COMPONENT
		 */
	
		/**
		 * This returns the available settings for each class of every component
		 * TODO Currently hardcoded, pending change in DINOS to dinamically get classes
		 * 
		 *  @return A map where each key refers to an available class, and in turn each value is another map with scomponent hort name as key and an array of hyperparameters as value
		 */
	public static final Map< String , Map < String , String[] > > getDefaultSettings() {
		
		LinkedHashMap<String, String[]> auxiliar = new LinkedHashMap<String, String[]>();
		Map< String , Map < String , String[] > > result = new LinkedHashMap<String, Map<String, String[]>>();

		 result.put( INFO_COMPONENTS[0]._1, null );
		 
		 result.put( INFO_COMPONENTS[1]._1, null );
		 
		 result.put( INFO_COMPONENTS[2]._1, null );

		 result.put( INFO_COMPONENTS[3]._1, null);
		 
		 result.put( INFO_COMPONENTS[4]._1 , null );
		 
		 auxiliar = new LinkedHashMap<String, String[]>();
		 auxiliar.put("RelativeDominance" , new String[]{"Dominance Overlap Threshold"} );

		 result.put( INFO_COMPONENTS[5]._1 , auxiliar );

		 auxiliar = new LinkedHashMap<String, String[]>();
		 result.put( INFO_COMPONENTS[6]._1 , Map.of("TwoStepRedundancy" , new String[] {"Redundancy Overlap Threshold"} ) );
		 
		 result.put( INFO_COMPONENTS[7]._1 , null );
		 	// Skip Subgroup Formatter
		 result.put( INFO_COMPONENTS[9]._1 , null );
		 
		 result.put( INFO_COMPONENTS[10]._1 , Map.of("DinosClassicGenerator" , new String[] {"Att Amplitude Factor"} ) );

		 auxiliar = new LinkedHashMap<String, String[]>();
		 auxiliar.put("SingleGeneOneTailMutation" , new String[] {"Att Amplitude Factor"} );
		 auxiliar.put("MultiGeneMutation" , new String[] {"Att Amplitude Factor"} );
		 result.put( INFO_COMPONENTS[11]._1 , auxiliar );
		 
		 result.put( INFO_COMPONENTS[12]._1, null );
		 
		 String[] generatorsParams = new String[] {"T",
				    "H",
				    "Number of Objectives",
				    "Crossover Probability",
				    "Neighborhood Crossover Probability",
				    "Mutation Probability",
				    "Max Ind Replace",
				    "Min Substitution Percent"
		 };
		 auxiliar = new LinkedHashMap<String, String[]>();
		 auxiliar.put("MoeaDinos" , generatorsParams );
		 auxiliar.put("NSGAII" , generatorsParams);
		 result.put( INFO_COMPONENTS[13]._1, auxiliar );

		return result;
	}
	
		/**  @return The settings that are specific to this mode, same structure as getDefaultClasses*/
	public abstract Map< String , Map < String , String[] > > getExclusiveSettings();

	public final LinkedHashMap<String, LinkedHashMap< String , Integer > > combinedSettings = getCombinedSettings();
		/**  @return A map with the the name of the hyperarameter as a key, and as an object another map with the short name of each component that uses it and as value it's tupe as defined in INFO_COMPONENTS  */
	private LinkedHashMap<String, LinkedHashMap< String , Integer > > getCombinedSettings() {
		
		Map<String, Map<String, String[]>> generalSettings = this.getDefaultSettings();
		Map<String, Map<String, String[]>> exclusiveSettings = this.getExclusiveSettings();
		
		Map<String, Map<String, String[]>>[] maps = new Map[] {generalSettings , exclusiveSettings};
		
		Map<String, Object> defaultSettingsValues = JavaConverters.mapAsJavaMapConverter( Configuration.defaultParams() ).asJava() ;
		LinkedHashMap<String, LinkedHashMap< String , Integer > > result = new LinkedHashMap();
		
		Iterator<String> iterator = defaultSettingsValues.keySet().iterator();
		while ( iterator.hasNext() ) {
			result.put(iterator.next(), new LinkedHashMap<String, Integer>());
		}
		
		for(int count = 0 ;  count < INFO_COMPONENTS.length ; ++count) {
			String currentKey = INFO_COMPONENTS[count]._1();
			result.get(currentKey);
			
			for(int mapCount = 0 ; mapCount < maps.length ; ++mapCount) {
				Map<String, Map<String, String[]>> currentMap = maps[mapCount];
				
				Map<String, String[]> values = currentMap.get(currentKey);
					
				if(values != null) {
					
					Iterator<String> iteratorKeys = values.keySet().iterator();
					
					while ( iteratorKeys.hasNext() ) {
						String xyz = iteratorKeys.next();
						String[] currentValueGroup = values.get(xyz);
						for(int valueCount = 0 ; valueCount < currentValueGroup.length ; ++valueCount) {
							
							String currentValue = currentValueGroup[valueCount];

							LinkedHashMap<String, Integer> whereToAdd = result.getOrDefault(currentValue, null);
							// If it doesn't exist, add it
							if(whereToAdd == null) {
								LinkedHashMap<String, Integer> newMap = new LinkedHashMap<String, Integer>();
								newMap.put(xyz, count);
								result.put(currentValue, newMap);
							}
							else {
								whereToAdd.put(xyz, count);
							}
						}
					}
				}
			}
		}

		return result;
		
	}
	
		/**  @return A model representing the settings, with even positions being the hyperparameter name and odd ones being a parseable value to Double  */
	public SettingsModelStringArray createSettingsModelForAvailableSettings() {
		
		LinkedHashMap<String, LinkedHashMap<String, Integer>> settingsDescriptions = combinedSettings;
		scala.collection.mutable.LinkedHashMap<String, Object> defaultParams = Configuration.defaultParams();
		String[] array = new String[ defaultParams.size() * 2 ];
		int whereToAdd = 0;
		Iterator< String > iterator = settingsDescriptions.keySet().iterator();
		
		while( iterator.hasNext() ) {
			String next = iterator.next();
			
			array[whereToAdd] = next;
			whereToAdd++;
			
			Double value = defaultParams.getOrElse(next, null);
			String valueString = null;
			if (value != null) valueString = value.toString();
			array[whereToAdd] = valueString;
			whereToAdd++;
		}

		
		SettingsModelStringArray answer = new SettingsModelStringArray(KEY_SETTINGS, array);
		
		return answer;
	}



	

		/*
		 *  NOW THE GOOD PART: EXECUTING THE ALGORITHM
		 */

	/** 
	 * 	This handling the creation of the object necessary to Run DINOS
	 *  from the previously congifured node information
	 *  
	 *  @param knimeTable Table from which to discover subgroups
	 *	@param exec Execution context, from whihc to allocate more memory and update progress bar
	 *	@param model The source model, used to add output variables to it
	 *	@param targets A set of the names of the strings to which discover subgroup
	 *	@param censor Survival censoring information TODO
	 *	@param trials How many trials to use for the algorithm
	 *	@param long Random seed to use
	 *	@param seDefaultSettings Use Default Settings for this mode or the specificed by the user
	 *	@param collectIterationMetrics Collect metric results after every iteration
	 *	@param classesConfig Which component to use for each class
	 *	@param settings Hyperparameters to use
	 *  
	 *  @return An array of result object to be displayed in KNIME result ports
	 * @throws Exception 
	 *  */
	public PortObject[] executeDiscovery(
			final BufferedDataTable knimeTable,
			final ExecutionContext exec,
			final GenericDinosKnimeModel model,
			final HashSet<String> targets,
			final String[] censor,
			final int trials,
			final long seed,
			final boolean useDefaultSettings,
			final boolean collectIterationMetrics,
			final Map<String,SettingsModel> classesConfig,
			final String[] settings) throws Exception {
		
			// TODO refactor to being able to reuse it in execute parser
				// Create Dataset
			Dataset dataset = KnimeTableToDinosDataset.KnimeTableToDinosDataset(knimeTable, exec, targets, censor);
		
				// Create configuration
			Configuration configuration = KnimeTableToDinosDataset.ArraySettingsToDinosConfig( trials, collectIterationMetrics, settings );
			
				// Create settings
			Algorithm dinos = useDefaultSettings ? getDefaultAlgorithm() : getCustomAlgorithm( classesConfig );
			
				// Set Seed
			Randomize.setSeed(seed);
			
				// Set dataset and configuration
			dinos.setContext(dataset, configuration);
	
				// This will periodically poll the current iteration to update the progress bar and check cancellation
			ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1); // Only need one
			Runnable task = new Runnable() { 
					// The thrown exception here will not be caught in the main workflow,
					// so the algorithm is stopped and the following check actually cancels
					// the execution
				@Override public void run() { updateProgressBar(exec, dinos, trials);
				try { exec.checkCanceled();	} catch (CanceledExecutionException e) {e.printStackTrace(); dinos.stop(); scheduler.shutdown(); } }};
			scheduler.scheduleAtFixedRate(task, DEFAULT_UPDATE_INITIALDELAY, DEFAULT_UPDATE_EVERY, DEFAULT_UPDATE_UNIT);
			exec.setMessage(MESSAGE_EXECUTION);
			
				// GO!
			dinos.run();
			
				// This is mainly here for immediate cancelations when stopped from the scheduler
			exec.checkCanceled();
			
				// We don't need this anymore
			scheduler.shutdown();
			
			exec.setProgress(1, MESSAGE_FINISHED);
			
			return new PortObject[] {
						// Subgroup descriptions
					subgroupInformation(dinos, dataset, exec),
						// Instances descriptions
					subgroupWithInstances(dinos, dataset, exec),
						// Variables
					discoveryResultsToVariable(model, dinos, dataset, classesConfig, settings, trials, collectIterationMetrics)
			};
	}
	
		/** 
		 * This will set the progress bar of the given Execution Context to the appropiate percentage
		 * 
		 * @param exec Execution Context to which update the progress bar
		 * @param dinos Instance of the algorithm
		 * @param maxTrials Maximum amount of trials
		 */
	private void updateProgressBar( ExecutionContext exec , Algorithm dinos , int maxTrials ) {
		int currentTrials = dinos.pollCurrentTrials();
		exec.setProgress(
				currentTrials / ( (double) maxTrials), // % Progress
				MESSAGE_TRIAL + currentTrials + " / " + maxTrials
				);
	}

		/**@return An instance of the algorithm with the default values for the current mode*/
	final protected Algorithm getDefaultAlgorithm() {
		return getModeHelper().defaultAlgorithm();
	}
	
		/**
		 * Given a map, gets an instance of the DINOS algorithm for the current mode with the specified parameters
		 * 
		 * @param classesConfig Map with key as short name of component and as object a string with the values to use
		 * 
		 * @return An instance of the algorithm with the specified parameters
		 */
	private Algorithm getCustomAlgorithm( Map<String,SettingsModel> classesConfig ) {
			//Save results here 
		HashMap<String, String[] > result = new HashMap<String, String[]>() ;
		
		for(int count = 0 ; count < INFO_CONFIGURABLES.length ; ++count) {
			
		Tuple4<String, String, String, Object> currentInfo = INFO_CONFIGURABLES[count];
		String currentIdentifier = currentInfo._1();
		int currentType = (int) currentInfo._4();
		SettingsModel currentConfig = classesConfig.get(currentIdentifier);
		
		switch (currentType) {
			case 0: {	// String (only use first of provided array)
				SettingsModelString castedConfig = (SettingsModelString) currentConfig;
				result.put( currentIdentifier, new String[]{ castedConfig.getStringValue() } );
				break;
			}
			case 1, 2: {	// Multi String
				SettingsModelStringArray castedConfig = (SettingsModelStringArray) currentConfig;
				result.put( currentIdentifier, castedConfig.getStringArrayValue() );
				break;
			}
			default:
				throw new IllegalArgumentException("Unexpected value for configurable: " + currentType);
			}
		}
			// And finally return it
		return Algorithm.algorithmFromMapParams(
				scala.collection.immutable.Map.from(CollectionConverters.MapHasAsScala(result).asScala())
				, getClassType() );
	}
	
		/**
		 * Results of the first output port showing subgroup and results
		 * Differs for each mode
		 * 
		 * @param dinos Algorithm instance from which to obtain the final subgroup information
		 * @param dataset The Dataset that was run on the algorithm
		 * @param exec KNIME Execution Context from which to ask to create tables
		 * 
		 * @return A table containing in each row a description of a subgroup
		 */
	public abstract BufferedDataTable subgroupInformation(
			Algorithm dinos,
			Dataset dataset,
			ExecutionContext exec);
	
		/**
		 *  Helper function for "subgroupInformation"
		 *  Get the information of the columns for the output table describing the subgroups
		 *  
		 * @param dinos Algorithm instance from which to obtain the final subgroup information
		 * @param dataset The Dataset that was run on the algorithm
		 * 
		 * @return The specifics of the columns for the table
		 */
	public abstract List<DataColumnSpec> columnSpecs(
			Algorithm dinos,
			Dataset dataset);

		/*
		 * Helper function for "subgroupInformation"
		 * Adds additional Double columns describing the metrics results
		 * Used before creating the final table
		 * 
		 * @params outputSpecs List of columns to which to add the columns
		 * @param dinos Algorithm instance from which to obtain the final subgroup information
		 * @param dataset The Dataset that was run on the algorithm
		 */
	public static void addMetricsToCandidateRow(
			List<DataColumnSpec> outputSpecs,
			Algorithm dinos) {
		scala.collection.immutable.List<Metric> metricsList= dinos.metrics();
		for( int cuenta = 0 ; cuenta < metricsList.length() ; ++cuenta) {
				Metric currentMetric = metricsList.apply(cuenta);
				outputSpecs.add( new DataColumnSpecCreator(currentMetric.verboseName(), DoubleCell.TYPE).createSpec() );
		}
	}

		/*
		 * Helper function for "subgroupInformation"
		 * Adds additional Double cells of a row describing the metrics results
		 * Used when filling the result table
		 * 
		 * @params cells The candidate row to which to add metrics result
		 * @param dinos Algorithm instance from which to obtain the final subgroup information
		 * @param individual The Dataset that was run on the algorithm
		 */
	protected void addMetricCellsToRow(
			List<DataCell> cells,
			Algorithm dinos,
			Individual individual) {
		scala.collection.immutable.List<Metric> metricsList= dinos.metrics(); // Get all the metrics
		for( int cuenta = 0 ; cuenta < metricsList.length() ; ++cuenta) {
				Metric currentMetric = metricsList.apply(cuenta);	// Get the metric
				cells.add( new DoubleCell( currentMetric.compute( individual) ) );	// Add it
		}
	}
	
		/** Helper function for "subgroupInformation"
		 * 
		 * Returns a String description of the "IF" part of a subgroup
		 * Adapted from code in StandardRuleFormatter:
		 * package cu.edu.cujae.daf.formatter.StandardRuleFormatter
		 * 
		 * @param dataset The Dataset that was run on the algorithm
		 * @param subgroup The current subgroup
		 * 
		 * @return A string representing the conditions of the subgroup
		 */
	protected String subgroupConditionsToString(
			Dataset dataset,
			Individual subgroup) {
		Gene[] genes = subgroup.chromosome();
		StringBuilder subgroupDescription = new StringBuilder();
		boolean addAnd = false;	// This is for an edge case to avoid adding it as first element
		for(int geneCount = 0 ; geneCount < genes.length ; ++geneCount) {
			Gene currentGene = genes[geneCount];
			if(currentGene.used() ) {	// No use in showing the variable if not used

				Attribute attribute = dataset.attributes()[geneCount];
				if(addAnd) subgroupDescription.append(SubgroupParser.whiteSpace() + SubgroupParser.and() + SubgroupParser.whiteSpace() );
					// Appends the attribute's name and exclusion flag if the interval is negative
				String included = SubgroupParser.emptySpace();
				if ( currentGene.positiveInterval() ) { included = SubgroupParser.whiteSpace();} else {included =SubgroupParser.whiteSpace() + SubgroupParser.not() + SubgroupParser.whiteSpace();}
				subgroupDescription.append(attribute.name() + included );
				
					// For a nominal attribute append its value
				if(attribute.attributeType() == NOMINAL$.MODULE$ )
					subgroupDescription.append( SubgroupParser.equals() + SubgroupParser.whiteSpace() + SubgroupParser.nominal(attribute.value( (int) currentGene.upperBound() )) + SubgroupParser.whiteSpace());
					// For a numeric attribute, specify the interval
				if(attribute.attributeType() == INTEGER$.MODULE$ || attribute.attributeType() == REAL$.MODULE$ )
					subgroupDescription.append( SubgroupParser.in() + SubgroupParser.whiteSpace() + SubgroupParser.interval(currentGene.lowerBound() , currentGene.upperBound()) );
				
				addAnd = true;
			}
		}
		
		return subgroupDescription.toString();
	}
	
		/**
		 * Results of the second output port showing for each subgroup
		 * it's instances and data, along with the predicted value
		 * 
		 * @param dinos Algorithm instance from which to obtain the final subgroup information
		 * @param dataset The Dataset that was run on the algorithm
		 * @param exec KNIME Execution Context from which to ask to create tables
		 * 
		 * @return A KNIME table with the information of the instances and to which subgroup it belongs
		 */
	public BufferedDataTable subgroupWithInstances(
			Algorithm dinos,
			Dataset dataset,
			ExecutionContext exec) {
			// Store the column names and types
		List<DataColumnSpec> outputSpecs = datasetSpecs(dataset);
		
			// Create the new table
		BufferedDataContainer container = exec.createDataContainer( new DataTableSpec( outputSpecs.toArray(new DataColumnSpec[outputSpecs.size()]) ) );
		
			// Get the subgroups
		Subgroup[] subgroupsList = dinos.externalPopulation();
		
		boolean isSurvival = (dataset instanceof SurvClassDataset);
		// Now, fill the table
		for(int subgroupCount = 0 ; subgroupCount < subgroupsList.length ; ++subgroupCount) {

			Individual currentSubgroup = subgroupsList[subgroupCount].typePattern();
			scala.collection.mutable.Set<Object> coveredInstances = currentSubgroup.covered();
			scala.collection.Iterator<Object> instancesIterator = coveredInstances.iterator();
			String subgroupName = subgroupConditionsToString(dataset, currentSubgroup);
			int instanceCount = 0;

			while( instancesIterator.hasNext() ) {
				instanceCount++;
				List<DataCell> cells = new ArrayList<>();
				Instance currentInstance = dataset.instances()[ ( (Integer) instancesIterator.next() ) ] ;

					// Name of Subgroup
				addInstanceAsTableRow(dataset, container, isSurvival, subgroupCount, currentSubgroup,
						subgroupName, instanceCount, cells, currentInstance);
			
			}
		
		}
		
			// Finally, return the table
		container.close();
		return container.getTable();
		
	}

		private void addInstanceAsTableRow(
				Dataset dataset,
				BufferedDataContainer container,
				boolean isSurvival,
				int subgroupCount,
				Individual currentSubgroup,
				String subgroupName,
				int instanceCount,
				List<DataCell> cells,
				Instance currentInstance) {
			cells.add(new StringCell( subgroupName ) );

				// Add Attributes
			for( int attributeCount = 0 ; attributeCount < currentInstance.numValues() ; ++attributeCount) {
				Option<Object> currentValue = currentInstance.valueAt(attributeCount);
				if( !currentValue.isDefined() )
					cells.add( new MissingCell( null ) );
				else {
					Attribute currentAttribute = dataset.attributes()[ attributeCount ];
					AttributeType currentType = currentAttribute.attributeType();
					currentAttribute.name();

						// Add cell depending on type
					if( currentType == INTEGER$.MODULE$ )
						cells.add(new IntCell( ( (Double) currentValue.get() ).intValue() ) );
					else if ( currentType == REAL$.MODULE$ )
						cells.add(new DoubleCell( (double) currentValue.get() ) );
					else if ( currentType == NOMINAL$.MODULE$ )
						cells.add(new StringCell( currentAttribute.values()[ ( (Double) currentValue.get() ).intValue() ] ) );
					else if ( currentType == SURVIVAL$.MODULE$ )
						cells.add(new DoubleCell( (double) currentValue.get() ) );
				}
			}

				// Add Classes
			for( int classCount = 0 ; classCount < currentInstance.numClass() ; ++classCount) {
				
				double currentValue = currentInstance.classAt(classCount);
				Attribute currentAttribute = dataset.attributes()[ dataset.dimensionality() + classCount ];
				AttributeType currentType = currentAttribute.attributeType();
				currentAttribute.name();
				
					// Add cell depending on type
				if( currentType == INTEGER$.MODULE$ )
					cells.add(new IntCell( (int) currentValue ) );
				else if ( currentType == REAL$.MODULE$ )
					cells.add(new DoubleCell( (double) currentValue ) );
				else if ( currentType == NOMINAL$.MODULE$ )
					cells.add(new StringCell( currentAttribute.values()[ (int) currentValue ] ) );
				else if ( currentType == SURVIVAL$.MODULE$ )
					cells.add(new DoubleCell( (double) currentValue ) );
			}
			
				// Do this dataset have censoring?
			if ( isSurvival ) {
					// If so, the censoring information
				if ( currentInstance.censored() )
					cells.add( BooleanCell.FALSE );
				else cells.add( BooleanCell.TRUE );				
			}
			
				// Add Prediction
			addPrediction( cells , currentSubgroup , dataset );
			
			// Create the row and add it
			// Row Id is the number of the subgroup (S) and the instance (I) of said subgroup
			DataRow row = new DefaultRow( "S" + (subgroupCount + 1) + " / I" + (instanceCount)  , cells);
			container.addRowToTable(row);
		}
	
		protected void addPrediction(List<DataCell> cells, Individual currentSubgroup, Dataset dataset) {
			cells.add( new MissingCell(null) );
		};

		/**
		 * Create a KNIME spec for recreating a dataset as a
		 * KNIME table
		 * 
		 * @param dataset The Dataset to be used
		 * 
		 * @return The column information of a KNIME table based on that dataset
		 */
	public List<DataColumnSpec> datasetSpecs(
			Dataset dataset) {
			// Store results here
		List<DataColumnSpec> outputSpecs = new ArrayList<>();
		outputSpecs.add( new DataColumnSpecCreator("Subgroup", StringCell.TYPE).createSpec() );
		
		Attribute[] attributesList = dataset.attributes();
		
		for( int count = 0 ; count < attributesList.length ; ++count ) {
			Attribute currentAttribute = attributesList[count];
			AttributeType currentType = currentAttribute.attributeType();
			String currentName = currentAttribute.name();

				// KNIME column type depends on dataset type
			if( currentType == INTEGER$.MODULE$ )
				outputSpecs.add(  new DataColumnSpecCreator(currentName, IntCell.TYPE).createSpec() );
			else if ( currentType == REAL$.MODULE$ )
				outputSpecs.add(  new DataColumnSpecCreator(currentName, DoubleCell.TYPE).createSpec() );
			else if ( currentType == NOMINAL$.MODULE$ )
				outputSpecs.add(  new DataColumnSpecCreator(currentName, StringCell.TYPE).createSpec() );
			else if ( currentType == SURVIVAL$.MODULE$ )
				outputSpecs.add(  new DataColumnSpecCreator(currentName, DoubleCell.TYPE).createSpec() );
		}
			// Censor information
		
		addTargetSpecs(dataset, outputSpecs);
		
		return outputSpecs;
	}
	
		protected abstract void addTargetSpecs(Dataset dataset, List<DataColumnSpec> outputSpecs);

		/**
		 * Results of the third output port,
		 * showing statistics variables for the run
		 * 
		 * Notice that that variables appear in KNIME
		 * as "First Added, Last Show", so the functions
		 * as been called in backward orders.
		 * 
		 * In the actual app they should appear in this order:
		 * 
		 * 1 - General results for the run of the algorithm itself
		 * 2 - Results specific to the algorithm mode
		 * 3 - Variables showing the classes used for each component
		 * 4 - Variables showing the hyperparameter values
		 * 
		 * These last two are for allowing the user to recreate
		 * the configuration with flow variables
		 * 
		 * @param nodeModel The model for this algorithm mode
		 * @param dinos Algorithm instance that was used to obtain subgroups
		 * @param dataset The Dataset that was run on the algorithm
		 * @param classesConfig Map with key as short name of component and as object a string with the values to use
		 * @param settings Hyperparameters
		 * @param collectIterationMetrics 
		 * @param trials 
		 * 
		 * @return The singleton object for variables port, which cannot be created (otherwise an different port would be used for the components and settings aside from results)
		 */
	public FlowVariablePortObject discoveryResultsToVariable(
			GenericDinosKnimeModel nodeModel,
			Algorithm dinos,
			Dataset dataset, Map<String,
			SettingsModel> classesConfig,
			String[] settings,
			int trials,
			boolean collectIterationMetrics)  {

		addSettingsResultsVariables( nodeModel , settings, trials, collectIterationMetrics);
		
		addComponentsResultsVariables( nodeModel , dinos , classesConfig);

		addSpecificResultsVariables( nodeModel , dinos , dataset);
		
		addOverallResultsVariables( nodeModel , dinos , dataset);
		
		return FlowVariablePortObject.INSTANCE;
		
	}

		/**
		 * Helper function for "resultsToVariable"
		 * 
		 * Add variables to the given model related to
		 * the overall performance of the algorithm
		 * 
		 * @param nodeModel The model for this algorithm mode
		 * @param dinos Algorithm instance that was used to obtain subgroups
		 * @param dataset The Dataset that was run on the algorithm
		 * 
		 */
	private void addOverallResultsVariables(
			GenericDinosKnimeModel nodeModel,
			Algorithm dinos,
			Dataset dataset) {
		nodeModel.addResultVariables("targets_total", IntType.INSTANCE, dataset.instances()[0].numClass() );			
		nodeModel.addResultVariables("attributes_total", IntType.INSTANCE, dataset.instances()[0].numValues() );
		nodeModel.addResultVariables("variables_total", IntType.INSTANCE, dataset.attributes().length );
		nodeModel.addResultVariables("subgroups_total", IntType.INSTANCE, dinos.externalPopulation().length );
		nodeModel.addResultVariables("executionTimeMillis", LongType.INSTANCE, dinos.executionTimeMillis() );
		nodeModel.addResultVariables("instances_num", IntType.INSTANCE, dataset.numInstances() );
		nodeModel.addResultVariables("dataset_type", StringType.INSTANCE, dataset.classType().getClass().getSimpleName() );
		
	}
	
		/**
		 * Helper function for "resultsToVariable"
		 * 
		 * Add variables to the given model related to
		 * runtime parameters specific to the current
		 * mode
		 * 
		 * @param nodeModel The model for this algorithm mode
		 * @param dinos Algorithm instance that was used to obtain subgroups
		 * @param dataset The Dataset that was run on the algorithm
		 */
	protected abstract void addSpecificResultsVariables(GenericDinosKnimeModel nodeModel, Algorithm dinos, Dataset dataset);
	
		/**
		 * Helper function for "resultsToVariable"
		 * 
		 * Add variables to the given model related to
		 * the classes used for each component
		 * 
		 * @param nodeModel The model for this algorithm mode
		 * @param dinos Algorithm instance that was used to obtain subgroups
		 * @param classesConfig Which component were used for each class
		 */
	private void addComponentsResultsVariables(GenericDinosKnimeModel nodeModel, Algorithm dinos, Map<String, SettingsModel> classesConfig) {
		for( int count = 0; count < INFO_CONFIGURABLES.length ; ++count) {
			Tuple4<String, String, String, Object> currentInfo = INFO_CONFIGURABLES[count];
			String currentIdentifier = currentInfo._1();
			int currentType = (int) currentInfo._4();
			
			switch (currentType) {
			case 0: {
				nodeModel.addResultVariables( currentIdentifier , StringType.INSTANCE , ( (SettingsModelString) classesConfig.get(currentIdentifier) ).getStringValue() );
				break;
			}
			case 1,2: {
				nodeModel.addResultVariables( currentIdentifier , StringArrayType.INSTANCE , ( (SettingsModelStringArray) classesConfig.get(currentIdentifier) ).getStringArrayValue() );
				break;
			}
			default:
				throw new IllegalArgumentException("Unexpected value: " + currentType);
			}
			
			
		}
	}
	
		/**
		 *  Helper function for "resultsToVariable"
		 * 
		 *  Add variables to the given model related to
		 *  settings for hyperparameters
		 * 
		 * @param nodeModel The model for this algorithm mode
		 * @param settings Used hyperparameters 
		 * @param collectIterationMetrics 
		 * @param trials 
		 */
	private void addSettingsResultsVariables(
			GenericDinosKnimeModel nodeModel,
			String[] settings,
			int trials,
			boolean collectIterationMetrics) {
		
		nodeModel.addResultVariables( KEY_SETTINGS , StringArrayType.INSTANCE , settings );
		
		nodeModel.addResultVariables( KEY_COLLECTITERATIONS , BooleanType.INSTANCE , collectIterationMetrics );
		
		nodeModel.addResultVariables( KEY_TRIALS , IntType.INSTANCE , trials );

	}
	
	public PortObject[] executeExtract(
			BufferedDataTable knimeTable,
			ExecutionContext exec,
			GenericDinosKnimeModel dinosNominalDatasetAsSubgroupExtractorNodeModel,
			HashSet<String> targets,
			String[] censor,
			int trialsAmount,
			long seedToLong,
			boolean useDefaultOrNot,
			boolean collectItaration,
			Map<String, SettingsModel> classesConfig,
			String[] settingsArray) throws Exception {

			// Create Dataset
		Dataset dataset = KnimeTableToDinosDataset.KnimeTableToDinosDataset(knimeTable, exec, targets, censor);		
		
		return new PortObject[] {
				// Amount of Instances
			instancesInfo(knimeTable, exec),
				// Instances descriptions
			instancesExtracted(dataset, exec),
				// Variables
			extractResultsToVariable(),
	};
	}


	private PortObject instancesInfo(BufferedDataTable inputTable, ExecutionContext exec) throws CanceledExecutionException {

		exec.checkCanceled();
		
		/*
		 * List<DataColumnSpec> outputSpecs = new ArrayList(); outputSpecs.add( new
		 * DataColumnSpecCreator("Info", StringCell.TYPE).createSpec() );
		 * outputSpecs.add( new DataColumnSpecCreator("Value",
		 * DoubleCell.TYPE).createSpec() ); // Create the new table
		 * BufferedDataContainer container = exec.createDataContainer( new
		 * DataTableSpec( outputSpecs.toArray(new DataColumnSpec[outputSpecs.size()]) )
		 * );
		 * 
		 * List<DataCell> cells = new ArrayList<>(); cells.add( new StringCell("Amount")
		 * ); cells.add( new DoubleCell( inputTable.size() ) ); DataRow row = new
		 * DefaultRow( "0" , cells); container.addRowToTable(row);
		 * 
		 * container.close();
		 */
		return InactiveBranchPortObject.INSTANCE; //container.getTable();
	}
	
	private PortObject instancesExtracted(Dataset dataset, ExecutionContext exec) {

			// Store the column names and types
		List<DataColumnSpec> outputSpecs = datasetSpecs(dataset);
		
			// Create the new table
		BufferedDataContainer container = exec.createDataContainer( new DataTableSpec( outputSpecs.toArray(new DataColumnSpec[outputSpecs.size()]) ) );

		Instance[] allInstances = dataset.instances();
		
		boolean isSurvival = (dataset instanceof SurvClassDataset);

			// Now, fill the table
		for(int count = 0 ; count < allInstances.length ; ++count) {
			
			List<DataCell> cells = new ArrayList<>();
			Instance currentInstance = allInstances[count] ;
			addInstanceAsTableRow(dataset, container, isSurvival, -1, null, "Dataset", count, cells, currentInstance);
			
		}

		container.close();
		return container.getTable();
	}
	
	private PortObject extractResultsToVariable() {

		return FlowVariablePortObject.INSTANCE;
	}

	public PortObject[] executeParser(
			final BufferedDataTable dataTable,
			final BufferedDataTable descriptionsTable,
			final ExecutionContext exec,
			final GenericDinosKnimeModel model,
			final HashSet<String> targets,
			final String descriptionColumn,
			final String inColumn,
			final String thenColumn,
			final String[] censor,
			final boolean useDefaultSettings,
			final Map<String,SettingsModel> classesConfig,
			final String[] settings
			) throws Exception {
	
			// Create Dataset
		Dataset dataset = KnimeTableToDinosDataset.KnimeTableToDinosDataset(dataTable, exec, targets, censor);
		
			// Create configuration
		Configuration configuration = KnimeTableToDinosDataset.ArraySettingsToDinosConfig( GenericDinosKnimeWorkflow.DEFAULT_TRIALS, GenericDinosKnimeWorkflow.DEFAULT_COLLECTITERATIONS, settings );
		
			// Create settings
		Algorithm dinos = useDefaultSettings ? getDefaultAlgorithm() : getCustomAlgorithm( classesConfig );
		
			// Set dataset and configuration
		dinos.setContext(dataset, configuration);
		
		exec.setMessage(MESSAGE_PARSER);
		
		String[] descriptions = getSubgroupDescriptions(descriptionsTable, descriptionColumn, inColumn , thenColumn, targets);
		
		dinos.parse( descriptions );
		
		return new PortObject[] {
				// Subgroup descriptions
			subgroupInformation(dinos, dataset, exec),
				// Instances descriptions
			subgroupWithInstances(dinos, dataset, exec),
				// Variables
			discoveryResultsToVariable(model, dinos, dataset, classesConfig, settings, 0, false)
		};
	}

	private String[] getSubgroupDescriptions(
			final BufferedDataTable descriptionsTable,
			final String descriptionColumn,
			final String inColumn,
			final String thenColumn,
			final HashSet<String> targets) {
		
			// First, check if these columns exist
		DataTableSpec descriptionSpec = descriptionsTable.getDataTableSpec();
				
		int descriptionPosition = tryToGetPositionInTable( descriptionSpec , descriptionColumn, false , " (for body description");

		int inPosition = tryToGetPositionInTable( descriptionSpec , inColumn, true , " (for position)");
		
		int thenPosition = tryToGetPositionInTable( descriptionSpec , thenColumn, true , " (for then clause)");

		// TODO check for size
		
		String[] answer = new String[ (int) descriptionsTable.size() ];
		
		CloseableRowIterator iterator = descriptionsTable.iterator();

		for(int count = 0 ; count < answer.length ; ++ count) {
			DataRow nextRow = iterator.next();
			String subgroupDescriptions
				= nextRow.getCell(descriptionPosition).toString()
				+ (thenPosition > -1 ? printThenCell(nextRow, inPosition, thenPosition, targets) : "");
			answer[count] = subgroupDescriptions;
		}
		
		iterator.close();

		return answer;
	}
	
	protected String printInCell(int inPosition, DataRow row, HashSet<String> targets) {
		String inSymbol = targets.iterator().next() + SubgroupParser.whiteSpace();
		
		if( inPosition > -1 ) {
			boolean value = ( (BooleanCell) row.getCell(inPosition) ).getBooleanValue();
			if (value == true)
				inSymbol += ( SubgroupParser.in() );
			else
				inSymbol += ( SubgroupParser.not() + SubgroupParser.whiteSpace() + SubgroupParser.in() );
		}
		
		return inSymbol + SubgroupParser.whiteSpace();
	}
	
	protected abstract String printThenCell( DataRow descriptionSpec , int inPosition, int thenPosition, HashSet<String> targets);

	private int tryToGetPositionInTable(
			final DataTableSpec descriptionSpec,
			final String descriptionColumn,
			final boolean canBeNull,
			final String possibleError) {
		
		var answer = -1;
		
		if( descriptionColumn == null) {
			if( canBeNull == false )
				throw new IllegalArgumentException("Error when parsing subgroup, " + "obligatory column was null" + possibleError);
		}
		else
			answer = tryToGetPositionInTable(descriptionSpec, descriptionColumn, possibleError);
		
		return answer;
	}
	
	private int tryToGetPositionInTable(
			final DataTableSpec descriptionSpec,
			final String descriptionColumn,
			final String possibleError) {

		int answer = descriptionSpec.findColumnIndex(descriptionColumn);
		if(answer == -1)
			throw new IllegalArgumentException("Error when parsing subgroup, " + "given column wasn't found" + possibleError);

		return answer;
	}






	
}
