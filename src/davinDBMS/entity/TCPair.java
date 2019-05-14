package davinDBMS.entity;

public class TCPair implements CompValue {
	private String tableName = null;
	private Table table = null;
	private String columnName = null;
	private Column column = null;
	private String alias = null;
	
	
	public void setTableName(String name) {
		this.tableName = name;
	}
	
	public String getTableName() {
		return tableName;
	}
	
	public void setTable(Table table) {
		this.table = table;
	}
	
	public Table getTable() {
		return table;
	}
	
	public void setColumnName(String name) {
		this.columnName = name;
	}
	
	public String getColumnName() {
		return columnName;
	}
	
	public void setColumn(Column column) {
		this.column = column;
	}
	
	public Column getColumn() {
		return column;
	}
	
	public void setAlias(String name) {
		this.alias = name;
	}
	
	public String getAlias() {
		return alias;
	}
	
}
