package cu.edu.cujae.daf.knime.nodes.survival;

import org.knime.core.data.DataColumnDomain;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.DataValue;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortType;

import cu.edu.cujae.daf.knime.nodes.GenericDinosKnimeModel;
import cu.edu.cujae.daf.knime.nodes.GenericDinosKnimeWorkflow;
import cu.edu.cujae.daf.knime.nodes.GenericDinosKnimeWorkflow.DINOS_NODE;


/**
 * Default execution logic for survival subgroup discovery,
 * AKA a numeric time to event as target with censoring information
 * 
 * Have extra logic to handle the necessities of survival analysis.
 * 
 * @author Jonathan David González Pereda, CUJAE
 */

public class DinosSurvivalSubgroupDiscoveryNodeModel extends GenericDinosKnimeModel {

	@Override
	protected GenericDinosKnimeWorkflow getInstance() { return SurvivalDinosKnimeWorkflow.INSTANCE_SURVIVAL; }
	
	@Override
	protected DINOS_NODE getMode() { return DINOS_NODE.DISCOVERY;	}
	
		/** Store this copy as the actual extended class of {@link SurvivalDinosKnimeWorkflow}, not just {@link GenericDinosKnimeWorkflow}e */
	protected SurvivalDinosKnimeWorkflow survivalWorkflow =  (SurvivalDinosKnimeWorkflow) SurvivalDinosKnimeWorkflow.INSTANCE_SURVIVAL;

		/** Storing the column with the censor indicator */
	private final SettingsModelString censor_column = survivalWorkflow.createCensorColumnModel();
	
		/** Store what's to use as censor indicator */
	private final SettingsModelString censor_indication = survivalWorkflow.createCensorIndicationModel();
	
	protected DinosSurvivalSubgroupDiscoveryNodeModel() {
	   	super();
	}
	 
		// Constructor for default value
	protected DinosSurvivalSubgroupDiscoveryNodeModel(final PortType[] inPortTypes, final PortType[] outPortTypes) {
	 	super(inPortTypes , outPortTypes);
	}

	
	@Override
	/** {@inheritDoc} */
	protected String[] getCensorInfo() {
		return new String[]{ 
				super.target_class.getStringValue() ,
				censor_column.getStringValue() ,
				censor_indication.getStringValue()};
		}

	@Override
	/** 
	 * {@inheritDoc}
	 * 
	 * Also, for checking censor column and indicator
	 */
	protected void targetConfigure(DataTableSpec[] inSpecs) throws InvalidSettingsException {
		
			// First, check the target value as in other modes
		tryToFindConfiguredColumn(target_class, inSpecs[0], workflow.getAceptedTargetTypes(), workflow.EXCEPTION_CONFIGNOTFOUND_CLASS , workflow.WARNING_GUESSING_TARGET );

			// Check the Censor column, which cannot be
			// the same as the target
        checkSpecCensorColumn(inSpecs);
        
        	// Depending on the type of the censor column,
        	// get a value for it
        checkCensorIndicator(inSpecs);
	}

		/**
		 * Validations for the censoring columns.
		 * 
		 * If it has previously been defined, check if it can be found again.
		 * If not, try to guess a value for it among suitable columns
		 * 
		 * @param inSpecs Specifications of table to validate
		 * @throws InvalidSettingsException
		 */
	private void checkSpecCensorColumn(DataTableSpec[] inSpecs) throws InvalidSettingsException {
        DataTableSpec inSpec = (DataTableSpec)inSpecs[workflow.PORT_INPUT_DATASET];
        Class<? extends DataValue>[] types = survivalWorkflow.getCensorTargetTypes();
		// check spec with selected column
        String censorColumn = censor_column.getStringValue();
        String targetColumn = target_class.getStringValue();
        DataColumnSpec columnSpec = inSpec.getColumnSpec(censorColumn);
        boolean isValid = columnSpec != null;
        boolean tryToFind = false;
        
        	// If a censor column has been defined, see if it can be found again
        if(isValid) {
	        for(int countType = 0 ; countType < types.length ; ++countType) {
	    		Class<? extends DataValue> currentType = types[countType];
	    	
	    		if (columnSpec.getType().isCompatible(currentType) ) {
	    			tryToFind = true;
	    			break;
	    		}
	        }
        }
        
        	// If it was defined but not found, complain
        if (censorColumn != null && (!isValid || !tryToFind) ) {
            throw new InvalidSettingsException( survivalWorkflow.EXCEPTION_CONFIGNOTFOUND_CENSOR + censorColumn);
        }
        
        if (censorColumn == null) { // auto-guessing, adapted from TreeLearner
            assert !isValid : workflow.EXCEPTION_NOCLASS_VALIDCONFIG;
            // if no useful column is selected guess one
            // get the first useful one starting at the end of the table
            for (int countColumn = inSpec.getNumColumns() - 1; countColumn >= 0; countColumn--) {
            	
            	for(int countType = 0 ; countType < types.length ; ++countType) {
            		Class<? extends DataValue> currentType = types[countType];
            			// Set as default the last column available of the supported type,
            			// as long as it is not the same as the target
            		DataColumnSpec specsToCheck = inSpec.getColumnSpec(countColumn);
            		boolean compatibleSpec = (specsToCheck.getType().isCompatible(currentType) );
            		boolean differentClassCensor = !targetColumn.equals( specsToCheck.getName() );
                    if ( compatibleSpec && (differentClassCensor) ) {
                    	censorColumn =  inSpec.getColumnSpec(countColumn).getName();
                    	censor_column.setStringValue(
                               censorColumn);
                    	this.addWarning(  survivalWorkflow.WARNING_GUESSING_CENSOR + censor_column.getStringValue() );
                        	// The break only work on this inner cycle,
                    		// so also stop the outer one by directly changing the counter
                        countColumn = -1;
                        break;
                    }
            	}
            }
            	// If no supported column was found, complain
            if (censor_column.getStringValue() == null) {
                throw new InvalidSettingsException(workflow.EXCEPTION_NOCLASS);
            }
        }
		
	}
	
		/**
		 * Validations for the censoring indicator,
		 * 
		 * @param inSpecs
		 * @throws InvalidSettingsException 
		 */
	private void checkCensorIndicator(DataTableSpec[] inSpecs) throws InvalidSettingsException {
		
			// We need these
        DataTableSpec tableSpec = (DataTableSpec)inSpecs[workflow.PORT_INPUT_DATASET];
		String censorColumn = this.censor_column.getStringValue();
        DataColumnSpec columnSpec = tableSpec.getColumnSpec(censorColumn);
		
    		// Auto guess censor indicator depending on column type
	    if(censor_indication.getStringValue() == null ) {
	    	DataColumnSpec censorSpec = tableSpec.getColumnSpec( censorColumn );
			DataType censorType = columnSpec.getType();
			String identifier = censorType.getIdentifier();
			DataColumnDomain censorDomain = censorSpec.getDomain();
			
				// Integer
			if ( identifier.equals(workflow.ID_INT) )
				{	censor_indication.setStringValue( "" +
						( (IntCell) censorDomain.getLowerBound() ).getIntValue()
						 );	}
			
				// Boolean
			else if ( identifier.equals(workflow.ID_BOOL) )
				{	censor_indication.setStringValue("FALSE");}
			
				// String
			else if ( identifier.equals(workflow.ID_STRING) )
				{
					censor_indication.setStringValue( "" +
							( (StringCell) censorDomain.getValues().iterator().next() ).getStringValue()
				);	}
			else {
				throw new InvalidSettingsException("Column chosen for censor indicator (" + censorColumn + ") not of a valid type");
			}
			
				// And finally complain
			this.addWarning( survivalWorkflow.WARNING_GUESSING_CENSORIDENT + censor_indication.getStringValue());
	    }
	    else {
	    	// TODO if indicator is set, validate if it is among the values of the chosen column
	    }
	
		
	}
	
    @Override
    /** {@inheritDoc} */
    protected void saveSettingsTo(final NodeSettingsWO settings) {
    	
    		// Save everything else the parent class needs and the censoring data
    	super.saveSettingsTo(settings);
    	
    	censor_column.saveSettingsTo(settings);
    	
    	censor_indication.saveSettingsTo(settings);
    }
    
    @Override
    /** {@inheritDoc} */
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
    	
			// Load everything else the parent class needs and the censoring data
    	super.loadValidatedSettingsFrom(settings);
    	
    	censor_column.loadSettingsFrom(settings);
    	
    	censor_indication.loadSettingsFrom(settings);
    }
    
    @Override
    /** {@inheritDoc} */
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
    
			// Validate everything else the parent class needs and the censoring data
    	super.validateSettings(settings);
    	
    	censor_column.validateSettings(settings);
    	
    	censor_indication.validateSettings(settings);
    }
}