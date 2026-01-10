package cu.edu.cujae.daf.knime.nodes.numeric;

import cu.edu.cujae.daf.knime.nodes.GenericDinosKnimeDialog;
import cu.edu.cujae.daf.knime.nodes.GenericDinosKnimeWorkflow;

/**
 * Configuration interface for nominal subgroup discovery,
 * AKA a String as target
 *
 * Doesn't have any extra options, it just filters strings
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
	
}