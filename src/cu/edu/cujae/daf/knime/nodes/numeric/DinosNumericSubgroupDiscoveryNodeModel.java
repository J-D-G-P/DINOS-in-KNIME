package cu.edu.cujae.daf.knime.nodes.numeric;

import org.knime.core.node.port.PortType;

import cu.edu.cujae.daf.knime.nodes.GenericDinosKnimeModel;
import cu.edu.cujae.daf.knime.nodes.GenericDinosKnimeWorkflow;
import cu.edu.cujae.daf.knime.nodes.GenericDinosKnimeWorkflow.NODES_TYPES;

/**
 * Default configuration logic for nominal subgroup discovery,
 * AKA a String as target
 * 
 * Methods has been made as modular as possible to make
 *  extending and replacing the class's functions easier
 * 
 * @author Jonathan David González Pereda, CUJAE
 */
public class DinosNumericSubgroupDiscoveryNodeModel extends GenericDinosKnimeModel {

	@Override
	protected GenericDinosKnimeWorkflow getInstance() { return NumericDinosKnimeWorkflow.INSTANCE_NUMERIC; }
	
	@Override
	protected NODES_TYPES getMode() { return NODES_TYPES.DISCOVERY;	}
	
	 protected DinosNumericSubgroupDiscoveryNodeModel() {
	    	super();
	    }
	
		// Constructor for default value
	protected DinosNumericSubgroupDiscoveryNodeModel(final PortType[] inPortTypes, final PortType[] outPortTypes) {
	 	super(inPortTypes , outPortTypes);
	}
 
} 

