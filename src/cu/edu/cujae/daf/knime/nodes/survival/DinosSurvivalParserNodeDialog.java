package cu.edu.cujae.daf.knime.nodes.survival;

import cu.edu.cujae.daf.knime.nodes.GenericDinosKnimeWorkflow.DINOS_NODE;

/**
 * Configuration interface for survival parser in {@link DinosSurvivalParserNodeModel},
 * AKA a numeric time to to event as target with censoring information
 *
 * Doesn't have any extra options beyond what was added
 * in {@link DinosSurvivalSubgroupDiscoveryNodeDialog}
 * 
 * @author Jonathan David González Pereda, CUJAE
 */

public class DinosSurvivalParserNodeDialog extends DinosSurvivalSubgroupDiscoveryNodeDialog {

    /**
     * New pane for configuring the DinosNominalParser node.
     */
	
    protected DinosSurvivalParserNodeDialog() {

    }

	protected DINOS_NODE getMode() { return DINOS_NODE.PARSER; }
	
	/*
	 * TODO Check this
	 * 
	 * @Override protected void addInClass(GenericDinosKnimeDialog panel) { // Do
	 * nothing! }
	 * 
	 * protected void addThenClass(GenericDinosKnimeDialog panel) { // Do nothing! }
	 */
}