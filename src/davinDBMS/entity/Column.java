package davinDBMS.entity;

import java.io.Serializable;
import java.util.ArrayList;

public class Column implements Serializable {
  public enum DataType{
	  INT, CHAR, DATE
  }
  
  private String name;
  private DataType dataType;
  private int charLength;
  private boolean canBeNull = true;
  private boolean isPri = false;
  private boolean isFor = false;
  
  private Table referToTable = null;
  private Column referToColumn = null;
  
  private ArrayList<Table> referredByTables = new ArrayList<Table>();
  
  
  //constructors
  public Column(){
	  name = "";
  }
	
  public Column(String name){
	  this.name = name;
  }
  
  
  //basic accessors and mutators
  public String getName(){
	  return name;
  }
  
  public DataType getType() {
	  return dataType;
  }
  
  public boolean isPrimaryKey() {
	  return isPri;
  }
  
  public boolean isForeignKey() {
	  return isFor;
  }
  
  public int getCharLength() {
	  return charLength;
  }
  
  public boolean canBeNull() {
	  return canBeNull;
  }
  
  public void setPrimaryKey() {
	  isPri = true;
	  canBeNull = false;
  }
  
  public void setForeignKey() {
	  isFor = true;
  }
  
  public void setNotNull() {
	  canBeNull = false;
  }
  
  public void setType(DataType datatype) {
	  this.dataType = datatype;
  }
  
  public void setCharLength(int n) {
	  charLength = n;
  }
  
  
  public void setReferToTable(Table table) {
	  referToTable = table;
  }
  
  public void setReferToColumn(Column column) {
	  referToColumn = column;
  }
  
  
  public Table getReferToTable() {
	  return referToTable;
  }
  
  public Column getReferToColumn() {
	  return referToColumn;
  }
  
  public ArrayList<Table> getReferredByTables() {
	  return referredByTables;
  }
  
  
  //methods for desc query
  public String getTypeToString() {
	  String typeInfo = "" + dataType;
	  
	  if(dataType == DataType.CHAR) {
		  typeInfo += "(" + charLength + ")";
	  }
	  
	  return typeInfo.toLowerCase();
  }
  
  public String getKeyTypeToString() {
	  String keyInfo = "";
	  
	  if(isPri) {
		keyInfo += "PRI";
		  
		if(isFor) {
		  keyInfo += "/FOR";
		}
	  }
	  else if(isFor){
	    keyInfo += "FOR";
	  }
	  
	  return keyInfo;
  }
}
