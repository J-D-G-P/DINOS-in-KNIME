package cu.edu.cujae.daf.knime.nodes.testing;

import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import cu.edu.cujae.daf.knime.dialogcomponents.DialogComponentStringSelectionReferenced;

/**
 * This is an example implementation of the node dialog of the
 * "RandomVariableSettings" node.
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more
 * complex dialog please derive directly from
 * {@link org.knime.core.node.NodeDialogPane}. In general, one can create an
 * arbitrary complex dialog using Java Swing.
 * 
 * @author 
 */


	

public class RandomVariableSettingsNodeDialog extends DefaultNodeSettingsPane {

	final DialogComponentStringSelection selectionBox;
	
	final DialogComponentBoolean useDefaultCheck;
	
	final DialogComponentBoolean overwriteCheck;

	
    protected RandomVariableSettingsNodeDialog() {
        super();
        
        	// mode select
		SettingsModelString modelSelect = RandomVariableSettingsNodeModel.createRVSSettingsModel();

		selectionBox = new DialogComponentStringSelection(modelSelect, "Mode", RandomVariableSettingsNodeModel.arrayModesIdentifiers);

		addDialogComponent(selectionBox);
		
			// Use default or completely random settings
		SettingsModelBoolean useDefault = RandomVariableSettingsNodeModel.createUseDefaultModel();
		
		useDefaultCheck = new DialogComponentBoolean(useDefault, "Use default settings (false to randomize)");
				
		addDialogComponent(useDefaultCheck);
		
		
			// Overwrite some values or not
		SettingsModelBoolean overwrite = RandomVariableSettingsNodeModel.createOverwriteModel();
		
		overwriteCheck = new DialogComponentBoolean(overwrite, "Overwrite values (set objective minimum to 1)");
		
		addDialogComponent(overwriteCheck);

    }
    
	@Override
	public void saveAdditionalSettingsTo(final NodeSettingsWO settings) {
		int hello = 45;
	}
	
}

