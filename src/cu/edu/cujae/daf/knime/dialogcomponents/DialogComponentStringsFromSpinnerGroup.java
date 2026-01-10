package cu.edu.cujae.daf.knime.dialogcomponents;

import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DialogComponent;
import org.knime.core.node.defaultnodesettings.SettingsModelStringArray;
import org.knime.core.node.port.PortObjectSpec;

/**
 * Custom visual KNIME component, this consist of several spinners
 * all saving to the same model
 * 
 * Receives a string arrya, and takes as labels of the spinners
 * the even positions and as default double value the parseable
 * odd position
 * 
 * 
 * @author Jonathan David González Pereda, CUJAE
 */

public class DialogComponentStringsFromSpinnerGroup extends DialogComponent {

		@SuppressWarnings("unused")
	private JSpinner[] spinners;
	
	private String[] array;

	public DialogComponentStringsFromSpinnerGroup(SettingsModelStringArray model) {

		super(model);

		array = model.getStringArrayValue();
		if (array.length % 2 != 0)
			throw new IllegalArgumentException("Cannot create a DialogComponentStringsFromSpinnerGroup with an odd amount of elements");
		spinners = new JSpinner[ array.length / 2 ];
		
		createSpinners(array);
		
	}
	
		/**
		 * Given the array of strings, add the spinners
		 * and labels for the components
		 * 
		 * @param array Use even positions as labels and odd ones as spinner values (must be parseable strings)
		 */
	private void createSpinners(String[] array) {
			// First clear the panel, then add components as a "column"
		JPanel masterPanel = getComponentPanel();
		masterPanel.removeAll();
		masterPanel.setLayout( ( new BoxLayout(masterPanel, BoxLayout.Y_AXIS )) );
		
			// Acess positions of array
		int whereToGet = 0;

		while (whereToGet < array.length) {
			
				// This new panel will work as a "row inside the column"
			JPanel toAddPanel = new JPanel();
			toAddPanel.setLayout( new FlowLayout() );
			
				// Add the label
			toAddPanel.add( new JLabel( array[whereToGet] ) );
			++whereToGet;
			
				// Add the value
			final int valueForSpinner = whereToGet;
			String nextValue = array[whereToGet];

				// Create spinner depending if the value has been defined or not
			final JSpinner toAddSpinner =
			 nextValue == null ?
				new JSpinner( new SpinnerNumberModel( null , Double.MIN_VALUE, Double.MAX_VALUE, 0.5) )
			:
				new JSpinner( new SpinnerNumberModel( Double.parseDouble(nextValue) , Double.MIN_VALUE, Double.MAX_VALUE, 0.5) ) ;

				// This will make sure the values are updated on spinner change
			toAddSpinner.addChangeListener( new ChangeListener() {
				@Override public void stateChanged(ChangeEvent e) {
					updateModel(valueForSpinner, toAddSpinner.getValue().toString() ); } });
			
				// Necessary to set this directly due to spinner's tendency for taking all space available no matter the model
			toAddSpinner.setPreferredSize(new Dimension(100, 20));
						
			//spinners[whereToSpinners] = toAddSpinner;
			
				// Add the spinner to the "row"
			toAddPanel.add(toAddSpinner);
			++whereToGet;
			
				// Add the "row" to the column
			masterPanel.add( toAddPanel	 );
		}
	}

		/**
		 * Save the references of the selected checkboxes to the
		 * settings
		 */

	protected void updateModel(int position, String text) {
		
		array[position] = text;
		SettingsModelStringArray model = ((SettingsModelStringArray)getModel());
		model.setStringArrayValue(array);

	}

		/**
		 * {@inheritDoc}
		 */
	@Override
	protected void updateComponent() {
		SettingsModelStringArray model = (SettingsModelStringArray) getModel() ;
		createSpinners( model.getStringArrayValue() );
	}

	@Override
	protected void validateSettingsBeforeSave() throws InvalidSettingsException {
	}

	@Override
	protected void checkConfigurabilityBeforeLoad(PortObjectSpec[] specs) throws NotConfigurableException {
		// We are always good.
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
