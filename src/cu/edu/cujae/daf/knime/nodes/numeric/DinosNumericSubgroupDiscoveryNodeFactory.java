package cu.edu.cujae.daf.knime.nodes.numeric;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

	/**
	 * Node factory of the {@link DinosNumericSubgroupDiscoveryNodeModel} node
	 * 
	 * Doesn't have any extra options, just the standard stuff
	 * provided by the workflow
	 *
	 * @author Jonathan David González Pereda, CUJAE
	 */
public class DinosNumericSubgroupDiscoveryNodeFactory 
        extends NodeFactory<DinosNumericSubgroupDiscoveryNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public DinosNumericSubgroupDiscoveryNodeModel createNodeModel() {
		// Create and return a new node model.
        return new DinosNumericSubgroupDiscoveryNodeModel();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNrNodeViews() {
		// The number of views the node should have, in this cases there is none.
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeView<DinosNumericSubgroupDiscoveryNodeModel> createNodeView(final int viewIndex,
            final DinosNumericSubgroupDiscoveryNodeModel nodeModel) {
		// We return null as this example node does not provide a view. Also see "getNrNodeViews()".
		return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasDialog() {
		// Indication whether the node has a dialog or not.
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeDialogPane createNodeDialogPane() {
		// This example node has a dialog, hence we create and return it here. Also see "hasDialog()".
        return new DinosNumericSubgroupDiscoveryNodeDialog();
    }

}

