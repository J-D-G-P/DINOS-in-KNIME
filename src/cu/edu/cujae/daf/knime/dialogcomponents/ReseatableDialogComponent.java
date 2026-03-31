package cu.edu.cujae.daf.knime.dialogcomponents;

import org.knime.core.node.defaultnodesettings.SettingsModel;

/**
	 * Custom interface for declaring that a visual component
	 * can be reset to a default value
	 * 
	 * @author Jonathan David González Pereda, CUJAE
	 */


public interface ReseatableDialogComponent{

		/**
		 * Set the component's visual state and settings
		 * to a previously specified default value
		 */
	public void resetToDefault();
	
		/**
		 * 
		 * Set if the user can interact with the component.
		 * Necessary since disabling the source JPanel doesn't
		 * work, but also because "setEnableComponents" is
		 * protected.
		 * 
		 * @param value True to enable, False to Disable
		 */
	public void setEnabledOrDisabledComponents(boolean value);
	
	public SettingsModel getModel();
}
