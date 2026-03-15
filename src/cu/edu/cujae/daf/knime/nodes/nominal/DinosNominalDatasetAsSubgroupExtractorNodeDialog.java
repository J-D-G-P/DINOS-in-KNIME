package cu.edu.cujae.daf.knime.nodes.nominal;

import cu.edu.cujae.daf.knime.nodes.GenericDinosKnimeDialog;
import cu.edu.cujae.daf.knime.nodes.GenericDinosKnimeWorkflow.DINOS_NODE;

	/**
	 * Configuration interface for nominal dataset extraction in {@link DinosNominalDatasetAsSubgroupExtractorNodeModel},
	 * AKA a String as target
	 *
	 * Doesn't have any extra options, it just filters strings
	 * as targets as provided by the workflow
	 * 
	 * @author Jonathan David González Pereda, CUJAE
	 */

public class DinosNominalDatasetAsSubgroupExtractorNodeDialog extends DinosNominalSubgroupDiscoveryNodeDialog {

    /**
     * New pane for configuring the DinosNominalDatasetAsSubgroupExtractor node.
     */
    protected DinosNominalDatasetAsSubgroupExtractorNodeDialog() {

    }
    
    @Override
    protected DINOS_NODE getMode() { return DINOS_NODE.EXTRACTOR; }
    
}

