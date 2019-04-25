package davinDBMS.query;

import java.io.UnsupportedEncodingException;

import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

import davinDBMS.entity.*;


public class DescQuery extends DDLQuery{ //DDLQuery extends Query
  //(actually desc query is not DDL query, but for convenience)

  //check semantic and syntactic correctness, and describe the table
  public void apply(Database db) {
	check(db);
    descTable(db);
  }
  
  public void check(Database db) { //not used for now
	  
  }
  
  private void descTable(Database db) {
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
			
			//describe the table
			System.out.println("-------------------------------------------------");
			System.out.println("table_name [" + table.getName() + "]");
			System.out.printf("%-10s\t%-10s\t%-10s\t%-10s\n", "column_name", "type", "null", "key");
			for(Column column : table.getColumns()) {
				System.out.printf("%-10s\t%-10s\t", column.getName(), column.getTypeToString());
				if(column.canBeNull()) {
					System.out.printf("%-10s\t", "Y");
				}
				else {
					System.out.printf("%-10s\t", "N");
				}
				System.out.printf("%-10s\n", column.getKeyTypeToString());
			}
			System.out.println("-------------------------------------------------");
		}
	}
	catch (UnsupportedEncodingException e) {
	  e.printStackTrace();
	}
	catch (QueryException qe) {
		System.out.println(qe);
	}
	finally {
	  cursor.close();
	}	
  }
}
