package davinDBMS.query;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.UnsupportedEncodingException;

import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

import davinDBMS.entity.Column;
import davinDBMS.entity.Table;

public abstract class DDLQuery extends Query{ //has methods for DDL queries

	//many DDL queries need to deserialize byte array for getting a table
	public static Table deserialize(DatabaseEntry data) {
	  ByteArrayInputStream bais = new ByteArrayInputStream(data.getData());
	  ObjectInputStream ois = null;
	  Table t = null;
	  
	  try {
		ois = new ObjectInputStream(bais);
	  } catch (IOException e) {
		e.printStackTrace();
	  }
	  try {
		t = (Table)ois.readObject();
	  }
	  catch (ClassNotFoundException | IOException e) {
		e.printStackTrace();
	  }
	  
	  return t;
    }
	
	
	//update the contents of a table, and tables referring the table in DB
    public static void updateTable(Database db, Table table) {
	  Cursor cursor = null;
	  
	  try {
		cursor = db.openCursor(null, null);  
		  
		DatabaseEntry key = new DatabaseEntry(table.getName().getBytes("UTF-8"));
		DatabaseEntry data = new DatabaseEntry();
		
		OperationStatus searchTable = cursor.getSearchKey(key, data, LockMode.DEFAULT);
		
		//if the table does not exist
		//ex> in create table query, parameter 'table' has new table as referredByTable,
		//    but the new table cannot be found in DB yet
		if (searchTable == OperationStatus.SUCCESS) {
			cursor.delete();
		}
		
		byte[] serializedTable = table.serialize();
		data = new DatabaseEntry(serializedTable);
		
		cursor.put(key, data);
		
		//need to update tables referring this table (recursive - risky)
		for(Column column : table.getColumns()) {
			if(column.getReferredByTables().size() != 0) {
				for(Table getReferredByTable : column.getReferredByTables()) {
					updateTable(db, getReferredByTable);
				}
			}
		}
	  }
	  catch (UnsupportedEncodingException e) {
		e.printStackTrace();
	  }
	  finally {
		  cursor.close();
	  }
    }
}
