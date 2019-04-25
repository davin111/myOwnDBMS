package davinDBMS.query;

public class QueryException extends Exception { //exception which can be generated in this program
	private String name = null;
	
	
	//constructors
	public QueryException(Messages msg){
		super(msg.getMessage());
	}
	
	public QueryException(Messages msg, String name){ //some messages need the name of table or column
		super(msg.getMessage());
		this.name = name;
	}
	
	
	@Override
	public String toString() {
		if(name != null) {
		  return String.format(this.getMessage(), name);
		}
		else {
		  return this.getMessage();
		}
	}
}
