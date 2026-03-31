package cu.edu.cujae.daf.knime.nodes.testing;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * This is an example implementation of the node factory of the
 * "DinosRandomDatasetCreator" node.
 *
 * @author 
 */
public class DinosRandomDatasetCreatorNodeFactory 
        extends NodeFactory<DinosRandomDatasetCreatorNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public DinosRandomDatasetCreatorNodeModel createNodeModel() {
		// Create and return a new node model.
        return new DinosRandomDatasetCreatorNodeModel();
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
    public NodeView<DinosRandomDatasetCreatorNodeModel> createNodeView(final int viewIndex,
            final DinosRandomDatasetCreatorNodeModel nodeModel) {
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
        return new DinosRandomDatasetCreatorNodeDialog();
    }

}

