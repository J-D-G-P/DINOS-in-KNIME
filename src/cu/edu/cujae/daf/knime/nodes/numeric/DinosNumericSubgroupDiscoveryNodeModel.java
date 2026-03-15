package cu.edu.cujae.daf.knime.nodes.numeric;

import org.knime.core.node.port.PortType;

import cu.edu.cujae.daf.knime.nodes.GenericDinosKnimeModel;
import cu.edu.cujae.daf.knime.nodes.GenericDinosKnimeWorkflow;
import cu.edu.cujae.daf.knime.nodes.GenericDinosKnimeWorkflow.DINOS_NODE;

/**
 * Default execution logic for numeric subgroup discovery,
 * AKA a Number as target
 * 
 * Doesn't have any extra logic beyond what's in {@link GenericDinosKnimeModel}
 * 
 * @author Jonathan David González Pereda, CUJAE
 */

public class DinosNumericSubgroupDiscoveryNodeModel extends GenericDinosKnimeModel {

	@Override
	protected GenericDinosKnimeWorkflow getInstance() { return NumericDinosKnimeWorkflow.INSTANCE_NUMERIC; }
	
	@Override
	protected DINOS_NODE getMode() { return DINOS_NODE.DISCOVERY;	}
	
	 protected DinosNumericSubgroupDiscoveryNodeModel() {
	    	super();
	    }
	
		// Constructor for default value
	protected DinosNumericSubgroupDiscoveryNodeModel(final PortType[] inPortTypes, final PortType[] outPortTypes) {
	 	super(inPortTypes , outPortTypes);
	}
 
} 

