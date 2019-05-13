package davinDBMS.entity;

import java.io.Serializable;
import java.util.HashMap;

public class Record implements Serializable {
	private HashMap<Column, Value> record;
	
	
	public Record() {
		record = new HashMap<Column, Value>();
	}
	
	public Record(Record otherRecord) {
		record = (HashMap<Column, Value>) otherRecord.record.clone();
	}
	
	
	public void putValue(Column column, Value value) {
		record.put(column, value);
	}
	
	public void putAllValue(Record otherRecord) {
		record.putAll(otherRecord.record);
	}
	
	public Value getValue(Column column) {
		return record.get(column);
	}
}
