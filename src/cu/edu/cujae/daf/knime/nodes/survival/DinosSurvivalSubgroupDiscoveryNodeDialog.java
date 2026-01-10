package cu.edu.cujae.daf.knime.nodes.survival;

import javax.swing.JOptionPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponent;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import cu.edu.cujae.daf.knime.dialogcomponents.DialogComponentCensoringInfo;
import cu.edu.cujae.daf.knime.nodes.GenericDinosKnimeDialog;
import cu.edu.cujae.daf.knime.nodes.GenericDinosKnimeWorkflow;
import cu.edu.cujae.daf.knime.nodes.numeric.NumericDinosKnimeWorkflow;

/**
 * Configuration interface for survival subgroup discovery,
 * AKA a numeric time to to event as target with censoring information
 *
// TODO description
 * 
 * @author Jonathan David González Pereda, CUJAE
 */

public class DinosSurvivalSubgroupDiscoveryNodeDialog extends GenericDinosKnimeDialog {

	DialogComponentColumnNameSelection censorCol;
	DialogComponentString censorIndic;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected GenericDinosKnimeWorkflow getInstance() { return SurvivalDinosKnimeWorkflow.INSTANCE_SURVIVAL; }
	
	protected SurvivalDinosKnimeWorkflow survivalWorkflow =  SurvivalDinosKnimeWorkflow.INSTANCE_SURVIVAL;
	
	@Override
	protected void addClassToGeneralTab(GenericDinosKnimeDialog panel) {
		
		super.addClassToGeneralTab(panel);
		
		SurvivalDinosKnimeWorkflow survivalWorkflow =  (SurvivalDinosKnimeWorkflow) SurvivalDinosKnimeWorkflow.INSTANCE_SURVIVAL;
		
        censorCol = new DialogComponentColumnNameSelection(
        		survivalWorkflow.createCensorColumnModel(),
                workflow.NAME_CENSORCOL,
                workflow.PORT_INPUT_DATASET,
                survivalWorkflow.getCensorTargetTypes() );
        panel.addDialogComponent(censorCol);

        censorIndic = new DialogComponentString(
        		survivalWorkflow.createCensorIndicationModel(),
        		survivalWorkflow.NAME_CENSORIND );
        panel.addDialogComponent(censorIndic);

	}
	
    @Override
    public void saveAdditionalSettingsTo(final NodeSettingsWO settings)
            throws InvalidSettingsException {
        assert settings != null;
        
		SettingsModelString classColModel = (SettingsModelString) classCol.getModel();
		SettingsModelString censorColModel = (SettingsModelString) censorCol.getModel();
		
		if( censorColModel.getStringValue().equals( classColModel.getStringValue() ) ) {
			throw new InvalidSettingsException(survivalWorkflow.MESSAGE_SAMECOLUMN);
		}
		
		String censorIndicString = ( (SettingsModelString) censorIndic.getModel() ).getStringValue();

		if( censorIndicString.isEmpty() ) {
			throw new InvalidSettingsException( survivalWorkflow.MESSAGE_EMPTYINDICATOR );
		}

        super.saveAdditionalSettingsTo(settings);
    }
 

 
	
}

