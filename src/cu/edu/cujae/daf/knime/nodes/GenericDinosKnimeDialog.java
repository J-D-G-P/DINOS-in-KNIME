package cu.edu.cujae.daf.knime.nodes;

import java.awt.CheckboxGroup;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponent;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentButton;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnFilter;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentLabel;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.DialogComponentSeed;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelFilterString;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.defaultnodesettings.SettingsModelStringArray;

import cu.edu.cujae.daf.knime.dialogcomponents.DialogComponentCheckBoxGroupReferenced;
import cu.edu.cujae.daf.knime.dialogcomponents.DialogComponentCheckboxWithActionListener;
import cu.edu.cujae.daf.knime.dialogcomponents.DialogComponentNumberResetable;
import cu.edu.cujae.daf.knime.dialogcomponents.DialogComponentStringSelectionReferenced;
import cu.edu.cujae.daf.knime.dialogcomponents.DialogComponentStringsFromSpinnerGroup;
import cu.edu.cujae.daf.knime.dialogcomponents.ReseatableDialogComponent;
import cu.edu.cujae.daf.knime.nodes.GenericDinosKnimeWorkflow.DINOS_NODE;
import scala.Tuple4;

/**
 * Default configuration interface for all subgroup discovery nodes
 * 
 * Programming wise, this makes use of the internal interface component
 * system of KNIME, which is mad on top of SWING. This will make use of
 * either:
 * 
 * * Pre built KNIME DialogComponent
 * * Custom classes that extend DialogComponent
 * 
 * This will make easier to manage position, size, behavior, appearance
 * and (more importantly) automatic loading and saving of config values.
 * Care has been taken to also make it absolutely compatible with KNIME's
 * flow Variable replacement function.
 * 
 * Methods that add visual elements has been made as modular as possible
 * as to make extending and replacing the class's functions easier
 * 
 * @author Jonathan David González Pereda, CUJAE
 */

	@SuppressWarnings("static-access")
public abstract class GenericDinosKnimeDialog extends DefaultNodeSettingsPane {

		/** 
		 * 1 - Subgroup Discovery
		 * 2 - Dataset Extractor
		 * 3 - Parser
		 */
	protected abstract DINOS_NODE getMode();		
		
		/** This class will get the workflow helper for this mode */
	protected abstract GenericDinosKnimeWorkflow getInstance();
	
		/** Work from this */
	protected final GenericDinosKnimeWorkflow workflow = getInstance();
	
		/** Target class, single */
	protected DialogComponentColumnNameSelection classCol;
	
		/** Description Parser column */
	protected DialogComponentColumnNameSelection descCol;
	
		/** Optional parser column for showing included or not */
	protected DialogComponentColumnNameSelection inCol;
	
		/** Optional parser column for the ELSE part of a rule */
	protected DialogComponentColumnNameSelection thenCol;
	
		/** This component add a field for a random seed an a checkbox to define if to use it or not */
	protected DialogComponentSeed seedSelection;
	
    	/** A button that resets all components in the "component" array */
	protected DialogComponentButton resetAllComponentsButton;
	
		/** Column for indicating fixed variables */
	protected DialogComponentColumnFilter fixedVariables;
	
    	/** Checkbox that shows whether to use or not the default settings of the workflow */
	protected DialogComponentCheckboxWithActionListener useDefaultOrNot;
	
    	/** Custom components for storing the configurations */
	protected ReseatableDialogComponent[] components;

		/** Position of objects in the componentes array */
	private int objsPos;

	
    	/** Constructor, it has been made as modular as possible to easily just extend and replace the necessary parts */
    protected GenericDinosKnimeDialog() {

    	super();
        
        DINOS_NODE mode = getMode();
        
        if(mode == DINOS_NODE.DISCOVERY) {
        	createDiscoveryDialog(this);
        }
        else if (mode == DINOS_NODE.EXTRACTOR) {
        	addClassToGeneralTab(this);
        }
        else if (mode == DINOS_NODE.PARSER) {
        	createParserDialog(this);
        }
    }

    	/** Add all tabs for dialog */
    protected void createDiscoveryDialog(GenericDinosKnimeDialog panel) {

        createGeneralTab(panel);

        createComponentsTab(panel);

        createSettingsTab(panel);	
	}

	
	
			// TAB 1: GENERAL
	
			/** Tab for general settings, by default the first one */

    	protected void createGeneralTab(GenericDinosKnimeDialog panel) {
	    		// Set title
	    	panel.setDefaultTabTitle( workflow.TAB_DEFAULT);
	        
	    	addClassToGeneralTab(panel);
	       
	    	addSeedToGeneralTab(panel);
	    }

				/** This will add a combobox for chosing the target class, which conveniently prevents configuration if the input table lacks the necessary type of column as required by the workflow*/
    		protected void addClassToGeneralTab(GenericDinosKnimeDialog panel) {
		        createNewGroup( workflow.NAME_CLASS );
		        classCol = new DialogComponentColumnNameSelection(
		                workflow.createTargetClass(), workflow.NAME_CLASSCOLUMN , workflow.PORT_INPUT_DATASET , workflow.getAceptedTargetTypes() );
		        panel.addDialogComponent(classCol);
			}
			
				/** This will add a box for inserting a long value and a checkbox to determine if to use that value or actually generate a random one */
    		protected void addSeedToGeneralTab(GenericDinosKnimeDialog panel) {
		        createNewGroup( workflow.NAME_SEED );
		        seedSelection = new DialogComponentSeed(
		        		workflow.createGeneratorSeed(), workflow.NAME_SEEDLABEL);
		        panel.addDialogComponent(seedSelection);
			}



		    // TAB 2: COMPONENTS
			
			/** Tab for setting which component to use for each part of the algorithm, by default the second one */
    	protected void createComponentsTab(GenericDinosKnimeDialog panel) {
	    	panel.createNewTab( workflow.TAB_COMPONENTS );
	    		
	    	addDefaultOrNotToComponentsTab(panel);
	    	
	    	addResetAllButtonToComponentsTab(panel);
	    	
	    	addComponentsToComponentsTab(panel);
	    	
	    }
		
	    		/** This will add a checkbox for choosing if to use use the default components (true) or just use the default ones for this workflow (false)  */
    		protected void addDefaultOrNotToComponentsTab(GenericDinosKnimeDialog panel) {
		    	useDefaultOrNot = new DialogComponentCheckboxWithActionListener( new SettingsModelBoolean(workflow.KEY_DEFAULTCOMPONENTS, workflow.DEFAULT_DEFAULTCOMPONENTS ), workflow.NAME_DEFAULTCOMPONENTS);

		    		// Action for the checkbox: disable the components if the defaults are chosen
		    	useDefaultOrNot.addActionListener( new ActionListener() {
					@Override public void actionPerformed(ActionEvent e)
					{ setComponentsVisible( !useDefaultOrNot.isSelected() ); }});

		    	panel.addDialogComponent(useDefaultOrNot);
		    }
		    
		    	/** Add button that will reset all the chosen classes for the components*/
    		protected void addResetAllButtonToComponentsTab(GenericDinosKnimeDialog panel) {
		    	resetAllComponentsButton = new DialogComponentButton( workflow.NAME_RESETCOMPONENTS );
		    	
		    	resetAllComponentsButton.addActionListener( new ActionListener() {
						// This action is the entire reason of being for the button
					@Override public void actionPerformed(ActionEvent e) {
						for(int countComponents = 0 ; countComponents < components.length ; ++ countComponents) {
							
							components[countComponents].resetToDefault(); } } } );

		    	panel.addDialogComponent(resetAllComponentsButton);
		    	
		    }
		    
		    	/** This adds the actual components, created by the workflow along with enclosing them in their own group showing their name and internal flow variable*/
    		protected void addComponentsToComponentsTab(GenericDinosKnimeDialog panel) {
		    	components = workflow.createDialogComponentForAllComponentes();
		    	boolean enabled = !useDefaultOrNot.isSelected();
		    		
		    	for(int count = 0; count < components.length ; ++count) {
		    		Tuple4<String, String, String, Object> currentInfo = workflow.INFO_CONFIGURABLES[count];
		    			// In the name show not only the verbose name but also the key short name
		    		panel.createNewGroup( currentInfo._2() + " (" + currentInfo._1() + ")" );
		    		ReseatableDialogComponent toAdd = components[count];
		    		panel.addDialogComponent( (DialogComponent) toAdd );
		    		
		    		if( currentInfo._1() == "objs" )
		    			objsPos = count;
		    	}

		    	setComponentsVisible(enabled);
		    }
		    
		    
		    
		    // TAB 3: SETTINGS
		    
		    /** Tab for setting the numerical value of settings */
    	protected void createSettingsTab(GenericDinosKnimeDialog panel) {

			panel.createNewTab( workflow.TAB_SETTINGS );
			
			if( getMode() == DINOS_NODE.DISCOVERY )
				addOverallConfigurationToSettingsTab(panel);
			
			addListConfigurationToSettingsTab(panel);
			
		}
    	
				/** This will add additional settings which are internally picked by for use by the class managing the algorithm, but not a specific component  */
    		protected void addOverallConfigurationToSettingsTab(GenericDinosKnimeDialog panel) {

    			panel.createNewGroup( workflow.NAME_OVERALCONFIGS );
    			
				addMaxTrialsToSettingsTab(panel);
				
				addCollectIterationsToSettingsTab(panel);
				
				if( getMode() == DINOS_NODE.DISCOVERY )
					addFixedVariablesToSettingsTab(panel);
			}
			
					/** This will add an integer positive non zero spinner for selection how many iterations of the algorithm to do*/
    			protected void addMaxTrialsToSettingsTab(GenericDinosKnimeDialog panel) {
					DialogComponentNumber maxTrials = new DialogComponentNumberResetable(workflow.createTrialModel(), workflow.DEFAULT_TRIALS , workflow.NAME_TRIALS, workflow.DEFAULT_TRIALSTEPSIZE, 10, null, true, workflow.NAME_TRIALS_ERROR);
					panel.addDialogComponent(maxTrials);	
				}
				
					/** This will add a checkbox to tell the algorithm to save or not the information of the metrics */
    			protected void addCollectIterationsToSettingsTab(GenericDinosKnimeDialog panel) {
					DialogComponentBoolean collectIterations = new DialogComponentBoolean( workflow.createCollectItarationsModel() , workflow.NAME_COLLECTITERATIONS);
					panel.addDialogComponent(collectIterations);
				}

					/** This will add a column filter to select which variables to fix, if any*/
    			protected void addFixedVariablesToSettingsTab(GenericDinosKnimeDialog panel) {
    				panel.addDialogComponent( new DialogComponentLabel(workflow.NAME_FIXED) );
					fixedVariables = new DialogComponentColumnFilter(
							workflow.createFixedModel(),
				            workflow.PORT_INPUT_DATASET,
				            false);
					fixedVariables.setExcludeTitle(workflow.NAME_FIXED_FIXED);
					fixedVariables.setIncludeTitle(workflow.NAME_FIXED_VARIABLE);
					panel.addDialogComponent(fixedVariables);
				}
		
				/** This will add the list of possible component specific configurations */
    		protected void addListConfigurationToSettingsTab(GenericDinosKnimeDialog panel) {
				panel.createNewGroup( workflow.NAME_SPECIFICCONFIGS );
				
				DialogComponentStringsFromSpinnerGroup settings =
						new DialogComponentStringsFromSpinnerGroup( workflow.createSettingsModelForAvailableSettings() , workflow.combinedSettings );
				panel.addDialogComponent(settings);
			}
		
		
        	/** Add all tabs for parser dialog */
    protected void createParserDialog(GenericDinosKnimeDialog panel) {

    	createParserTab(panel);

    	createComponentsTab(panel);

    	createSettingsTab(panel);
    }
    
    		/** Similar to the General Tab, but with the parsing components and without random seed*/
    	protected void createParserTab(GenericDinosKnimeDialog panel) {
	    		// Set title
	    	panel.setDefaultTabTitle( workflow.TAB_DEFAULT );
	    	
	    	panel.addClassToGeneralTab(panel);
	    	
	    	panel.addParsingComponents(panel);

    	}
    	
    		/** Components specific to Parsing */
    	protected void addParsingComponents(GenericDinosKnimeDialog panel) {
    			// Create Name of Group
    		panel.createNewGroup( workflow.NAME_PARSER );
    		
    		addDescriptionClass(this);
    	}
    	
    		/** Add an element to define the column to store descriptions of subgroups */
    	protected void addDescriptionClass(GenericDinosKnimeDialog panel) {
    		descCol = new DialogComponentColumnNameSelection(
    				workflow.createDescClass(),
    				workflow.NAME_PARSER_DESCRIPTION,
    				workflow.PORT_INPUT_DESCRIPTIONS,
    				true,
    				false,
    				workflow.TARGETS_DESCTYPES);
    		panel.addDialogComponent(descCol);
    	}
		
			// HELPER FUNCTIONS
		
			/** 
			 * TODO: Works on start for {@link DialogComponentCheckBoxGroupReferenced},
			 *  but not for {@link DialogComponentStringSelectionReferenced}. However,
			 *  works as normal during normal user operations.
			 *  
			 *  @param value if true, will enable the components, false will disable them
			 */
    	protected void setComponentsVisible(boolean value) {
			
			for( int count = 0 ; count < components.length ; ++ count) {
				components[count].setEnabledOrDisabledComponents(value);
			}
		}
    	
        @Override
        /**
         * For sanity checks in the dialog windows, so that they can be fixed by the
         * user instead of having to execute the node.
         * 
         * In subgroup discovery, checks that the fixed column is not the same as the target
         */
        public void saveAdditionalSettingsTo(final NodeSettingsWO settings)
                throws InvalidSettingsException {
        	
            DINOS_NODE type = getMode();
            
            if (type == DINOS_NODE.DISCOVERY) {
            	SettingsModelFilterString filterModel = (SettingsModelFilterString) fixedVariables.getModel();
            	var filter = filterModel.getExcludeList();
            	SettingsModelString classNameModel = (SettingsModelString) classCol.getModel();
            	String className = classNameModel.getStringValue();
            		if( filter.contains( className ) ) {
            			throw new InvalidSettingsException(workflow.EXCEPTION_FIXED_EQUALSCLASS + "(" + className + ")");
            		}
            }
            
            if (type != DINOS_NODE.EXTRACTOR) {
            	var objsModel = (SettingsModelStringArray) ( components[objsPos].getModel() );
            	workflow.checkMaxObjectives( objsModel );
            }
            
        }
}

