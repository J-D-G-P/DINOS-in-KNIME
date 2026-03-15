package cu.edu.cujae.daf.knime.nodes.survival;

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
import org.knime.core.data.MissingCell;
import org.knime.core.data.NominalValue;
import org.knime.core.data.def.BooleanCell;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.workflow.VariableType.DoubleType;
import org.knime.core.node.workflow.VariableType.IntType;

import cu.edu.cujae.daf.codification.SurvivalMatrix;
import cu.edu.cujae.daf.codification.individual.Individual;
import cu.edu.cujae.daf.context.dataset.Dataset;
import cu.edu.cujae.daf.context.dataset.SurvClassDataset;
import cu.edu.cujae.daf.core.Algorithm;
import cu.edu.cujae.daf.core.DiscoveryMode;
import cu.edu.cujae.daf.core.Subgroup;
import cu.edu.cujae.daf.core.SurvivalMode$;
import cu.edu.cujae.daf.knime.nodes.GenericDinosKnimeModel;
import cu.edu.cujae.daf.knime.nodes.GenericDinosKnimeWorkflow;
import scala.Option;

/**
 * Contains all methods and constants for interfacing with the DAF library
 * This is survival analysis, AKA " a numeric time to to event as target with censoring information"
 * 
 * @author Jonathan David González Pereda, CUJAE
 */

	@SuppressWarnings("static-access")
public class SurvivalDinosKnimeWorkflow extends GenericDinosKnimeWorkflow {

		// Strings for configuration keys
	public static final String KEY_CENSOR_COLUMN = "censor_Column";
	public static final String KEY_CENSOR_INDICATION = "censor_Indication";

		// Default values
	public static final String DEFAULT_CENSOR_COLUMN = null;
	public static final String DEFAULT_CENSOR_INDICATION = null;

		// Messages for the node's warning, error and progress bar
	public static final String WARNING_GUESSING_CENSOR = " Guessing censor column: ";
	public static final String WARNING_GUESSING_CENSORIDENT = " Guessing censor indication: ";
	public static final String EXCEPTION_CONFIGNOTFOUND_CENSOR = "Previously configured censor column not found or incompatible: ";
	public static final String EXCEPTION_SAMECOLUMN = "Survival time and censoring column indicator cannot be the same";
	public static final String EXCEPTION_EMPTYINDICATOR = "The censor indicator cannot be empty";
	
		// Name of columns to show in result tables
	public static final String RESULT_CENSORED = "Censored (" + GenericDinosKnimeWorkflow.BOOL_FALSE_TEXT + ") / Alive (" + GenericDinosKnimeWorkflow.BOOL_TRUE_TEXT + ")";
	private static final String RESULT_CENSORED_TOTAL = "Amount of Censored Instances";
	private static final String RESULT_CENSORED_RATE = "Rate of Censored Instances";
	private static final String RESULT_MEDIAN = "Survival Median";
	private static final String RESULT_MEAN = "Survival Mean";
	private static final String RESULT_FOLLOWUP = "Maximum Follow Up";
	
		// The Singleton Instance
	public static final SurvivalDinosKnimeWorkflow INSTANCE_SURVIVAL = new SurvivalDinosKnimeWorkflow();
	
		// The Helper Instance
	public static final DiscoveryMode MODE_SURVIVAL = SurvivalMode$.MODULE$;
	
		// Target types for filtering incoming tables
	public static final Class<? extends DataValue>[] TARGETS_SURVIVAL = new Class[] {IntValue.class , DoubleValue.class};
	public static final Class<? extends DataValue>[] TARGETS_CENSOR = new Class[] {IntValue.class , NominalValue.class};

	public static Class<? extends DataValue>[] getCensorTargetTypes( ) { return TARGETS_CENSOR; }
	
		/** {@inheritDoc} */
	@Override
	public Class<? extends DataValue> getThenTargetType() {return DoubleValue.class;}

		/** {@inheritDoc} */
//	@Override
//	public ClassType getClassType() { return SurvivalClass$.MODULE$;}
//	
		/** {@inheritDoc} */
	@Override
	public DiscoveryMode getModeHelper() { return SurvivalMode$.MODULE$;}
	
		/** {@inheritDoc} */
	@Override
	public Class<? extends DataValue>[] getAceptedTargetTypes() { return this.TARGETS_SURVIVAL; }
	
		// Constructor, nothing to initialize
	private SurvivalDinosKnimeWorkflow() {}
	
		/**  @return A model for use in storing the censor class column*/
	public static SettingsModelString createCensorColumnModel() {
		return new SettingsModelString( KEY_CENSOR_COLUMN , DEFAULT_CENSOR_COLUMN );
		}

		/**  @return A model for use in storing the value to use as censor indicator*/
	public static SettingsModelString createCensorIndicationModel() {
		return new SettingsModelString( KEY_CENSOR_INDICATION , DEFAULT_CENSOR_INDICATION );
		}

		/** {@inheritDoc} */
		@Override
	public BufferedDataTable subgroupInformation(Algorithm dinos, Dataset dataset, ExecutionContext exec) {

			// Store the column names and types
		List<DataColumnSpec> outputSpecs = columnSpecs(dinos, dataset);
		
			// Create the new table
		var specs = new DataTableSpec( outputSpecs.toArray(new DataColumnSpec[outputSpecs.size()]) );
		String[] columnNames = specs.getColumnNames();
		BufferedDataContainer container = exec.createDataContainer( specs );

			// Get the subgroups
		Subgroup[] subgroupsList = dinos.externalPopulation();
		
			// Now, fill the table
		for(int count = 0 ; count < subgroupsList.length ; ++count) {
			List<DataCell> cells = new ArrayList<>();
			Individual currentSubgroup = subgroupsList[count].typePattern();
			SurvivalMatrix currentMatrix = (SurvivalMatrix) currentSubgroup.contingencyMatrix();
			scala.collection.mutable.LinkedHashMap<String, Object> currentCalcs = currentMatrix.calcs();
	
				// Description of Subgroup
			cells.add(new StringCell( dataset.modeHelper().bodyString(currentSubgroup, dataset) ) );
			
			int currentCalcsPos = 1;
			
			while (currentCalcsPos > 0 ) {
				String currentName = columnNames[currentCalcsPos];
				Double valueToAdd = currentCalcs.getOrElse(currentName, null);
				if(valueToAdd != null) {
					cells.add( new DoubleCell(valueToAdd) );
					currentCalcsPos = -1;
				}
				else {
					cells.add( new DoubleCell(valueToAdd) );
					++currentCalcsPos;
				}
				
			}
			
					// Amount of Instances
			cells.add( new IntCell( currentMatrix.coverage() ) );
					// Amount of Censored Instances
			cells.add( new IntCell( currentMatrix.getAmountCensored() ) );
					// Rate of Censored Instances
			cells.add( new DoubleCell( currentMatrix.getRateCensored() ) );
					// Median
			cells.add( parseMedian( currentMatrix.getMedian() )  );
					// Survival Mean
			cells.add( new DoubleCell( currentMatrix.getMeanMaxFollowUp() ) );
					// Maximum Follow Up
			cells.add( new DoubleCell( currentMatrix.maxFollowUp() ) );			
			
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

			private DataCell parseMedian(Option<Object> median) {
				return median.isDefined()
						? new DoubleCell( (double) median.get() )
						: new MissingCell(null);
		}

			/** {@inheritDoc} */
		@Override
		public List<DataColumnSpec> columnSpecs(Algorithm dinos, Dataset dataset) {
				// Store the columns of the output column
		List<DataColumnSpec> outputSpecs = new ArrayList<>();
				// Description of Subgroup
		outputSpecs.add( new DataColumnSpecCreator( this.RESULT_SUBGROUP_FULL, StringCell.TYPE).createSpec() );
		
				// Result from metrics (as a sort of target value)
		var hello = ( (SurvivalMatrix) dinos.externalPopulation()[0].typePattern().contingencyMatrix() ).calcs().keysIterator();
		while ( hello.hasNext() )
		{	outputSpecs.add( new DataColumnSpecCreator( hello.next() , DoubleCell.TYPE).createSpec() );	}
				// Amount of Instances
		outputSpecs.add( new DataColumnSpecCreator( this.RESULT_COV, IntCell.TYPE).createSpec() );
				// Amount of Censored Instances
		outputSpecs.add( new DataColumnSpecCreator( this.RESULT_CENSORED_TOTAL, IntCell.TYPE).createSpec() );
				// Rate of Censored Instances
		outputSpecs.add( new DataColumnSpecCreator( this.RESULT_CENSORED_RATE, DoubleCell.TYPE).createSpec() );
				// Median
		outputSpecs.add( new DataColumnSpecCreator( this.RESULT_MEDIAN, DoubleCell.TYPE).createSpec() );
				// Survival Mean
		outputSpecs.add( new DataColumnSpecCreator( this.RESULT_MEAN, DoubleCell.TYPE).createSpec() );
				// Maximum Follow Up
		outputSpecs.add( new DataColumnSpecCreator( this.RESULT_FOLLOWUP, DoubleCell.TYPE).createSpec() );	
		
				// Now, add a column for each metric
		addMetricsToCandidateRow(outputSpecs, dinos);
		
		return outputSpecs;
		}
		
			/** {@inheritDoc} */
		@Override
		protected void addTargetSpecs(Dataset dataset, List<DataColumnSpec> outputSpecs) {
			outputSpecs.add( new DataColumnSpecCreator( RESULT_CENSORED , BooleanCell.TYPE).createSpec()
					);
		}
		
			/** {@inheritDoc} */
		@Override
		protected void addPrediction(List<DataCell> cells, Individual currentSubgroup, Dataset dataset) {
			// Do nothing
		}

			/** {@inheritDoc} */
		@Override
		protected void addSpecificResultsVariables(
				GenericDinosKnimeModel nodeModel,
				Algorithm dinos,
				Dataset dataset) {

			SurvClassDataset survivalDataset = (SurvClassDataset) dataset;
			final String datasetPrefix = "dataset_";

				// Subgroup Amount of Censored Instances
			nodeModel.addResultVariables( datasetPrefix + "censoredAmount", IntType.INSTANCE, survivalDataset.getAmountCensored() );
				// Subgroup Rate of Censored Instances
			nodeModel.addResultVariables( datasetPrefix + "censoredRate", DoubleType.INSTANCE, survivalDataset.getRateCensored() );
				// Subgroup Max Follow Up
			nodeModel.addResultVariables( datasetPrefix + "maxFollowUp", DoubleType.INSTANCE, survivalDataset.getMaximumFollowup() );
				// Survival Max Mean (survivalMean)
			nodeModel.addResultVariables( datasetPrefix + "maxMean", DoubleType.INSTANCE, survivalDataset.getSurvivalMean() );
				// Subgroup median
			var median = survivalDataset.getSurvivalMedian();
			nodeModel.addResultVariables( datasetPrefix + "median", DoubleType.INSTANCE, median.isDefined() ? (double) median.get() : null );			
		}

	
}
