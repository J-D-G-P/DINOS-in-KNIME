package cu.edu.cujae.daf.knime.nodes.testing;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.flowvariable.FlowVariablePortObject;
import org.knime.core.node.workflow.VariableType.BooleanType;
import org.knime.core.node.workflow.VariableType.IntType;
import org.knime.core.node.workflow.VariableType.LongType;
import org.knime.core.node.workflow.VariableType.StringArrayType;
import org.knime.core.node.workflow.VariableType.StringType;
import cu.edu.cujae.daf.knime.nodes.GenericDinosKnimeWorkflow;
import cu.edu.cujae.daf.knime.nodes.nominal.NominalDinosKnimeWorkflow;
import cu.edu.cujae.daf.knime.nodes.numeric.NumericDinosKnimeWorkflow;
import cu.edu.cujae.daf.knime.nodes.survival.SurvivalDinosKnimeWorkflow;


/**
 * This is an example implementation of the node model of the
 * "RandomVariableSettings" node.
 * 
 * This example node performs simple number formatting
 * ({@link String#format(String, Object...)}) using a user defined format string
 * on all double columns of its input table.
 *
 * @author 
 */

public class RandomVariableSettingsNodeModel extends NodeModel {
	
	protected final SettingsModelString currentModeSettings = createRVSSettingsModel();
	protected String getModeClass() {return currentModeSettings.getStringValue(); }
	
	static SettingsModelString createRVSSettingsModel() {
		return new SettingsModelString("RandomVariableSettingsNode_Mode", "");
	}
    
	 public static GenericDinosKnimeWorkflow[] arrayHelpers = new GenericDinosKnimeWorkflow[] {
		NominalDinosKnimeWorkflow.INSTANCE_NOMINAL,
		NumericDinosKnimeWorkflow.INSTANCE_NUMERIC,
		SurvivalDinosKnimeWorkflow.INSTANCE_SURVIVAL
	 };
	 
	 SettingsModelBoolean useDefault = createUseDefaultModel();
	public static SettingsModelBoolean createUseDefaultModel() {
		return new SettingsModelBoolean("useDefaultSettings", true);
	}
	
	SettingsModelBoolean overwrite = createOverwriteModel();
	public static SettingsModelBoolean createOverwriteModel() {
		return new SettingsModelBoolean("overwrite", true);
	}
 	 
	public static HashMap<String, GenericDinosKnimeWorkflow> mapIdentifierHelper = createMapIdentifierHelper();
	 
	private static HashMap<String, GenericDinosKnimeWorkflow> createMapIdentifierHelper() {
			HashMap<String, GenericDinosKnimeWorkflow> answer = new HashMap<String, GenericDinosKnimeWorkflow>();

			for(int count = 0 ; count < arrayHelpers.length ; ++ count) {
				var currentWorkflow = arrayHelpers[count];
				var auxiliarHelper = currentWorkflow.getModeHelper();
				answer.put( auxiliarHelper.identifier() , currentWorkflow);
			}
			
			return answer;
	}
	
	public static String[] arrayModesIdentifiers = createArrayModesIdentifiers();
	
	
	private static String[] createArrayModesIdentifiers() {
		
		String[] answer = new String[arrayHelpers.length];

		for(int count = 0 ; count < arrayHelpers.length ; ++ count) {
			var currentWorkflow = arrayHelpers[count];
			answer[count] = currentWorkflow.getModeHelper().identifier();
		}
		
		return answer;
	}
	
	 
	protected RandomVariableSettingsNodeModel() {

    	super(
    			new PortType[]{},
    			new PortType[]{
    					FlowVariablePortObject.TYPE}
        		);
	}


	
	/**
	 * 
	 * {@inheritDoc}
	 */
	@Override
	final protected PortObject[] execute(final PortObject[] inObjects, final ExecutionContext exec) throws Exception {
		
		String currentMode = getModeClass();
		
		var currentWorkflow = mapIdentifierHelper.getOrDefault(currentMode, null);
		
		Random random = new Random();
		
		if(currentWorkflow == null)
			throw new Exception("Tried to use the testing node RandomVariableSettings with a mode either non existing or not supported by this test:" + currentMode);
		
			// Components
		addComponents(exec, currentWorkflow, random);
		
			// Add settings
		addSettings(exec, currentWorkflow, random);
		
			// Use defaults or not
		var useDefaults = false;
		this.pushFlowVariable( currentWorkflow.KEY_DEFAULTCOMPONENTS , BooleanType.INSTANCE, useDefaults );
		
			// Trials
		var trialsToAdd = random.nextInt( 1 , currentWorkflow.DEFAULT_TRIALS);
		this.pushFlowVariable( currentWorkflow.KEY_TRIALS , IntType.INSTANCE, trialsToAdd );
		
			// Collect Iterations
		var collectToAdd = random.nextBoolean();
		this.pushFlowVariable( currentWorkflow.KEY_COLLECTITERATIONS , BooleanType.INSTANCE, collectToAdd );
		
			// Algorithm Seed
		var seedToAdd = random.nextLong();
		this.pushFlowVariable( currentWorkflow.KEY_SEED , LongType.INSTANCE, seedToAdd );
		this.pushFlowVariable( currentWorkflow.KEY_SEED + "_BOOL" , BooleanType.INSTANCE, true );
		
		return new PortObject[] {
				FlowVariablePortObject.INSTANCE
		};
	}


	private void addSettings(ExecutionContext exec, GenericDinosKnimeWorkflow currentWorkflow, Random random) {

		var overwriteMap = Map.of(
				"Number of Objectives" , 1d
				);
		
		boolean allowedToOverwrite = getAllowedToOverwrite();
		
		boolean useDefaults = getUseDefaults();
		
		var settings = currentWorkflow.combinedSettings;
		
		var iterator = settings.values().iterator();
		
		int originalSize = settings.size();
		int createSize = originalSize * 2;
		
		var answer = new String[ createSize ];
		var position = 0;
		
		while ( iterator.hasNext() ) {
			
			var next = iterator.next();
			var info = next._1;
			var name = info.name();
			var minimum = info.minimum();
			var maximum = info.maximum();
			Double valueToAdd = 0d;
			Double toOverWrite = overwriteMap.getOrDefault(name, null);
			
			if( allowedToOverwrite == true && toOverWrite != null ) {
				if( toOverWrite.isNaN() )
					valueToAdd = info.initial();
				else
					valueToAdd = toOverWrite;
			}
			else {
				var assign = 0d;
				if(useDefaults == true) 
					assign = info.initial();
				else
					assign = random.nextDouble(minimum, maximum);
				valueToAdd = assign;
			}
			
			answer[position] = name;
			position++;
			answer[position] = valueToAdd.toString();
			position++;
			
		}
	
		this.pushFlowVariable( currentWorkflow.KEY_SETTINGS , StringArrayType.INSTANCE, answer);
	}


	private boolean getUseDefaults() {
		return useDefault.getBooleanValue();
	}

	private boolean getAllowedToOverwrite() {
		return overwrite.getBooleanValue();
	}

	private void addComponents(final ExecutionContext exec, GenericDinosKnimeWorkflow currentWorkflow, Random random)
			throws CanceledExecutionException {
		var configurables = currentWorkflow.INFO_CONFIGURABLES;
		boolean objectivesFound = false;
		int length = configurables.length;
		
		for( int count = 0 ; count < length ; count++) {
			var currentInfo = configurables[count];
			String currentShortName = currentInfo._1();
			int currentClassType = (int) currentInfo._4();
			var combinedClasses = currentWorkflow.combinedClasses;
			var currentComponentClass = combinedClasses.get(currentShortName);
			int amountOfAvailableClasses = currentComponentClass.length;
			
			switch (currentClassType) {
				// Single selection, with a String Model
				// Choose one at random
			case 0: {
				int positionToAdd = random.nextInt(amountOfAvailableClasses);
				String valueToAdd = currentComponentClass[positionToAdd].shortName();
				this.pushFlowVariable(currentShortName, StringType.INSTANCE, valueToAdd);
				
				break;
			}
				// Multi selection, with a model of String Arrays
				// First, chose how many will be added, then generate the random indexes
			case 1, 2: {
				
				if(amountOfAvailableClasses == 1) {
					this.pushFlowVariable(currentShortName, StringArrayType.INSTANCE, new String[] {currentComponentClass[0].shortName()} );
				}
				else {
						// Edge Case for maximum objectivs
					var maxToCreate = 0;
					if(!objectivesFound && currentShortName.equals("objs"))
						maxToCreate = currentWorkflow.DEFAULT_OBJECTIVES_MAX + 1;
					else
						maxToCreate = amountOfAvailableClasses;
					
					int amountToChose = random.nextInt( 1 , maxToCreate );
					
					int[] arrayOfIndexes = generateDistinctRandomInts(0, amountOfAvailableClasses - 1, amountToChose);

					String[] forModel = new String[arrayOfIndexes.length];
					
					for( int count2 = 0 ; count2 < arrayOfIndexes.length ; ++count2 ) {
						forModel[count2] = currentComponentClass[ arrayOfIndexes[count2] ].shortName();
						exec.checkCanceled();
					}
					
					this.pushFlowVariable(currentShortName, StringArrayType.INSTANCE, forModel);
					
				}
						
				break;
			}
			default:
				throw new IllegalArgumentException("Unexpected type of class when using RandomVariableSettings: " + currentClassType);
			}
			
			exec.checkCanceled();
		}
	}
	
	public static int[] generateDistinctRandomInts(int min, int max, int howMany) {
        // Validate input parameters
        if (min > max) {
            throw new IllegalArgumentException("Minimum bound cannot be greater than maximum bound");
        }
        
        int rangeSize = max - min + 1;
        
        if (howMany < 0) {
            throw new IllegalArgumentException("How many cannot be negative");
        }
        
        if (howMany > rangeSize) {
            throw new IllegalArgumentException(
                String.format("Cannot generate %d distinct numbers in range [%d, %d] (only %d possible values)", 
                             howMany, min, max, rangeSize)
            );
        }
        
        // Create a list with all possible numbers in the range
        List<Integer> numbers = new ArrayList<>();
        for (int i = min; i <= max; i++) {
            numbers.add(i);
        }
        
        // Shuffle the list
        Collections.shuffle(numbers);
        
        // Take the first 'count' elements
        int[] result = new int[howMany];
        for (int i = 0; i < howMany; i++) {
            result[i] = numbers.get(i);
        }
        
        return result;
    }

	@Override
	protected DataTableSpec[] configure(DataTableSpec[] inSpecs) throws InvalidSettingsException {
		
		String currentValue = getModeClass();
		
		if( currentValue.isEmpty() ) {
			var setDefault = arrayHelpers[0].getModeHelper().identifier();
			currentModeSettings.setStringValue( setDefault );
			this.setWarningMessage( "Default chosen mode: " + setDefault );
		}

		return null;
	}
	
	@Override
	protected void loadInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void saveInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) {

		currentModeSettings.saveSettingsTo(settings);
		useDefault.saveSettingsTo(settings);
		overwrite.saveSettingsTo(settings);
	}

	@Override
	protected void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		currentModeSettings.validateSettings(settings);
		useDefault.validateSettings(settings);
		overwrite.validateSettings(settings);
	}

	@Override
	protected void loadValidatedSettingsFrom(NodeSettingsRO settings) throws InvalidSettingsException {
		currentModeSettings.loadSettingsFrom(settings);
		useDefault.loadSettingsFrom(settings);
		overwrite.loadSettingsFrom(settings);
	}

	@Override
	protected void reset() {
		// TODO Auto-generated method stub
		
	}

}

