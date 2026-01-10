package cu.edu.cujae.daf.knime.dialogcomponents;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DialogComponent;
import org.knime.core.node.defaultnodesettings.SettingsModelStringArray;
import org.knime.core.node.port.PortObjectSpec;

/**
 * Custom visual KNIME component, this consist of several checkboxes
 * grouped in a column with results saved to a single Settings Model 
 * 
 * This components requires one label for each desired chceckbox
 * and a matching reference, the references will be the values that
 * will actually be saved to the Setings.
 * 
 * @author Jonathan David González Pereda, CUJAE
 */

public class DialogComponentCheckBoxGroupReferenced extends DialogComponent implements ReseatableDialogComponent {

		// The actual checkboxrs
	private final JCheckBox[] m_checkboxes;
	
		// Label for the whole group
	private final String name;
	
		// For each checkbox, the values to actually save
	private final String[] references;
	
		// How many checkboxes per row
		@SuppressWarnings("unused")
	private final int amountPerRow;
	
		// How many checkboxes should be selected
	private final int minimumSelected;
	
		// Indexes in m_checkboxes
    private final int[] defaultIndexes;
    
    	// Button that will set the checkboxes to the default values as indicated in "defaultIndexes"
    private final JButton m_resetBtn;
    
    	// Key is a value in "m_checkboxes", value is it's respective position
    private final LinkedHashMap<String, Integer> referencesIndexes;
	
    	/**
    	 * Default constructor for several checkboxes
    	 * referencing a single StringSettingsModel
    	 * 
    	 * @param model The model where to store the reference strings of the selected checkboxes
    	 * @param name How to label the entire group
    	 * @param labels Text to give to each checkbox
    	 * @param references The strings saved by each respective checkbox when saved
    	 * @param amountPerRow How many checkboxes  to place per row
    	 * @param minimumSelected Give a warning if less checkboxes than this value is selected
    	 * @param defaults The strings of in "references" to take as default as default
    	 */
	public DialogComponentCheckBoxGroupReferenced(
			SettingsModelStringArray model,
			String name,
			String[] labels,
			String[] references,
			int amountPerRow,
			int minimumSelected,
			String[] defaults) {
		super(model);
			// Box layout will make simply
			// adding components to get the desired "columns"
			// and "row" structure easier by making this the "column"
		JPanel masterPanel = getComponentPanel();
		masterPanel.setLayout( new BoxLayout(masterPanel, BoxLayout.Y_AXIS ));
		
			// Validate these given variables
		this.name = name;
		this.references = validateReferences(labels, references);
		this.amountPerRow = validateAmountPerRow(amountPerRow);
		this.minimumSelected = validateMinimumSelected(minimumSelected, references.length);
		this.defaultIndexes = validateDefaultIndexes(references , defaults);
		
		m_checkboxes = new JCheckBox[ labels.length ];
		
			// Current "row"
		JPanel toAddPanel = new JPanel();
		toAddPanel.setLayout( new BoxLayout(toAddPanel , BoxLayout.X_AXIS) );
		
	    for(int count = 0 , currentPanel = 0; count < references.length ; ++count) {
	    		// Remember "m_checkboxes" is initialized null
	    	JCheckBox toAddBox = ( new JCheckBox( labels[count] , false) );
	    	m_checkboxes[count] = toAddBox;
	    	toAddPanel.add( toAddBox );  
	   	   ++currentPanel;
	   	   	// if the current "row" has been filled add it
    	   if( !(currentPanel < amountPerRow) ) {
    		   masterPanel.add(toAddPanel);
	    	   toAddPanel = new JPanel();
	           currentPanel = 0;
	    	   }
	       }
	    	// Edge case for when the amount of amounts
	    	//per row isn't exactly divisible by the amount of checkboxes
	    if( labels.length % amountPerRow != 0)
	    	masterPanel.add(toAddPanel);
	    
	    	// Create the button and add it's action
        m_resetBtn = new JButton("Reset");
        m_resetBtn.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {}} );
        getComponentPanel().add( m_resetBtn );
	    
	    referencesIndexes = getReferencesIndexes(references);
	}

		/**
		 * Check if the "minimumSelected" given integer value:
		 * 
		 * - Is positive
		 * - Doesn't equal the amount of references
		 * - Isn't larger than the amount of references
		 * 
		 * @param minimumSelected
		 * @param references
		 * 
		 * @return The same "minimumSelected" provided integer
		 */
	private int validateMinimumSelected(int minimumSelected, int references) {

		if(minimumSelected < 0)
			throw new IllegalArgumentException("Trying to create a checkbox group with negative minimum" );
		
		if(minimumSelected == references && references > 1)	// && for edge case if there is only one reference
			throw new IllegalArgumentException("Trying to create a checkbox group with equal number of minimum and provided values (meaning everyone needs to be selected)" );
		
		if(minimumSelected > references)
			throw new IllegalArgumentException("Trying to create a checkbox group with a minimum larger than the amount of provided values" );
	

		return minimumSelected;
	}

		/**
		 * For each of the values of the given array, create
		 * a map entry with the values as keys and positions
		 * as values
		 * 
		 * @param references Array to check
		 * @return A map with elements of "references" as keys and it's respective positions as values
		 */
	private LinkedHashMap<String, Integer> getReferencesIndexes(String[] references) {

		LinkedHashMap<String, Integer> answer = new LinkedHashMap<String, Integer>();
		
		for(int count = 0 ; count < references.length ; ++count) {
			answer.put( references[count], count);
		}

		return answer;
	}

		/**
		 * This checks that the provided "default" values:
		 * 
		 * - Aren't repeated
		 * - Are actually in the given references
		 * 
		 * @param references All the values to save in the settings model
		 * @param defaults The list of values to consider defaults for the component
		 * 
		 * @return An array with the indexes of where to find default values in references
		 */
	private int[] validateDefaultIndexes(String[] references, String[] defaults) {
		
		int[] answer = new int[defaults.length];
		int addNextAnswer = 0;
			// Store the keys of default here
		LinkedHashSet<String> setDefaults = new LinkedHashSet<String>();
			// Try to find if values in "defaults" are unique
			// If a value is repeated then it is not added and it's size doesn't change
		for(int count = 0 ; count < defaults.length ; ++count) {
			setDefaults.add( defaults[count] );
			if( setDefaults.size() != (count + 1) )
				throw new IllegalArgumentException( "Trying to create a checkbox group with repeated default value:" + defaults[count] );
		}
			// Go through the references to get the index were to find the defaults
		for(int count = 0 ; count < references.length && setDefaults.size() > 0; ++count) {
			String currentReference = references[count];
			if( setDefaults.remove(currentReference) ) {
				answer[addNextAnswer] = count;
				++addNextAnswer;
			}
		}
			// If a default value wasn't added, notify it
		if( setDefaults.size() > 0 ) {
			throw new IllegalArgumentException( "Trying to create a checkbox group with defaults that are not in the reference:" + setDefaults.toString() );
		}

		return answer;
	}

		/**
		 * Makes sure the given number is positive and non zero
		 * Doesn't make sense to have -7 or zero elements in a row
		 * 
		 * @param amountPerPanel The integer value to validate
		 * 
		 * @return The same given value
		 */
	private int validateAmountPerRow(int amountPerPanel) {
		if(amountPerPanel < 1)
			throw new IllegalArgumentException("Tried to create a referenced checkbox group with an amount per row smaller than 1");			

		return amountPerPanel;
	}

		/**
		 * Makes sure the amount of labels for checkboxes
		 * and the references are the same
		 * 
		 * @param labels The checkboxes labels that will be shown to the user
		 * @param references The values that will be saved to the Node's settings
		 * 
		 * @return The same provided array of references
		 */
	private String[] validateReferences(String[] labels, String[] references) {
		if(references.length != labels.length) {
			throw new IllegalArgumentException("Tried to create a referenced checkbox group with differing size of reference and shown labels");
			
		}
		return references;
	}


		/**
		 * {@inheritDoc}
		 */
	@Override
	protected void updateComponent() {
		
		for(int count = 0 ; count < m_checkboxes.length ; ++count)
			m_checkboxes[count].setSelected(false);

		SettingsModelStringArray model = ((SettingsModelStringArray)getModel());
		
		String[] array = model.getStringArrayValue();
		
		for( int count = 0; count < array.length ; ++count ) {
			int indexToToggle = referencesIndexes.get( array[count] );
			m_checkboxes[ indexToToggle ].setSelected(true);
		}
		
	}
	
		/**
		 * Save the references of the selected checkboxes to the
		 * settings
		 */
    private void updateModel() throws InvalidSettingsException {
    	
		SettingsModelStringArray model = ((SettingsModelStringArray)getModel());
		LinkedHashSet<String> enabledReferences = new LinkedHashSet<String>();
		
		for(int count = 0 ; count < m_checkboxes.length ; ++count) {
			if( m_checkboxes[count].isSelected() ) {
				enabledReferences.add( references[count] );
			}
		}
		
		if(enabledReferences.size() < this.minimumSelected)
			throw new InvalidSettingsException( name + " expected at least " + minimumSelected + " option(s), but " + enabledReferences.size() + " where chosen");
		String[] newModelValues = new String[enabledReferences.size()];
		newModelValues = enabledReferences.toArray(newModelValues);
		model.setStringArrayValue( newModelValues );
    }
	

	@Override
	protected void validateSettingsBeforeSave() throws InvalidSettingsException {
        updateModel(); // Just update the model
	}

	@Override
	protected void checkConfigurabilityBeforeLoad(PortObjectSpec[] specs) throws NotConfigurableException {
        // We are always good
	}

		/**
		 * Set or enable the checkboxes given the provided value
		 * 
		 * @param enabled Value to set the chceckboxes
		 */
	@Override
	protected void setEnabledComponents(boolean enabled) {
		for (int countCheckBox = 0 ; countCheckBox < m_checkboxes.length ; ++countCheckBox) {
			m_checkboxes[countCheckBox].setEnabled(enabled);
		}
		
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

		for( int count = 0 ; count < m_checkboxes.length ; ++count)
		{	m_checkboxes[count].setSelected(false);	}

		for( int count = 0 ; count < defaultIndexes.length ; ++count)
		{	m_checkboxes[ defaultIndexes[count] ].setSelected(true); }
		
		try {
			updateModel();
		} catch (InvalidSettingsException e) {
			e.printStackTrace();
		}
	}

}
