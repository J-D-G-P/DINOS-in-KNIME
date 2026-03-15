package cu.edu.cujae.daf.knime.nodes.survival;

import cu.edu.cujae.daf.knime.nodes.GenericDinosKnimeWorkflow;
import cu.edu.cujae.daf.knime.nodes.GenericDinosKnimeWorkflow.DINOS_NODE;

/**
 * Default execution logic for survival extractor,
 * AKA a numeric time to event as target with censoring information
 * 
 * Doesn't add much beyond the extra survival logic defined in {@link DinosSurvivalSubgroupDiscoveryNodeModel}
 * 
 * @author Jonathan David González Pereda, CUJAE
 */
public class DinosSurvivalParserNodeModel extends DinosSurvivalSubgroupDiscoveryNodeModel {

	public DinosSurvivalParserNodeModel() {
    	super(GenericDinosKnimeWorkflow.PORT_INPUT_PARSER , GenericDinosKnimeWorkflow.PORT_OUTPUT_TYPES);
	}
	
	@Override
	protected DINOS_NODE getMode() { return DINOS_NODE.PARSER;	}

}