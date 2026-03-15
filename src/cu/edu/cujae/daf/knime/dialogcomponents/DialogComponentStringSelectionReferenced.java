/*  Copyright by KNIME AG, Zurich, Switzerland
 *  Website: http://www.knime.com; Email: contact@knime.com
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME AG herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * -------------------------------------------------------------------
 *
 * History
 *   16.11.2005 (gdf): created
 */

package cu.edu.cujae.daf.knime.dialogcomponents;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Arrays;
import java.util.Collection;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.xmlbeans.impl.xb.xsdschema.WhiteSpaceDocument.WhiteSpace.Value;
import org.knime.core.node.FlowVariableModel;
import org.knime.core.node.FlowVariableModelButton;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DialogComponent;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.util.DefaultStringIconOption;
import org.knime.core.node.util.StringIconListCellEditor;
import org.knime.core.node.util.StringIconListCellRenderer;
import org.knime.core.node.util.StringIconOption;

import cu.edu.cujae.daf.core.ComponentInfo;

/**
 * Triple Ughh...
 * 
 * Given that the original {@link DialogComponentStringSelection} has
 * VERY annoyingly declared final, for the simple purpose
 * of adding the references methods had to copy the ENTIRE
 * CLASS FILE SOURCE CODE HAD TO BE COPIED BY HAND INSTEAD
 * OF A SIMPLE EXTEND
 * 
 * Quadruple Ughh...
 * 
 * TODO Complain to KNIME devs to change the final modifier
 *
 * Author is for the original file and the original copyright
 * info and GPL license has been kept
 * 
 * @author M. Berthold, University of Konstanz
 */
public final class DialogComponentStringSelectionReferenced extends DialogComponent implements ReseatableDialogComponent{

    private final JComboBox<StringIconOption> m_combobox;
    
    private final String[] reference;
    
    	@SuppressWarnings("unused")
	private final String defaultValue;
   
    private final int defaultIndex;

    private final JLabel m_label;
    
    private final JButton m_resetBtn;
    
    private final String[] toolTips;

    private final FlowVariableModelButton m_fvmButton;
    
    private int lastSavedIndex;

		/**
		 * Default constructor for several checkboxes,
		 * referencing a single StringSettingsModel and creating empty tooltips
		 * for every checkbox, also creating empty items for the tooltip
		 * 
		 * @param stringModel the model that stores the value for this component.
		 * @param label label for dialog in front of combobox
		 * @param references The string actually saved by the combobox
		 * @param defaultValue Reference value to turn the combobox when using the reset button
		 * @param list Items for the combobox
		 */
    public DialogComponentStringSelectionReferenced(
            final SettingsModelString stringModel,
            final String label,
            final String[] reference,
            final String defaultValue,
            final String... list) {
        this(stringModel, label, reference, defaultValue, list, new String[reference.length]);
    }
    
	    /**
	     * Constructor that puts label and combobox into panel. It expects the user
	     * to make a selection, thus, at least one item in the list of selectable
	     * items is required. When the settings are applied, the model stores one of
	     * the strings of the provided list.
	     *
		 * @param stringModel the model that stores the value for this component.
		 * @param label label for dialog in front of combobox
		 * @param references The string actually saved by the combobox
		 * @param defaultValue Reference value to turn the combobox when using the reset button
		 * @param list Items for the combobox
		 * @param toolTips Messages for each reference shown when hovering over the combobox
		 */
    public DialogComponentStringSelectionReferenced(
            final SettingsModelString stringModel,
            final String label,
            final String[] reference,
            final String defaultValue,
            final String[] list,
            final String[] toolTips) {
        this(stringModel, label, reference, defaultValue,
                DefaultStringIconOption.createOptionArray(Arrays.asList(list)), toolTips);
    }

	    /**
	     * Constructor that puts label and combobox into panel. It expects the user
	     * to make a selection, thus, at least one item in the list of selectable
	     * items is required. When the settings are applied, the model stores one of
	     * the strings of the provided list.
	     *
		 * @param stringModel the model that stores the value for this component.
		 * @param references The string actually saved by the combobox
		 * @param defaultValue Reference value to turn the combobox when using the reset button
		 * @param list Items for the combobox
		 * @param toolTips Messages for each reference shown when hovering over the combobox
	     * @param list list (not empty) of strings (not null) for the combobox
	     *
	     * @throws NullPointerException if one of the strings in the list is null
	     * @throws IllegalArgumentException if the list is empty or null.
	     */
    public DialogComponentStringSelectionReferenced(
            final SettingsModelString stringModel, final String label,
            final String[] reference,
            final String defaultValue,
            final Collection<String> list,
            final String[] toolTips) {
        this(stringModel, label, reference, defaultValue,
                DefaultStringIconOption.createOptionArray(list), toolTips);
    }

    /**
     * Constructor that puts label and combobox into panel. It expects the user
     * to make a selection, thus, at least one item in the list of selectable
     * items is required. When the settings are applied, the model stores one of
     * the strings of the provided list.
     *
     * @param stringModel the model that stores the value for this component.
     * @param references The string actually saved by the combobox
     * @param defaultValue Reference value to turn the combobox when using the reset button
     * @param list list (not empty) of strings (not null) for the combobox
     * @param editable true if the user should be able to add a value to the
     *  combo box
     *
     * @throws NullPointerException if one of the strings in the list is null
     * @throws IllegalArgumentException if the list is empty or null.
     */
    public DialogComponentStringSelectionReferenced(
            final SettingsModelString stringModel, final String label,
            final String[] reference,
            final String defaultValue,
            final Collection<String> list, final boolean editable, final String[] toolTips) {
        this(stringModel, label, reference , defaultValue, list, editable, null, toolTips);
    }

    /**
     * Constructor that puts label and combobox into panel. It expects the user
     * to make a selection, thus, at least one item in the list of selectable
     * items is required. When the settings are applied, the model stores one of
     * the strings of the provided list.
     *
     * @param stringModel the model that stores the value for this component.
     * @param label label for dialog in front of combobox
     * @param references The string actually saved by the combobox
     * @param defaultValue Reference value to turn the combobox when using the reset button
     * @param list list (not empty) of strings (not null) for the combobox
     * @param editable true if the user should be able to add a value to the combo box
     * @param fvm model exposed to choose from available flow variables
     * @param toolTips Messages for each reference shown when hovering over the combobox
     *
     * @throws NullPointerException if one of the strings in the list is null
     * @throws IllegalArgumentException if the list is empty or null.
     */
    public DialogComponentStringSelectionReferenced(
            final SettingsModelString stringModel,
            final String label,
            final String[] reference,
            final String defaultValue,
            final Collection<String> list, final boolean editable,
            final FlowVariableModel fvm,
            final String[] toolTips) {
        this(stringModel, label, reference, defaultValue,
                DefaultStringIconOption.createOptionArray(list), fvm, toolTips);
        	// Validate this
        m_combobox.setEditable(editable);
        setLastSavedIndex(stringModel);
        if (editable) {
            final StringIconListCellEditor editor
                = new StringIconListCellEditor();
            ((JTextField)editor.getEditorComponent()).getDocument()
                // in order to get informed about model changes...
                .addDocumentListener(new DocumentListener() {

                @Override
                public void changedUpdate(final DocumentEvent e) {
                    try {
                        m_combobox.setSelectedItem(editor.getItem());
                        updateModel();
                    } catch (final InvalidSettingsException ise) {
                        // Ignore it here.
                    }
                }

                @Override
                public void insertUpdate(final DocumentEvent e) {
                    try {
                        m_combobox.setSelectedItem(editor.getItem());
                        updateModel();
                    } catch (final InvalidSettingsException ise) {
                        // Ignore it here.
                    }
                }

                @Override
                public void removeUpdate(final DocumentEvent e) {
                    try {
                        m_combobox.setSelectedItem(editor.getItem());
                        updateModel();
                    } catch (final InvalidSettingsException ise) {
                        // Ignore it here.
                    }
                }
            });
            m_combobox.setEditor(editor);
        }
    }

    /**
     * Constructor that puts label and combobox into panel. It expects the user
     * to make a selection, thus, at least one item in the list of selectable
     * items is required. When the settings are applied, the model stores one of
     * the strings of the provided list.
     *
     * @param stringModel the model that stores the value for this component.
     * @param label label for dialog in front of combobox
     * @param list list (not empty) of {@link StringIconOption}s for
     * the combobox. The text of the selected component is stored in the
     * {@link SettingsModelString}.
     * @param toolTips Messages for each reference shown when hovering over the combobox
     *
     * @throws NullPointerException if one of the strings in the list is null
     * @throws IllegalArgumentException if the list is empty or null.
     */
    public DialogComponentStringSelectionReferenced(
            final SettingsModelString stringModel,final String label,
            final String[] reference,
            final String defaultValue,
            final StringIconOption[] list,
            final String[] toolTips) {
        this(stringModel, label, reference, defaultValue, list, null, toolTips);
    }

    /**
     * Constructor that puts label and combobox into panel. It expects the user
     * to make a selection, thus, at least one item in the list of selectable
     * items is required. When the settings are applied, the model stores one of
     * the strings of the provided list.
     *
     * @param stringModel the model that stores the value for this component.
     * @param label label for dialog in front of combobox
     * @param list list (not empty) of {@link StringIconOption}s for
     *        the combobox. The text of the selected component is stored in the
     *        {@link SettingsModelString}.
     * @param fvm model exposed to choose from available flow variables
     * @param toolTips Messages for each reference shown when hovering over the combobox
     *
     * @throws NullPointerException if one of the strings in the list is null
     * @throws IllegalArgumentException if the list is empty or null.
     */
    public DialogComponentStringSelectionReferenced(
            final SettingsModelString stringModel, final String label,
            final String[] reference,
            final String defaultValue,
            final StringIconOption[] list, final FlowVariableModel fvm, final String[] toolTips) {
        super(stringModel);
        
        	// The amount of references, labels and tooltips must be the same
        this.reference = validateReference(reference, list, toolTips);
        this.toolTips = toolTips;
        
        if ((list == null) || (list.length == 0)) {
            throw new IllegalArgumentException("Selection list of options "
                    + "shouldn't be null or empty");
        }
        m_label = new JLabel(label);
        getComponentPanel().add( m_label );
        m_combobox = new JComboBox<StringIconOption>();
        m_combobox.setRenderer(new StringIconListCellRenderer());

        for (final StringIconOption o : list) {
            if (o == null) {
                throw new NullPointerException("Options in the selection"
                        + " list can't be null");
            }
            m_combobox.addItem(o);
        }

        getComponentPanel().add(m_combobox);
        
        m_resetBtn = new JButton("Reset");
        m_resetBtn.addActionListener( new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				resetToDefault();
			}
		} );
        getComponentPanel().add( m_resetBtn );
        
        	// When the selected item on the combobox changes,
        	// update the model with the respective reference
        	// and the tooltip message
        	// TODO Is it possible to assign a tooltip to each combobox element instead of changing it after the user choses an item?
        m_combobox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(final ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    // if a new item is selected update the model
                    try {
                        updateModel();
                        updateToolTip();
                    } catch (final InvalidSettingsException ise) {
                        // ignore it here
                    }
                }
            }
        });
        setLastSavedIndex(stringModel);
        // we need to update the selection, when the model changes.
        getModel().addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(final ChangeEvent e) {
                updateComponent();
            }
        });
        
        	// Set the message for the default / last saved element
        updateToolTip();

        // add variable editor button if so desired
        if (fvm != null) {
            fvm.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(final ChangeEvent evt) {
                     getModel().setEnabled(!fvm.isVariableReplacementEnabled());
                }
            });
            m_fvmButton = new FlowVariableModelButton(fvm);
            getComponentPanel().add(m_fvmButton);
        } else {
            m_fvmButton = null;
        }

        this.defaultIndex = validateDefaultIndex(defaultValue);
        this.defaultValue = defaultValue;
        
        //call this method to be in sync with the settings model
        updateComponent();
    } 

    		/**
    		 *  Helper / Constructor Wrapper for getting a component of this type from
			 *  a {@link ComponentInfo} item, from it gets the references, the checkbox
			 *  labels and the hovering tooltip descriptions
    		 * 
    		 * @param stringModel the model that stores the value for this component.
    		 * @param label label for dialog in front of combobox
    		 * @param referencesAndLabels Objects from where to get  components names, references, and descriptions
    		 * @param defaultValue Reference value to turn the combobox when using the reset button
    		 * 
    		 * @return A component from this type with the parameters of the information given
    		 */
    	public static DialogComponentStringSelectionReferenced DialogComponentStringSelectionReferencedFromComponentInfo(
    			final SettingsModelString stringModel,
    			final String label,
    			ComponentInfo[] referencesAndLabels,
    			String defaultValue) {

    		int size = referencesAndLabels.length;
    		
    		String[] reference = new String[size];
    		String[] list = new String[size];
    		String[] description = new String[size];
    		
    		for(int count = 0 ; count < referencesAndLabels.length ; ++count) {
    			ComponentInfo current = referencesAndLabels[count];
    			
    			reference[count] = current.shortName();
    			list[count] = current.verboseName();
    			description[count] = current.description();
    		}
    		
    		return new DialogComponentStringSelectionReferenced(stringModel, label, reference, defaultValue, list, description);
	}

    		/** Change the message of the combobox to describe the currently selected element */
		private void updateToolTip() {
			setToolTipText( toolTips[m_combobox.getSelectedIndex()] );
		}
    	
		/**
    	 * Makes sure that the given default String
    	 * 
    	 * @param defaultValue Value to validate
    	 * 
    	 * @return The index in the references of the provided String
    	 */
    private int validateDefaultIndex(String defaultValue) {
		int answer = findIndex(defaultValue);
		if(answer == -1) throw new IllegalArgumentException("Tried to create a referenced combobox with nonexisting defaultvalue: " + defaultValue);
		return answer;
	}

    	/**
    	 * Find in the references array the given string
    	 * 
    	 * @param defaultValue String to find
    	 * 
    	 * @return Index of the provided String, -1 if not found
    	 */
	private int findIndex(String defaultValue) {

    	for(int count = 0 ; count < reference.length ; ++count) {
    		if( reference[count].equals(defaultValue) )
    			return count;
    	}

		return -1;
	}

		/**
		 * This makes sure that the two provided arrays have
		 * the same length
		 * 
		 * @param reference The array with the references
		 * @param list The labels for the checkbox
		 * @param toolTips Messages for each reference shown when hovering over the combobox
		 * 
		 * @return The array provided as references
		 */
	private String[] validateReference(String[] reference, StringIconOption[] list, String[] toolTips) {
		if(reference.length != list.length) {
			throw new IllegalArgumentException("Tried to create a referenced combobox with differing size of reference and shown values");	
		}
		
		if(reference.length != toolTips.length) {
			throw new IllegalArgumentException("Tried to create a referenced combobox with differing size of reference and tool tips");	
		}
		
		return reference;
	}

		/**
	     * {@inheritDoc}
	     */
    @Override
    protected void updateComponent() {
    	
    	SettingsModelString model = ((SettingsModelString)getModel());
    	setLastSavedIndex(model);
    	
        final String strVal = ( (DefaultStringIconOption) m_combobox.getItemAt(lastSavedIndex) ).getText();

        StringIconOption val = null;
        if (strVal == null) {
            val = null;
        } else {
            for (int i = 0, length = m_combobox.getItemCount();
                i < length; i++) {
                final StringIconOption curVal =
                    (StringIconOption)m_combobox.getItemAt(i);
                if (curVal.getText().equals(strVal)) {
                    val = curVal;
                    break;
                }
            }
            if (val == null) {
                val = new DefaultStringIconOption(strVal);
            }
        }
        boolean update;
        if (val == null) {
            update = m_combobox.getSelectedItem() != null;
        } else {
            update = !val.equals(m_combobox.getSelectedItem());
        }
        if (update) {
            m_combobox.setSelectedItem(val);
        }
        // also update the enable status
        setEnabledComponents(getModel().isEnabled());

        // make sure the model is in sync (in case model value isn't selected)
        StringIconOption selItem =
            (StringIconOption)m_combobox.getSelectedItem();
        try {
            if ((selItem == null && strVal != null)
                    || (selItem != null && !selItem.getText().equals(strVal))) {
                // if the (initial) value in the model is not in the list
                updateModel();
            }
        } catch (InvalidSettingsException e) {
            // ignore invalid values here
        }
    }

	    /**
	     * Transfers the current value from the component into the model.
	     */
    private void updateModel() throws InvalidSettingsException {

        if (m_combobox.getSelectedItem() == null) {
            ((SettingsModelString)getModel()).setStringValue(null);
            m_combobox.setBackground(Color.RED);
            // put the color back to normal with the next selection.
            m_combobox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    m_combobox.setBackground(DialogComponent.DEFAULT_BG);
                }
            });
            throw new InvalidSettingsException(
                    "Please select an item from the list.");
        }
        // we transfer the value from the field into the model
        lastSavedIndex = m_combobox.getSelectedIndex();
        ((SettingsModelString)getModel()).setStringValue(
                ( reference[ lastSavedIndex  ] ) );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettingsBeforeSave()
            throws InvalidSettingsException {
        updateModel();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void checkConfigurabilityBeforeLoad(final PortObjectSpec[] specs)
            throws NotConfigurableException {
        // we are always good.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setEnabledComponents(final boolean enabled) {
    	// Eny use setEnabledOrDisabledComponents
    }

    /**
     * Sets the preferred size of the internal component.
     *
     * @param width The width.
     * @param height The height.
     */
    public void setSizeComponents(final int width, final int height) {
        m_combobox.setPreferredSize(new Dimension(width, height));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setToolTipText(final String text) {
        m_label.setToolTipText(text);
        m_combobox.setToolTipText(text);
        
        System.out.println("ToolTip: " + text);
    }

    /**
     * Try to find the reference string that with which the combobox was last save
     * 
     * @param setting Model from where to retrive the saved string
     * 
     * @throws IllegalArgumentException If the reference wasn't found
     */
    private void setLastSavedIndex(final SettingsModelString setting) {
    	
    	String text = setting.getStringValue();
    	for (int count = 0 ; count < m_combobox.getItemCount() ; ++ count) {
    		if(reference[count].equals(text) ) {
    			lastSavedIndex = count;
    			return;
    		}
    	}
    	
    	throw new IllegalArgumentException("For Referenced Combobox not found the setting: " + text);
    }
    
    	/**
    	 * {@inheritDoc}
    	 */
    public void resetToDefault() {
    	m_combobox.setSelectedIndex(defaultIndex);
    	try {
			updateModel();
		} catch (InvalidSettingsException e) {
			e.printStackTrace();
		}
    }
    
    	/**
    	 * {@inheritDoc}
    	 */
    @Override
    public void setEnabledOrDisabledComponents(boolean value) {
		m_combobox.setEnabled(value);
		m_resetBtn.setEnabled(value);
		m_label.setEnabled(value);
    }
}