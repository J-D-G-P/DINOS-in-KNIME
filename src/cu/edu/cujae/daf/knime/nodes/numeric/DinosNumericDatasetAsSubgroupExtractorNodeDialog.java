package cu.edu.cujae.daf.knime.nodes.numeric;

import cu.edu.cujae.daf.knime.nodes.GenericDinosKnimeWorkflow.DINOS_NODE;

/**
 * Configuration interface for numeric dataset extractor in {@link DinosNumericDatasetAsSubgroupExtractorNodeModel},
 * AKA a Number as target
 *
 * Doesn't have any extra options, it just filters numbers
 * as targets as provided by the workflow
 * 
 * @author Jonathan David González Pereda, CUJAE
 */

public class DinosNumericDatasetAsSubgroupExtractorNodeDialog extends DinosNumericSubgroupDiscoveryNodeDialog {

    /**
     * New pane for configuring the DinosNominalDatasetAsSubgroupExtractor node.
     */
    protected DinosNumericDatasetAsSubgroupExtractorNodeDialog() {

    }
    
    @Override
    protected DINOS_NODE getMode() { return DINOS_NODE.EXTRACTOR; }
}

