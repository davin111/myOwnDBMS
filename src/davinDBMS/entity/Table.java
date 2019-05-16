package davinDBMS.entity;

import java.io.Serializable;
import java.io.ObjectOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Iterator;

public class Table implements Serializable {
	private String name;
	private ArrayList<Column> columns;
	private ArrayList<Record> records;
	private ArrayList<Column> primaryKeys = new ArrayList<Column>();
	
	
	//constructors
	public Table(){
		name = "";
		columns = new ArrayList<Column>();
		records = new ArrayList<Record>();
	}
	
	public Table(String name){
		this.name = name;
		columns = new ArrayList<Column>();
		records = new ArrayList<Record>();
	}
	
	
	//basic accessors and mutators
	public String getName(){
		return name;
	}
	
	public ArrayList<Column> getColumns() {
		return columns;
	}
	
	public ArrayList<Record> getRecords() {
		return records;
	}
	
	public ArrayList<Column> getPrimaryKeys(){
		return primaryKeys;
	}
	
	public void addColumn(Column column) {
		columns.add(column);
		column.setBelongToTable(this);
	}
	
	public void addRecord(Record record) {
		records.add(record);
	}
	
	public void removeRecords(ArrayList<Record> deleteRecords) {
		Record rec = null;
		for(Iterator<Record> iter = records.iterator(); iter.hasNext(); ) {
			rec = iter.next();
			if(deleteRecords.contains(rec)) {
				iter.remove();
			}
		}
	}
	
	
	//Table class is needed to serialize to be saved in DB
	public byte[] serialize(){
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(baos);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			oos.writeObject(this);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		return baos.toByteArray();
	}
	
	
	public Table temporaryProduct(Table otherTable){
		Table resultTable = new Table();
		Record newRecord = null;
		
		for(Column column : this.columns) {
			resultTable.columns.add(column);
		}
		
		for(Column column : otherTable.columns) {
			resultTable.columns.add(column);
		}
		
		for(Record record : this.records) {
			for(Record otherRecord : otherTable.records) {
				newRecord = new Record(record);
				newRecord.putAllValue(otherRecord);
				
				resultTable.records.add(newRecord);
			}
		}
		
		
		return resultTable;
	}
}
