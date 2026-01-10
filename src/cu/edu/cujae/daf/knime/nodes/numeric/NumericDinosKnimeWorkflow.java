package cu.edu.cujae.daf.knime.nodes.numeric;

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
import org.knime.core.data.DoubleValue;
import org.knime.core.data.IntValue;
import org.knime.core.data.def.BooleanCell;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.IntervalCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.workflow.VariableType.DoubleType;

import cu.edu.cujae.daf.codification.NumericMatrix;
import cu.edu.cujae.daf.codification.NumericProperty;
import cu.edu.cujae.daf.codification.individual.Individual;
import cu.edu.cujae.daf.context.dataset.Dataset;
import cu.edu.cujae.daf.context.dataset.NumClassDataset;
import cu.edu.cujae.daf.context.dataset.Dataset.ClassType;
import cu.edu.cujae.daf.context.dataset.Dataset.NumericClass$;
import cu.edu.cujae.daf.core.Algorithm;
import cu.edu.cujae.daf.core.Subgroup;
import cu.edu.cujae.daf.knime.nodes.GenericDinosKnimeModel;
import cu.edu.cujae.daf.knime.nodes.GenericDinosKnimeWorkflow;

/**
 * Contains all methods and constants for interfacing with the DAF library
 * This is the single target numeric, AKA "A number in target"
 * 
 * @author Jonathan David González Pereda, CUJAE
 */

	@SuppressWarnings("static-access")
public class NumericDinosKnimeWorkflow extends GenericDinosKnimeWorkflow {

		// The Singleton Instance
	public static final GenericDinosKnimeWorkflow INSTANCE_NUMERIC = new NumericDinosKnimeWorkflow();
	
		// Variable to store the cells type supported by this as target
		@SuppressWarnings("unchecked")
	public static final Class<? extends DataValue>[] TARGETS_NUMERIC = new Class[] {IntValue.class , DoubleValue.class};

		/**
		 * {@inheritDoc}
		 */
	@Override
	public ClassType getClassType() { return NumericClass$.MODULE$;}
	
		/**
		 * {@inheritDoc}
		 */
	@Override
	public Class<? extends DataValue>[] getAceptedTargetTypes() { return this.TARGETS_NUMERIC; }
	
		// Constructor, nothing to initialize
	private NumericDinosKnimeWorkflow() {}
	
		/**
		 * {@inheritDoc}
		 */
	@Override
	public Map< String , Map < String , String > >getExclusiveClasses() {
		 Map< String , Map < String , String > > result = new HashMap<String, Map<String, String>>();
		 
		 LinkedHashMap<String, String> auxiliar = null;
		 
		 result.put( super.INFO_COMPONENTS[0]._1, null );
		 result.put( super.INFO_COMPONENTS[1]._1, null );
		 result.put( super.INFO_COMPONENTS[2]._1, null );
		 
		 auxiliar = new LinkedHashMap<String, String>();
		 auxiliar.put("InterclassVariance", "Interclass Variance");
		 auxiliar.put("MeanDifference", "Difference between dataset and subgroup mean");
		 auxiliar.put("MedianDifference", "Difference between dataset and subgroup median");
		 auxiliar.put("VarianceReduction", "Variance Reduction");
		 result.put( super.INFO_COMPONENTS[3]._1, auxiliar );
		 
		 auxiliar = new LinkedHashMap<String, String>();
		 auxiliar.put("FullRangeSelector", "Full Range Selector");
		 auxiliar.put("InterquartileRangeSelector", "Interquartile Selector");
		 auxiliar.put("MADRangeSelector", "Median Absolute Deviation Range Selector");
		 result.put( super.INFO_COMPONENTS[4]._1 , auxiliar );
		 
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

		LinkedHashMap<String, String[]> auxiliar = new LinkedHashMap<String, String[]>();
		Map< String , Map < String , String[] > > result = new LinkedHashMap<String, Map<String, String[]>>();

		 result.put( INFO_COMPONENTS[0]._1, null );
		 
		 result.put( INFO_COMPONENTS[1]._1, null );
		 
		 result.put( INFO_COMPONENTS[2]._1, null );

		 auxiliar = new LinkedHashMap<String, String[]>();
		 auxiliar.put("MeanDifference" , new String[]{"MeanDifferenceAVariable"});
		 auxiliar.put("MedianDifference" , new String[]{"MedianDifferenceAVariable"});
		 result.put( INFO_COMPONENTS[3]._1, auxiliar);
		 
		 auxiliar = new LinkedHashMap<String, String[]>();
		 auxiliar.put("MADRangeSelector" , new String[]{"MADRangeFactor", "MADRangeBVariable"} );
		 
		 result.put( INFO_COMPONENTS[4]._1 , auxiliar );
		 
		 result.put( INFO_COMPONENTS[5]._1 , null );
		
		return new LinkedHashMap<String, Map<String, String[]>>();
	}
	
		/**
		 * {@inheritDoc}
		 */
	@Override
	public Algorithm defaultAlgorithmSettings() {
		return Algorithm.numericDinos();
	}

		/**
		 * {@inheritDoc}
		 */
	@Override
	public scala.collection.immutable.Map<String, String[]> getDefaultClasses() {
			return Algorithm.getNumericDefaultParameters();
	}

		/**
		 * {@inheritDoc}
		 */
	@Override
	protected Algorithm getDefaultAlgorithm() {
		return Algorithm.numericDinos();
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
			
				// Use as numeric dataset
			NumClassDataset numericDataset = (NumClassDataset) dataset;
	
				// Get the subgroups
			Subgroup[] subgroupsList = dinos.externalPopulation();
			
				// Now, fill the table
			for(int count = 0 ; count < subgroupsList.length ; ++count) {
				List<DataCell> cells = new ArrayList<>();
				Individual currentSubgroup = subgroupsList[count].typePattern();
				NumericMatrix currentData = (NumericMatrix) currentSubgroup.contingencyMatrix();
				// Description of Subgroup
			cells.add(new StringCell( subgroupConditionsToString(dataset, currentSubgroup) ) );
				// If Target is Included or Not
			//var inOrNot = BooleanCell.TRUE;
			//if (currentSubgroup.mainClass().positive() ) { inOrNot = BooleanCell.TRUE ; } else { inOrNot = BooleanCell.FALSE ; }
			//cells.add( inOrNot );
				// Limits of the Target Interval
			NumericProperty target = (NumericProperty) currentSubgroup.mainClass();
			cells.add( new IntervalCell(target.lowerBound() , target.upperBound() , true , true) );

				// True Positives
			cells.add( new IntCell( currentSubgroup.contingencyMatrix().tp() ) );
				// Coverage
			cells.add( new IntCell( currentSubgroup.contingencyMatrix().coverage() ) );
				// Mean and difference
			cells.add( new DoubleCell( currentData.meanTp() ) );
			cells.add( new DoubleCell( Math.abs( currentData.meanTp() - numericDataset.getMean() ) ) );
				// Median and difference
			cells.add( new DoubleCell( currentData.medianTp() ) );
			cells.add( new DoubleCell( Math.abs( currentData.medianTp() - numericDataset.getMedian() ) ) );
				// Standard Deviation and Difference
			double stdevSubgroup = Math.sqrt( currentData.varianceTp() );
			double stdevDataset = Math.sqrt( numericDataset.getVariance() );
			cells.add( new DoubleCell( stdevSubgroup ) );
			cells.add( new DoubleCell( Math.abs(stdevSubgroup - stdevDataset) ) );
				// Now, add a column for each metric
			addMetricCellsToRow(cells, dinos, currentSubgroup);
				// Create the row and add it
			DataRow row = new DefaultRow( "Row" + count , cells);
			System.out.print(row.toString());
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
//			outputSpecs.add( new DataColumnSpecCreator("Class (" + dataset.classAtt().name() +  ")", BooleanCell.TYPE).createSpec() );
				// Target Variable
			outputSpecs.add( new DataColumnSpecCreator("Class (" + dataset.classAtt().name() +  ")", IntervalCell.TYPE).createSpec() );
				// True Positives
			outputSpecs.add( new DataColumnSpecCreator("True Positives", IntCell.TYPE).createSpec() );
				// Coverage
			outputSpecs.add( new DataColumnSpecCreator("Covered", IntCell.TYPE).createSpec() );
				// Mean and difference
			outputSpecs.add( new DataColumnSpecCreator("Mean", DoubleCell.TYPE).createSpec() );
			outputSpecs.add( new DataColumnSpecCreator("Mean Difference", DoubleCell.TYPE).createSpec() );	
				// Median and difference
			outputSpecs.add( new DataColumnSpecCreator("Median", DoubleCell.TYPE).createSpec() );
			outputSpecs.add( new DataColumnSpecCreator("Median Difference", DoubleCell.TYPE).createSpec() );	
				// Standard Deviation and Difference
			outputSpecs.add( new DataColumnSpecCreator("Standard Deviation", DoubleCell.TYPE).createSpec() );
			outputSpecs.add( new DataColumnSpecCreator("Standard Deviation Difference", DoubleCell.TYPE).createSpec() );
			
					// Now, add a column for each metric
			addMetricsToCandidateRow(outputSpecs, dinos);
			
		return outputSpecs;
		}
		
			/**
			 * {@inheritDoc}
			 */
		@Override
		protected void addTargetSpecs(Dataset dataset, List<DataColumnSpec> outputSpecs) {
			outputSpecs.add( new DataColumnSpecCreator( DEFAULT_PREDICTION + " (" + dataset.classAtt().name() + ")" , IntervalCell.TYPE).createSpec()
					);
		}
		
			/**
			 * 
			 */
		@Override
		protected void addPrediction(List<DataCell> cells, Individual currentSubgroup, Dataset dataset) {
			NumericProperty target = (NumericProperty) currentSubgroup.mainClass();
			cells.add( new IntervalCell( target.lowerBound() , target.upperBound() , true , true) );
		}

		
			/**
			 * {@inheritDoc}
			 */
		
		@Override
		protected void addSpecificResultsVariables(
				GenericDinosKnimeModel nodeModel,
				Algorithm dinos,
				Dataset dataset) {

				String target1name = "target_1_" + dataset.classAtt().name();
				NumClassDataset numericDataset = (NumClassDataset) dataset;
				
				nodeModel.addResultVariables( target1name + "_stdev" , DoubleType.INSTANCE, Math.sqrt( numericDataset.getVariance() ) );
				nodeModel.addResultVariables( target1name + "_median" , DoubleType.INSTANCE, numericDataset.getMedian() );			
				nodeModel.addResultVariables( target1name + "_mean" , DoubleType.INSTANCE, numericDataset.getMean() );
			
		}


	
}
