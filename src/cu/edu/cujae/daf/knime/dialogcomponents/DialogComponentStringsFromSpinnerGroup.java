package cu.edu.cujae.daf.knime.dialogcomponents;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JButton;
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

import cu.edu.cujae.daf.core.HyperparamInfo;
import cu.edu.cujae.daf.core.HyperparamInfoInteger;
import scala.Tuple2;
import scala.Tuple4;
import scala.Tuple5;

/**
 * Custom visual KNIME component, this consist of several spinners
 * all saving to the same model
 * 
 * Receives a string array, and takes as labels of the spinners
 * the even positions and as default double value the parseable
 * odd position
 * 
 * @author Jonathan David González Pereda, CUJAE
 */

public class DialogComponentStringsFromSpinnerGroup extends DialogComponent implements ReseatableDialogComponent {

		@SuppressWarnings("unused")
	private JSpinner[] spinners;
		
	private JButton[] resetBtns;
	
	private String[] array;
	
	private double[] initialValues;
	
	private JButton generalReset;
	
	private final LinkedHashMap<String,Tuple2<HyperparamInfo,HashSet<String>>> combinedClasses;

	public DialogComponentStringsFromSpinnerGroup(SettingsModelStringArray model, LinkedHashMap<String,Tuple2<HyperparamInfo,HashSet<String>>> combinedClasses) {

		super(model);
		
		array = model.getStringArrayValue();
		if (array.length % 2 != 0)
			throw new IllegalArgumentException("Cannot create a DialogComponentStringsFromSpinnerGroup with an odd amount of elements");
		spinners = new JSpinner[ array.length / 2 ];
		
		if(combinedClasses != null)
			this.combinedClasses = combinedClasses;
		else
			this.combinedClasses = new LinkedHashMap<String, Tuple2<HyperparamInfo,HashSet<String>>>();
		
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
		
        generalReset = new JButton("Reset All");
        generalReset.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) { resetToDefault(); }} );
        masterPanel.add( generalReset );
		
		initialValues = new double[ array.length ];
		resetBtns = new JButton[ array.length ];
		
			// Acess positions of array
		int whereToGet = 0;
		int whereToSpinners = 0;

		while (whereToGet < array.length) {
			
				// This new panel will work as a "row inside the column"
			JPanel toAddPanel = new JPanel();
			toAddPanel.setLayout( new FlowLayout() );
			String labelText = array[whereToGet];
			
				// Add the label
			JLabel toAddLabel = new JLabel( labelText );
			toAddPanel.add( toAddLabel );
			++whereToGet;
			
				// Add the value
			final int valueForSpinner = whereToGet;
			String nextValue = array[whereToGet];
			
			Tuple2<HyperparamInfo, HashSet<String>> info = combinedClasses.get(labelText);

				// Create spinner depending if the value has been defined or not
			final var helperResults = createSpinnerHelper(nextValue , info);
			final var defaultInitialValue = helperResults._4();
			final JSpinner toAddSpinner = helperResults._1();
			final String description = helperResults._5();

				// This will make sure the values are updated on spinner change
			toAddSpinner.addChangeListener( new ChangeListener() {
				@Override public void stateChanged(ChangeEvent e) {
					updateModel(valueForSpinner, toAddSpinner.getValue().toString() ); } });
			
				// Necessary to set this directly due to spinner's tendency for taking all space available no matter the model
			toAddSpinner.setPreferredSize(new Dimension(100, 20));
			
				// Add the spinner to the "row"
			toAddPanel.add(toAddSpinner);
			++whereToGet;
						
	        var m_resetBtn = new JButton("Reset");
	        final int resetPos = whereToSpinners;
	        m_resetBtn.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) { resetSpinner(resetPos); }} );
	        toAddPanel.add( m_resetBtn );
	        
	        	// Add tooltip text
	        toAddLabel.setToolTipText(description);
	        toAddSpinner.setToolTipText(description);
	        m_resetBtn.setToolTipText(description);
			
				// Add the "row" to the column, the spinner to the list and the default value
			masterPanel.add( toAddPanel	 );
			spinners[whereToSpinners] = toAddSpinner;
			initialValues[whereToSpinners] = defaultInitialValue;
			resetBtns[whereToSpinners] = m_resetBtn;
			whereToSpinners++;
		}
	}

		private Tuple5<JSpinner , Double , Double , Double, String> createSpinnerHelper(String nextValue, Tuple2<HyperparamInfo, HashSet<String>> info) {
			JSpinner answer = null;
			boolean alreadyCreated = false;
			double value = 0;
			double minimum = Double.MIN_VALUE;
			double maximum = Double.MAX_VALUE;
			double initial = 0;
			double stepSize = 1;
			String description = "";
			
			if(info == null) {
				if(nextValue == null) {
					// Use defaults for variables
				}
				else {
					value = Double.parseDouble(nextValue);
				}
			}
			else {
				HyperparamInfo param = info._1;
				if( param.integer() ) {
					HyperparamInfoInteger intParam = (HyperparamInfoInteger) param;
					value = Double.parseDouble(nextValue);
					minimum = intParam.intMinimum();
					maximum = intParam.intMaximum();
					answer = new JSpinner( new SpinnerNumberModel( value , intParam.intMinimum() , intParam.intMaximum(), intParam.stepSize() ) );
					initial = intParam.intInitial();
					alreadyCreated = true;
					description = param.description();
				}
				else {
					value = Double.parseDouble(nextValue);
					minimum = param.minimum();
					maximum = param.maximum();
					stepSize = param.stepSize();
					initial = param.initial();
					description = param.description();
				}
			}
			
			if(alreadyCreated == false) {
				answer = new JSpinner( new SpinnerNumberModel( value , minimum , maximum, stepSize ) );
			}
				
			
			return new Tuple5<JSpinner, Double, Double, Double, String>(answer, minimum, maximum, initial, description);
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
	
	protected void resetSpinner(int position) {
		spinners[position].setValue( initialValues[position] );
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
		setEnabledOrDisabledComponents(enabled);
	}

	@Override
	public void setToolTipText(String text) {
		// TODO Auto-generated method stub
	}

	/**
	 * {@inheritDoc}
	 */
	
	@Override
	public void resetToDefault() {
		for(int count  = 0 ; count < array.length ; ++count) {
			resetSpinner(count);
		}
		
	}

	@Override
	public void setEnabledOrDisabledComponents(boolean value) {
		// TODO Auto-generated method stub
		generalReset.setEnabled(value);
		
		for(JSpinner spinner : spinners) {
			spinner.setEnabled(value);
		}
		
		for(JButton button : resetBtns) {
			button.setEnabled(value);
		}
	}

}
