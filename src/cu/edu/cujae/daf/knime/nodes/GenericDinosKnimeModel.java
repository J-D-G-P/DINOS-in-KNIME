package cu.edu.cujae.daf.knime.nodes;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataValue;
import org.knime.core.data.NominalValue;
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
import org.knime.core.node.workflow.VariableType;

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

		// Settings models, each one have both the actual variable
		// and an overrideable setting to get them
		// They should not be directly accessed
	
		// The target class
	protected final SettingsModelString target_class = workflow.createTargetClass();
	protected HashSet<String> targetToHashSet() {HashSet<String> set = new HashSet<String>() ; set.add(target_class.getStringValue()) ; return set; }
	
		// In case the nodes need it (right now, only survival mode)
	protected String[] getCensorInfo() {return null;}

		// Random Number Generator Seed and indication if to use a pre determined one or not
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

		// Constructor
    protected GenericDinosKnimeModel() {
    
    	super(		// By default should be one for input (a table)
    				// and three for output (two tables and variables)
        		GenericDinosKnimeWorkflow.PORT_INPUT_TYPES,
        		GenericDinosKnimeWorkflow.PORT_OUTPUT_TYPES
        		);
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
    protected PortObject[] execute(final PortObject[] inObjects, final ExecutionContext exec) throws Exception {

        return workflow.execute(
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
    	
    	DataTableSpec input = inSpecs[workflow.PORT_INPUT_DATASET];
        DataTableSpec inSpec = (DataTableSpec)inSpecs[workflow.PORT_INPUT_DATASET];
        Class<? extends DataValue>[] types = workflow.getAceptedTargetTypes();
    	
    	if (input.getNumColumns() == 0)
    		throw new InvalidSettingsException(workflow.MESSAGE_EMPTYTABLE);
    	
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
        
        if (classifyColumn == null) { // auto-guessing, adapted from TreeLearner
            assert !isValid : workflow.MESSAGE_NOCLASSVALIDCONFIG;
            // if no useful column is selected guess one
            // get the first useful one starting at the end of the table
            for (int countColumn = inSpec.getNumColumns() - 1; countColumn >= 0; countColumn--) {
            	
            	for(int countType = 0 ; countType < types.length ; ++countType) {
            		Class<? extends DataValue> currentType = types[countType];
            			// Set as default the last column available of the supported type
                    if (inSpec.getColumnSpec(countColumn).getType().isCompatible(currentType)) {
                    	target_class.setStringValue(
                                inSpec.getColumnSpec(countColumn).getName());
                        super.setWarningMessage( workflow.MESSAGE_GUESING + target_class.getStringValue());
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
    	
        	// Due to metrics the columns at runtime are actually variable
        return new DataTableSpec[]{null};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
    	
		target_class.saveSettingsTo(settings);
		
		generator_seed.saveSettingsTo(settings);
		
		useDefaultOrNot.saveSettingsTo(settings);
		
		trialsAmount.saveSettingsTo(settings);
		
		collectIterations.saveSettingsTo(settings);
		
		generalSettings.saveSettingsTo(settings);
		
		Collection<SettingsModel> values = classesConfig.values();
		for(SettingsModel element : values)
			element.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
		target_class.loadSettingsFrom(settings);
		
		generator_seed.loadSettingsFrom(settings);
		
		useDefaultOrNot.loadSettingsFrom(settings);
		
		trialsAmount.loadSettingsFrom(settings);
		
		collectIterations.loadSettingsFrom(settings);
		
		generalSettings.loadSettingsFrom(settings);
		
		Collection<SettingsModel> values = classesConfig.values();
		for(SettingsModel element : values)
			element.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
		target_class.validateSettings(settings);

		generator_seed.validateSettings(settings);

		useDefaultOrNot.validateSettings(settings);

		trialsAmount.validateSettings(settings);

		collectIterations.validateSettings(settings);
		
		generalSettings.validateSettings(settings);

		Collection<SettingsModel> values = classesConfig.values();
		for(SettingsModel element : values)
			element.validateSettings(settings);
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

