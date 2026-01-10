package cu.edu.cujae.daf.knime.dialogcomponents;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.border.Border;

import org.eclipse.ui.internal.handlers.WizardHandler.New;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataValue;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DialogComponent;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnName;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.defaultnodesettings.SettingsModelStringArray;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.util.ColumnFilter;
import org.knime.core.node.util.ColumnSelectionPanel;

/**
 * Custom visual KNIME component, TODO
 * 
 * @author Jonathan David González Pereda, CUJAE
 */

public class DialogComponentCensoringInfo extends DialogComponent implements ReseatableDialogComponent {

	public DialogComponentCensoringInfo(
			SettingsModelString targetColumn,
			String targetLabel,
			Class<? extends DataValue>[] targetTypes,
			SettingsModelString censorColumn,
			String censorLabel,
			Class<? extends DataValue>[] censorTypes,
			SettingsModelString censorValue,
			String valueLabel,
			final int specIndex) {
		super( new EmptySettingsModel() );
		
		JPanel masterPanel = super.getComponentPanel();
		DialogComponentColumnNameSelection dialogComponentColumnNameSelection = new DialogComponentColumnNameSelection(
				targetColumn,
				targetLabel,
				specIndex,
				true,
				true,
				targetTypes);
		
		masterPanel.add( dialogComponentColumnNameSelection.getComponentPanel() );

}
		

	@Override
	public void resetToDefault() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void updateComponent() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void validateSettingsBeforeSave() throws InvalidSettingsException {
		//throw new InvalidSettingsException( name + " expected at least " + minimumSelected + " option(s), but " + enabledReferences.size() + " where chosen");
		
	}

	@Override
	protected void checkConfigurabilityBeforeLoad(PortObjectSpec[] specs) throws NotConfigurableException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void setEnabledComponents(boolean enabled) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setToolTipText(String text) {
		// TODO Auto-generated method stub
		
	}


}
