package cu.edu.cujae.daf.knime.nodes.survival;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.knime.core.data.NominalValue;
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
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.workflow.VariableType.DoubleType;
import org.knime.core.node.workflow.VariableType.IntType;

import cu.edu.cujae.daf.codification.NumericMatrix;
import cu.edu.cujae.daf.codification.NumericProperty;
import cu.edu.cujae.daf.codification.SurvivalMatrix;
import cu.edu.cujae.daf.codification.individual.Individual;
import cu.edu.cujae.daf.context.dataset.Dataset;
import cu.edu.cujae.daf.context.dataset.DiscClassDataset;
import cu.edu.cujae.daf.context.dataset.NumClassDataset;
import cu.edu.cujae.daf.context.dataset.SurvClassDataset;
import cu.edu.cujae.daf.context.dataset.Dataset.ClassType;
import cu.edu.cujae.daf.context.dataset.Dataset.SurvivalClass$;
import cu.edu.cujae.daf.core.Algorithm;
import cu.edu.cujae.daf.core.Subgroup;
import cu.edu.cujae.daf.knime.nodes.GenericDinosKnimeModel;
import cu.edu.cujae.daf.knime.nodes.GenericDinosKnimeWorkflow;

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
	public static final String MESSAGE_SAMECOLUMN = "Survival and censoring column indicator cannot be the same";
	public static final String MESSAGE_EMPTYINDICATOR = "The censor indicator cannot be empty";
	public static final String MESSAGE_GUESSING_CENSOR = " Guessing Censor column:";
	public static final String MESSAGE_GUESSING_CENSORIDENT = " Guessing Censor indication:";
	public static final String MESSAGE_CENSORCLASSNOTFOUND = "Previously configured censor column not found or incompatible: ";
	
		// The Singleton Instance
	public static final SurvivalDinosKnimeWorkflow INSTANCE_SURVIVAL = new SurvivalDinosKnimeWorkflow();
	
		// Variable to store the cells type supported by this as target
		@SuppressWarnings("unchecked")
	public static final Class<? extends DataValue>[] TARGETS_SURVIVAL = new Class[] {IntValue.class , DoubleValue.class};

		@SuppressWarnings("unchecked")
	public static final Class<? extends DataValue>[] TARGETS_CENSOR = new Class[] {IntValue.class , NominalValue.class};


		
	public static Class<? extends DataValue>[] getCensorTargetTypes( ) { return TARGETS_CENSOR; }

		/**
		 * {@inheritDoc}
		 */
	@Override
	public ClassType getClassType() { return SurvivalClass$.MODULE$;}
	
		/**
		 * {@inheritDoc}
		 */
	@Override
	public Class<? extends DataValue>[] getAceptedTargetTypes() { return this.TARGETS_SURVIVAL; }
	
		// Constructor, nothing to initialize
	private SurvivalDinosKnimeWorkflow() {}
	
	public static SettingsModelString createCensorColumnModel() {
		return new SettingsModelString( KEY_CENSOR_COLUMN , DEFAULT_CENSOR_COLUMN );
		}
	
	public static SettingsModelString createCensorIndicationModel() {
		return new SettingsModelString( KEY_CENSOR_INDICATION , DEFAULT_CENSOR_INDICATION );
		}
	
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
		 auxiliar.put("ESGDataset", "ESG Metric Against Entire Dataset");
		 auxiliar.put("ESGComplement", "ESG Metric Against Complement");
		 result.put( super.INFO_COMPONENTS[3]._1, auxiliar );
		 
		 auxiliar = new LinkedHashMap<String, String>();
		 auxiliar.put("DummySurvivalSelector", "Dummy Survival Selector");
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
		 auxiliar.put("ESGComplement" , new String[]{"ESGComplementAVariable"});
		 auxiliar.put("ESGDataset" , new String[]{"ESGDatasetAVariable"});
		 result.put( INFO_COMPONENTS[3]._1, auxiliar);
		 
		 result.put( INFO_COMPONENTS[4]._1 , null );
		 
		 result.put( INFO_COMPONENTS[5]._1 , null );
		
		return new LinkedHashMap<String, Map<String, String[]>>();
	}
	
		/**
		 * {@inheritDoc}
		 */
	@Override
	public Algorithm defaultAlgorithmSettings() {
		return Algorithm.survivalDinos();
	}

		/**
		 * {@inheritDoc}
		 */
	@Override
	public scala.collection.immutable.Map<String, String[]> getDefaultClasses() {
			return Algorithm.getSurvivalDefaultParameters();
	}

		/**
		 * {@inheritDoc}
		 */
	@Override
	protected Algorithm getDefaultAlgorithm() {
		return Algorithm.survivalDinos();
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
		cells.add(new StringCell( subgroupConditionsToString(dataset, currentSubgroup) ) );
		
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
		
				// Amount of Censored Instances
		cells.add( new IntCell( currentMatrix.getAmountCensored() ) );
				// Rate of Censored Instances
		cells.add( new DoubleCell( currentMatrix.getRateCensored() ) );
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

			/**
			 * {@inheritDoc}
			 */
		@Override
		public List<DataColumnSpec> columnSpecs(Algorithm dinos, Dataset dataset) {
					// Store the columns of the output column
		List<DataColumnSpec> outputSpecs = new ArrayList<>();
					// Description of Subgroup
		outputSpecs.add( new DataColumnSpecCreator("Subgroup", StringCell.TYPE).createSpec() );
		
				// Result from metrics (as a sort of target value)
		var hello = ( (SurvivalMatrix) dinos.externalPopulation()[0].typePattern().contingencyMatrix() ).calcs().keysIterator();
		while ( hello.hasNext() )
		{	outputSpecs.add( new DataColumnSpecCreator( hello.next() , DoubleCell.TYPE).createSpec() );	}
				// Amount of Censored Instances
		outputSpecs.add( new DataColumnSpecCreator("Amount of Censored Instances", IntCell.TYPE).createSpec() );
				// Rate of Censored Instances
		outputSpecs.add( new DataColumnSpecCreator("Rate of Censored Instances", DoubleCell.TYPE).createSpec() );
				// Survival Mean
		outputSpecs.add( new DataColumnSpecCreator("Survival Mean", DoubleCell.TYPE).createSpec() );
				// Maximum Follow Up
		outputSpecs.add( new DataColumnSpecCreator("Maximum Follow Up", DoubleCell.TYPE).createSpec() );	
		
				// Now, add a column for each metric
		addMetricsToCandidateRow(outputSpecs, dinos);
		
		return outputSpecs;
		}
		
			/**
			 * {@inheritDoc}
			 */
		@Override
		protected void addTargetSpecs(Dataset dataset, List<DataColumnSpec> outputSpecs) {
			outputSpecs.add( new DataColumnSpecCreator( "CENSORED (" + this.BOOL_FALSE_TEXT + ") / ALIVE (" + this.BOOL_TRUE_TEXT + ")" , BooleanCell.TYPE).createSpec()
					);
		}
		
			/**
			 * 
			 */
		@Override
		protected void addPrediction(List<DataCell> cells, Individual currentSubgroup, Dataset dataset) {
			// Do nothing
		}

		
			/**
			 * {@inheritDoc}
			 */
		
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
			
		}


	
}
