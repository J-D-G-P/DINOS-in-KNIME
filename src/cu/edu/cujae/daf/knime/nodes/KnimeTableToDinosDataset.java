package cu.edu.cujae.daf.knime.nodes;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Set;

import org.knime.core.data.BooleanValue;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnDomain;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.IntValue;
import org.knime.core.data.StringValue;
import org.knime.core.data.container.CloseableRowIterator;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;

import cu.edu.cujae.daf.context.Configuration;
import cu.edu.cujae.daf.context.dataset.Attribute;
import cu.edu.cujae.daf.context.dataset.CENSORED$;
import cu.edu.cujae.daf.context.dataset.Dataset;
import cu.edu.cujae.daf.context.dataset.INTEGER$;
import cu.edu.cujae.daf.context.dataset.Instance;
import cu.edu.cujae.daf.context.dataset.NOMINAL$;
import cu.edu.cujae.daf.context.dataset.REAL$;
import cu.edu.cujae.daf.context.dataset.SURVIVAL$;
import scala.None$;
import scala.Option;
import scala.jdk.CollectionConverters;

/**
 * A class for converting a KNIME table to the internal
 * class DINOS uses, so as to make able to use the original
 * DAF as a library
 * 
 * This class is largely an adaption of
 * AnnotatedFileLoaderWithArbitraryColumnPosition at:
 * cu.edu.cujae.daf.context.dataset.loader.AnnotatedFileLoaderWithArbitraryColumnPosition;
 * 
 * @author Jonathan David González Pereda, CUJAE
 */

public class KnimeTableToDinosDataset {


	/**
	 * Takes a KNIME table to the internal and makes an
	 * internal DINOS dataset from it
	 * 
	 * TODO Boolean cels support (make them nominal)
	 * 
	 * @param table The Knime Table to use
	 * @param exec The exection context
	 * @param targets Set of the name of columns to define as targets
	 * 
	 * @author Jonathan David González Pereda, CUJAE
	 * @throws Exception 
	 */
	
		// Variables
	final static LinkedHashMap<String, Integer> BOOL_VALUES = createBoolValues();
	
		private static LinkedHashMap<String, Integer> createBoolValues() {
		LinkedHashMap<String, Integer> answer = new LinkedHashMap<String, Integer>();
		answer.put( GenericDinosKnimeWorkflow.BOOL_FALSE_TEXT , GenericDinosKnimeWorkflow.BOOL_FALSE_NUM);
		answer.put( GenericDinosKnimeWorkflow.BOOL_TRUE_TEXT , GenericDinosKnimeWorkflow.BOOL_TRUE_NUM);
		
		return answer;
	}
	
	public static Dataset KnimeTableToDinosDataset(
			final BufferedDataTable table,
			final ExecutionContext exec,
			final HashSet targets,
			final String[] censoring) throws Exception {

		if(table.size() == 0)
			throw new Exception("No rows in the given table");

	    // The algorithm is divided in three parts:
	    // * PART 1: PROCESS COLUMNS FOR ATTRIBUTES *
	    // * PART 2: CREATE INSTANCES *
	    // * PART 3: CREATE ATTRIBUTES *

		DataTableSpec tableSpecs = table.getDataTableSpec();
		String[] columnNames = tableSpecs.getColumnNames();
		int columnAmount = columnNames.length;
		DataColumnSpec columnSpecs = tableSpecs.getColumnSpec(0);
		CandidateAttribute[] attributes = new CandidateAttribute[columnAmount];
		
	    // * PART 1: PROCESS COLUMNS FOR ATTRIBUTES  *
		exec.setProgress( 0, GenericDinosKnimeWorkflow.MESSAGE_PROCESSCOLUMNS );

		for(int count = 0 ; count < columnAmount ; ++count ) {
			DataColumnSpec currentColumnSpecs = tableSpecs.getColumnSpec(count);
			DataType currentType = currentColumnSpecs.getType();
			String identifier = currentType.getIdentifier();
			DataColumnDomain currentColumnDomain = currentColumnSpecs.getDomain();
			boolean survivalRelated = false;
			
			
			if ( (censoring != null) ) {
				// Check if it is the survival indicator
				if(columnNames[count].equals(censoring[0]) )
				{	attributes[count] = new CandidateAttribute(columnNames[count] ,SURVIVAL$.MODULE$ , false, false, -1, ( (DoubleValue) currentColumnDomain.getLowerBound() ).getDoubleValue() , ( (DoubleValue) currentColumnDomain.getUpperBound() ).getDoubleValue(), null, "");
				survivalRelated = true;}
					// Check if it is the censoring indicator
				else if (columnNames[count].equals(censoring[1]))
				{	attributes[count] = new CandidateAttribute(columnNames[count] , CENSORED$.MODULE$ , false, false, -1, 0, 0, null, censoring[2]) ;	survivalRelated = true;
				validateCensor( censoring[2] , currentColumnDomain, columnNames[count] , identifier);
				}
			}
			
			if(survivalRelated == false) {
					// Nominal tags? ("String")
				if ( identifier.equals(GenericDinosKnimeWorkflow.ID_STRING) )
				{	attributes[count] = new CandidateAttribute(columnNames[count] , NOMINAL$.MODULE$ , false, false, -1, 0 , 0,  setOfStringCellsToHashMapPlusIndex(currentColumnDomain.getValues() ), "") ;	survivalRelated = true;}	
					// Nominal tags? ("Boolean")
				else if ( identifier.equals(GenericDinosKnimeWorkflow.ID_BOOL) )
				{	attributes[count] = new CandidateAttribute(columnNames[count] , NOMINAL$.MODULE$ , false, false, -1, 0 , 0, BOOL_VALUES , "") ;	survivalRelated = true;}	
				// Real value? ("Double")
				else if(identifier.equals(GenericDinosKnimeWorkflow.ID_DOUBLE) )
				{	attributes[count] = new CandidateAttribute(columnNames[count] , REAL$.MODULE$ , false, false, -1, ( (DoubleValue) currentColumnDomain.getLowerBound() ).getDoubleValue() , ( (DoubleValue) currentColumnDomain.getUpperBound() ).getDoubleValue(), null, "") ;	survivalRelated = true;}
					// Integer value? ("Int")
				else if ( identifier.equals(GenericDinosKnimeWorkflow.ID_INT) )
				{	attributes[count] = new CandidateAttribute(columnNames[count] , INTEGER$.MODULE$ , false, false, -1, ( (IntValue) currentColumnDomain.getLowerBound() ).getIntValue() , (double) ( (IntValue) currentColumnDomain.getUpperBound() ).getIntValue() , null, "") ;	survivalRelated = true;}
					// Throw an error
				else
				{	throw new IllegalArgumentException("Unsopported data type " + currentType.getName() + " (" + identifier + ") for column " + columnNames[count]);	}
			}
			exec.checkCanceled();
			}
		
			// Now, define attributes as either target or non target
		int countAttributes = 0;
		int countTargets = 0;
		int countVariables = 0;
		for(int count = 0 ; count < attributes.length ; ++count) {
			// Skip censored values
	          CandidateAttribute candidateAttribute = attributes[count];
	          if(candidateAttribute.vtype != CENSORED$.MODULE$) {
	        	  String variableName = candidateAttribute.name;
	        	  
	        	  // Is it a target (in output)?
	        	if(targets.contains(variableName) ) {
	                candidateAttribute.target = true;
	                candidateAttribute.position = countTargets;
	                countTargets = countTargets + 1;
	        	}
	        	// Otherwise, it is an attribute (in input)
	        	else {
	        		candidateAttribute.target = false;
	                candidateAttribute.position = countAttributes;
	                countAttributes = countAttributes + 1;
	        	}
	          }
	          exec.checkCanceled();
		}
		
		countVariables = countAttributes + countTargets;
		
		// * PART 2: CREATE INSTANCES *
		exec.setProgress( 0, GenericDinosKnimeWorkflow.MESSAGE_CREATINGINSTANCES );
		
		CloseableRowIterator rows = table.iterator();
		LinkedList<Instance> instances = new LinkedList();
		int countInstances = 0;
		while (rows.hasNext() ) {
			DataRow currentRow = rows.next();
			Option [] valuesToAdd = new Option[countAttributes];
			double[] targetsToAdd = new double[countTargets];
			int countValues = 0;
			boolean isInstanceCensored = false;
			
			while(countValues < attributes.length) {
				DataCell cell = currentRow.getCell(countValues);
				CandidateAttribute attributeInfo = attributes[countValues];
				 @SuppressWarnings("rawtypes")
				Option insertValue = null;
				 boolean insert = true;
				 double cellValue = 0d;
				 
				 if(attributeInfo.skip == false) {
					 if ( cell.isMissing() ) // Is the value missing?
			            { insertValue = scala.None$.MODULE$; }
			          else {
			        	  if(attributeInfo.vtype == NOMINAL$.MODULE$) {
			        		  LinkedHashMap<String, Integer> possibleValues = attributeInfo.values;
			        		  if( cell instanceof StringValue ) {
			        		  int position = possibleValues.get( ( (StringValue) cell ).getStringValue() );
			        		  cellValue = position;
			        		  }
			        		  else {
			        			  if( ( (BooleanValue) cell ).getBooleanValue() == false )
			        				  cellValue = GenericDinosKnimeWorkflow.BOOL_FALSE_NUM;
			        			  else
			        				  cellValue = GenericDinosKnimeWorkflow.BOOL_TRUE_NUM;
			        		  }
			        	  }
			        	  else if(attributeInfo.vtype == REAL$.MODULE$) {
			        		  cellValue = ( (DoubleValue) cell ).getDoubleValue();
			        	  }
			        	  else if(attributeInfo.vtype == INTEGER$.MODULE$) {
			        		  cellValue = ( (IntValue) cell ).getIntValue();
			        	  }
			        	  else if(attributeInfo.vtype == SURVIVAL$.MODULE$) {
			        		  cellValue = ( (DoubleValue) cell ).getDoubleValue();
			        		  if (cellValue <= 0)
			                      throw new RuntimeException("Instance at index " + countInstances + " have a negative survival value of " + cellValue);
			        	  }
			        	  else if(attributeInfo.vtype == CENSORED$.MODULE$) {
			        		  isInstanceCensored = attributeInfo.censor.equals( cell.toString() );
			        	      insert = false;
			        	  }
			          }
					 
					 if(insert) {
						 if (insertValue != None$.MODULE$)
							 insertValue = Option.apply(cellValue);
						 if (attributeInfo.target == false) {
							 valuesToAdd[attributeInfo.position] = insertValue;
						 }
						 else {
							 if (insertValue != None$.MODULE$)
								 targetsToAdd[attributeInfo.position] = (double) insertValue.get();
							 else
					             throw new RuntimeException("Instance at index " + countInstances + " has no class value");
						 }
					 }
				 }
			        // Loop Count
			        ++ countValues;
			}
			
			// Create instance, update loop count and get the next element
		      instances.add( new Instance(valuesToAdd, targetsToAdd, countInstances, isInstanceCensored) );
		      ++countInstances;
		      exec.checkCanceled();
		}

	    // * PART 3: CREATE ATTRIBUTES *
	    // Create attributes in the way datasets need it
	    // from candidate attributes
		exec.setProgress( 0, GenericDinosKnimeWorkflow.MESSAGE_CREATINGATTRIBUTES);
		Attribute[] finalAttributes = new Attribute[countAttributes + countTargets];
		int loopAttributes = 0;
		while(loopAttributes < attributes.length) {
			CandidateAttribute attributeInfo = attributes[loopAttributes];
			if(attributeInfo.skip == false && attributeInfo.vtype != CENSORED$.MODULE$) {
				// All attributes are inserted in one array, so this math is needed
				 int insertPosition = -1;
				 if (attributeInfo.target) 
					 insertPosition = countAttributes + attributeInfo.position;  // Target variable
		        else
		        	insertPosition = attributeInfo.position; // Non target variable
				 
				 Attribute newAttribute = null;
				 if(attributeInfo.vtype == REAL$.MODULE$ || attributeInfo.vtype == INTEGER$.MODULE$ || attributeInfo.vtype == SURVIVAL$.MODULE$) {
					 newAttribute = new Attribute(
					          // Numeric (Integer or Real) attribute, it has an interval of possible values
					          attributeInfo.name,
					          attributeInfo.vtype,
					          new String[0],
					          attributeInfo.minimum,
					          attributeInfo.maximum,
					          insertPosition
					        );
				 }
				 else if (attributeInfo.vtype == NOMINAL$.MODULE$) {
					 newAttribute = new Attribute(
					          // Numeric (Integer or Real) attribute, it has an interval of possible values
					          attributeInfo.name,
					          attributeInfo.vtype,
					          genericSetToArrayOfArray( attributeInfo.values.keySet() ),
					          attributeInfo.minimum,
					          attributeInfo.maximum,
					          insertPosition
					        );
				 }
			        // Insert the attributes
			        finalAttributes[insertPosition] = newAttribute;
			}
	        // Update loop count
	        ++loopAttributes;
	        exec.checkCanceled();
		}
		
		exec.setProgress( 0, GenericDinosKnimeWorkflow.MESSAGE_READY);
		return Dataset.apply(finalAttributes, toInstanceArray(instances) );

		}


			private static void validateCensor(
					final String indicator,
					final DataColumnDomain currentColumnDomain,
					final String name,
					String identifier) {

				if ( identifier.equals(GenericDinosKnimeWorkflow.ID_STRING) ) {
					if ( !currentColumnDomain.getValues().contains( new StringCell(indicator) ) )
						throw new IllegalArgumentException( "Value defined as censor indicator (" + indicator + ") does not exist for the column \"" + name + "\" of type String" );
				} 
				else if ( identifier.equals(GenericDinosKnimeWorkflow.ID_INT) ) {
					var lower = ( (IntCell) currentColumnDomain.getLowerBound() ).getIntValue();
					var upper = ( (IntCell) currentColumnDomain.getUpperBound() ).getIntValue();
					int value = 0;
					boolean formatError= false;
					
					try {value = Integer.parseInt(indicator); }
					catch (NumberFormatException e) { formatError = true; }
					if ( formatError )
						throw new IllegalArgumentException( "Value defined as censor indicator (" + indicator + ") for the chosen column \"" + name + "\" of type Int is not a valid number" );
					if ( value < lower || value > upper )
						throw new IllegalArgumentException( "Value defined as censor indicator (" + indicator + ") is outside the range of values [ " + lower + " ; " + upper + " ] for the column \"" + name + "\" of type Int" );
				}
				else if ( identifier.equals(GenericDinosKnimeWorkflow.ID_BOOL) ) {
					
					if(
						!indicator.equals( GenericDinosKnimeWorkflow.BOOL_FALSE_TEXT ) &&
						!indicator.equals( GenericDinosKnimeWorkflow.BOOL_TRUE_TEXT )
					)
						throw new IllegalArgumentException( "Value defined as censor indicator (" + indicator + ") for the chosen column \"" + name + "\" of type Boolean is neither \"" + GenericDinosKnimeWorkflow.BOOL_FALSE_NUM + "\" or \" " + GenericDinosKnimeWorkflow.BOOL_FALSE_NUM + "\"" );
				}
		
	}

			/**
			 * Helper function, for getting the possible values of a String column
			 * 
			 * @param Values The set of possible values of the column as present in the set returned by the getValues method of a StringCell's DatacolumnDomain
			 * 
			 * @return A linked HashMap where each key is a possible value and the value is it's position number (each one unique)
		 	*/
		private static LinkedHashMap<java.lang.String, Integer> setOfStringCellsToHashMapPlusIndex(
				Set<DataCell> values) {
		Iterator <DataCell> iterator = values.iterator();
		LinkedHashMap<java.lang.String, Integer> answer = new LinkedHashMap<String, Integer>();
		int count = 0;
		while(iterator.hasNext()) {
			answer.put( ( (StringValue) iterator.next() ).getStringValue(), count);
			count += 1;
		}
		return answer;
	}
		
		@Deprecated
		// TODO remove
		private static String[] setOfStringCellsToArray(
				Set<DataCell> values) {
		Iterator <DataCell> iterator = values.iterator();
		String [] answer = new String[values.size()];
		int position = 0;
		while( iterator.hasNext() ) {
			answer[position] = ( (StringValue) iterator.next() ).getStringValue();
			++ position;
		}
		return answer;
	}
		
			/**
			 * Helper function, for getting the strings of the values of a set to an array
			 * 
			 * @param Values The set of possible values
			 * 
			 * @return An array with the elemetns of the set
		 	*/
		private static <T> String[] genericSetToArrayOfArray(
				Set<T> values) {
		Iterator <T> iterator = values.iterator();
		String [] answer = new String[values.size()];
		int position = 0;
		while( iterator.hasNext() ) {
			answer[position] = iterator.next().toString();
			++ position;
		}
		return answer;
	}

			/**
			 * Helper function, for getting as an array the linked list in absence of the convenience of Scala's "toArray"
			 * 
			 * @param linkedlist The list of possible values
			 * 
			 * @return An array with the elemetns of the list
		 	*/
		private static Instance[] toInstanceArray(
				LinkedList<Instance> linkedlist) {
			Instance[] answer = new Instance[linkedlist.size()];
			Iterator<Instance> iter = linkedlist.iterator();
			int count = 0;
			while(iter.hasNext()) {
				answer[count] = iter.next();
				++count;
			}
			
			return answer;
		}

		/**
		 * Obtain a Configuration object for use in DINOS
		 * 
		 * @param trials How many trials to use in the algorithm
		 * @param collectItarationMetrics Wheter to collecto or not metrics results after each iteration
		 * @param settings With an even amount of elements where each is assumed to alternate in being the name of the hyperparameter and a string which can be parsed to double for it's value
		 * 
		 * @return An array with the elemetns of the list
	 	*/
		
		@SuppressWarnings({ "unchecked" })
		public static Configuration ArraySettingsToDinosConfig(
				int trials,
				boolean collectItarationMetrics,
				java.lang.String[] settings) {
			
			@SuppressWarnings("rawtypes")
		LinkedHashMap map = new LinkedHashMap<String, Double>(); // Have this ready

			// Remember, even positions are names, even are values that can be parsed to double
		for( int count = 0; count < settings.length ; ++count  ) {
			map.put(settings[count] , Double.parseDouble( settings[++count] ) );
		}
				// Create and return the object
			return new Configuration(
					trials,
					collectItarationMetrics,
					scala.collection.immutable.Map.from(CollectionConverters.MapHasAsScala(map).asScala()) ); // Use this scala converter

		}


	}
	
