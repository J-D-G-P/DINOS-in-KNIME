package cu.edu.cujae.daf.knime.nodes.numeric;

import cu.edu.cujae.daf.knime.nodes.GenericDinosKnimeDialog;
import cu.edu.cujae.daf.knime.nodes.GenericDinosKnimeWorkflow;
import cu.edu.cujae.daf.knime.nodes.GenericDinosKnimeWorkflow.DINOS_NODE;

/**
 * Configuration interface for numeric subgroup discovery in {@link DinosNumericSubgroupDiscoveryNodeModel},
 * AKA a Number as target
 *
 * Doesn't have any extra options, it just filters numbers
 * as targets as provided by the workflow
 * 
 * @author Jonathan David González Pereda, CUJAE
 */

public class DinosNumericSubgroupDiscoveryNodeDialog extends GenericDinosKnimeDialog {


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected GenericDinosKnimeWorkflow getInstance() { return NumericDinosKnimeWorkflow.INSTANCE_NUMERIC; }

	@Override
	protected DINOS_NODE getMode() { return DINOS_NODE.DISCOVERY; }
	
}