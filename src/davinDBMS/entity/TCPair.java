package davinDBMS.entity;

public class TCPair implements CompValue {
	private String tableName;
	private Table table;
	private String columnName;
	private Column column;
	private String alias;
	
	
	public void setTableName(String name) {
		this.tableName = name;
	}
	
	public void setColumnName(String name) {
		this.columnName = name;
	}
	
	public void setAlias(String name) {
		this.alias = name;
	}
	
}
