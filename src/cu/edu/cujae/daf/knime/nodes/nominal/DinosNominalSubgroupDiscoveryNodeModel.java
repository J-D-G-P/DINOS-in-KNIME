package cu.edu.cujae.daf.knime.nodes.nominal;

import org.knime.core.node.port.PortType;

import cu.edu.cujae.daf.knime.nodes.GenericDinosKnimeModel;
import cu.edu.cujae.daf.knime.nodes.GenericDinosKnimeWorkflow;
import cu.edu.cujae.daf.knime.nodes.GenericDinosKnimeWorkflow.DINOS_NODE;

/**
 * Default execution logic for nominal subgroup discovery,
 * AKA a String as target
 * 
 * Doesn't have any extra logic beyond what's in {@link GenericDinosKnimeModel}
 * 
 * @author Jonathan David González Pereda, CUJAE
 */

public class DinosNominalSubgroupDiscoveryNodeModel extends GenericDinosKnimeModel {

	@Override
	protected GenericDinosKnimeWorkflow getInstance() { return NominalDinosKnimeWorkflow.INSTANCE_NOMINAL; }

	@Override
	protected DINOS_NODE getMode() { return DINOS_NODE.DISCOVERY;	}
	
	protected DinosNominalSubgroupDiscoveryNodeModel() {
	    	super();
	    }
	
		// Constructor for default value
    protected DinosNominalSubgroupDiscoveryNodeModel(final PortType[] inPortTypes, final PortType[] outPortTypes) {
    	super(inPortTypes , outPortTypes);
    }
	
} 

