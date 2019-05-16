package davinDBMS.entity;

import java.io.Serializable;

import davinDBMS.entity.Column.DataType;
import davinDBMS.query.Messages;
import davinDBMS.query.QueryException;

public class Value implements Serializable, CompValue {
	public enum ThreeValue{
		TRUE, FALSE, UNKNOWN
	}
	
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
	
	public String toString() {
		if(!isNull) {
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
		else {
			return "null";
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
			if(!isNull && !val.isNull) {
				if(dataType == val.dataType) {
					if(dataType == DataType.INT) {
						return intVal == val.intVal;
					}
					else if(dataType == DataType.CHAR) {
						return strVal.equals(val.strVal);
					}
					else {
						return dateVal.equals(val.dateVal);
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
		else {
			return false;
		}
	}
	

	public ThreeValue compare(Value other, String op) throws QueryException {
		if(!isNull && !other.isNull) {
			if(dataType == DataType.INT && other.dataType == DataType.INT) {
				if(op.equals(">")) {
					if(intVal > other.intVal) {
						return ThreeValue.TRUE;
					}
					else {
						return ThreeValue.FALSE;
					}
				}
				else if(op.equals("<")) {
					if(intVal < other.intVal) {
						return ThreeValue.TRUE;
					}
					else {
						return ThreeValue.FALSE;
					}
				}
				else if(op.equals("=")) {
					if(intVal == other.intVal) {
						return ThreeValue.TRUE;
					}
					else {
						return ThreeValue.FALSE;
					}
				}
				else if(op.equals("!=")) {
					if(intVal != other.intVal) {
						return ThreeValue.TRUE;
					}
					else {
						return ThreeValue.FALSE;
					}
				}
				else if(op.equals(">=")) {
					if(intVal >= other.intVal) {
						return ThreeValue.TRUE;
					}
					else {
						return ThreeValue.FALSE;
					}
				}
				else if(op.equals("<=")) {
					if(intVal <= other.intVal) {
						return ThreeValue.TRUE;
					}
					else {
						return ThreeValue.FALSE;
					}
				}
			}
			else if(dataType == DataType.CHAR && other.dataType == DataType.CHAR) {
				if(op.equals(">")) {
					if(strVal.compareToIgnoreCase(other.strVal) > 0) {
						return ThreeValue.TRUE;
					}
					else {
						return ThreeValue.FALSE;
					}
				}
				else if(op.equals("<")) {
					if(strVal.compareToIgnoreCase(other.strVal) < 0) {
						return ThreeValue.TRUE;
					}
					else {
						return ThreeValue.FALSE;
					}
				}
				else if(op.equals("=")) {
					if(strVal.compareToIgnoreCase(other.strVal) == 0) {
						return ThreeValue.TRUE;
					}
					else {
						return ThreeValue.FALSE;
					}
				}
				else if(op.equals("!=")) {
					if(strVal.compareToIgnoreCase(other.strVal) != 0) {
						return ThreeValue.TRUE;
					}
					else {
						return ThreeValue.FALSE;
					}
				}
				else if(op.equals(">=")) {
					if(strVal.compareToIgnoreCase(other.strVal) >= 0) {
						return ThreeValue.TRUE;
					}
					else {
						return ThreeValue.FALSE;
					}
				}
				else if(op.equals("<=")) {
					if(strVal.compareToIgnoreCase(other.strVal) <= 0) {
						return ThreeValue.TRUE;
					}
					else {
						return ThreeValue.FALSE;
					}
				}
			}
			else if(dataType == DataType.DATE && other.dataType == DataType.DATE) {
				if(op.equals(">")) {
					if(dateVal.compareTo(other.dateVal) > 0) {
						return ThreeValue.TRUE;
					}
					else {
						return ThreeValue.FALSE;
					}
				}
				else if(op.equals("<")) {
					if(dateVal.compareTo(other.dateVal) < 0) {
						return ThreeValue.TRUE;
					}
					else {
						return ThreeValue.FALSE;
					}
				}
				else if(op.equals("=")) {
					if(dateVal.compareTo(other.dateVal) == 0) {
						return ThreeValue.TRUE;
					}
					else {
						return ThreeValue.FALSE;
					}
				}
				else if(op.equals("!=")) {
					if(dateVal.compareTo(other.dateVal) != 0) {
						return ThreeValue.TRUE;
					}
					else {
						return ThreeValue.FALSE;
					}
				}
				else if(op.equals(">=")) {
					if(dateVal.compareTo(other.dateVal) >= 0) {
						return ThreeValue.TRUE;
					}
					else {
						return ThreeValue.FALSE;
					}
				}
				else if(op.equals("<=")) {
					if(dateVal.compareTo(other.dateVal) <= 0) {
						return ThreeValue.TRUE;
					}
					else {
						return ThreeValue.FALSE;
					}
				}
			}
			else {
				throw new QueryException(Messages.WHERE_INCOMPARABLE_ERROR);
			}
		}
		//at least one of the values is null
		else {
			return ThreeValue.UNKNOWN;
		}
		return null;
	}
}
