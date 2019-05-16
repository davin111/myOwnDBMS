package davinDBMS.query;

import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;

import davinDBMS.entity.*;


public class CreateTableQuery extends DDLQuery { //DDLQuery extends Query
  private boolean duplicatePrimaryKeys = false;
  
  //column names mentioned in the definition of primary keys
  private ArrayList<String> primaryKeys = new ArrayList<String>();
  
  //each reference represents one definition of foreign keys
  private ArrayList<Reference> references = new ArrayList<Reference>();

  
  //basic accessors and mutators
  public ArrayList<String> getPriamryKeys() {
	  return primaryKeys;
  }
  
  public ArrayList<Reference> getReferences() {
	  return references;
  }
  
  public void setPrimaryKeys(ArrayList<String> primaryKeys) {
	  if(this.primaryKeys.size() == 0) {
		  this.primaryKeys = primaryKeys;
	  }
	  else { //if already has primary keys, then ready for DUPLICATE_PRIMARY_KEY_DEF_ERROR
		  duplicatePrimaryKeys = true;
	  }
  }
  
  
  //check semantic and syntactic correctness, and create new table in DB
  public void apply(Database db){
	try {
		check(db);
		connectReference(db);
		createTable(db);
		
		//update the content of referred tables in DB
		for(Column column : table.getColumns()) { //for each column of this table
			Table referToTable = column.getReferToTable();
			if(referToTable != null) { //if there is a table referred by this column	
				updateTable(db, referToTable); //update the content of referred table in DB
			}
		}
	}
	catch (QueryException qe) {
		System.out.println(qe);
	}
	catch (UnsupportedEncodingException e) {
		e.printStackTrace();
	}
  }
  
  //check semantic and syntactic correctness
  public void check(Database db) throws QueryException {
	  boolean columnExists = false;
	  
	  Cursor cursor = null;
	  DatabaseEntry key;
	  DatabaseEntry data;
	  
	  try {
		  cursor = db.openCursor(null, null);
		  
		  key = new DatabaseEntry(table.getName().getBytes("UTF-8"));
		  
		  byte[] serializedTable = table.serialize();
		  data = new DatabaseEntry(serializedTable);
		  
		  OperationStatus searchTable = cursor.getSearchKey(key, data, LockMode.DEFAULT);
	  
		  //if there is another table which has the same name
		  if (searchTable == OperationStatus.SUCCESS) {
		    throw new QueryException(Messages.TABLE_EXISTENCE_ERROR);
		  }
	  }
	  catch (DatabaseException de) {
		  de.printStackTrace();
	  }
	  catch (UnsupportedEncodingException e) {
		  e.printStackTrace();
	  }
	  finally {
		  cursor.close();
	  }
		
	  
	  if(duplicatePrimaryKeys) { //if there were multiple definitions of primary keys
		  throw new QueryException(Messages.DUPLICATE_PRIMARY_KEY_DEF_ERROR);
	  }
	
	  for(Column column1 : table.getColumns()) { //for each column
		//if it is CHAR type and has shorter length than 1
		if(column1.getType() == Column.DataType.CHAR && column1.getCharLength() < 1) {
			throw new QueryException(Messages.CHAR_LENGTH_ERROR);
		}	
		  
		//if there is another column which has the same name
		for(Column column2 : table.getColumns()) {
		  if(!column1.equals(column2) && column1.getName().equals(column2.getName())) {
		    throw new QueryException(Messages.DUPLICATE_COLUMN_DEF_ERROR);
		  }
		}
	  }
	  
	  /* process primary keys */
	  for(String primaryKey : primaryKeys) {
		columnExists = false;
		//find the column mentioned in primary keys
		for(Column column : table.getColumns()) {
			if(primaryKey.equals(column.getName())) {
				columnExists = true;
				column.setPrimaryKey(); //set it as an primary key
				table.getPrimaryKeys().add(column);
				break;
			}
		}
		if(!columnExists) { //if the column mentioned in primary keys does not exist
			throw new QueryException(Messages.NON_EXISTING_COLUMN_DEF_ERROR, primaryKey);
		}
	  }
  }
  
  /* process foreign keys */
  //check semantic and syntactic correctness about references, and realize the references
  private void connectReference(Database db) throws QueryException, UnsupportedEncodingException {
	  boolean columnExists = false;
	  
	  Cursor cursor = null;
	  
	  Table referToTable = null;
	  Column referToColumn = null;
	  
	  try {
		  cursor = db.openCursor(null, null);
		  
		  for(Reference reference : references) { //for each reference(each definition of foreign keys)
		    DatabaseEntry key = new DatabaseEntry(reference.getReferToTableName().getBytes("UTF-8"));
			DatabaseEntry data = new DatabaseEntry();
			  
			OperationStatus searchTable = cursor.getSearchKey(key, data, LockMode.DEFAULT);
				
			if (searchTable == OperationStatus.NOTFOUND) { //if referred table cannot be found
		      throw new QueryException(Messages.REFERENCE_TABLE_EXISTENCE_ERROR);
			}
			else {
				for(String foreignKey : reference.getForeignKeysName()) {
					columnExists = false;
					//find the referring column mentioned in foreign keys
					for(Column column : table.getColumns()) {
						if(foreignKey.equals(column.getName())) {
							columnExists = true;
							column.setForeignKey(); //set it as an foreign key
							reference.getForeignKeys().add(column);
							break;
						}
					}
					if(!columnExists) { //if the referring column mentioned in foreign keys does not exist
						throw new QueryException(Messages.NON_EXISTING_COLUMN_DEF_ERROR, foreignKey);
					}
				}
				
				referToTable = deserialize(data); //read the referred table
				
				//check REFERENCE_TYPE_ERROR - the number of columns
				if(reference.getForeignKeys().size() != reference.getReferToColumnsName().size()) {
					throw new QueryException(Messages.REFERENCE_TYPE_ERROR);
				}
				
				//check REFERENCE_COLUMN_EXISTENCE_ERROR and REFERENCE_NON_PRIMARY_KEY_ERROR
				for(String columnName : reference.getReferToColumnsName()) {
					columnExists = false;
					//find the referred column mentioned in foreign keys
					for(Column column : referToTable.getColumns()) {		
						if(columnName.equals(column.getName())) {
							columnExists = true;
							if(!column.isPrimaryKey()) { //if it is not a primary key
								throw new QueryException(Messages.REFERENCE_NON_PRIMARY_KEY_ERROR);
							}
							reference.getReferToColumns().add(column);
							break;
						}
						
						
					}
					if(!columnExists) { //if the referred column mentioned in foreign keys does not exist
						throw new QueryException(Messages.REFERENCE_COLUMN_EXISTENCE_ERROR);
					}
				}
				
				//check REFERENCE_TYPE_ERROR - the type of columns
				Iterator<Column> iter = reference.getReferToColumns().iterator();
				
				for(Column column : reference.getForeignKeys()) {
					referToColumn = iter.next();
					
					//if referring and referred column have different data type
					if(column.getType() != referToColumn.getType()) {
						throw new QueryException(Messages.REFERENCE_TYPE_ERROR);
					}
					else if(column.getType() == Column.DataType.CHAR) {
						if(column.getCharLength() != referToColumn.getCharLength()) {
							throw new QueryException(Messages.REFERENCE_TYPE_ERROR);
						}
					}
					
					//connect mutual tables and columns
					column.setReferToTable(referToTable);
					column.setReferToColumn(referToColumn);
					
					//create referring information
					referToColumn.getReferredByTables().add(table);
				}
			}
		  }
	  }
	  finally {
	  	cursor.close();
	  }
  }

  //create new table in DB
  private void createTable(Database db) {
	  Cursor cursor = null;
	  DatabaseEntry key;
	  DatabaseEntry data;
	  
	  try {
		  cursor = db.openCursor(null, null);
		  
		  key = new DatabaseEntry(table.getName().getBytes("UTF-8"));
		  
		  byte[] serializedTable = table.serialize();
		  data = new DatabaseEntry(serializedTable);
		  
		  //already check TABLE_EXISTENCE_ERROR
		  cursor.getSearchKey(key, data, LockMode.DEFAULT);
			
		  cursor.put(key, data); //create new table
		  
		  System.out.printf(Messages.CREATE_TABLE_SUCCESS.getMessage(), table.getName());
		  System.out.println();
	  }
	  catch (DatabaseException de) {
	    de.printStackTrace();
	  }
	  catch (UnsupportedEncodingException e) {
		e.printStackTrace();
	  }
	  finally {
		  cursor.close();
	  }
  }
}
