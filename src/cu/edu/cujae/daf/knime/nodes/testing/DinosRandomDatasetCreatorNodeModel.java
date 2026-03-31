package cu.edu.cujae.daf.knime.nodes.testing;

import java.io.File;
import java.io.IOException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IllegalFormatException;
import java.util.List;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.MissingCell;
import org.knime.core.data.container.CloseableRowIterator;
import org.knime.core.data.def.BooleanCell;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelInteger;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.flowvariable.FlowVariablePortObject;
import org.knime.core.node.workflow.VariableType;
import org.knime.core.node.workflow.VariableType.BooleanType;
import org.knime.core.node.workflow.VariableType.StringType;

import cu.edu.cujae.daf.knime.nodes.AddExternalFlowVariables;
import cu.edu.cujae.daf.knime.nodes.Utils;

/**
 * This is an example implementation of the node model of the
 * "DinosRandomDatasetCreator" node.
 * 
 * This example node performs simple number formatting
 * ({@link String#format(String, Object...)}) using a user defined format string
 * on all double columns of its input table.
 *
 * @author 
 */
public class DinosRandomDatasetCreatorNodeModel extends NodeModel implements AddExternalFlowVariables {
	protected DinosRandomDatasetCreatorNodeModel() {
    	super(
    			new PortType[]{},
    			new PortType[]{
    					FlowVariablePortObject.TYPE,
    					BufferedDataTable.TYPE}
        		);
	}

	public <T> void addResultVariables(final String name, final VariableType<T> type, final T value) {
		this.pushFlowVariable(name, type, value);
	}
	
	private static java.util.Random random = new java.util.Random();

		// Amount of Rows
	private final SettingsModelIntegerBounded amountOfRows = createAmountSettingsModel();
	
	static SettingsModelIntegerBounded createAmountSettingsModel() {
		return new SettingsModelIntegerBounded("amountOfRows", 100, 1, Integer.MAX_VALUE);
	}
	
		// Target Type
	private final SettingsModelString typeOfTarget = createTypeOfTargetModel();
	
	static SettingsModelString createTypeOfTargetModel() {
		return new SettingsModelString("typeOfTarget", "");
	}
	
		// Attribute missing probability
	SettingsModelDoubleBounded attributeMissingProb = createAttributeMissingProbModel();

	public static SettingsModelDoubleBounded createAttributeMissingProbModel() {
		return new SettingsModelDoubleBounded("attributeMissingProbability", 0.1, 0, 1);
	}

		// Class missing probability
	SettingsModelDoubleBounded classMissingProb = createclassMissingProbModel();

	public static SettingsModelDoubleBounded createclassMissingProbModel() {
		return new SettingsModelDoubleBounded("classMissingProbability", 0, 0, 1);
	}
	
		// Helper classes to manage adding values
	private abstract class ColumnDatasetCreatorHelper {
		
		protected final String name;
		protected final Double missingProb;
		protected int columnsToAdd() {return 1;}
		
		public ColumnDatasetCreatorHelper(String name, double missingProb) {
			this.name = checkName(name);
			this.missingProb = checkProb(missingProb);
		}

		public Object getCensorMark() {
			// TODO Auto-generated method stub
			return null;
		}

		public void addResultFlowVariables(AddExternalFlowVariables model ) {
			model.addResultVariables("target", StringType.INSTANCE, name);
		}
		
		private String checkName(String name) {
			String toSet = "";
			if(name == null)
				toSet = random.nextInt() + "";
			else
				toSet = name;
				
			return toSet;
		}
		
		private Double checkProb(double missingProb) {
			// TODO Auto-generated method stub
			return missingProb;
		}
		
			// Specs Methods
		
		abstract DataColumnSpec[] getSpecs();
		
		public final void addSpecs(List<DataColumnSpec> list) {
			var toAdd = getSpecs();
			
			for(int count = 0 ; count < toAdd.length ; ++count) {
				var addThis = toAdd[count];
				list.add(addThis);
			}
		}
		
			// Cells Methods
		
		public final void addCells(List<DataCell> list) {
			var toAdd = getCells();
			
			for(int count = 0 ; count < toAdd.length ; ++count) {
				var addThis = toAdd[count];
				list.add(addThis);
			}			
		}
		
		final private DataCell[] getCells() {
			DataCell[] answer = null;
			var prob = random.nextDouble(0, 1);
			if(prob < missingProb)
				answer = getMissing(columnsToAdd() );
			else
				answer = getCellsHelper();
			
			return answer;
		}
		
		protected abstract DataCell[] getCellsHelper();
		
		private DataCell[] getMissing(final int amount) {
			var result = new DataCell[amount];
			for( int count = 0 ; count < amount ; ++count) {
				result [ count ] = ( new MissingCell(null) ); 
			}
			
			return result;
		}
	}
	
	private class IntegerAttribute extends ColumnDatasetCreatorHelper {

		public IntegerAttribute(String name, double missingProb) {
			super("Integer " + name, missingProb);
		}

		@Override
		DataColumnSpec[] getSpecs() {
			return new DataColumnSpec[] {
				new DataColumnSpecCreator( name, IntCell.TYPE).createSpec(),
			};
		}

		@Override
		protected DataCell[] getCellsHelper() {
			return new DataCell[] {
					new IntCell( random.nextInt() )
				};
		}
		
	}
	
	private class DoubleAttribute extends ColumnDatasetCreatorHelper {

		public DoubleAttribute(String name, double missingProb) {
			super( "Double " + name, missingProb);
		}

		@Override
		DataColumnSpec[] getSpecs() {
			return new DataColumnSpec[] {
				new DataColumnSpecCreator( name, DoubleCell.TYPE).createSpec(),
			};
		}

		@Override
		protected DataCell[] getCellsHelper() {
			return new DataCell[] {
					new DoubleCell( random.nextDouble() )
				};
		}
		
	}
	
	private class BooleanAttribute extends ColumnDatasetCreatorHelper {

		public BooleanAttribute(String name, double missingProb) {
			super( "Boolean " + name, missingProb);
		}

		@Override
		DataColumnSpec[] getSpecs() {
			return new DataColumnSpec[] {
				new DataColumnSpecCreator( name, BooleanCell.TYPE).createSpec(),
			};
		}

		@Override
		protected DataCell[] getCellsHelper() {
			
			BooleanCell toAdd = Utils.getRandomBooleanCell();
			
			return new DataCell[] {
					toAdd
				};
		}
		
	}
	
	private class StringAttribute extends ColumnDatasetCreatorHelper {

		final String[] cache;
		
		public StringAttribute(String name, double missingProb) {
			this(name, missingProb, 5, 5);
		}
		
		public StringAttribute( String name, double missingProb, int lenght , int amount ) {
			super( "String " + name, missingProb);
			cache = new String[amount];
			
			for(int count = 0 ; count < cache.length ; ++ count) {
				cache[count] = Utils.randomString(lenght);
			}
		}
		
		

		
		
		@Override
		DataColumnSpec[] getSpecs() {
			return new DataColumnSpec[] {
				new DataColumnSpecCreator( name, StringCell.TYPE).createSpec(),
			};
		}

		@Override
		protected DataCell[] getCellsHelper() {
			
			var index = random.nextInt( cache.length );
			String toAdd = cache[index];
			
			return new DataCell[] {
					
					new StringCell(toAdd)
				};
		}
		
	}
	
	private abstract class SurvivalAttribute extends ColumnDatasetCreatorHelper {

		@Override
		protected int columnsToAdd() {return 2;}
		
		public SurvivalAttribute(String name, double missingProb) {
			super("Survival Time " + name, missingProb);
		}
		
		public abstract Object getCensorMark();
		
		protected abstract Object getDeadMark();
		
		private String getCensorName() {
			return "Status " + name;
		}

		@Override
		DataColumnSpec[] getSpecs() {
			return new DataColumnSpec[] {
					new DataColumnSpecCreator( name, DoubleCell.TYPE).createSpec(),
					new DataColumnSpecCreator( getCensorName(), getCensorType() ).createSpec(),
				};
		}

		protected abstract DataType getCensorType();

		@Override
		protected DataCell[] getCellsHelper() {
			var toAdd = random.nextDouble(0, Double.MAX_VALUE);
			
			return new DataCell[] {
					new DoubleCell(toAdd),
					getCensorCell()
				};
		}
		
		protected abstract DataCell getCensorCell();
		
		public void addResultFlowVariables(AddExternalFlowVariables model ) {
			super.addResultFlowVariables(model);
			model.addResultVariables("indicator_censor", StringType.INSTANCE, getCensorMark().toString() );
			model.addResultVariables("indicator_alive", StringType.INSTANCE, getDeadMark().toString() );
			model.addResultVariables("censor", StringType.INSTANCE, getCensorName() );
		}
	}
	
	private class SurvivalIntegerCensorAttribute extends SurvivalAttribute {

		private final int[] values;
		
		@Override
		public Object getCensorMark()
			{ return values[0]; }
		
		@Override
		public Object getDeadMark()
			{ return values[1]; }
		
		public SurvivalIntegerCensorAttribute(String name, double missingProb) {
			super(name, missingProb);

			int censored = random.nextInt( Integer.MIN_VALUE , Integer.MAX_VALUE - 1);
			
			values = new int[2];
			
			values[0] = censored;
			values[1] = censored + 1;
		}

		@Override
		protected DataType getCensorType() {
			return IntCell.TYPE;
		}

		@Override
		protected DataCell getCensorCell() {
			int index = random.nextInt( 0 , 2 );
			var toAdd = values[index];
			return new IntCell(toAdd);
		}
		
	}
	
	private class SurvivalStringCensorAttribute extends SurvivalAttribute {
		
		private final String[] values;
		
		public SurvivalStringCensorAttribute(String name, double missingProb) {
			this(name, missingProb, 5);
		}
		
		public SurvivalStringCensorAttribute( String name, double missingProb, int lenght ) {
			super(name, missingProb);

			values = new String[2];
			
			values[0] = "alive " + Utils.randomString( lenght );
			
			do {
				values[1] = "dead " + Utils.randomString( lenght );
			}
			while( values[0].equals( values[1] ) );
			
		}

		@Override
		public Object getCensorMark() {
			return values[0];
		}
		
		@Override
		public Object getDeadMark() {
			return values[1];
		}

		@Override
		protected DataType getCensorType() {
			return StringCell.TYPE;
		}

		@Override
		protected DataCell getCensorCell() {
			int index = random.nextInt( 0 , 2 );
			var toAdd = values[index];
			return new StringCell(toAdd);
		}

	}
	
	private class SurvivalBooleanCensorAttribute extends SurvivalAttribute  {

		public SurvivalBooleanCensorAttribute(String name, double missingProb) {
			super(name, missingProb);
			// TODO Auto-generated constructor stub
		}

		@Override
		public Object getCensorMark() {
			return false;
		}
		
		@Override
		public Object getDeadMark() {
			return true;
		}

		@Override
		protected DataType getCensorType() {
			return BooleanCell.TYPE;
		}

		@Override
		protected DataCell getCensorCell() {
			
			BooleanCell toAdd = Utils.getRandomBooleanCell();
			
			return toAdd;
		}
		
		
	}


	public static final String SYMBOL_INTEGER = "Numeric Integer";
	public static final String SYMBOL_DOUBLE = "Numeric Double";
	public static final String SYMBOL_BOOLEAN = "Nominal Boolean";
	public static final String SYMBOL_STRING = "Nominal String";
	public static final String SYMBOL_SURVIVAL_INTEGER = "Survival time (with Integer censor indicator)";
	public static final String SYMBOL_SURVIVAL_STRING = "Survival time (with String censor indicator)";
	public static final String SYMBOL_SURVIVAL_BOOLEAN = "Survival time (with Boolean censor indicator)";
	
	public static final String[] supportedTargets = {
			SYMBOL_INTEGER,
			SYMBOL_DOUBLE,
			SYMBOL_BOOLEAN,
			SYMBOL_STRING,
			SYMBOL_SURVIVAL_INTEGER,
			SYMBOL_SURVIVAL_STRING,
			SYMBOL_SURVIVAL_BOOLEAN
	};

	@Override
	final protected PortObject[] execute(final PortObject[] inObjects, final ExecutionContext exec) throws Exception {
		
		ArrayList<ColumnDatasetCreatorHelper> initialAttributes = createInitialAttributes();
		
		addClass(initialAttributes);
		
		List<DataColumnSpec> specs = new ArrayList<>();
		for(int count = 0 ; count < initialAttributes.size() ; ++count ) {
			var currentHelper = initialAttributes.get(count);
			currentHelper.addResultFlowVariables(this);
			currentHelper.addSpecs(specs);
		}

		BufferedDataContainer container
			= exec.createDataContainer( new DataTableSpec( specs.toArray(new DataColumnSpec[specs.size()]) ) );
		
		int rowsToAdd = amountOfRows.getIntValue();
		
		for( int count = 0 ; count < rowsToAdd ; ++count ) {
			
			List<DataCell> cells = new ArrayList<>();
			
			for(int count2 = 0 ; count2 < initialAttributes.size() ; ++count2 ) {
				var currentHelper = initialAttributes.get(count2);
				currentHelper.addCells(cells);
			}
			
			DataRow row = new DefaultRow( "Row" + count , cells);
			container.addRowToTable(row);
			
		}
		
		container.close();
		BufferedDataTable table = container.getTable();
		
		return new PortObject[] {
				FlowVariablePortObject.INSTANCE,
				table
		};

	}

	private ArrayList<ColumnDatasetCreatorHelper> createInitialAttributes() {
		
		var attributeTag = "Attribute";
		var missingProb = attributeMissingProb.getDoubleValue();
		
		ArrayList<ColumnDatasetCreatorHelper> initialAttributes = new  ArrayList<DinosRandomDatasetCreatorNodeModel.ColumnDatasetCreatorHelper>();
		initialAttributes.add( new IntegerAttribute( attributeTag , missingProb ) );
		initialAttributes.add( new DoubleAttribute( attributeTag , missingProb ) );
		initialAttributes.add( new BooleanAttribute( attributeTag , missingProb ) );
		initialAttributes.add( new StringAttribute( attributeTag , missingProb ) );
		
		Collections.shuffle(initialAttributes);
		
		return initialAttributes;
	}
	
	private void addClass( ArrayList<ColumnDatasetCreatorHelper> initialAttributes ) {

		var name = "Class";
		var currentTarget = typeOfTarget.getStringValue();
		var missingProb = classMissingProb.getDoubleValue();

		ColumnDatasetCreatorHelper toAdd = null;
			
		if( currentTarget.equals(SYMBOL_INTEGER) )
			toAdd = new IntegerAttribute( name , missingProb );
		
		else if( currentTarget.equals(SYMBOL_DOUBLE) )
			toAdd = new DoubleAttribute( name , missingProb );
		
		else if( currentTarget.equals(SYMBOL_BOOLEAN) )
			toAdd = new BooleanAttribute( name , missingProb );

		else if( currentTarget.equals(SYMBOL_STRING) )
			toAdd = new StringAttribute( name , missingProb );
		
		else if( currentTarget.equals(SYMBOL_SURVIVAL_INTEGER) )
			toAdd = new SurvivalIntegerCensorAttribute( name , missingProb );
		
		else if( currentTarget.equals(SYMBOL_SURVIVAL_STRING) )
			toAdd = new SurvivalStringCensorAttribute( name , missingProb );
		
		else if ( currentTarget.equals(SYMBOL_SURVIVAL_BOOLEAN) )
			toAdd = new SurvivalBooleanCensorAttribute(name , missingProb);
		
		else{
			throw new IllegalArgumentException("Unexpected target type for Dinos Random DatasetCreator: " + currentTarget );
		}
			
		initialAttributes.addLast(toAdd);
	}
		

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {

		String currentValue = typeOfTarget.getStringValue();
		
		if( currentValue.isEmpty() ) {
			var setDefault = supportedTargets[0];
			typeOfTarget.setStringValue( setDefault );
			this.setWarningMessage( "Default chosen target type: " + setDefault );
		}
		
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {

		amountOfRows.saveSettingsTo(settings);
		typeOfTarget.saveSettingsTo(settings);
		attributeMissingProb.saveSettingsTo(settings);
		classMissingProb.saveSettingsTo(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {

		amountOfRows.loadSettingsFrom(settings);
		typeOfTarget.loadSettingsFrom(settings);
		attributeMissingProb.loadSettingsFrom(settings);
		classMissingProb.loadSettingsFrom(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {

		amountOfRows.validateSettings(settings);
		typeOfTarget.validateSettings(settings);
		attributeMissingProb.validateSettings(settings);
		classMissingProb.validateSettings(settings);
	}

	@Override
	protected void loadInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {

	}

	@Override
	protected void saveInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {

	}

	@Override
	protected void reset() {

	}
}


