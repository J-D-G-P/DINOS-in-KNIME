package cu.edu.cujae.daf.knime.nodes;

import org.knime.core.node.workflow.VariableType;

public interface AddExternalFlowVariables {

	public <T> void addResultVariables(final String name, final VariableType<T> type, final T value);
	
}
