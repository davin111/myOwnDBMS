package davinDBMS.query;

import com.sleepycat.je.Database;
import davinDBMS.entity.*;

public abstract class Query {
	public enum QueryType{ //actually almost not used for now
		EXIT
	}
	
	Table table; //many queries focus on one table at a time,
	//so this variable is used freely according to query type
	
	QueryType queryType;
	
	
	//basic accessors and mutators
	public QueryType getType() {
		return queryType;
	}
	
	public Table getTable() {
		return table;
	}
	
	public void setType(QueryType type) {
		queryType = type;
	}
	
	public void setTable(Table table) {
		this.table = table;
	}
	
	
	/* abstract methods will be implemented by child classes */
	//apply the query according to its type
	public abstract void apply(Database db);
	
	//check semantic and syntactic correctness of this query (mostly included in 'apply' method)
	public abstract void check(Database db) throws QueryException;
}
