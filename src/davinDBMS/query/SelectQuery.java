package davinDBMS.query;

import java.util.ArrayList;

import com.sleepycat.je.Database;

public class SelectQuery extends DMLQuery {
	private ArrayList<String[]> fromTables;
	
	
	public void setFromTables(ArrayList<String[]> names) {
		fromTables = names;
	}
	

	public void apply(Database db) {
		
		
	}


	public void check(Database db) throws QueryException {
		
		
	}

}
