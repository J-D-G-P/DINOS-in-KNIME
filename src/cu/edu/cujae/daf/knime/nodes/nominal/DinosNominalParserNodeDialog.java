package cu.edu.cujae.daf.knime.nodes.nominal;

import cu.edu.cujae.daf.knime.nodes.GenericDinosKnimeWorkflow.DINOS_NODE;

	/**
	 * Configuration interface for nominal parser in {@link DinosNominalParserNodeModel},
	 * AKA a String as target
	 *
	 * Doesn't have any extra options, it just filters strings
	 * as targets as provided by the workflow
	 * 
	 * @author Jonathan David González Pereda, CUJAE
	 */

public class DinosNominalParserNodeDialog extends DinosNominalSubgroupDiscoveryNodeDialog {

    /**
     * New pane for configuring the DinosNominalParser node.
     */
	
    protected DinosNominalParserNodeDialog() {

    }

	protected DINOS_NODE getMode() { return DINOS_NODE.PARSER; }
}

