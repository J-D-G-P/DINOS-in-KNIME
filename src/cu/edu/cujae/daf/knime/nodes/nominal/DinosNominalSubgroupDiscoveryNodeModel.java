package cu.edu.cujae.daf.knime.nodes.nominal;

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
public class DinosNominalSubgroupDiscoveryNodeModel extends GenericDinosKnimeModel {

	@Override
	protected GenericDinosKnimeWorkflow getInstance() { return NominalDinosKnimeWorkflow.INSTANCE_NOMINAL; }

	@Override
	protected NODES_TYPES getMode() { return NODES_TYPES.DISCOVERY;	}
	
	 protected DinosNominalSubgroupDiscoveryNodeModel() {
	    	super();
	    }
	
		// Constructor for parser
    protected DinosNominalSubgroupDiscoveryNodeModel(final PortType[] inPortTypes, final PortType[] outPortTypes) {
    	super(inPortTypes , outPortTypes);
    }
	
} 

