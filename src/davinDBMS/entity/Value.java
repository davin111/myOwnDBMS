package davinDBMS.entity;

import java.io.Serializable;

import davinDBMS.entity.Column.DataType;

public class Value implements Serializable {
	private DataType dataType;
	private int intVal;
	private String strVal;
	private String dateVal;
	boolean isNull = false;
	
	
	public Value() {
		dataType = null;
	}
	
	public Value(String val) {
		dataType = null;
		setVal(val);
	}
	
	public Value(DataType type, String val) {
		setType(type);
		setVal(val);
	}
	
	
	public void setType(DataType datatype) {
		this.dataType = datatype;
	}
	
	public void setVal(String val) {
		if(val.equals("null")) {
			isNull = true;
		}
		else if(dataType == DataType.INT) {
			intVal = Integer.parseInt(val);
		}
		else if(dataType == DataType.CHAR) {
			strVal = val;
		}
		else if(dataType == DataType.DATE){
			dateVal = val;
		}
	}
	
	
	public DataType getType() {
		return dataType;
	}
	
	public String getVal() {
		if(dataType == DataType.INT) {
			return "" + intVal;
		}
		else if(dataType == DataType.CHAR) {
			return strVal;
		}
		else {
			return dateVal;
		}
	}
	
	public int getIntVal() {
		if(dataType == DataType.INT) {
			return intVal;
		}
		else { //
			return 0;
		}
	}
	
	public String getStrVal() {
		if(dataType == DataType.CHAR) {
			return strVal;
		}
		else { //
			return null;
		}
	}
	
	public String getDateVal() {
		if(dataType == DataType.DATE) {
			return dateVal;
		}
		else { //
			return null;
		}
	}
	
	public boolean isNull() {
		return isNull;
	}
	
	public boolean equals(Object obj) {
		if(obj instanceof Value) {
			Value val = (Value)obj;
			if(dataType == val.dataType) {
				if(dataType == DataType.INT) {
					return intVal == val.intVal;
				}
				else if(dataType == DataType.CHAR) {
					return strVal == val.strVal;
				}
				else {
					return dateVal == val.dateVal;
				}
			}
			else {
				return false;
			}
		}
		else {
			return false;
		}
	}
}
