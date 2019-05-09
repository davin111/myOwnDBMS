package davinDBMS.entity;

import java.io.Serializable;
import java.util.HashMap;

public class Record implements Serializable {
	private HashMap<Column, Value> record = new HashMap<Column, Value>();
	
	public void putValue(Column column, Value value) {
		record.put(column, value);
	}
	
	public Value getValue(Column column) {
		return record.get(column);
	}
}
