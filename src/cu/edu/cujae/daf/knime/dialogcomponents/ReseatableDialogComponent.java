package cu.edu.cujae.daf.knime.dialogcomponents;

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
}
