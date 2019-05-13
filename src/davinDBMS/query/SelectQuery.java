package davinDBMS.query;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

import davinDBMS.entity.*;

public class SelectQuery extends DMLQuery {
	private ArrayList<String[]> fromTables;
	private ArrayList<ArrayList<BooleanFactor>> boolExp = null;
	
	
	public void setFromTables(ArrayList<String[]> names) {
		fromTables = names;
	}
	
	public void setBoolExp(ArrayList<ArrayList<BooleanFactor>> boolExp) {
		this.boolExp = boolExp;
	}
	

	public void apply(Database db) {
		try {
			check(db);	
		}
		catch (QueryException qe) {
			System.out.println(qe);
		}
	}


	public void check(Database db) throws QueryException {
		Cursor cursor = null;
		DatabaseEntry key = null;
		DatabaseEntry data = new DatabaseEntry();
		Table fromTable = null, tmpTable = null;
		
		try {
			for(String[] tableName : fromTables) {
				key = new DatabaseEntry(tableName[0].getBytes("UTF-8"));
				
				OperationStatus searchTable = cursor.getSearchKey(key, data, LockMode.DEFAULT);
				
				if (searchTable == OperationStatus.NOTFOUND) { //if the table does not exist
					throw new QueryException(Messages.SELECT_TABLE_EXISTENCE_ERROR);
				}
				else {
					tmpTable = deserialize(data);
					if(fromTable == null) {
						fromTable = tmpTable;
					}
					else {
						fromTable = fromTable.product(tmpTable);
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
