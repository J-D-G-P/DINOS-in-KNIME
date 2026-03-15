package cu.edu.cujae.daf.knime.nodes.numeric;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

	/**
	 * Node factory of the {@link DinosNumericParserNodeModel} node
	 * 
	 * Doesn't have any extra options, just the standard stuff
	 * provided by the workflow
	 *
	 * @author Jonathan David González Pereda, CUJAE
	 */
public class DinosNumericParserNodeFactory 
        extends NodeFactory<DinosNumericParserNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public DinosNumericParserNodeModel createNodeModel() {
		// Create and return a new node model.
        return new DinosNumericParserNodeModel();
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
    public NodeView<DinosNumericParserNodeModel> createNodeView(final int viewIndex,
            final DinosNumericParserNodeModel nodeModel) {
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
        return new DinosNumericParserNodeDialog();
    }

}

