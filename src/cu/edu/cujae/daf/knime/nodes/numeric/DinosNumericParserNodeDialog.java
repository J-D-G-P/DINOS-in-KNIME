package cu.edu.cujae.daf.knime.nodes.numeric;

import cu.edu.cujae.daf.knime.nodes.GenericDinosKnimeWorkflow.DINOS_NODE;

/**
 * Configuration interface for numeric parser in {@link DinosNumericParserNodeModel},
 * AKA a Number as target
 *
 * Doesn't have any extra options, it just filters numbers
 * as targets as provided by the workflow
 * 
 * @author Jonathan David González Pereda, CUJAE
 */

public class DinosNumericParserNodeDialog extends DinosNumericSubgroupDiscoveryNodeDialog {

    /**
     * New pane for configuring the DinosNominalParser node.
     */
	
    protected DinosNumericParserNodeDialog() {

    }

	protected DINOS_NODE getMode() { return DINOS_NODE.PARSER; }
}