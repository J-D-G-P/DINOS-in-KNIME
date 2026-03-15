package cu.edu.cujae.daf.knime.nodes.nominal;

import cu.edu.cujae.daf.knime.nodes.GenericDinosKnimeModel;
import cu.edu.cujae.daf.knime.nodes.GenericDinosKnimeWorkflow.DINOS_NODE;

/**
 * Default execution logic for nominal extractor,
 * AKA a String as target
 * 
 * Doesn't have any extra logic beyond what's in {@link GenericDinosKnimeModel}
 * 
 * @author Jonathan David González Pereda, CUJAE
 */

public class DinosNominalDatasetAsSubgroupExtractorNodeModel extends DinosNominalSubgroupDiscoveryNodeModel {
    
    	
    protected DinosNominalDatasetAsSubgroupExtractorNodeModel() {

    }
    
    @Override
	protected DINOS_NODE getMode() { return DINOS_NODE.EXTRACTOR;	}

}

