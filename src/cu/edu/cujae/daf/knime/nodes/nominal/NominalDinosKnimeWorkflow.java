package cu.edu.cujae.daf.knime.nodes.nominal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataValue;
import org.knime.core.data.NominalValue;
import org.knime.core.data.def.BooleanCell;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.workflow.VariableType.IntType;
import org.knime.core.node.workflow.VariableType.StringType;

import cu.edu.cujae.daf.codification.individual.Individual;
import cu.edu.cujae.daf.context.dataset.Attribute;
import cu.edu.cujae.daf.context.dataset.Dataset;
import cu.edu.cujae.daf.context.dataset.DiscClassDataset;
import cu.edu.cujae.daf.context.dataset.Instance;
import cu.edu.cujae.daf.context.dataset.Dataset.ClassType;
import cu.edu.cujae.daf.context.dataset.Dataset.DiscreteClass$;
import cu.edu.cujae.daf.core.Algorithm;
import cu.edu.cujae.daf.core.Subgroup;
import cu.edu.cujae.daf.knime.nodes.GenericDinosKnimeModel;
import cu.edu.cujae.daf.knime.nodes.GenericDinosKnimeWorkflow;

/**
 * Contains all methods and constants for interfacing with the DAF library
 * This is the single target nominal/discrete, AKA "A string in target"
 * 
 * @author Jonathan David González Pereda, CUJAE
 */

	@SuppressWarnings("static-access")
public class NominalDinosKnimeWorkflow extends GenericDinosKnimeWorkflow {

		// The Singleton Instance
	public static final GenericDinosKnimeWorkflow INSTANCE_NOMINAL = new NominalDinosKnimeWorkflow();

		// Variable to store the cells type supported by this as target
		@SuppressWarnings("unchecked")
	public static final Class<? extends DataValue>[] TARGETS_NOMINAL = new Class[] {NominalValue.class};

		/**
		 * {@inheritDoc}
		 */
	@Override
	public ClassType getClassType() { return DiscreteClass$.MODULE$;}
	
		/**
		 * {@inheritDoc}
		 */
	@Override
	public Class<? extends DataValue>[] getAceptedTargetTypes() { return this.TARGETS_NOMINAL; }
	
		// Constructor, nothing to initialize
	private NominalDinosKnimeWorkflow() {}
	
		/**
		 * {@inheritDoc}
		 */
	@Override
	public Map< String , Map < String , String > >getExclusiveClasses() {
		 Map< String , Map < String , String > > result = new HashMap<String, Map<String, String>>();
		 result.put( super.INFO_COMPONENTS[0]._1, null );
		 result.put( super.INFO_COMPONENTS[1]._1, null );
		 result.put( super.INFO_COMPONENTS[2]._1, null );
		 result.put( super.INFO_COMPONENTS[3]._1, Map.of(

				 	"CertaintyFactor" , "Certainty Factor"
				 ) );
		 result.put( super.INFO_COMPONENTS[4]._1 ,Map.of(

				 	"CertaintyFactorSelector" , "Certainty Factor Selector",
				 	"GreaterLiftSelector" , "Greater Lift Selector"
				 ) );
		 result.put( super.INFO_COMPONENTS[5]._1 , null );
		 result.put( super.INFO_COMPONENTS[6]._1 , null );
		 result.put( super.INFO_COMPONENTS[7]._1 , null );
		 	// Skip Subgroup Formatter
		 result.put( super.INFO_COMPONENTS[9]._1 , null );
		 result.put( super.INFO_COMPONENTS[10]._1 , null );
		 result.put( super.INFO_COMPONENTS[11]._1 , null );
		 result.put( super.INFO_COMPONENTS[12]._1, null );
		 result.put( super.INFO_COMPONENTS[13]._1, null
				 );
		 result.values();

		 return result;
	}
	
		/**
		 * {@inheritDoc}
		 */
	@Override
	public Map< String , Map < String , String[] > > getExclusiveSettings() {
			// There are no discrete only classes with configuration parameters
		return new LinkedHashMap<String, Map<String, String[]>>();
	}
	
		/**
		 * {@inheritDoc}
		 */
	@Override
	public Algorithm defaultAlgorithmSettings() {
		return Algorithm.classicDinos();
	}

		/**
		 * {@inheritDoc}
		 */
	@Override
	public scala.collection.immutable.Map<String, String[]> getDefaultClasses() {
			return Algorithm.getDiscreteDefaultParameters();
	}

		/**
		 * {@inheritDoc}
		 */
	@Override
	protected Algorithm getDefaultAlgorithm() {
		return Algorithm.classicDinos();
	}

		/**
		 * This mode have true positives, coverage and metrics info
		 * 
		 * {@inheritDoc}
		 */
		@Override
	public BufferedDataTable subgroupInformation(Algorithm dinos, Dataset dataset, ExecutionContext exec) {
			// Store the column names and types
		List<DataColumnSpec> outputSpecs = columnSpecs(dinos, dataset);

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
			cells.add(new StringCell( subgroupConditionsToString(dataset, currentSubgroup) ) );
				// If Target is Included or Not
			var inOrNot = BooleanCell.TRUE;
			if (currentSubgroup.mainClass().positive() ) { inOrNot = BooleanCell.TRUE ; } else { inOrNot = BooleanCell.FALSE ; }
			cells.add( inOrNot );
			
				// Value of the target Variable
			String hola = dataset.classAtt().value( currentSubgroup.mainClass().value().left().getOrElse(null) );
			cells.add( new StringCell( hola ) );
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

			/**
			 * {@inheritDoc}
			 */
		@Override
		public List<DataColumnSpec> columnSpecs(Algorithm dinos, Dataset dataset) {
					// Store the columns of the output column
			List<DataColumnSpec> outputSpecs = new ArrayList<>();
					// Description of Subgroup
			outputSpecs.add( new DataColumnSpecCreator("Subgroup", StringCell.TYPE).createSpec() );
					// If Target is Included or Not
			outputSpecs.add( new DataColumnSpecCreator("In", BooleanCell.TYPE).createSpec() );
					// Value of the target Variable
			outputSpecs.add( new DataColumnSpecCreator("Class (" + dataset.classAtt().name() +  ")", StringCell.TYPE).createSpec() );
					// True Positives
			outputSpecs.add( new DataColumnSpecCreator("True Positives", IntCell.TYPE).createSpec() );
					// Coverage
			outputSpecs.add( new DataColumnSpecCreator("Covered", IntCell.TYPE).createSpec() );
			
					// Now, add a column for each metric
			addMetricsToCandidateRow(outputSpecs, dinos);
			
			return outputSpecs;
		}
		
			/**
			 * {@inheritDoc}
			 */
		@Override
		protected void addTargetSpecs(Dataset dataset, List<DataColumnSpec> outputSpecs) {
			outputSpecs.add( new DataColumnSpecCreator( DEFAULT_PREDICTION + " (" + dataset.classAtt().name() + ")" , StringCell.TYPE).createSpec()
					);
		}
		
			/**
			 * 
			 */
		@Override
		protected void addPrediction(List<DataCell> cells, Individual currentSubgroup, Dataset dataset) {
			cells.add( new StringCell( dataset.classAtt().value( currentSubgroup.mainClass().value().left().getOrElse(null) ) ) );
		}

		
			/**
			 * {@inheritDoc}
			 */
		
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
