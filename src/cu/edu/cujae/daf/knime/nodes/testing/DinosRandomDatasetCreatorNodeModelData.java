package cu.edu.cujae.daf.knime.nodes.testing;

import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

public class DinosRandomDatasetCreatorNodeModelData {
	public SettingsModelIntegerBounded amountOfRows;
	public SettingsModelString typeOfTarget;
	public SettingsModelDoubleBounded attributeMissingProb;
	public SettingsModelDoubleBounded classMissingProb;

	public DinosRandomDatasetCreatorNodeModelData(SettingsModelIntegerBounded amountOfRows,
			SettingsModelString typeOfTarget, SettingsModelDoubleBounded attributeMissingProb,
			SettingsModelDoubleBounded classMissingProb) {
		this.amountOfRows = amountOfRows;
		this.typeOfTarget = typeOfTarget;
		this.attributeMissingProb = attributeMissingProb;
		this.classMissingProb = classMissingProb;
	}
}