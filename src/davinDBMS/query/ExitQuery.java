package davinDBMS.query;

import com.sleepycat.je.Database;
public class ExitQuery extends Query { //actually almost not used for now
	
  public ExitQuery(){ //born with QueryType EXIT
	  this.setType(QueryType.EXIT);
  }
  
  public void apply(Database db) {
	  
  }
  
  public void check(Database db) {
	  
  }
}
