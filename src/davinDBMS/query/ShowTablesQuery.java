package davinDBMS.query;

import java.io.UnsupportedEncodingException;

import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;


public class ShowTablesQuery extends Query {
  
  //show names of tables
  public void apply(Database db) {
	check(db);
    showTables(db);
  }
  
  public void check(Database db) { //not used for now
	  
  }
  
  //show names of tables
  private void showTables(Database db) {
    Cursor cursor = null;
    int cnt = 0;
	  
	try {
	  cursor = db.openCursor(null, null);
	  DatabaseEntry key = new DatabaseEntry();
	  DatabaseEntry data = new DatabaseEntry();
	  
	  String output = "----------------\n";
	  while(cursor.getNext(key, data, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
	    String foundKey = new String(key.getData(), "UTF-8"); //just need the name of the table
	    output += foundKey + "\n";
	    cnt++;
	  }
	  output += "----------------";
	  
	  if(cnt == 0) { //if there is no table
		  throw new QueryException(Messages.SHOW_TABLES_NO_TABLE);
	  }
	  
	  System.out.println(output);
	}
	catch (DatabaseException de) {
	  de.printStackTrace();
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
