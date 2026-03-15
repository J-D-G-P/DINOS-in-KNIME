package cu.edu.cujae.daf.knime.nodes.numeric;

import cu.edu.cujae.daf.knime.nodes.GenericDinosKnimeModel;
import cu.edu.cujae.daf.knime.nodes.GenericDinosKnimeWorkflow.DINOS_NODE;

/**
 * Default execution logic for numeric extractor,
 * AKA a Number as target
 * 
 * Doesn't have any extra logic beyond what's in {@link GenericDinosKnimeModel}
 * 
 * @author Jonathan David González Pereda, CUJAE
 */

public class DinosNumericDatasetAsSubgroupExtractorNodeModel extends DinosNumericSubgroupDiscoveryNodeModel {
    
    	
    protected DinosNumericDatasetAsSubgroupExtractorNodeModel() {

    }
    
    @Override
	protected DINOS_NODE getMode() { return DINOS_NODE.EXTRACTOR;	}

}