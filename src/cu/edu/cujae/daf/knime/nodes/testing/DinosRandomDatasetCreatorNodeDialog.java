package cu.edu.cujae.daf.knime.nodes.testing;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelInteger;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * This is an example implementation of the node dialog of the
 * "DinosRandomDatasetCreator" node.
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more
 * complex dialog please derive directly from
 * {@link org.knime.core.node.NodeDialogPane}. In general, one can create an
 * arbitrary complex dialog using Java Swing.
 * 
 * @author 
 */
public class DinosRandomDatasetCreatorNodeDialog extends DefaultNodeSettingsPane {

	/**
	 * New dialog pane for configuring the node. The dialog created here
	 * will show up when double clicking on a node in KNIME Analytics Platform.
	 */
    protected DinosRandomDatasetCreatorNodeDialog() {
        super();
        
        addAmountOfRows(this);
        
        addTargetType(this);
        
        addMissingValuesAttribute(this);
        
        addMissingValuesClass(this);

    }
    
    private void addAmountOfRows(DinosRandomDatasetCreatorNodeDialog pane) {
	    	// Amount of rows
		SettingsModelInteger intSettings
			= DinosRandomDatasetCreatorNodeModel.createAmountSettingsModel();
	
		var component
			= new DialogComponentNumber(intSettings, "Amount of Rows To Generate", 1);
		
		pane.addDialogComponent( component );
    }
    
    private void addMissingValuesAttribute(DinosRandomDatasetCreatorNodeDialog pane) {
			// Missing values in attributes probability
		SettingsModelDoubleBounded attributeMissingModel
			= DinosRandomDatasetCreatorNodeModel.createAttributeMissingProbModel();
		
		var attributeMissingProb
			= new DialogComponentNumber(attributeMissingModel, "Probability of Missing Values in Attributes", 0.1);
		
		pane.addDialogComponent( attributeMissingProb );
    }
    
    private void addMissingValuesClass(DinosRandomDatasetCreatorNodeDialog pane) {
			// Missing values in attributes probability
		SettingsModelDoubleBounded classMissingModel
			= DinosRandomDatasetCreatorNodeModel.createclassMissingProbModel();
		
		var classMissingProb
			= new DialogComponentNumber(classMissingModel, "Probability of Missing Values in Classes", 0.1);
		
		pane.addDialogComponent( classMissingProb );
    }
    
    private void addTargetType(DinosRandomDatasetCreatorNodeDialog pane) {
			// Target type
		SettingsModelString targetMode = DinosRandomDatasetCreatorNodeModel.createTypeOfTargetModel();
		
		var targetsComponent = new DialogComponentStringSelection(targetMode, "Type of target to append", DinosRandomDatasetCreatorNodeModel.supportedTargets );
		
		addDialogComponent( targetsComponent );
    }
}

