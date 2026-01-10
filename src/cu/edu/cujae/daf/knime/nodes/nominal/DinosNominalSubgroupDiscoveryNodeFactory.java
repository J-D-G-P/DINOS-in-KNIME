package cu.edu.cujae.daf.knime.nodes.nominal;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * Node factory of the "DinosNominalSubgroupDiscovery" node
 * 
 * Doesn't have any extra options, just the standard stuff
 * provided by the workflow
 *
 * @author Jonathan David González Pereda, CUJAE
 */
public class DinosNominalSubgroupDiscoveryNodeFactory 
        extends NodeFactory<DinosNominalSubgroupDiscoveryNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public DinosNominalSubgroupDiscoveryNodeModel createNodeModel() {
        return new DinosNominalSubgroupDiscoveryNodeModel();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNrNodeViews() {
		// No views, for now
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeView<DinosNominalSubgroupDiscoveryNodeModel> createNodeView(final int viewIndex,
            final DinosNominalSubgroupDiscoveryNodeModel nodeModel) {
		// Currently does not provide a view.
		return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasDialog() {
		// Node has Dialog
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeDialogPane createNodeDialogPane() {
        return new DinosNominalSubgroupDiscoveryNodeDialog();
    }

}

