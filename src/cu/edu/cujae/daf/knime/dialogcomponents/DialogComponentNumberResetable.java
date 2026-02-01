package cu.edu.cujae.daf.knime.dialogcomponents;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import org.knime.core.node.FlowVariableModel;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelNumber;

	/**
	 * This class extends from DialogComponentNumber, a
	 * class that provides a spinner for chosing a value
	 * 
	 * This class adds the option of a button to reset
	 * the spinner to a value defined as default
	 * 
	 * Note: It is arbitrary i could extend from this
	 * class but not from DialogComponentCheckBoxGroupReferenced
	 * or DialogComponentStringSelection.
	 */

public class DialogComponentNumberResetable extends DialogComponentNumber implements ReseatableDialogComponent {

		// Additional button
	private final JButton m_button;
	
		// The value to reset the spinner when "m_button" is clicked
	private final Number resetValue;
	
	public DialogComponentNumberResetable(SettingsModelNumber numberModel, Number resetValue, String label, Number stepSize, int compWidth,
			FlowVariableModel fvm, boolean showWarning, String customErrorMsg) {
			// Call the SUPER constructor
		super(numberModel, label, stepSize, compWidth, fvm, showWarning, customErrorMsg);
		
			// Can be any value, deosn't need to validate
		this.resetValue = resetValue;

		m_button = new JButton("Reset");
		m_button.addActionListener( new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				resetToDefault(); }	} );
		
			// And finally just add it
		getComponentPanel().add( m_button );
	}
	
		/**
		 * Resets the component's spinner to the
		 * value previously specified in the constructor
		 */
	public void resetToDefault() {
		getSpinner().setValue( resetValue );
		updateComponent();
	}

}
