package cu.edu.cujae.daf.knime.nodes.survival;

import org.knime.core.node.defaultnodesettings.SettingsModel;

import cu.edu.cujae.daf.knime.nodes.GenericDinosKnimeModel;
import cu.edu.cujae.daf.knime.nodes.GenericDinosKnimeWorkflow.DINOS_NODE;

/**
 * Default execution logic for survival extractor,
 * AKA a numeric time to event as target with censoring information
 * 
 * Doesn't add much beyond the extra survival logic defined in {@link DinosSurvivalSubgroupDiscoveryNodeModel}
 * 
 * @author Jonathan David González Pereda, CUJAE
 */

public class DinosSurvivalDatasetAsSubgroupExtractorNodeModel extends DinosSurvivalSubgroupDiscoveryNodeModel {
    
    	
    protected DinosSurvivalDatasetAsSubgroupExtractorNodeModel() {

    }
    
    @Override
	protected DINOS_NODE getMode() { return DINOS_NODE.EXTRACTOR;	}
    
    	// TODO Check this duplicate
    private SettingsModel[] MODELS_PARSE_SURVIVALEXTRACTOR = {target_class , desc_class };
    @Override
    protected SettingsModel[] getModelsParse() { return MODELS_PARSE_SURVIVALEXTRACTOR; }
}