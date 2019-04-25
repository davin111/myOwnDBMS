package davinDBMS.query;

import java.io.UnsupportedEncodingException;

import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

import davinDBMS.entity.*;


public class DropTableQuery extends DDLQuery { //DDLQuery extends Query
  
  //check semantic and syntactic correctness, and delete the table in DB
  public void apply(Database db) {
    try {
		check(db); //include 'delete' method
	}
    catch (QueryException qe) {
		System.out.println(qe);
	}
  }
  
  public void check(Database db) throws QueryException {
	Cursor cursor = null;
		
	try {
	  cursor = db.openCursor(null, null);
			
	  DatabaseEntry key = new DatabaseEntry(table.getName().getBytes("UTF-8"));
	  DatabaseEntry data = new DatabaseEntry();
			
	  OperationStatus searchTable = cursor.getSearchKey(key, data, LockMode.DEFAULT);
			
	  if (searchTable == OperationStatus.NOTFOUND) { //if the table does not exist
	    throw new QueryException(Messages.NO_SUCH_TABLE);
	  }
	  else {		
		table = deserialize(data); //read the table
		
		checkReference(db); //check if there is any table referring this table
		dropTable(db, cursor); //delete the table in DB
	  }
	}
	catch (UnsupportedEncodingException e) {
      e.printStackTrace();
	}
	finally {
		cursor.close();
	}
  }
  
  //check if there is any table referring this table
  private void checkReference(Database db) throws QueryException {
	  for(Column column : table.getColumns()) { //for each column of this table
		  if(column.getReferredByTables().size() != 0) { //if there is a table referring it
			  throw new QueryException(Messages.DROP_REFERENCED_TABLE_ERROR, table.getName());
		  }
	  }
  }
  
  //delete the table in DB
  private void dropTable(Database db, Cursor cursor) {
	cursor.delete(); //delete the table
	
	removeReference(db); //delete referring information of this table
		
	System.out.printf(Messages.DROP_SUCCESS.getMessage(), table.getName());
	System.out.println();
  }
  
  private void removeReference(Database db) {
	Table referToTable = null;
	Column referToColumn = null;
	
	//update the content of referred tables in DB
    for(Column column : table.getColumns()) { //for each column of this table
    	referToTable = column.getReferToTable();
    	referToColumn = column.getReferToColumn();
    	if(referToColumn != null) { //if there is a column referred by this column	
    		//delete referring information
    		referToColumn.getReferredByTables().remove(table);
    		    		
    		updateTable(db, referToTable); //update the content of referred table in DB
    	}
    }
  }
}
