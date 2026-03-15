package cu.edu.cujae.daf.knime.nodes.numeric;

import java.util.ArrayList;
import java.util.HashSet;
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
import org.knime.core.data.IntervalValue;
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
import cu.edu.cujae.daf.core.Algorithm;
import cu.edu.cujae.daf.core.DiscoveryMode;
import cu.edu.cujae.daf.core.NumericMode$;
import cu.edu.cujae.daf.core.Subgroup;
import cu.edu.cujae.daf.formatter.FormatSymbols;
import cu.edu.cujae.daf.formatter.SubgroupFormatter;
import cu.edu.cujae.daf.knime.nodes.GenericDinosKnimeModel;
import cu.edu.cujae.daf.knime.nodes.GenericDinosKnimeWorkflow;
import cu.edu.cujae.daf.utils.SubgroupParser;

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
	
		// The Helper Instance
	public static final DiscoveryMode MODE_NUMERIC = NumericMode$.MODULE$;
	
		// Name of columns to show in result tables
	public static final String RESULT_MEAN = "Mean";
	public static final String RESULT_MEAN_DIFF = addDif(RESULT_MEAN);
	public static final String RESULT_MEDIAN = "Median";
	public static final String RESULT_MEDIAN_DIFF = addDif(RESULT_MEDIAN);
	public static final String RESULT_STD = "Standard Deviation";
	public static final String RESULT_STD_DIFF = addDif(RESULT_STD);
	public static final String RESULT_DIFF = "Difference";
	private static final String addDif(String string) {return string + " " + RESULT_DIFF;}
	
	public Class<? extends DataValue> getThenTargetType() {return IntervalValue.class;}
	
		// Variable to store the cells type supported by this as target
		@SuppressWarnings("unchecked")
	public static final Class<? extends DataValue>[] TARGETS_NUMERIC = new Class[] {IntValue.class , DoubleValue.class};

		/**
		 * {@inheritDoc}
		 */

		/**
		 * {@inheritDoc}
		 */
	@Override
	public DiscoveryMode getModeHelper() { return NumericMode$.MODULE$;}
	
		/**
		 * {@inheritDoc}
		 */
	@Override
	public Class<? extends DataValue>[] getAceptedTargetTypes() { return this.TARGETS_NUMERIC; }
	
		// Constructor, nothing to initialize
	private NumericDinosKnimeWorkflow() {}

//	@Override
//	public Map< String , Map < String , String[] > > getExclusiveSettings() {
//
//		LinkedHashMap<String, String[]> auxiliar = new LinkedHashMap<String, String[]>();
//		Map< String , Map < String , String[] > > result = new LinkedHashMap<String, Map<String, String[]>>();
//
//		 result.put( INFO_COMPONENTS[0]._1, null );
//		 
//		 result.put( INFO_COMPONENTS[1]._1, null );
//		 
//		 result.put( INFO_COMPONENTS[2]._1, null );
//
//		 auxiliar = new LinkedHashMap<String, String[]>();
//		 auxiliar.put("MeanDifference" , new String[]{"MeanDifferenceAVariable"});
//		 auxiliar.put("MedianDifference" , new String[]{"MedianDifferenceAVariable"});
//		 result.put( INFO_COMPONENTS[3]._1, auxiliar);
//		 
//		 auxiliar = new LinkedHashMap<String, String[]>();
//		 auxiliar.put("MADRangeSelector" , new String[]{"MADRangeFactor", "MADRangeBVariable"} );
//		 
//		 result.put( INFO_COMPONENTS[4]._1 , auxiliar );
//		 
//		 result.put( INFO_COMPONENTS[5]._1 , null );
//		
//		return new LinkedHashMap<String, Map<String, String[]>>();
//	}
	

		@Override
	public BufferedDataTable subgroupInformation(Algorithm dinos, Dataset dataset, ExecutionContext exec) {
				// Store the column names and types
			List<DataColumnSpec> outputSpecs = columnSpecs(dinos, dataset);
	
				// Mode Helper
			var mode = dataset.modeHelper();
			
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
			cells.add( new StringCell( mode.bodyString(currentSubgroup, dataset) ) );
			
				// If Target is Included or Not
			var inOrNot = BooleanCell.TRUE;
			if (currentSubgroup.mainClass().positive() ) { inOrNot = BooleanCell.TRUE ; } else { inOrNot = BooleanCell.FALSE ; }
			cells.add( inOrNot );
				// Limits of the Target Interval
			NumericProperty target = (NumericProperty) currentSubgroup.mainClass();
			cells.add( new IntervalCell(target.lowerBound() , target.upperBound() , true , true) );

				// Full Subgroup Description
			cells.add( new StringCell( mode.fullRuleString(currentSubgroup, dataset) ) );
			
				// True Positives
			cells.add( new IntCell( currentSubgroup.contingencyMatrix().tp() ) );
			
				// Coverage
			cells.add( new IntCell( currentSubgroup.contingencyMatrix().coverage() ) );
			
				// Mean and difference
			cells.add( new DoubleCell( currentData.meanTp() ) );
			cells.add( new DoubleCell( ( currentData.meanTp() - numericDataset.getMean() ) ) );
			
				// Median and difference
			cells.add( new DoubleCell( currentData.medianTp() ) );
			cells.add( new DoubleCell( ( currentData.medianTp() - numericDataset.getMedian() ) ) );
			
				// Standard Deviation and Difference
			double stdevSubgroup = Math.sqrt( currentData.varianceTp() );
			double stdevDataset = Math.sqrt( numericDataset.getVariance() );
			cells.add( new DoubleCell( stdevSubgroup ) );
			cells.add( new DoubleCell( (stdevSubgroup - stdevDataset) ) );
			
				// Now, add a column for each metric
			addMetricCellsToRow(cells, dinos, currentSubgroup);
			
				// Create the row and add it
			DataRow row = new DefaultRow( "Row" + count , cells);
			//System.out.print(row.toString());
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
			outputSpecs.add( new DataColumnSpecCreator( this.RESULT_SUBGROUP_BODY, StringCell.TYPE).createSpec() );
				// If Target is Included or Not
			outputSpecs.add( new DataColumnSpecCreator( this.RESULT_IN, BooleanCell.TYPE).createSpec() );
				// Target Variable
			outputSpecs.add( new DataColumnSpecCreator( this.RESULT_CLASS +" (" + dataset.classAtt().name() +  ")", IntervalCell.TYPE).createSpec() );
				// Full Subgroup Description
			outputSpecs.add( new DataColumnSpecCreator( this.RESULT_SUBGROUP_FULL, StringCell.TYPE).createSpec() );
				// True Positives
			outputSpecs.add( new DataColumnSpecCreator( this.RESULT_TP, IntCell.TYPE).createSpec() );
				// Coverage
			outputSpecs.add( new DataColumnSpecCreator( this.RESULT_COV, IntCell.TYPE).createSpec() );
				// Mean and difference
			outputSpecs.add( new DataColumnSpecCreator( this.RESULT_MEAN, DoubleCell.TYPE).createSpec() );
			outputSpecs.add( new DataColumnSpecCreator( this.RESULT_MEAN_DIFF, DoubleCell.TYPE).createSpec() );	
				// Median and difference
			outputSpecs.add( new DataColumnSpecCreator( this.RESULT_MEDIAN, DoubleCell.TYPE).createSpec() );
			outputSpecs.add( new DataColumnSpecCreator( this.RESULT_MEDIAN_DIFF, DoubleCell.TYPE).createSpec() );	
				// Standard Deviation and Difference
			outputSpecs.add( new DataColumnSpecCreator( this.RESULT_STD, DoubleCell.TYPE).createSpec() );
			outputSpecs.add( new DataColumnSpecCreator( this.RESULT_STD_DIFF, DoubleCell.TYPE).createSpec() );
			
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
			if(currentSubgroup == null)
				super.addPrediction(cells, currentSubgroup, dataset);
			else {
				NumericProperty target = (NumericProperty) currentSubgroup.mainClass();
				cells.add( new IntervalCell( target.lowerBound() , target.upperBound() , true , true) );
			}
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
