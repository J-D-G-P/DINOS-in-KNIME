package cu.edu.cujae.daf.knime.nodes;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
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
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelSeed;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.defaultnodesettings.SettingsModelStringArray;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortType;
import org.knime.core.node.workflow.VariableType;

import cu.edu.cujae.daf.knime.nodes.GenericDinosKnimeWorkflow.NODES_TYPES;


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
	protected abstract NODES_TYPES getMode();

		// Settings models, each one have both the actual variable
		// and an overrideable setting to get them
		// They should not be directly accessed
	
		// The target class
	protected final SettingsModelString target_class = workflow.createTargetClass();
	protected HashSet<String> targetToHashSet() {HashSet<String> set = new HashSet<String>() ; set.add(target_class.getStringValue()) ; return set; }
	
	protected final SettingsModelString desc_class = workflow.createDescClass();
	protected String getDescClass() {return desc_class.getStringValue(); }
	
	protected final SettingsModelString in_class = workflow.createInClass();
	protected String getInClass() {return in_class.getStringValue(); }
	
	protected final SettingsModelString then_class = workflow.createThenClass();
	protected String getThenClass() {return then_class.getStringValue(); }
	
		// In case the nodes need it (right now, only survival mode)
	protected String[] getCensorInfo() {return null;}

		// Random Number Generator Seed and indication if to use a pre-determined one or not
	protected final SettingsModelSeed generator_seed = workflow.createGeneratorSeed();
	protected long seedToLong() {return generator_seed.getLongValue(); }

		// Yes or no value to use default settings or ones specified by the user
	protected final SettingsModelBoolean useDefaultOrNot = workflow.createUseDefaultOrNot();
	protected boolean getUseDefaultOrNot() { return useDefaultOrNot.getBooleanValue(); }
	
		// Trials of the algorithm, each iteration consumes a certain amount of trials
	protected final SettingsModelIntegerBounded trialsAmount = workflow.createTrialModel();
	protected int getTrialsAmount() {return trialsAmount.getIntValue(); }
	
		// Collect results of metrics after each iteration of the algorithm
	protected final SettingsModelBoolean collectIterations = workflow.createCollectItarationsModel();
	protected boolean getCollectItaration() { return collectIterations.getBooleanValue(); }

		// WHich class for each component
	protected final Map< String , SettingsModel> classesConfig = workflow.createSettingsModelForAllComponents();
	protected Map< String , SettingsModel> getClassesConfig() {return classesConfig;}
	
		// Hyperparameters for algorithm
	protected final SettingsModelStringArray generalSettings = workflow.createSettingsModelForAvailableSettings();
	protected String[] getSettingsArray() {return generalSettings.getStringArrayValue(); }

		// Constructor for default value
    protected GenericDinosKnimeModel() {
    	
    	super(
        		GenericDinosKnimeWorkflow.PORT_INPUT_USUAL,
        		GenericDinosKnimeWorkflow.PORT_OUTPUT_TYPES
        		);
    }
    
		// Constructor for differing amount of ports
    protected GenericDinosKnimeModel(final PortType[] inPortTypes, final PortType[] outPortTypes) {
    	super(inPortTypes , outPortTypes);
    }
    	/* 
    	 * This is just a wrapper for "pushFlowVariable",
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
     * Execution of the node, which is transfered to the workflow helper
     * 
     * @param inObjects The input table.
     * @param exec For {@link BufferedDataTable} creation and progress.
     * @return The output objects, by default a table with the subgorup, other with the instances, and the result variables
     * 
     * @throws Exception If the node execution fails for any reason (which should not)
     */
    @Override
    final protected PortObject[] execute(final PortObject[] inObjects, final ExecutionContext exec) throws Exception {
    	
    	NODES_TYPES mode = getMode();
    	
    	PortObject[] answer = null;
    	
    	if(mode == NODES_TYPES.DISCOVERY)
    		answer = workflow.executeDiscovery(
        			(BufferedDataTable) inObjects[workflow.PORT_INPUT_DATASET],
        			exec,
        			this,
        			targetToHashSet(),
        			getCensorInfo(),
        			getTrialsAmount(),
        			seedToLong(),
        			getUseDefaultOrNot(),
        			getCollectItaration(),
        			getClassesConfig(),
        			getSettingsArray()
        		);
    	else if (mode == NODES_TYPES.EXTRACTOR)
    		answer = workflow.executeExtract(
        			(BufferedDataTable) inObjects[workflow.PORT_INPUT_DATASET],
        			exec,
        			this,
        			targetToHashSet(),
        			getCensorInfo(),
        			getTrialsAmount(),
        			seedToLong(),
        			getUseDefaultOrNot(),
        			getCollectItaration(),
        			getClassesConfig(),
        			getSettingsArray()
        		);
    	else if (mode == NODES_TYPES.PARSER)
    		answer = workflow.executeParser(
				  (BufferedDataTable) inObjects[workflow.PORT_INPUT_DATASET],
				  (BufferedDataTable) inObjects[workflow.PORT_INPUT_DESCRIPTIONS],
				  exec,
				  this,
				  targetToHashSet(),
				  getDescClass(),
				  getInClass(),
				  getThenClass(),
				  getCensorInfo(),
				  getUseDefaultOrNot(),
				  getClassesConfig(),
				  getSettingsArray()
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
    	
    	NODES_TYPES mode = getMode();
    	
    	if(mode == NODES_TYPES.DISCOVERY) {
    		mode1Configure(inSpecs);
    	}
    	else if (mode == NODES_TYPES.EXTRACTOR) {
        	//checkCompatibleValuesForModelAgainstColumns(inSpecs[workflow.PORT_INPUT_DATASET], workflow.getAceptedTargetTypes(), false, target_class);
    	}
    	else if (mode == NODES_TYPES.PARSER) {

    	}
    	
        	// Due to metrics the columns at runtime are actually variable
        return new DataTableSpec[]{null};
    }
    
    protected void mode1Configure(final DataTableSpec[] inSpecs) 
    	throws InvalidSettingsException {
    	
    	DataTableSpec input = inSpecs[workflow.PORT_INPUT_DATASET];
        DataTableSpec inSpec = (DataTableSpec)inSpecs[workflow.PORT_INPUT_DATASET];
        Class<? extends DataValue>[] types = workflow.getAceptedTargetTypes();

    	if (input.getNumColumns() == 0)
    		throw new InvalidSettingsException(workflow.MESSAGE_EMPTYTABLE);
    	
       checkExistingDomainForStringColumns(inSpec);
    	
    	// Auto guessing logic adapted from decisionTreeLearnerNode
    	
        // check spec with selected column
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

        if (classifyColumn != null && (!isValid || !tryToFind) ) {
            throw new InvalidSettingsException( workflow.MESSAGE_CONFIGCLASSNOTFOUND + classifyColumn);
        }
        
        if (classifyColumn == null) {
        	checkCompatibleValuesForModelAgainstColumns(inSpec, types, isValid, target_class);
        }
    	
    }

	private void checkCompatibleValuesForModelAgainstColumns(DataTableSpec inSpec, Class<? extends DataValue>[] types, boolean isValid, SettingsModelString model)
			throws InvalidSettingsException {
		// auto-guessing, adapted from TreeLearner
            assert !isValid : workflow.MESSAGE_NOCLASSVALIDCONFIG;
            // if no useful column is selected guess one
            // get the first useful one starting at the end of the table
            for (int countColumn = inSpec.getNumColumns() - 1; countColumn >= 0; countColumn--) {
            	
            	for(int countType = 0 ; countType < types.length ; ++countType) {
            		Class<? extends DataValue> currentType = types[countType];
            			// Set as default the last column available of the supported type
                    if (inSpec.getColumnSpec(countColumn).getType().isCompatible(currentType)) {
                    	model.setStringValue(
                                inSpec.getColumnSpec(countColumn).getName());
                    	addWarning( workflow.MESSAGE_GUESING + model.getStringValue());
                        	// The break only work on this inner cycle, so also stop the enclosing one by directly changing the counter
                        countColumn = -1;
                        break;
                    }
                    
            	}
       
            }
            	// If no supported column was found, complain
            if (target_class.getStringValue() == null) {
                throw new InvalidSettingsException(workflow.MESSAGE_NOCLASS);
            }
	}

    	// TODO comment
    private void checkExistingDomainForStringColumns(DataTableSpec inSpec) throws InvalidSettingsException {

    	int columnAmount = inSpec.getNumColumns();
    	HashSet<String> nonExistingSpecs = new HashSet<String>();
    	for(int count = 0 ; count < columnAmount ; ++count) {
    		
    		DataColumnSpec currentSpec = inSpec.getColumnSpec(count);
    		DataColumnDomain currentDomain = currentSpec.getDomain();
    		String identifier = currentSpec.getType().getIdentifier();
   
    		if( identifier.equals(workflow.ID_STRING)
    		&& ( !currentDomain.hasValues() ) ) {
    			nonExistingSpecs.add( currentSpec.getName() );
    		}
    	}

    	if( !nonExistingSpecs.isEmpty() )
    		{ throw new InvalidSettingsException( "No value list found in domain for the columns: " + nonExistingSpecs.toString() + " (If you still want to use these column re-create the table with the \"Domain Calculator\" node with unrestricted number of possible values) " ); }
		
	}

    interface SettingsOps {
    	void ops( SettingsModel[] a);
    }
    
    interface SettingsOpsThrow {
    	void ops( SettingsModel[] a) throws InvalidSettingsException;
    }
    
    protected SettingsModel[] classesConfigToArray() {
    	return classesConfig.values().toArray( new SettingsModel[0]  );
    }
    
    private SettingsModel[] MODELS_DICOVERY = {target_class , generator_seed , useDefaultOrNot , trialsAmount , collectIterations , generalSettings};
    protected SettingsModel[] getModelsDiscovery() { return MODELS_DICOVERY; }

    private SettingsModel[] MODELS_EXTRACTOR = {target_class};
    protected SettingsModel[] getModelsExtractor() { return MODELS_EXTRACTOR; }

    private SettingsModel[] MODELS_PARSE = {target_class , desc_class , in_class, then_class};
    protected SettingsModel[] getModelsParse() { return MODELS_PARSE; }
    
	/**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
    	
    	NODES_TYPES mode = getMode();
    	
    	SettingsOps ops = (a) -> {for(SettingsModel b : a) b.saveSettingsTo(settings);};
    	
    	if(mode == NODES_TYPES.DISCOVERY) {

			ops.ops(getModelsDiscovery());
			
			ops.ops( classesConfigToArray() );

    	}
    	else if(mode == NODES_TYPES.EXTRACTOR) {
    		
    		ops.ops(getModelsExtractor());
    		
    	}
    	else if (mode == NODES_TYPES.PARSER) {
    		
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
    	
    	NODES_TYPES mode = getMode();
    	
    	SettingsOpsThrow ops = (a) -> {for(SettingsModel b : a) b.loadSettingsFrom(settings); };
    	
    	if (mode == NODES_TYPES.DISCOVERY) {
    		
    		ops.ops(getModelsDiscovery());
		
			ops.ops( classesConfig.values().toArray( new SettingsModel[0]  ) );
    	}
    	else if (mode == NODES_TYPES.EXTRACTOR) {
    		
    		ops.ops(getModelsExtractor());
    	}
    	else if (mode == NODES_TYPES.PARSER) {
    		
    		ops.ops(getModelsParse());
    		
			ops.ops( classesConfig.values().toArray( new SettingsModel[0]  ) );
    	}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
    	
    	NODES_TYPES mode = getMode();
    	
    	SettingsOpsThrow ops = (a) -> {for(SettingsModel b : a) b.validateSettings(settings); };
    	
    	if (mode == NODES_TYPES.DISCOVERY) {
    	
    		ops.ops(getModelsDiscovery());

			ops.ops( classesConfig.values().toArray( new SettingsModel[0]  ) );
    	}
    	else if (mode == NODES_TYPES.EXTRACTOR) {
    		
    		ops.ops(getModelsExtractor());
    	}
    	else if (mode == NODES_TYPES.PARSER) {
    		
    		ops.ops(getModelsParse());

			ops.ops( classesConfig.values().toArray( new SettingsModel[0]  ) );
    		
    	}
    }
    
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

