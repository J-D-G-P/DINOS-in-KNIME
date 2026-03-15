package cu.edu.cujae.daf.knime.nodes;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.knime.core.data.DataColumnDomain;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataValue;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnFilter2;
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
import org.knime.core.node.util.filter.NameFilterConfiguration.FilterResult;
import org.knime.core.node.workflow.VariableType;
import org.knime.node.v210.DescriptionDocument.Description;

import cu.edu.cujae.daf.knime.nodes.GenericDinosKnimeWorkflow.DINOS_NODE;


/**
 * Default configuration logic for all subgroup discovery nodes
 * 
 * Methods has been made as modular as possible to make
 *  extending and replacing the class's functions easier
 * 
 * @author Jonathan David González Pereda, CUJAE
 */

	@SuppressWarnings("static-access")
public abstract class GenericDinosKnimeModel extends NodeModel {

		/** Get the specific workflow helper */
	protected abstract GenericDinosKnimeWorkflow getInstance();
	
		/** Operations will be called to this */
	protected final GenericDinosKnimeWorkflow workflow = getInstance();
	
		/** 
		 * 1 - Subgroup Discovery
		 * 2 - Dataset Extractor
		 * 3 - Parser
		 */
	protected abstract DINOS_NODE getMode();

		// Settings models, each one have both the actual variable
		// and an overrideable setting to get them
		// They should not be directly accessed
	
		/** The target class for all types */
	protected final SettingsModelString target_class = workflow.createTargetClass();
	protected HashSet<String> targetToHashSet() {HashSet<String> set = new HashSet<String>() ; set.add(target_class.getStringValue()) ; return set; }
	
		/** {@link Description} Class for parsers */
	protected final SettingsModelString desc_class = workflow.createDescClass();
	protected String getDescClass() {return desc_class.getStringValue(); }
		
		/** Fixed variables info */
	protected final SettingsModelFilterString fixed = workflow.createFixedModel();
	protected List<String> getFilterString() {return fixed.getExcludeList(); }
	protected String getFixed() { return null; }
	protected String[] getFixedInfo() {return null;}
	
		/** In case the nodes need it (right now, only survival mode) */
	protected String[] getCensorInfo() {return null;}
	
		/** Random Number Generator Seed and indication if to use a pre-determined one or not */
	protected final SettingsModelSeed generator_seed = workflow.createGeneratorSeed();
	protected long seedToLong() {return generator_seed.getLongValue(); }

		/** Yes or no value to use default settings or ones specified by the user */
	protected final SettingsModelBoolean useDefaultOrNot = workflow.createUseDefaultOrNot();
	protected boolean getUseDefaultOrNot() { return useDefaultOrNot.getBooleanValue(); }
	
		/** Trials of the algorithm, each iteration consumes a certain amount of trials */
	protected final SettingsModelIntegerBounded trialsAmount = workflow.createTrialModel();
	protected int getTrialsAmount() {return trialsAmount.getIntValue(); }
	
		/** Collect results of metrics after each iteration of the algorithm */
	protected final SettingsModelBoolean collectIterations = workflow.createCollectItarationsModel();
	protected boolean getCollectIteration() { return collectIterations.getBooleanValue(); }

		/** Which class for each component  */
	protected final Map< String , SettingsModel> classesConfig = workflow.createSettingsModelForAllComponents();
	protected Map< String , SettingsModel> getClassesConfig() {return classesConfig;}
    protected SettingsModel[] classesConfigToArray() {	return classesConfig.values().toArray( new SettingsModel[0]  );	}
	
		/** Hyperparameters for algorithm */
	protected final SettingsModelStringArray generalSettings = workflow.createSettingsModelForAvailableSettings();
	protected String[] getSettingsArray() {return generalSettings.getStringArrayValue(); }

		/** Constructor for default value */
    protected GenericDinosKnimeModel() {
    	
    	super(
        		GenericDinosKnimeWorkflow.PORT_INPUT_USUAL,
        		GenericDinosKnimeWorkflow.PORT_OUTPUT_TYPES
        		);
    }
    
		/** Constructor for differing amount of ports */
    protected GenericDinosKnimeModel(final PortType[] inPortTypes, final PortType[] outPortTypes) {
    	super(inPortTypes , outPortTypes);
    }
    	/**
    	 * This is just a wrapper for {@link NodeModel#pushFlowVariable},
    	 * directly accessing the method gets an error since
    	 * it is protected
    	 * 
    	 * @param Name Variable to add
    	 * @param Type of variable to add
    	 * @param Value the variable to add
    	 * 
    	 */
	public <T> void addResultVariables(final String name, final VariableType<T> type, final T value) {
		this.pushFlowVariable(name, type, value);
	}

		/**
		 * This is just a wrapper for writing to the logger,
		 * since the get method is protected by {@link NodeModel}
		 * 
		 * @param write
		 */
	public void logDebug(String write) {
		getLogger().debug(write);
	}
	    /**
	     * Execution of the node, which is transfered to the workflow helper
	     * 
	     * @param inObjects The input table.
	     * @param exec For {@link BufferedDataTable} creation and progress.
	     * @return The output objects, by default a table with the subgroup, other with the instances, and the result variables
	     * 
	     * @throws Exception If the node execution fails for any reason (which should not)
	     */
    @Override
    final protected PortObject[] execute(final PortObject[] inObjects, final ExecutionContext exec) throws Exception {
    	
    	DINOS_NODE mode = getMode();
    	
    	PortObject[] answer = null;
    	
    	if(mode == DINOS_NODE.DISCOVERY)
    		answer = workflow.executeDiscovery(
        			(BufferedDataTable) inObjects[workflow.PORT_INPUT_DATASET],
        			exec,
        			this
        		);
    	else if (mode == DINOS_NODE.EXTRACTOR)
    		answer = workflow.executeExtract(
        			(BufferedDataTable) inObjects[workflow.PORT_INPUT_DATASET],
        			exec,
        			this
        		);
    	else if (mode == DINOS_NODE.PARSER)
    		answer = workflow.executeParser(
				  (BufferedDataTable) inObjects[workflow.PORT_INPUT_DATASET],
				  (BufferedDataTable) inObjects[workflow.PORT_INPUT_DESCRIPTIONS],
				  exec,
				  this
				  );
			 
    	
    	return answer;
    }

    /**
     * Not used here. Original:
     * {@inheritDoc}
     */
    @Override
    @Deprecated
    protected void reset() {}

	    /**
	     * Check that the incoming table at least have a
	     * column of the suitable target type and at
	     * least one column
	     * 
	     * Also, this is usually so that KNIME knows beforehand the
	     * column type and specification of incoming tables.
	     * However, DINOS results have a variable number of
	     * columns depending of input table and settings.
	     * Furthermore, the transpose node
	     * (TODO package of transpose)
	     * which swaps columns for rows just returns null
	     * as a ways of declaring unknown specifications,
	     * so we are going to do the same
	     * 
	     * @param inSpecs The specifications of the incoming tables
	     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException {
    	
    	DINOS_NODE mode = getMode();
    	
    	if(mode == DINOS_NODE.DISCOVERY) {
    		targetConfigure(inSpecs);
    	}
    	else if (mode == DINOS_NODE.EXTRACTOR) {
    		targetConfigure(inSpecs);
    	}
    	else if (mode == DINOS_NODE.PARSER) {
        	tablesNotSame(inSpecs);
    		targetConfigure(inSpecs);
    		parsingConfigure(inSpecs);
    	}
    	
        	// Due to metrics the columns at runtime are actually variable
        return new DataTableSpec[]{null};
    }

    	/**
    	 * Helper function for automatically validating existing
    	 * values of the target class, and if not setting a value to it
    	 * 
    	 * @param inSpecs
    	 * @throws InvalidSettingsException
    	 */
    protected void targetConfigure(final DataTableSpec[] inSpecs) 
    	throws InvalidSettingsException {
    	
    		// We will need these variables
    	DataTableSpec input = inSpecs[workflow.PORT_INPUT_DATASET];
        DataTableSpec inSpec = (DataTableSpec)inSpecs[workflow.PORT_INPUT_DATASET];
        Class<? extends DataValue>[] types = workflow.getAceptedTargetTypes();

        	// We have no use for empty tables
        checkNoColumns(input, workflow.EXCEPTION_NOCOLUMNS_DATASET);

    		// Make sure all String Columns have values defined
    	checkExistingDomainForStringColumns(inSpec);
    	
    		// Auto guessing logic adapted from decisionTreeLearnerNode
    	
    		// If the node had a previous configuration, make sure the same target
    		// column is still present
        String classifyColumn = target_class.getStringValue();
        DataColumnSpec columnSpec = inSpec.getColumnSpec(classifyColumn);
        boolean isValid = columnSpec != null;
        boolean tryToFind = false;
        
        if(isValid) {
	        for(int countType = 0 ; countType < types.length ; ++countType) {
	    		Class<? extends DataValue> currentType = types[countType];
	    	
	    		if (columnSpec.getType().isCompatible(currentType) ) {
	    			tryToFind = true;
	    			break;
	    		}
	        }
        }
	
        	// Target Column Configured but not found? Complain!
        if (classifyColumn != null && (!isValid || !tryToFind) ) {
            throw new InvalidSettingsException( workflow.EXCEPTION_CONFIGNOTFOUND_CLASS + classifyColumn);
        }
        
        	// Target column not configured? Try to guess a suitable one based
        	// on supported target columns
        if (classifyColumn == null) {
        	checkCompatibleValuesForModelAgainstColumns(inSpec, types, isValid, target_class, workflow.WARNING_GUESSING_TARGET);
        }
    	
    }
    
	   	/**
		 * Helper function for automatically validating
		 * the input table containing subgroup descriptions
		 * for parsers
		 * 
		 * @param inSpecs
		 * @throws InvalidSettingsException
		 */
    protected void parsingConfigure(final DataTableSpec[] inSpecs) 
        throws InvalidSettingsException {
    	
			// We will need these variables
		DataTableSpec input = inSpecs[workflow.PORT_INPUT_DESCRIPTIONS];
		DataTableSpec inSpec = (DataTableSpec)input;
	    Class<? extends DataValue>[] datasetTypes = workflow.TARGETS_DESCTYPES;
    	
	    	// We have no use for empty description tables
    	checkNoColumns(input, workflow.EXCEPTION_NOCOLUMNS_PARSER);
    	
    	tryToFindConfiguredColumn(desc_class, inSpec, datasetTypes, workflow.EXCEPTION_CONFIGNOTFOUND_PARSER, workflow.WARNING_GUESSING_PARSER);
    }

    		/**
    		 * Check that the (assumed to be) two given columns
    		 * are not the same
    		 * 
    		 * @param inSpecs Columns to check
    		 * @throws InvalidSettingsException 
    		 */
    	protected void tablesNotSame(DataTableSpec[] inSpecs) throws InvalidSettingsException {
    		var spec1 = inSpecs[workflow.PORT_INPUT_DATASET];
    		var spec2 = inSpecs[workflow.PORT_INPUT_DESCRIPTIONS];
    		
    		if ( spec1.equals(spec2) ) {
    			throw new InvalidSettingsException(workflow.EXCEPTION_IDENTICALCOLUMNS_PARSER);
    		}
    	}
		/**
    	 * See if the given model have a valid value, if it have a value
    	 * see if that column can be found, and if enabled try to
    	 * assign a possible value to the model
    	 * 
    	 * @param model Model Which to check the value and maybe assign one if it doesn't exists
    	 * @param inSpec Specifications of the table
    	 * @param types Types of valid columns the model can reference
    	 * @param errorMessage Message to show if the model have a value assigned but the column could not be found or don't have a valid type
    	 * @param assignMessage Message to show in case of auto guessing for an empty model, set to null to not use this feature
    	 * 
    	 * @throws InvalidSettingsException
    	 */
    protected void tryToFindConfiguredColumn(SettingsModelString model , final DataTableSpec inSpec, Class<? extends DataValue>[] types, String errorMessage, String assignMessage)
    	throws InvalidSettingsException {
        String classifyColumn = model.getStringValue();
        DataColumnSpec columnSpec = inSpec.getColumnSpec(classifyColumn);
        boolean isValid = columnSpec != null;
        boolean tryToFind = false;
        
        if(isValid) {
	        for(int countType = 0 ; countType < types.length ; ++countType) {
	    		Class<? extends DataValue> currentType = types[countType];
	    	
	    		if (columnSpec.getType().isCompatible(currentType) ) {
	    			tryToFind = true;
	    			break;
	    		}
	        }
        }
	
        if (classifyColumn != null && (!isValid || !tryToFind) ) {
            throw new InvalidSettingsException( errorMessage + classifyColumn);
        }
        
        	// Target column not configured? Try to guess a suitable one based
        	// on supported target columns
        if (classifyColumn == null && assignMessage != null) {
        	checkCompatibleValuesForModelAgainstColumns(inSpec, types, isValid, model, assignMessage);
        }
    }
    
    	/**
    	 * Helper function, check if a table spec has no column defined.
    	 * 
    	 * @param input
    	 * @param errorMessage
    	 * @throws InvalidSettingsException
    	 */
    protected void checkNoColumns(DataTableSpec input, String errorMessage)
    	throws InvalidSettingsException {
    	if (input.getNumColumns() == 0)
    		throw new InvalidSettingsException(errorMessage);
    }

    	// TODO comment
	private void checkCompatibleValuesForModelAgainstColumns(DataTableSpec inSpec, Class<? extends DataValue>[] types, boolean isValid, SettingsModelString model, String guessingMessage)
			throws InvalidSettingsException {
		// auto-guessing, adapted from TreeLearner
            assert !isValid : workflow.EXCEPTION_NOCLASS_VALIDCONFIG;
            // if no useful column is selected guess one
            // get the first useful one starting at the end of the table
            for (int countColumn = inSpec.getNumColumns() - 1; countColumn >= 0; countColumn--) {
            	
            	for(int countType = 0 ; countType < types.length ; ++countType) {
            		Class<? extends DataValue> currentType = types[countType];
            			// Set as default the last column available of the supported type
                    if (inSpec.getColumnSpec(countColumn).getType().isCompatible(currentType)) {
                    	model.setStringValue(
                                inSpec.getColumnSpec(countColumn).getName());
                    	addWarning( guessingMessage + model.getStringValue());
                        	// The break only work on this inner cycle, so also stop the enclosing one by directly changing the counter
                        countColumn = -1;
                        break;
                    }
                    
            	}
       
            }
            	// If no supported column was found, complain
            if (target_class.getStringValue() == null) {
            	StringBuilder builder = new StringBuilder();
            	Class<? extends DataValue>[] acceptedTypes = workflow.getAceptedTargetTypes();
            	for(int count = 0 ; count < acceptedTypes.length ; ++count) {
            		builder.append( acceptedTypes[count].getSimpleName() );
            	}
                throw new InvalidSettingsException(workflow.EXCEPTION_NOCLASS + ", accepted: " + builder );
            }
	}

    		/**
    		 * Check if the string column of the input spec have a list of values.
    		 * If a string column have too many values, then this list is not set.
    		 * In case this happens, a warning with instructions for the user
    		 * will be shown.
    		 * 
    		 * @param inSpec
    		 * @throws InvalidSettingsException
    		 */
    private void checkExistingDomainForStringColumns(DataTableSpec inSpec) throws InvalidSettingsException {
    		// We will need these
    	int columnAmount = inSpec.getNumColumns();
    	HashSet<String> nonExistingSpecs = new HashSet<String>();
    	for(int count = 0 ; count < columnAmount ; ++count) {
    		
    			// Get info for the current column
    		DataColumnSpec currentSpec = inSpec.getColumnSpec(count);
    		DataColumnDomain currentDomain = currentSpec.getDomain();
    		String identifier = currentSpec.getType().getIdentifier();
   
    			// Now, check if it is a String column, and then the values list
    		if( identifier.equals(workflow.ID_STRING)
    		&& ( !currentDomain.hasValues() ) ) {
    				// If it doesn't exists, add to this list so as to show all problematic columns to the user
    			nonExistingSpecs.add( currentSpec.getName() );
    		}
    	}
    	
    		// And finally complain if a problematic column was found
    	if( !nonExistingSpecs.isEmpty() )
    		{ throw new InvalidSettingsException( "No value list found in domain for the columns: " + nonExistingSpecs.toString() + " (If you still want to use these column re-create the table with the \"Domain Calculator\" node with unrestricted number of possible values) " ); }
		
	}

    	/** Interface for convenient and quick lambda operations on the SettingsTo Methods */
    private interface SettingsOps {
    	void ops( SettingsModel[] a);
    }

		/** Interface for convenient and quick lambda operations on the SettingsTo Methods, if it needs to throw errors */
    private interface SettingsOpsThrow {
    	void ops( SettingsModel[] a) throws InvalidSettingsException;
    }

    	/** Store the specific components to store for Discovery mode */
    private SettingsModel[] MODELS_DICOVERY = {target_class , generator_seed , useDefaultOrNot , trialsAmount , fixed , collectIterations , generalSettings};
    protected SettingsModel[] getModelsDiscovery() { return MODELS_DICOVERY; }

    	/** Store the specific components to store for Extractor mode */
    private SettingsModel[] MODELS_EXTRACTOR = {target_class};
    protected SettingsModel[] getModelsExtractor() { return MODELS_EXTRACTOR; }

		/** Store the specific components to store for Parsing mode */
    private SettingsModel[] MODELS_PARSE = {target_class , desc_class };
    protected SettingsModel[] getModelsParse() { return MODELS_PARSE; }
    
		/**
	     * {@inheritDoc}
	     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
    	
    	DINOS_NODE mode = getMode();
    	
    	SettingsOps ops = (a) -> {for(SettingsModel b : a) b.saveSettingsTo(settings);};
    	
    	if(mode == DINOS_NODE.DISCOVERY) {

			ops.ops(getModelsDiscovery());
			
			ops.ops( classesConfigToArray() );

    	}
    	else if(mode == DINOS_NODE.EXTRACTOR) {
    		
    		ops.ops(getModelsExtractor());
    		
    	}
    	else if (mode == DINOS_NODE.PARSER) {
    		
    		ops.ops(getModelsParse());
    		
			ops.ops( classesConfigToArray() );
    		
    	}
    }

	    /**
	     * {@inheritDoc}
	     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
    	
    	DINOS_NODE mode = getMode();
    	
    	SettingsOpsThrow ops = (a) -> {for(SettingsModel b : a) b.loadSettingsFrom(settings); };
    	
    	if (mode == DINOS_NODE.DISCOVERY) {
    		
    		ops.ops(getModelsDiscovery());
		
			ops.ops( classesConfigToArray() );
    	}
    	else if (mode == DINOS_NODE.EXTRACTOR) {
    		
    		ops.ops(getModelsExtractor());
    	}
    	else if (mode == DINOS_NODE.PARSER) {
    		
    		ops.ops(getModelsParse());
    		
			ops.ops( classesConfigToArray() );
    	}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {

    	DINOS_NODE mode = getMode();
    	
    	SettingsOpsThrow ops = (a) -> {for(SettingsModel b : a) b.validateSettings(settings); };
    	
    	if (mode == DINOS_NODE.DISCOVERY) {
    	
    		ops.ops(getModelsDiscovery());

			ops.ops( classesConfigToArray() );
    	}
    	else if (mode == DINOS_NODE.EXTRACTOR) {
    		
    		ops.ops(getModelsExtractor());
    	}
    	else if (mode == DINOS_NODE.PARSER) {
    		
    		ops.ops(getModelsParse());

			ops.ops( classesConfigToArray() );
    		
    	}
    }
    
    	/**
    	 * Helper function for the entire class
    	 * Set message in {@link NodeModel#setWarningMessage}
    	 * replaces the previous message, there are 
    	 * useful situations where concatenating a message
    	 * to the previous existing one can be useful
    	 * to show all possible warnings to the user
    	 * instead of just the last one
    	 * 
    	 * TODO check if this is really needed
    	 * 
    	 * @param message Text to append to message
    	 */
    protected void addWarning(final String message) {
    	String previousWarning = this.getWarningMessage();
    	if( previousWarning == null || previousWarning.isBlank() )
    		this.setWarningMessage(message);
    	else
    		this.setWarningMessage( this.getWarningMessage() + ", " + message );
    }
    
    /**
     * We don't need this, original:
     * 
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // We don't need this
    }
    
    /**
     * We don't need this, original:
     * 
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // We don't need this
    }
 
}

