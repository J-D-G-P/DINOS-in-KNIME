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
import cu.edu.cujae.daf.knime.nodes.GenericDinosKnimeWorkflow.NODES_TYPES;


/**
 * Default configuration logic for survival subgroup discovery,
 * AKA a numeric time to to event as target with censoring information
 * 
TODO 
 * 
 * @author Jonathan David González Pereda, CUJAE
 */
public class DinosSurvivalSubgroupDiscoveryNodeModel extends GenericDinosKnimeModel {

	@Override
	protected GenericDinosKnimeWorkflow getInstance() { return SurvivalDinosKnimeWorkflow.INSTANCE_SURVIVAL; }
	
	@Override
	protected NODES_TYPES getMode() { return NODES_TYPES.DISCOVERY;	}
	
	protected SurvivalDinosKnimeWorkflow survivalWorkflow =  (SurvivalDinosKnimeWorkflow) SurvivalDinosKnimeWorkflow.INSTANCE_SURVIVAL;

	private final SettingsModelString censor_column = survivalWorkflow.createCensorColumnModel();
	
	private final SettingsModelString censor_indication = survivalWorkflow.createCensorIndicationModel();
	
	protected DinosSurvivalSubgroupDiscoveryNodeModel() {
	   	super();
	}
	 
		// Constructor for default value
	protected DinosSurvivalSubgroupDiscoveryNodeModel(final PortType[] inPortTypes, final PortType[] outPortTypes) {
	 	super(inPortTypes , outPortTypes);
	}

	
	@Override
	protected String[] getCensorInfo() {
		return new String[]{ 
				super.target_class.getStringValue() ,
				censor_column.getStringValue() ,
				censor_indication.getStringValue()};
		}

	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
	        throws InvalidSettingsException {

		DataTableSpec[] result = super.configure(inSpecs);
		
        checkSpecCensorColumn(inSpecs);
        
        checkCensorIndicator(inSpecs);
        
        return result;
	}


	private void checkSpecCensorColumn(DataTableSpec[] inSpecs) throws InvalidSettingsException {
		DataTableSpec[] input = super.configure(inSpecs);
        DataTableSpec inSpec = (DataTableSpec)inSpecs[workflow.PORT_INPUT_DATASET];
        Class<? extends DataValue>[] types = survivalWorkflow.getCensorTargetTypes();
		// check spec with selected column
        String censorColumn = censor_column.getStringValue();
        String targetColumn = target_class.getStringValue();
        DataColumnSpec columnSpec = inSpec.getColumnSpec(censorColumn);
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
        
        if (censorColumn != null && (!isValid || !tryToFind) ) {
            throw new InvalidSettingsException( survivalWorkflow.MESSAGE_CENSORCLASSNOTFOUND + censorColumn);
        }
        
        if (censorColumn == null) { // auto-guessing, adapted from TreeLearner
            assert !isValid : workflow.MESSAGE_NOCLASSVALIDCONFIG;
            // if no useful column is selected guess one
            // get the first useful one starting at the end of the table
            for (int countColumn = inSpec.getNumColumns() - 1; countColumn >= 0; countColumn--) {
            	
            	for(int countType = 0 ; countType < types.length ; ++countType) {
            		Class<? extends DataValue> currentType = types[countType];
            			// Set as default the last column available of the supported type as in not the same as target
            		DataColumnSpec specsToCheck = inSpec.getColumnSpec(countColumn);
                    if (specsToCheck.getType().isCompatible(currentType) && !( targetColumn.equals(censorColumn) ) ) {
                    	censorColumn =  inSpec.getColumnSpec(countColumn).getName();
                    	censor_column.setStringValue(
                               censorColumn);
                    	this.addWarning(  survivalWorkflow.MESSAGE_GUESSING_CENSOR + censor_column.getStringValue() );
                        	// The break only work on this inner cycle, so also stop the enclosing one by directly changing the counter
                        countColumn = -1;
                        break;
                    }
                    
            	}
       
            }
            	// If no supported column was found, complain
            if (censor_column.getStringValue() == null) {
                throw new InvalidSettingsException(workflow.MESSAGE_NOCLASS);
            }
        }
		
	}
	
	private void checkCensorIndicator(DataTableSpec[] inSpecs) {
		
        DataTableSpec tableSpec = (DataTableSpec)inSpecs[workflow.PORT_INPUT_DATASET];
		String censorColumn = this.censor_column.getStringValue();
        DataColumnSpec columnSpec = tableSpec.getColumnSpec(censorColumn);
		
    	// Auto guess censor indicator depending on column type
	    if(censor_indication.getStringValue() == null ) {
	    	DataColumnSpec censorSpec = tableSpec.getColumnSpec( censorColumn );
			DataType censorType = columnSpec.getType();
			String identifier = censorType.getIdentifier();
			DataColumnDomain censorDomain = censorSpec.getDomain();;
			
			if ( identifier.equals("org.knime.core.data.def.IntCell") )
				{	censor_indication.setStringValue( "" +
						( (IntCell) censorDomain.getLowerBound() ).getIntValue()
						 );	}
			else if ( identifier.equals("org.knime.core.data.def.BooleanCell") )
				{	censor_indication.setStringValue("FALSE");
					
				}
			else if ( identifier.equals("org.knime.core.data.def.StringCell") )
				{
					censor_indication.setStringValue( "" +
							( (StringCell) censorDomain.getValues().iterator().next() ).getStringValue()
				);	}
			
			this.addWarning( survivalWorkflow.MESSAGE_GUESSING_CENSORIDENT + censor_indication.getStringValue());
	    }
	
		
	}
	
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
    	
    	super.saveSettingsTo(settings);
    	
    	censor_column.saveSettingsTo(settings);
    	
    	censor_indication.saveSettingsTo(settings);
    }
    
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
    	
    	super.loadValidatedSettingsFrom(settings);
    	
    	censor_column.loadSettingsFrom(settings);
    	
    	censor_indication.loadSettingsFrom(settings);
    }
    
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
    
    	super.validateSettings(settings);
    	
    	censor_column.validateSettings(settings);
    	
    	censor_indication.validateSettings(settings);
    }
}