package cu.edu.cujae.daf.knime.nodes.nominal;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.knime.core.data.BooleanValue;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataValue;
import org.knime.core.data.NominalValue;
import org.knime.core.data.StringValue;
import org.knime.core.data.def.BooleanCell;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.workflow.VariableType.IntType;
import org.knime.core.node.workflow.VariableType.StringType;
import org.knime.core.util.workflowsummary.WorkflowSummary.Workflow;

import cu.edu.cujae.daf.codification.DiscreteProperty;
import cu.edu.cujae.daf.codification.individual.Individual;
import cu.edu.cujae.daf.context.dataset.Attribute;
import cu.edu.cujae.daf.context.dataset.BooleanAttribute;
import cu.edu.cujae.daf.context.dataset.Dataset;
import cu.edu.cujae.daf.context.dataset.DiscClassDataset;
import cu.edu.cujae.daf.core.Algorithm;
import cu.edu.cujae.daf.core.DiscoveryMode;
import cu.edu.cujae.daf.core.DiscreteMode$;
import cu.edu.cujae.daf.core.Subgroup;
import cu.edu.cujae.daf.formatter.FormatSymbols;
import cu.edu.cujae.daf.knime.nodes.GenericDinosKnimeModel;
import cu.edu.cujae.daf.knime.nodes.GenericDinosKnimeWorkflow;
import cu.edu.cujae.daf.utils.SubgroupParser;

/**
 * Contains all methods and constants for interfacing with the DAF library
 * This is for the single target nominal/discrete mode, AKA "A string in target"
 * 
 * @author Jonathan David González Pereda, CUJAE
 */

	@SuppressWarnings("static-access")
public class NominalDinosKnimeWorkflow extends GenericDinosKnimeWorkflow {

		/** The Singleton Instance */
	public static final GenericDinosKnimeWorkflow INSTANCE_NOMINAL = new NominalDinosKnimeWorkflow();

		/* The Helper Instance **/
	public static final DiscoveryMode MODE_NOMINAL = DiscreteMode$.MODULE$;
	
	@Override
	public Class<? extends DataValue> getThenTargetType() {return StringValue.class;}
	
		// Variable to store the cells type supported by this as target
		@SuppressWarnings("unchecked")
	public static final Class<? extends DataValue>[] TARGETS_NOMINAL = new Class[] {StringValue.class, BooleanValue.class};

		/** {@inheritDoc} */
	@Override
	public DiscoveryMode getModeHelper() { return DiscreteMode$.MODULE$;}
	
		/** {@inheritDoc} */
	@Override
	public Class<? extends DataValue>[] getAceptedTargetTypes() { return this.TARGETS_NOMINAL; }
	
		/** Constructor, nothing to initialize */
	private NominalDinosKnimeWorkflow() {}

		@Override
		/** {@inheritDoc} */
	public BufferedDataTable subgroupInformation(Algorithm dinos, Dataset dataset, ExecutionContext exec) {
			// Store the column names and types
		List<DataColumnSpec> outputSpecs = columnSpecs(dinos, dataset);

			// Mode Helper
		var mode = dataset.modeHelper();
		
			// Create the new table
		BufferedDataContainer container = exec.createDataContainer( new DataTableSpec( outputSpecs.toArray(new DataColumnSpec[outputSpecs.size()]) ) );

			// Get the subgroups
		Subgroup[] subgroupsList = dinos.externalPopulation();

			// Now, fill the table
		for(int count = 0 ; count < subgroupsList.length ; ++count) {
			List<DataCell> cells = new ArrayList<>();
			Individual currentSubgroup = subgroupsList[count].typePattern();
			currentSubgroup.mainClass();
			
				// Description of Subgroup
			cells.add(new StringCell( mode.bodyString(currentSubgroup, dataset) ) );
			
				// If Target is Included or Not
			var inOrNot = BooleanCell.TRUE;
			if (currentSubgroup.mainClass().positive() ) { inOrNot = BooleanCell.TRUE ; } else { inOrNot = BooleanCell.FALSE ; }
			cells.add( inOrNot );
			
				// Value of the target Variable
			String hola = dataset.classAtt().value( currentSubgroup.mainClass().value().left().getOrElse(null) );
			cells.add( new StringCell( hola ) );
			
				// Full Subgroup Description
			cells.add( new StringCell( mode.fullRuleString(currentSubgroup, dataset) ) );
			
				// True Positives
			cells.add( new IntCell( currentSubgroup.contingencyMatrix().tp() ) );
			
				// Coverage
			cells.add( new IntCell( currentSubgroup.contingencyMatrix().coverage() ) );
			
				// Now, add a column for each metric
			addMetricCellsToRow(cells, dinos, currentSubgroup);
			
				// Create the row and add it
			DataRow row = new DefaultRow( "Row" + count , cells);
			container.addRowToTable(row);
		}
			// Finally, return the table
		container.close();
		return container.getTable();
	}

			/** {@inheritDoc} */
		@Override
		public List<DataColumnSpec> columnSpecs(Algorithm dinos, Dataset dataset) {
					// Store the columns of the output column
			List<DataColumnSpec> outputSpecs = new ArrayList<>();
					// Description of Subgroup
			outputSpecs.add( new DataColumnSpecCreator( this.RESULT_SUBGROUP_BODY, StringCell.TYPE).createSpec() );
					// If Target is Included or Not
			outputSpecs.add( new DataColumnSpecCreator( this.RESULT_IN, BooleanCell.TYPE).createSpec() );
					// Value of the target Variable
			outputSpecs.add( new DataColumnSpecCreator( this.RESULT_CLASS +" (" + dataset.classAtt().name() +  ")", StringCell.TYPE).createSpec() );
					// Full Subgroup Description
			outputSpecs.add( new DataColumnSpecCreator( this.RESULT_SUBGROUP_FULL, StringCell.TYPE).createSpec() );
					// True Positives
			outputSpecs.add( new DataColumnSpecCreator( this.RESULT_TP, IntCell.TYPE).createSpec() );
					// Coverage
			outputSpecs.add( new DataColumnSpecCreator( this.RESULT_COV, IntCell.TYPE).createSpec() );
			
					// Now, add a column for each metric
			addMetricsToCandidateRow(outputSpecs, dinos);
			
			return outputSpecs;
		}
		
			/** {@inheritDoc} */
		@Override
		protected void addTargetSpecs(Dataset dataset, List<DataColumnSpec> outputSpecs) {
			
			var currentAttribute = dataset.classAtt();
			var currentName = DEFAULT_PREDICTION + " (" + currentAttribute.name() + ")";
			
			addNominalSpec(outputSpecs, currentAttribute, currentName);
		}
		
			/** {@inheritDoc} */
		@SuppressWarnings("boxing")
		@Override
		protected void addPrediction(List<DataCell> cells, Individual currentSubgroup, Dataset dataset) {
			
			if(currentSubgroup == null) 
				super.addPrediction(cells, currentSubgroup, dataset);
			else {
				Attribute attribute = dataset.classAtt();
				var property = ( (DiscreteProperty) currentSubgroup.mainClass() );
				double value = property.classIndex();
				addNominalCell(cells, attribute, value);
			}
		}

			/** {@inheritDoc} */
		@Override
		protected void addSpecificResultsVariables(
				GenericDinosKnimeModel nodeModel,
				Algorithm dinos,
				Dataset dataset) {

				DiscClassDataset discreteDataset = (DiscClassDataset) dataset;
				Attribute target1attribute = dataset.classAtt();
				String target1OriginalName = target1attribute.name();
				String target1Prefix = "target_1_";
				String target1name =  target1Prefix + target1OriginalName; 
				String[] target1Classes = target1attribute.values();
				int[] target1Support = discreteDataset._classSupport();
				
				nodeModel.addResultVariables( target1name + "_numClasses" , IntType.INSTANCE, target1Classes.length );
				
				for(int countClass = 0 ; countClass < target1Classes.length ; ++countClass)
					nodeModel.addResultVariables( target1name + "_class_" + target1Classes[countClass] + "_amount" , IntType.INSTANCE, target1Support[countClass] );

				nodeModel.addResultVariables(target1Prefix.substring(0, target1Prefix.length() - 1), StringType.INSTANCE, target1OriginalName);
				
				return;
			
		}
	
}
