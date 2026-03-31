package cu.edu.cujae.daf.knime.nodes.survival;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.SettingsModelFilterString;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import cu.edu.cujae.daf.knime.nodes.GenericDinosKnimeDialog;
import cu.edu.cujae.daf.knime.nodes.GenericDinosKnimeWorkflow;
import cu.edu.cujae.daf.knime.nodes.GenericDinosKnimeWorkflow.DINOS_NODE;
import cu.edu.cujae.daf.knime.nodes.KnimeTableToDinosDataset;

/**
 * Configuration interface for survival subgroup discovery in {@link DinosSurvivalSubgroupDiscoveryNodeModel},
 * AKA a numeric time to to event as target with censoring information
 *
 * This also adds in the general tab:
 * - Select the censoring indicator column
 * - A value for the censoring indicator of said column
 * - Sanity checks to make sure target and censoring columns are different
 * 
 * TODO Currently, the check to see if the censoring value is in
 * the list of values of the actual column is in {@link KnimeTableToDinosDataset}
 * when creating datasets, since {@link DefaultNodeSettingsPane#saveAdditionalSettingsTo(NodeSettingsWO)}
 * doesn't receive the column specs, is there a workaround to this?
 * 
 * @author Jonathan David González Pereda, CUJAE
 */

public class DinosSurvivalSubgroupDiscoveryNodeDialog extends GenericDinosKnimeDialog {

		/** Storing which column to use as the censor indicator */
	protected DialogComponentColumnNameSelection censorCol;
	
		/** Storing which value of the censor column to use as censor indicator */
	protected DialogComponentString censorIndic;


	@Override
	/** {@inheritDoc} */
	protected GenericDinosKnimeWorkflow getInstance() { return SurvivalDinosKnimeWorkflow.INSTANCE_SURVIVAL; }
	
		/** Store this copy as the actual extended class of {@link SurvivalDinosKnimeWorkflow}, not just {@link GenericDinosKnimeWorkflow} */
	protected SurvivalDinosKnimeWorkflow survivalWorkflow =  SurvivalDinosKnimeWorkflow.INSTANCE_SURVIVAL;
	
	@Override
	/** {@inheritDoc} */
	protected DINOS_NODE getMode() { return DINOS_NODE.DISCOVERY; }
	
	@Override
	/** {@inheritDoc} */
	protected void addClassToGeneralTab(GenericDinosKnimeDialog panel) {
		
			// Add the actual target column indicator
		super.addClassToGeneralTab(panel);
		
			// TODO This code repeat was because somehow using the survivalWorkflow field returned a null value, see what can be done
		SurvivalDinosKnimeWorkflow survivalWorkflow =  (SurvivalDinosKnimeWorkflow) SurvivalDinosKnimeWorkflow.INSTANCE_SURVIVAL;
		
			// Add the selection box with the censoring column
        censorCol = new DialogComponentColumnNameSelection(
        		survivalWorkflow.createCensorColumnModel(),
                workflow.NAME_CENSORCOL,
                workflow.PORT_INPUT_DATASET,
                survivalWorkflow.getCensorTargetTypes() );
        panel.addDialogComponent(censorCol);

        	// Add the text field to input the censoring value
        censorIndic = new DialogComponentString(
        		survivalWorkflow.createCensorIndicationModel(),
        		survivalWorkflow.NAME_CENSORIND );
        panel.addDialogComponent(censorIndic);

	}
	
    @Override
    	/**
    	 *  In survival mode, this check sthat target and censoring columns are not the same,
    	 *  and that the indicator field is not empty.
    	 *  
    	 *  In survival discovery it also checks that the list of ficed values
    	 *  contains neither the target nor censoring indicator
    	 */
    public void saveAdditionalSettingsTo(final NodeSettingsWO settings)
            throws InvalidSettingsException {
        assert settings != null;
        
        	// We will need the values of the models
		SettingsModelString classColModel = (SettingsModelString) classCol.getModel();
		SettingsModelString censorColModel = (SettingsModelString) censorCol.getModel();
		String censorColString = censorColModel.getStringValue();
		String censorIndicString = ( (SettingsModelString) censorIndic.getModel() ).getStringValue();
		DINOS_NODE type = getMode();
		
			// Censor column and target column cannot be the same
		if( censorColString.equals( classColModel.getStringValue() ) ) {
			throw new InvalidSettingsException(survivalWorkflow.EXCEPTION_SAMECOLUMN + " (" + censorColString  + ")");
		}
		
			// Censor indicator cannot be empty
		if( censorIndicString.isEmpty() ) {
			throw new InvalidSettingsException( survivalWorkflow.EXCEPTION_EMPTYINDICATOR);
		}
		
			// Also, the fixed column cannot be the same as the censoring column
        if (type == DINOS_NODE.DISCOVERY) {
        	SettingsModelFilterString filterModel = (SettingsModelFilterString) fixedVariables.getModel();
        	var filter = filterModel.getExcludeList();
        	SettingsModelString classNameModel = (SettingsModelString) censorCol.getModel();
        	String className = classNameModel.getStringValue();
        		if( filter.contains( className ) ) {
        			throw new InvalidSettingsException(workflow.EXCEPTION_FIXED_EQUALSCENSOR + " (" + classNameModel + ")");
        		}
        }

        	// If everything is right, call this
        super.saveAdditionalSettingsTo(settings);
    }
	
}

