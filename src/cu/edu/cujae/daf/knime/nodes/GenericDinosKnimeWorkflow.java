package cu.edu.cujae.daf.knime.nodes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.ToolTipManager;

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
import org.knime.core.node.NodeModel;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponent;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnFilter2;
import org.knime.core.node.defaultnodesettings.SettingsModelFilterString;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelSeed;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.defaultnodesettings.SettingsModelStringArray;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.flowvariable.FlowVariablePortObject;
import org.knime.core.node.port.inactive.InactiveBranchPortObject;
import org.knime.core.node.util.DateInputDialog.Mode;
import org.knime.core.node.workflow.VariableType.BooleanType;
import org.knime.core.node.workflow.VariableType.DoubleArrayType;
import org.knime.core.node.workflow.VariableType.IntArrayType;
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
import cu.edu.cujae.daf.context.dataset.INTEGER;
import cu.edu.cujae.daf.context.dataset.INTEGER$;
import cu.edu.cujae.daf.context.dataset.Instance;
import cu.edu.cujae.daf.context.dataset.NOMINAL$;
import cu.edu.cujae.daf.context.dataset.REAL$;
import cu.edu.cujae.daf.context.dataset.SURVIVAL$;
import cu.edu.cujae.daf.context.dataset.SurvClassDataset;
import cu.edu.cujae.daf.core.Algorithm;
import cu.edu.cujae.daf.core.ComponentInfo;
import cu.edu.cujae.daf.core.DiscoveryMode;
import cu.edu.cujae.daf.core.HyperparamInfo;
import cu.edu.cujae.daf.core.Subgroup;
import cu.edu.cujae.daf.evaluation.metric.Metric;
import cu.edu.cujae.daf.knime.dialogcomponents.DialogComponentCheckBoxGroupReferenced;
import cu.edu.cujae.daf.knime.dialogcomponents.DialogComponentStringSelectionReferenced;
import cu.edu.cujae.daf.knime.dialogcomponents.ReseatableDialogComponent;
import cu.edu.cujae.daf.randomize.Randomize;
import cu.edu.cujae.daf.formatter.FormatSymbols;
import cu.edu.cujae.daf.formatter.SubgroupFormatter;
import scala.Option;
import scala.Tuple2;
import scala.Tuple3;
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


		// Results of querying the components available in DINOS
	public static final scala.collection.mutable.Map<String, scala.collection.mutable.Map<String, Set<ComponentInfo>>>
		TREE_COMPONENTS = getComponentsTree();
	
	public static final Map<String, scala.collection.mutable.Map<String, Set<ComponentInfo>>>
		TREE_COMPONENTS_CONVERTED = JavaConverters.mapAsJavaMapConverter(TREE_COMPONENTS).asJava();
	
	public static final Map<ClassType, DiscoveryMode>
		MAP_MODE_CONVERTED = CollectionConverters.MapHasAsJava( Algorithm.modesMap_type() ).asJava();

		// Specify kind of nodes, for determining which methods and settings to use
	public enum DINOS_NODE {
		DISCOVERY,
		EXTRACTOR,
		PARSER
	}
	
		// Names of Available Components types
	public static final Tuple2<String, String>[] INFO_COMPONENTS = Algorithm.componentNames();
	public static final Tuple4<String, String, String, Object>[] INFO_CONFIGURABLES = Algorithm.configurableNames();

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
	
		// Strings for configuration keys
	public static final String KEY_DEFAULTCOMPONENTS = "useDefault";
	public static final String KEY_TARGET = "targetClass";
	public static final String KEY_DESC = "descClass";
	public static final String KEY_SEED = "seedAlgorithm";
	public static final String KEY_TRIALS = "trials";
	public static final String KEY_COLLECTITERATIONS = "collectIterations";
	public static final String KEY_SETTINGS = "settings";
	public static final String KEY_FIXED = "fixed";

		// Default values
	public static final String DEFAULT_TARGET = null;
	public static final String DEFAULT_DESC = null;
	public static final String DEFAULT_BODY = null;
	public static final String DEFAULT_PREDICTION = "Prediction";
	public static final boolean DEFAULT_DEFAULTCOMPONENTS = true;
	public static final boolean DEFAULT_GENERATESEED = false;
	public static final boolean DEFAULT_COLLECTITERATIONS = true;
	public static final int DEFAULT_CHECKBOXGROUP_ELEMENTSPERROW = 2;
	public static final int DEFAULT_CHECKBOXGROUP_METRICSMINIMUM = 1;
	public static final int DEFAULT_TRIALS = Configuration.getDefaultMaxTrials();
	public static final int DEFAULT_TRIALS_MIN = 1;
	public static final int DEFAULT_TRIALS_MAX = Integer.MAX_VALUE;
	public static final int DEFAULT_TRIALSTEPSIZE = 1000;
	public static final int DEFAULT_UPDATE_INITIALDELAY = 0;
	public static final int DEFAULT_UPDATE_EVERY = 100;	// Tenth of second
	public static final long DEFAULT_SEED = 0;
	public static final TimeUnit DEFAULT_UPDATE_UNIT = TimeUnit.MILLISECONDS;
	
		// Target types for filtering incoming tables
	public static final Class<? extends DataValue>[] TARGETS_DESCTYPES = new Class[] {StringValue.class};
	public static final Class<? extends DataValue>[] TARGETS_INTYPES = new Class[] {BooleanValue.class};
	public static final Class<? extends DataValue>[] TARGETS_THENYPES = new Class[] {StringValue.class};
	
		// Boolean Text
	public static final String BOOL_TRUE_TEXT = Boolean.toString(true);
	public static final int    BOOL_TRUE_NUM = 1;
	public static final String BOOL_FALSE_TEXT = Boolean.toString(false);
	public static final int    BOOL_FALSE_NUM = 0;
	
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
	public static final String NAME_FIXED = "Choose fixed variables";
	public static final String NAME_FIXED_FIXED = "Fixed";
	public static final String NAME_FIXED_VARIABLE = "Not fixed";
	
		// Name of columns to show in result tables
	public static final String RESULT_SUBGROUP_BODY = "Subgroup Body";
	public static final String RESULT_SUBGROUP_FULL = "Full Subgroup Description";
	public static final String RESULT_IN = "In";
	public static final String RESULT_CLASS = "Class";
	public static final String RESULT_PREDICTION = "Prediction";
	public static final String RESULT_TP = "True Positives";
	public static final String RESULT_COV = "Covered";

		// Name of Tabs for Nodes Dialogues
	public static final String TAB_DEFAULT = "General";
	public static final String TAB_COMPONENTS = "Components";
	public static final String TAB_SETTINGS = "Settings";
	public static final String TAB_PARSER = "Parsing options";

		// Messages for the node's warning, error and progress bar
	public static final String MESSAGE_PROCESSCOLUMNS = "Processing Attributes (Columns of the Table)";
	public static final String MESSAGE_EXECUTION = "Discovering Subgroups";
	public static final String MESSAGE_CREATING_INSTANCES = "Creating instances for use in the Algorithm";
	public static final String MESSAGE_CREATING_ATTRIBUTES = "Creating attributes as used in the algorithm";
	public static final String MESSAGE_READY = "Dataset ready";
	public static final String MESSAGE_TRIAL = MESSAGE_EXECUTION + ", trial:";
	public static final String MESSAGE_FINISHED = "Finished Discovering Subgroups, parsing results";
	public static final String MESSAGE_PARSER = "Parsing Subgroups";
	public static final String WARNING_GUESSING_TARGET = "Guessing target column: ";
	public static final String WARNING_GUESSING_PARSER = "Guessing parser column: ";
	public static final String EXCEPTION_NOCOLUMNS_DATASET = "The provided dataset table has no columns";
	public static final String EXCEPTION_NOCOLUMNS_PARSER = "The provided parsing table has no columns";
	public static final String EXCEPTION_NOCLASS_VALIDCONFIG = "No class column set but valid configuration";
	public static final String EXCEPTION_NOCLASS = "Table contains no suitable column to set as target";
	public static final String EXCEPTION_CONFIGNOTFOUND_CLASS = "Previously configured class column not found or incompatible: ";
	public static final String EXCEPTION_CONFIGNOTFOUND_PARSER = "Previously configured parsing column not found or incompatible: ";
	public static final String EXCEPTION_PARSER_SAMECLASSTHEN = "The subgroup body and class columns cannot be the same";
	public static final String EXCEPTION_FIXED_EQUALSCLASS = "The class column cannot be in the list of fixed variables";
	public static final String EXCEPTION_FIXED_EQUALSCENSOR = "The censor column cannot be in the list of fixed variables";
	public static final String EXCEPTION_PARSER = "Error when parsing subgroup, ";
	public static final String EXCEPTION_UNEXPECTED_DIALOGCOMPONENT = "Unexpected value for dialog component type";
	public static final String EXCEPTION_COLUMNNOTFOUND_NOTFOUND = "Given column wasn't found";
	public static final String EXCEPTION_COLUMNNOTFOUND_OBLIGATORYNULL = "Obligatory column was null";
	public static final String EXCEPTION_IDENTICALCOLUMNS_PARSER = "Input tables with the dataset and subgroup descriptions cannot be the same";
	
		// Cell types identifiers
		// TODO see if it can be obtained (similar to the boolean values) from the cels instead of hardocded
	public static final String ID_STRING = "org.knime.core.data.def.StringCell";
	public static final String ID_BOOL = "org.knime.core.data.def.BooleanCell";
	public static final String ID_DOUBLE = "org.knime.core.data.def.DoubleCell";
	public static final String ID_INT = "org.knime.core.data.def.IntCell";



			/*
			 * STATIC CODE BLOCKS
			 */

		// "Infinite" tooltip hover time
	static {
		ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);
	}


	
	
	
			/*
			 * SETTINGS MODEL CREATORS
			 */

		/**
		 * This returns the type of data cells supported, mainly as use 
		 * for filtering which can be used as classes in node dialogs
		 * 
		 *  @return The kinds of inputs cells supported by the current mode
		 */
	public abstract Class<? extends DataValue>[] getAceptedTargetTypes();

		/**
		 * This returns the type of data cell created for the class when
		 * showing subgroups to the user
		 */
	public abstract Class<? extends DataValue> getThenTargetType();

	private static scala.collection.mutable.Map<String, scala.collection.mutable.Map<String, Set<ComponentInfo>>> getComponentsTree() {	
			var tree = Algorithm.allAvailable();
				tree.remove("format");	// Don't need the formatter
				return tree;
			}

		/**
		 * Retrieve the mode helper from DAF
		 * It features important methods that don't need
		 * to be remade here in KNIME.
		 * 
		 * @return The helper for this mode
		 */
	public abstract DiscoveryMode getModeHelper();
	
		/**
		 * Each dataset have an identifier,
		 * this can be used to quickly get it
		 * 
		 *  @return The identifier of the supported dataset
		 */
	public ClassType getClassType() {
		return getModeHelper().datasetType();
	}
	
		/**  @return A model for use in storing the target class column*/
	public static SettingsModelString createTargetClass() {
		return new SettingsModelString( KEY_TARGET , DEFAULT_TARGET );
		}

		/**  @return A model for use in storing subgroup descriptions columns, for parsers*/
	public static SettingsModelString createDescClass() {
		return new SettingsModelString( KEY_DESC, DEFAULT_DESC);
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
	
	public SettingsModelFilterString createFixedModel() {
		return new SettingsModelFilterString( KEY_FIXED );
	}


		/*
		 *  CLASSES FOR EACH COMPONENT
		 */
	
		/**
		 * This returns the available classes for each component
		 * 
		 *  @return A map where each key refers to an available classes, and in turn each value is another map with short name as key and verbose human readable name as value
		 */
	private static final Map< String , Map < String , ComponentInfo > > getGenericClasses() {

		return getClassesSpecifiedMode("");
	}
	
		/**
		 * Goes through the available classes tree to search for the classes of a specific mode
		 * 
		 * @param mode The identifier of the mode to search (empty string to search generic)
		 * @return A nested map, outer one have the type of components as key, inner one have the shortName as key and the verboseName as value
		 */
	public static final Map<String, Map<String, ComponentInfo>> getClassesSpecifiedMode(String mode) {

		Map< String , Map < String , ComponentInfo > > result = new LinkedHashMap<String, Map<String, ComponentInfo>>();
	
		for(int count = 0 ; count < INFO_COMPONENTS.length; ++count) {
			Tuple2<String, String> currentComponent = INFO_COMPONENTS[count];
			var currentInfo1 = JavaConverters.mapAsJavaMapConverter( TREE_COMPONENTS_CONVERTED.get(currentComponent._1) ).asJava();
			
				// If not null, continue
			if(currentInfo1 != null) {
				
					// Skip this type don't have components defined for the specified mode.
				var currentInfo2 = currentInfo1.getOrDefault(mode , null);
				if(currentInfo2 != null) {

					// Now, just add the short and verbose names of every existing component
				var iterator = currentInfo2.iterator();
				Map < String , ComponentInfo > insideMap = new LinkedHashMap<String, ComponentInfo>();
					// Add everything inside the iterator
				while( iterator.hasNext() ) {
					ComponentInfo next = iterator.next();
					insideMap.put( next.shortName(), next );
				}
					// And insert the info for this type
				result.put(currentComponent._1, insideMap);
				}
			}
		}
		
		return result;
	}
	
		/**  @return The classes that are specific to this mode, same structure as getAvailabeClasses*/
	public final Map< String , Map < String , ComponentInfo > >getExclusiveClasses() {
	
		return getClassesSpecifiedMode( this.getModeHelper().identifier() );
		
	}
	
		/** A map with the short name of components that can be used in this mode as key
		 * and as value two arrays as a 2d array: first have the short name of each class,
		 * in the same position in the second the verbose name */
	public final Map<String, ComponentInfo[]> combinedClasses = getCombinedClasses();
	
		/**  @return A map with the short name of type of component as key and as value two arrays as a 2d array: first have the short name of each class and in the same position in the second the verbose name*/
	private Map<String, ComponentInfo[]> getCombinedClasses() {

			// Get the information from here
		Map<String, Map<String, ComponentInfo>> generalClasses = this.getGenericClasses();
		Map<String, Map<String, ComponentInfo>> exclusiveClasses = this.getExclusiveClasses();

			// Store results here
		Map<String, ComponentInfo[] > answer = new LinkedHashMap <String, ComponentInfo[]> ();
		
		for(int count = 0 ; count < INFO_CONFIGURABLES.length ; ++count) {
			
				// Since it will be easier to traverse an array, it will be used over an inner map
				// The size of it will be determined before by adding the sizes
				// of the generals and exclusives
			
				// Keys for the maps
			String keyToAdd = INFO_CONFIGURABLES[count]._1();
			String keyToFind = INFO_CONFIGURABLES[count]._3();
			int newSize = 0;
			
			Map<String, ComponentInfo> valueGeneral = generalClasses.getOrDefault(keyToFind, null);
			if (valueGeneral != null)
				newSize += valueGeneral.size();
			
			Map<String, ComponentInfo> valueExclusive = exclusiveClasses.getOrDefault(keyToFind, null);
			if (valueExclusive != null)
				newSize += valueExclusive.size();
			
				// Create the array
				// First key, then label
			ComponentInfo[] toAdd = new ComponentInfo[newSize];
			int positionToAdd = 0;
			
				// First, add the exclusives
			if (valueExclusive != null) {
				Iterator<ComponentInfo> iterator = valueExclusive.values().iterator();
				while( iterator.hasNext() ) {
					ComponentInfo next = iterator.next();
					toAdd[positionToAdd] = next;
					++positionToAdd;
				}
			}
				// Second, add general
			if (valueGeneral != null) {
				Iterator<ComponentInfo> iterator = valueGeneral.values().iterator();
				while( iterator.hasNext() ) {
					ComponentInfo next = iterator.next();
					toAdd[positionToAdd] = next;
					++positionToAdd;
				}
			}
			
				// And finish the loop by adding it
			answer.put(keyToAdd, toAdd);
		}
			// We're Done
		return answer;
	}
	
		/** @return A map where each key is one of the the available components types and the value is an array with the default values */
	public scala.collection.immutable.Map<String, String[] > getDefaultClasses() {
		return getModeHelper().getCombinedDefaultParameters();
	}

		/**
		 * This returns the visual Java Swing objects for selecting which classes to use for each component
		 * 
		 *  @return The visual components ready to be added to a {@link DefaultNodeSettingsPane}
		 */
	public ReseatableDialogComponent[] createDialogComponentForAllComponentes() {
		
			// We will need this
		ReseatableDialogComponent[] answer = new ReseatableDialogComponent[ INFO_CONFIGURABLES.length ];
		Map<String, String[]> defaults = defaultParameters;
		Map<String, ComponentInfo[]> combinedClasses = this.combinedClasses;

		for(int count = 0 ; count < this.INFO_CONFIGURABLES.length ; ++count) {
			
			Tuple4<String, String, String, Object> currentDescription = INFO_CONFIGURABLES[count];
			
				// Single selection with string selection referenced,
				// or a selection box with the reference system that can be reset
			switch ( (int) currentDescription._4() ) {
			case 0: {
				ComponentInfo[] referencesAndLabels = combinedClasses.get( currentDescription._1() );
				answer[count] = DialogComponentStringSelectionReferenced.DialogComponentStringSelectionReferencedFromComponentInfo(
						(SettingsModelString) createSettingsModelFromComponentDescription(currentDescription),
						currentDescription._2(),
						referencesAndLabels,
						defaults.get( currentDescription._1() )[0]);
				break;  } 
			
				// Multi-selection with checkbox group
			case 1, 2: {
				ComponentInfo[] referencesAndLabels = combinedClasses.get( currentDescription._1() );
				answer[count] = DialogComponentCheckBoxGroupReferenced.DialogComponentCheckBoxGroupReferencedFromComponentInfo(
						(SettingsModelStringArray) createSettingsModelFromComponentDescription(currentDescription),
						currentDescription._2(),
						referencesAndLabels,
						DEFAULT_CHECKBOXGROUP_ELEMENTSPERROW,
						DEFAULT_CHECKBOXGROUP_METRICSMINIMUM,
						defaults.get( currentDescription._1() ) );
				break;  } 
			default:
				throw new IllegalArgumentException( EXCEPTION_UNEXPECTED_DIALOGCOMPONENT +  (int) currentDescription._4() );
			}
		}
		
		return answer;
	}

		/**
		 * This returns a settings model for selecting a class given a description
		 * 
		 *  @return The models ready to be used either by a {@link NodeModel} or a {@link DialogComponent}
		 */
	private SettingsModel createSettingsModelFromComponentDescription( Tuple4<String, String, String, Object> componentDescription ) {

		SettingsModel answer = null;
		Map<String, String[]> defaults = defaultParameters;
		
			// Single selection
		switch ( (int) componentDescription._4() ) {
		case 0: { answer = new SettingsModelString(componentDescription._1() , defaults.get( componentDescription._1() )[0]); break;  } 
		
			// Multi-selection
		case 1, 2: { answer = new SettingsModelStringArray(componentDescription._1() , defaults.get( componentDescription._1() )); break;  } 
		default:
			throw new IllegalArgumentException("Unexpected value for component type" +  (int) componentDescription._4() );
		}
		
		return answer;
	}



		/*
		 *  SETTINGS FOR EACH COMPONENT
		 */
	
		/**
		 * This returns the available settings for each class of every component
		 * 
		 *  @return A map where each key refers to an available class, and in turn each value is another map with component short name as key and an array of hyper-parameters as values
		 */
		
		/** Default classes in a convenient Java friendly {@link java.util.Map} */
	public final Map<String, String[]> defaultParameters = JavaConverters.mapAsJavaMapConverter(getDefaultClasses()).asJava() ;

		/** @return A map where the key is the short name for each configurable available and the value is it's corresponding setting model*/
	public LinkedHashMap<String, SettingsModel> createSettingsModelForAllComponents() {
		
			// Just go trough the available types of components and call the function to create the model
		LinkedHashMap< String , SettingsModel> answer = new LinkedHashMap<String, SettingsModel>();
		
		for(int count = 0 ; count < this.INFO_CONFIGURABLES.length ; ++count) {
			answer.put( this.INFO_CONFIGURABLES[count]._1() , createSettingsModelFromComponentDescription( this.INFO_CONFIGURABLES[count]));
		}
		
		return answer;
	}

		/**  @return The settings that can appear in any mode */
	public static final Map<String, Map<String, HyperparamInfo[]>> getDefaultSettings() {
		
		return getSettingsSpecifiedMode("");
	}
	
		/**  @return The settings that are specific to this mode, same structure same getDefaultClasses*/
	public static final Map<String, Map<String, HyperparamInfo[]>> getSettingsSpecifiedMode(String mode) {

		Map< String , Map < String , HyperparamInfo[] > > result = new LinkedHashMap<String, Map<String, HyperparamInfo[]>>();

		for(int count = 0 ; count < INFO_COMPONENTS.length; ++count) {
			Tuple2<String, String> currentComponent = INFO_COMPONENTS[count];
			var currentInfo1 = JavaConverters.mapAsJavaMapConverter( TREE_COMPONENTS_CONVERTED.get(currentComponent._1) ).asJava();
			
				// If not null, continue
			if(currentInfo1 != null) {
				var currentInfo2 = currentInfo1.getOrDefault(mode , null);
				
					// Skip this type if it don't have components defined for the specified mode (and by extension hyperparams)
				if(currentInfo2 != null) {
					
					// Now, just add all the hyper-params of the component
				var iterator = currentInfo2.iterator();
				Map<String, HyperparamInfo[]> insideMap = new LinkedHashMap<String, HyperparamInfo[]>();
					// Add all hyper-params of everything inside the iterator
				while( iterator.hasNext() ) {
					ComponentInfo next = iterator.next();
					HyperparamInfo[] paramsrequired = next.params();
					insideMap.put( next.shortName(), paramsrequired );
				}

					// And insert the info for this type
				result.put(currentComponent._1, insideMap);
				}
			}
		}
		
		return result;		
	}
	
		/**  @return The settings that are specific to this mode, same structure same getDefaultClasses*/
	public final Map< String , Map < String , HyperparamInfo[] > > getExclusiveSettings() {
		
		return getSettingsSpecifiedMode( this.getModeHelper().identifier() );
		
	}

		/** A map with the name of hyper-parameters as a key that can be used in this mode as key,
		 * and as value a tuple with the actual hyper-parameters and a set of the short-name of
		 * components that use it
		 */
	public final LinkedHashMap<String,Tuple2<HyperparamInfo,HashSet<String>>> combinedSettings = getCombinedSettings();

		/**  @return A map with the the name of the hyper-parameter as a key, and as an object another map with the short name of each component that uses it and as value it's tupe as defined in INFO_COMPONENTS  */
	private LinkedHashMap<String, Tuple2< HyperparamInfo , HashSet<String> > > getCombinedSettings() {
		
			// We will need
		Map<String, Map<String, HyperparamInfo[]>> generalSettings = this.getDefaultSettings();
		Map<String, Map<String, HyperparamInfo[]>> exclusiveSettings = this.getExclusiveSettings();
		
		Map<String, Map<String, HyperparamInfo[]>>[] maps = new Map[] {generalSettings , exclusiveSettings};
		
		Map<String, Object> defaultSettingsValues = JavaConverters.mapAsJavaMapConverter( Configuration.defaultParams() ).asJava() ;
		LinkedHashMap<String, Tuple2<HyperparamInfo, HashSet<String>>> result = new LinkedHashMap();

		for(int count = 0 ;  count < INFO_COMPONENTS.length ; ++count) {
			String currentKey = INFO_COMPONENTS[count]._1();
			result.get(currentKey);
			
			for(int mapCount = 0 ; mapCount < maps.length ; ++mapCount) {
				Map<String, Map<String, HyperparamInfo[]>> currentMap = maps[mapCount];
				
				Map<String, HyperparamInfo[]> values = currentMap.get(currentKey);
					
				if(values != null) {
					
					var iterator = values.values().iterator();
					while(iterator.hasNext()) {
						HyperparamInfo[] next = iterator.next();
						if(next != null) {
							for(int valueCount = 0 ; valueCount < next.length ; ++valueCount) {
								
								HyperparamInfo currentValue = next[valueCount];

								Tuple2<HyperparamInfo, HashSet<String>> whereToAdd = result.getOrDefault(currentValue.name() , null);
								// If it doesn't exist, add it
								if(whereToAdd == null) {
									LinkedHashSet<String> newSet = new LinkedHashSet<String>();
									newSet.add(currentValue.name());
									result.put(currentValue.name(), new Tuple2<HyperparamInfo, HashSet<String>>(currentValue, newSet));
								}
								else {
									whereToAdd._2.add(currentValue.name());
								}
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

			// We will need these
		LinkedHashMap<String, Tuple2<HyperparamInfo, HashSet<String>>> settingsDescriptions = combinedSettings;
		Iterator<Tuple2<HyperparamInfo, HashSet<String>>> iterator = settingsDescriptions.values().iterator();
		String[] array = new String[ combinedSettings.size() * 2 ];
		int whereToAdd = 0;
		
			// All hyper-parameters will be used
		while( iterator.hasNext() ) {
				// Get the parameter...
			Tuple2<HyperparamInfo, HashSet<String>> next = iterator.next();
			HyperparamInfo param = next._1;
			
				// ...get the name...
			array[whereToAdd] = param.name();
			whereToAdd++;
			
				// ...and the default value
			Double value = param.initial();
			String valueString = value.toString();
			array[whereToAdd] = valueString;
			whereToAdd++;
		}
		
			// Now from the array create the actual model and return it
		SettingsModelStringArray answer = new SettingsModelStringArray(KEY_SETTINGS, array);
		return answer;
	}



	

		/*
		 *  NOW THE GOOD PART: EXECUTING THE ALGORITHM
		 */

	/** 
	 * 	This handles the creation of a DINOS algorithm object
	 *  for subgroup discovery from the previously configured
	 *  node information, and then returning it's results
	 *  
	 *  @param knimeTable Table from which to discover subgroups
	 *	@param exec Execution context, from which to allocate more memory and update progress bar
	 *	@param model The source model, used to add output variables to it
	 *  
	 *  @return An array of result object to be displayed in KNIME result ports
	 *  @throws Exception Of any kind 
	 */
	public PortObject[] executeDiscovery(
			final BufferedDataTable knimeTable,
			final ExecutionContext exec,
			final GenericDinosKnimeModel model
			) throws Exception {

				// We will need these
			Tuple3<Dataset, Configuration, Algorithm> results = createDiscoveryAlgorithm(knimeTable, exec, model);
			Dataset dataset = results._1();
			Configuration configuration = results._2();
			Algorithm dinos = results._3();
	
				// This will (1) periodically poll the current iteration to update the progress bar, (2) check cancellation
			ScheduledExecutorService scheduler = createDiscoveryPoller(exec, dinos, model.getTrialsAmount() );
			
				// GO!
			dinos.run();

				// We don't need this anymore
			scheduler.shutdown();
			
				// This is mainly here for immediate cancel when stopped from the scheduler
			exec.checkCanceled();
			
				// Set the message and return the results
			exec.setProgress(1, MESSAGE_FINISHED);
			
			return new PortObject[] {
						// Subgroup descriptions
					subgroupInformation(dinos, dataset, exec),
						// Instances descriptions
					subgroupWithInstances(dinos, dataset, exec),
						// Variables
					discoveryResultsToVariable(model, dinos, dataset, configuration)
			};
	}
	
		/** @return An already running object that for polling status and checking algorithm cancelations */
	public ScheduledExecutorService createDiscoveryPoller(
			ExecutionContext exec,
			Algorithm dinos,
			int trials) {
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1); // Only need one
		Runnable task = new Runnable() { 
				// The thrown exception here will not be caught in the main workflow,
				// so the algorithm is stopped and the following check actually cancels
				// the execution
			@Override public void run() { updateProgressBar(exec, dinos, trials );
			try { exec.checkCanceled();	} catch (CanceledExecutionException e) {e.printStackTrace(); dinos.stop(); scheduler.shutdown(); } }};
		scheduler.scheduleAtFixedRate(task, DEFAULT_UPDATE_INITIALDELAY, DEFAULT_UPDATE_EVERY, DEFAULT_UPDATE_UNIT);
		return scheduler;
	}
	
		/** 
		 *  Wrapper for createAlgorithm for subgroup discovery
		 *  @return The Dataset object that was created from the input table, the Configuration applied to DINOS and an Algorithm with those two sets ready for subgroup discovery
		 */
	public Tuple3<Dataset, Configuration, Algorithm> createDiscoveryAlgorithm(
			final BufferedDataTable knimeTable,
			final ExecutionContext exec,
			final GenericDinosKnimeModel model
			) throws Exception {
		
			// We will need these
		final HashSet<String> targets = model.targetToHashSet();
		final String[] censor = model.getCensorInfo();
		final int trials = model.getTrialsAmount();
		final long seed = model.seedToLong();
		final boolean useDefaultSettings = model.getUseDefaultOrNot();
		final boolean collectIterationMetrics = model.getCollectIteration();
		final Map<String,SettingsModel> classesConfig = model.getClassesConfig();
		final String[] settings = model.getSettingsArray();
		final List<String> fixedVars = model.getFilterString();

			// And then use this
		return createAlgorithm(knimeTable, exec, targets, censor, trials, collectIterationMetrics, settings, useDefaultSettings, classesConfig, seed, fixedVars);
	}

		/**
		 * Utility for creating algorithm, better to use a wrapper than directly
		 * 
		 * @return The Dataset object that was created from the input table, the Configuration applied to DINOS and an Algorithm with those two sets ready for subgroup discovery
		 */
	private Tuple3<Dataset, Configuration, Algorithm> createAlgorithm(
				BufferedDataTable knimeTable,
				ExecutionContext exec,
				HashSet<String> targets,
				String[] censor,
				int trials,
				boolean collectIterationMetrics,
				String[] settings,
				boolean useDefaultSettings,
				Map<String,SettingsModel> classesConfig,
				long seed,
				List<String> fixedVars
				) throws Exception {
	
				// Create Dataset
			Dataset dataset = KnimeTableToDinosDataset.KnimeTableToDinosDataset(knimeTable, exec, targets, censor);
		
				// Create configuration
			Configuration configuration = KnimeTableToDinosDataset.ArraySettingsToDinosConfig( trials, collectIterationMetrics, settings, fixedVars, dataset );

				// Create settings
			Algorithm dinos = useDefaultSettings ? getDefaultAlgorithm() : getCustomAlgorithm( classesConfig );
			
				// Set Seed
			Randomize.setSeed(seed);
			
				// Set dataset and configuration
			dinos.setContext(dataset, configuration);

			return new Tuple3<Dataset, Configuration, Algorithm>(dataset, configuration, dinos);
		}

		/** 
		 * This will set the progress bar of the given Execution Context to the appropriate percentage
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
		
		DiscoveryMode mode = MAP_MODE_CONVERTED.get( getClassType() );
		
		return mode.algorithmFromMapParams(
				scala.collection.immutable.Map.from(CollectionConverters.MapHasAsScala(result).asScala()) );
	}
	
		/**
		 * Results of the first output port showing subgroup and results
		 * Differs for each mode
		 * 
		 * @param dinos Algorithm instance from which to obtain the final subgroup information
		 * @param dataset The Dataset that was run on the algorithm
		 * @param exec KNIME Execution Context from which to ask to create tables
		 * @param discoveryMode The mode under which the subgroup discovery was made
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

		scala.collection.mutable.StringBuilder subgroupDescription = new scala.collection.mutable.StringBuilder();
		
		FormatSymbols.ruleBodyToStringBuilder(subgroup, dataset, subgroupDescription);
				
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
			String subgroupName = dataset.modeHelper().fullRuleString(currentSubgroup, dataset);
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

			// TODO comment for this helper class
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
		 * During runtime they should appear in this order:
		 * 
		 * 1 - General results for the run of the algorithm itself
		 * 2 - Results specific to the algorithm mode
		 * 3 - Variables showing the classes used for each component
		 * 4 - Variables showing the hyper-parameter values
		 * 
		 * These last two are for allowing the user to recreate
		 * the configuration with flow variables
		 * 
		 * @param nodeModel The model for this algorithm mode
		 * @param dinos Algorithm instance that was used to obtain subgroups
		 * @param dataset The Dataset that was run on the algorithm
		 * @param configuration Configuration used for the algorithm
		 * 
		 * @return The singleton object for variables port, which cannot be created (otherwise an different port would be used for the components and settings aside from results)
		 */
	public FlowVariablePortObject discoveryResultsToVariable(
			GenericDinosKnimeModel nodeModel,
			Algorithm dinos,
			Dataset dataset,
			Configuration configuration)  {

		addSettingsResultsVariables( nodeModel , dinos, dataset, configuration );
		
		addComponentsResultsVariables( nodeModel , dinos );

		addSpecificResultsVariables( nodeModel , dinos , dataset );
		
		addOverallResultsVariables( nodeModel , dinos , dataset) ;
		
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
	private void addComponentsResultsVariables(
			GenericDinosKnimeModel nodeModel,
			Algorithm dinos
			) {
			Map<String, SettingsModel> classesConfig = nodeModel.classesConfig;
			
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
			Algorithm dinos,
			Dataset dataset,
			Configuration configuration) {

			// Without configuration, there cannot be a fixed settings
		if(configuration != null)
			nodeModel.addResultVariables( KEY_FIXED , StringArrayType.INSTANCE, configuration._4().fixedVarsArray() );
		
		nodeModel.addResultVariables( KEY_SETTINGS , StringArrayType.INSTANCE , nodeModel.getSettingsArray() );
		
		nodeModel.addResultVariables( KEY_COLLECTITERATIONS , BooleanType.INSTANCE , nodeModel.getCollectIteration() );
		
		nodeModel.addResultVariables( KEY_TRIALS , IntType.INSTANCE , nodeModel.getTrialsAmount() );

	}
	
		/** 
		 * 	This handles the creation of a table containing all 
		 *  instances of a given dataset formatted as a single,
		 *  giant subgroup
		 *  
		 *  @param knimeTable Table from which to extract the subgroup
		 *	@param exec Execution context, from which to allocate more memory for new tables
		 *	@param model The source model, used to add output variables to it
		 *  
		 *  @return An array of result object to be displayed in KNIME result ports
		 *  @throws Exception Of any kind 
		 */
	public PortObject[] executeExtract(
			BufferedDataTable knimeTable,
			ExecutionContext exec,
			GenericDinosKnimeModel nodeModel) throws Exception {
		
			// Just need this, really
		HashSet<String> targets = nodeModel.targetToHashSet();
		String[] censor = nodeModel.getCensorInfo();
		
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


		/** @return Just disable the port, simply to maintain consistency with the other output nodes */
	private PortObject instancesInfo(BufferedDataTable inputTable, ExecutionContext exec) throws CanceledExecutionException {

		exec.checkCanceled();

		return InactiveBranchPortObject.INSTANCE; //container.getTable();
	}
	
		/**
		 * Where the actual "extraction" is made.
		 * Largely a fancy wrapper for {@link GenericDinosKnimeWorkflow#addInstanceAsTableRow},
		 * which is a helper function of {@link GenericDinosKnimeWorkflow#subgroupWithInstances}
		 * 
		 * @param dataset Dataset from which to get a single subgoup
		 * @param exec Needed for creating the result table object
		 * 
		 * @return The Table with the extracted subgroup
		 */
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

		/** 
		 * 	This handles the creation of a DINOS algorithm object
		 *  for parsing from the previously configured node information,
		 *  and then returning it's results
		 *  
		 *  @param dataTable Dataset table from which to get metrics for parsed subgroups
		 *  @param descriptionsTable With information about the subgroups to parse
		 *	@param exec Execution context, from which to allocate more memory and update progress bar
		 *	@param model The source model, used to add output variables to it
		 *  
		 *  @return An array of result object to be displayed in KNIME result ports
		 *  @throws Exception Of any kind 
		 */
	public PortObject[] executeParser(
			final BufferedDataTable dataTable,
			final BufferedDataTable descriptionsTable,
			final ExecutionContext exec,
			final GenericDinosKnimeModel model
			) throws Exception {
	
			// Create Dataset
		Tuple3<Dataset, Configuration, Algorithm> results = createParseAlgorithm(dataTable, descriptionsTable, exec, model);

			// Get these from the created algorithm...
		Dataset dataset = results._1();
		Algorithm dinos = results._3();
		exec.setMessage(MESSAGE_PARSER);
		
			// ...and these from the input model
		final HashSet<String> targets = model.targetToHashSet();
		final String descriptionColumn = model.getDescClass();
		final String[] censor = model.getCensorInfo();
		
			// The actual string descriptions
		String[] descriptions = getSubgroupDescriptions(descriptionsTable, descriptionColumn, targets);
		
		model.logDebug( "Parsing Subgroup: \n" + descriptions.toString() + "\n" );
		
			// GO!
		dinos.parse( descriptions );
		
		return new PortObject[] {
				// Subgroup descriptions
			subgroupInformation(dinos, dataset, exec),
				// Instances descriptions
			subgroupWithInstances(dinos, dataset, exec),
				// Variables
			discoveryResultsToVariable(model, dinos, dataset, null)
		};
	}

		/** 
		 *  Wrapper for createAlgorithm for subgroup discovery
		 *  @return The Dataset object that was created from the input table, the Configuration applied to DINOS and an Algorithm with those two sets ready for subgroup discovery
		 */
	Tuple3<Dataset, Configuration, Algorithm> createParseAlgorithm(
		final BufferedDataTable dataTable,
		final BufferedDataTable descriptionsTable,
		final ExecutionContext exec,
		final GenericDinosKnimeModel model
		) throws Exception {
			
			final HashSet<String> targets = model.targetToHashSet();
			final String descriptionColumn = model.getDescClass();
			final String[] censor = model.getCensorInfo();
			final boolean useDefaultSettings = model.getUseDefaultOrNot();
			final Map<String,SettingsModel> classesConfig = model.getClassesConfig();
			final String[] settings = model.getSettingsArray();
			
			return createAlgorithm(dataTable, exec, targets, censor, 0, Configuration.getDefaultCollectIterations(), settings, useDefaultSettings, classesConfig, 0, null);
		}
	
	private String[] getSubgroupDescriptions(
			final BufferedDataTable descriptionsTable,
			final String descriptionColumn,
			final HashSet<String> targets) {
		
			// We will need these
		DataTableSpec descriptionSpec = descriptionsTable.getDataTableSpec();
		String[] answer = new String[ (int) descriptionsTable.size() ];
		CloseableRowIterator iterator = descriptionsTable.iterator();
				
			// First, check if these columns exist
		int descriptionPosition = tryToGetPositionInTable( descriptionSpec , descriptionColumn, false , " (for body description)");

			// TODO check for size
		for(int count = 0 ; count < answer.length ; ++ count) {
			DataRow nextRow = iterator.next();
			String subgroupDescriptions
				= nextRow.getCell(descriptionPosition).toString();
			answer[count] = subgroupDescriptions;
		}
		
		iterator.close();

		return answer;
	}
	
		/**
		 * Helper function for parser,
		 * which tries tofind a column in a table spec
		 * 
		 * @param descriptionSpec Specifications of tables where to find
		 * @param descriptionColumn Column to try to find
		 * @param canBeNull If false, throw an {@link IllegalArgumentException} if descriptionColumn is null
		 * @param possibleError Specific error message explaining the column in case the function fails
		 * 
		 * @return position of column in spec or -1 if not found and canBeNull equals true
		 */
	private int tryToGetPositionInTable(
			final DataTableSpec descriptionSpec,
			final String descriptionColumn,
			final boolean canBeNull,
			final String possibleError) {
		
		var answer = -1;
		
		if( descriptionColumn == null) {
			if( canBeNull == false )
				throw new IllegalArgumentException(EXCEPTION_PARSER + EXCEPTION_COLUMNNOTFOUND_OBLIGATORYNULL + possibleError);
		}
		else
			answer = tryToGetPositionInTable(descriptionSpec, descriptionColumn, possibleError);
		
		return answer;
	}
	
		/** Helper function for parser helper function {@link GenericDinosKnimeWorkflow#tryToGetPositionInTable(DataTableSpec, String, boolean, String)} */
	private int tryToGetPositionInTable(
			final DataTableSpec descriptionSpec,
			final String descriptionColumn,
			final String possibleError) {

		int answer = descriptionSpec.findColumnIndex(descriptionColumn);
		if(answer == -1)
			throw new IllegalArgumentException(EXCEPTION_PARSER + EXCEPTION_COLUMNNOTFOUND_NOTFOUND + possibleError);

		return answer;
	}


}
