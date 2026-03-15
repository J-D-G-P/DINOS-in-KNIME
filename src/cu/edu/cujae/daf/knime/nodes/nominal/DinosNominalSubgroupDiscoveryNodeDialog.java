package cu.edu.cujae.daf.knime.nodes.nominal;

import cu.edu.cujae.daf.knime.nodes.GenericDinosKnimeDialog;
import cu.edu.cujae.daf.knime.nodes.GenericDinosKnimeWorkflow;
import cu.edu.cujae.daf.knime.nodes.GenericDinosKnimeWorkflow.DINOS_NODE;

/**
 * Configuration interface for nominal subgroup discovery in {@link DinosNominalSubgroupDiscoveryNodeModel},
 * AKA a String as target
 *
 * Doesn't have any extra options, it just filters strings
 * as targets as provided by the workflow
 * 
 * @author Jonathan David González Pereda, CUJAE
 */

public class DinosNominalSubgroupDiscoveryNodeDialog extends GenericDinosKnimeDialog {


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected GenericDinosKnimeWorkflow getInstance() { return NominalDinosKnimeWorkflow.INSTANCE_NOMINAL; }

	@Override
	protected DINOS_NODE getMode() { return DINOS_NODE.DISCOVERY; }
	
}