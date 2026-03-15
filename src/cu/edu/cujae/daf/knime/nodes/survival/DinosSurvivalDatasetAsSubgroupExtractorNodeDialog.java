package cu.edu.cujae.daf.knime.nodes.survival;

import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;

import cu.edu.cujae.daf.knime.nodes.KnimeTableToDinosDataset;
import cu.edu.cujae.daf.knime.nodes.GenericDinosKnimeWorkflow.DINOS_NODE;

/**
 * Configuration interface for survival subgroup extractor in {@link DinosSurvivalDatasetAsSubgroupExtractorNodeModel},
 * AKA a numeric time to to event as target with censoring information
 *
 * Doesn't have any extra options beyond what was added
 * in {@link DinosSurvivalSubgroupDiscoveryNodeDialog}
 * 
 * @author Jonathan David González Pereda, CUJAE
 */

public class DinosSurvivalDatasetAsSubgroupExtractorNodeDialog extends DinosSurvivalSubgroupDiscoveryNodeDialog {

    /**
     * New pane for configuring the DinosNominalDatasetAsSubgroupExtractor node.
     */
    protected DinosSurvivalDatasetAsSubgroupExtractorNodeDialog() {

    }
    
    @Override
    protected DINOS_NODE getMode() { return DINOS_NODE.EXTRACTOR; }
}

