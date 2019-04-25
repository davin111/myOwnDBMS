package davinDBMS.query;

import java.util.ArrayList;

import davinDBMS.entity.*;

public class Reference { //one reference represents one definition of foreign keys
	
	//referring column names mentioned in the definition of foreign keys
	private ArrayList<String> foreignKeysName = new ArrayList<String>();
	//referring Column objects (will be connected in CreateTableQuery)
	private ArrayList<Column> foreignKeys = new ArrayList<Column>();
	
	//referred table name mentioned in the definition of foreign keys
	private String referToTableName;
	
	//referred column names mentioned in the definition of foreign keys
	private ArrayList<String> referToColumnsName = new ArrayList<String>();
	//referred Column objects (will be connected in CreateTableQuery)
	private ArrayList<Column> referToColumns = new ArrayList<Column>();
	
	
	//constructor
	public Reference(ArrayList<String> fKeys, String tName, ArrayList<String> columnsName) {
		this.foreignKeysName = fKeys;
		this.referToTableName = tName;
		this.referToColumnsName = columnsName;
	}
	
	
	//basic accessors and mutators
	public ArrayList<String> getForeignKeysName(){
		return foreignKeysName;
	}
	
	public ArrayList<Column> getForeignKeys(){
		return foreignKeys;
	}
	
	public String getReferToTableName() {
		return referToTableName;
	}
	
	public ArrayList<String> getReferToColumnsName(){
		return referToColumnsName;
	}
	
	public ArrayList<Column> getReferToColumns(){
		return referToColumns;
	}
	
	public void setForeignKeys(ArrayList<String> foreignKeys){
		this.foreignKeysName = foreignKeys;
	}
}
