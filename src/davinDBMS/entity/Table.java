package davinDBMS.entity;

import java.io.Serializable;
import java.io.ObjectOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.util.ArrayList;

public class Table implements Serializable {
	private String name;
	private ArrayList<Column> columns;
	private ArrayList<Record> records;
	
	
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
	
	public void addColumn(Column column) {
		columns.add(column);
	}
	
	public void addRecord(Record record) {
		records.add(record);
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
}
