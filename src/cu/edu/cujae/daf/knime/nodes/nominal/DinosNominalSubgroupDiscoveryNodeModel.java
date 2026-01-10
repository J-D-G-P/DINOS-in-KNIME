package cu.edu.cujae.daf.knime.nodes.nominal;

import cu.edu.cujae.daf.knime.nodes.GenericDinosKnimeModel;
import cu.edu.cujae.daf.knime.nodes.GenericDinosKnimeWorkflow;

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
	
} 

