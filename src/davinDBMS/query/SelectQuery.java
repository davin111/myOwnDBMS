package davinDBMS.query;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

import davinDBMS.entity.*;

public class SelectQuery extends DMLQuery {
	private ArrayList<TCPair> selectedTCPairs = null;
	
	
	public void setSelectedTCPairs(ArrayList<TCPair> tcPairs) {
		selectedTCPairs = tcPairs;
	}
	
	public void setFromTables(ArrayList<String[]> names) {
		fromTablesName = names;
	}
	

	public void apply(Database db) {
		try {
			check(db);
			select(db);
		}
		catch (QueryException qe) {
			System.out.println(qe);
		}
	}


	public void check(Database db) throws QueryException {
		Cursor cursor = null;
		DatabaseEntry key = null;
		DatabaseEntry data = new DatabaseEntry();
		table = null;
		Table tmpTable = null;
		
		try {
			cursor = db.openCursor(null, null);
			
			for(String[] tableName : fromTablesName) {
				key = new DatabaseEntry(tableName[0].getBytes("UTF-8"));
				
				OperationStatus searchTable = cursor.getSearchKey(key, data, LockMode.DEFAULT);
				
				if (searchTable == OperationStatus.NOTFOUND) { //if the table does not exist
					throw new QueryException(Messages.SELECT_TABLE_EXISTENCE_ERROR, tableName[0]);
				}
				else {
					tmpTable = deserialize(data);
					fromTables.add(tmpTable);
					if(table == null) {
						table = tmpTable;
					}
					else {
						table = table.temporaryProduct(tmpTable);
					}
				}
			}
		}
		catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		finally {
			cursor.close();
		}
	}
	
	
	private void select(Database db) throws QueryException {
		if(boolExp != null) { //'where' clause exists
			if(table.getRecords().size() == 0) {
				evaluateBoolExp(null, boolExp, db);
			}
			for(Record record : table.getRecords()) {
				if(evaluateBoolExp(record, boolExp, db) == Value.ThreeValue.TRUE) {
					selectedRecords.add(record);
				}
			}
		}
		else { //'where' clause doesn't exist
			selectedRecords = table.getRecords();
		}
		
		//print the selected columns from the selected records
		int gap = 0, maxWidth = 0;
		HashMap<Column, Integer> maxWidths = new HashMap<Column, Integer>();
		String boundary = "+";
		String title = "|";
		String content = "|";
		String columnName = null;
		Value value = null;
		
		if(selectedTCPairs != null) {			
			//get the max width for each column
			for(TCPair tc : selectedTCPairs) {
				processTCPair(tc, db);
				
				if(tc.getAlias() == null) {
					maxWidth = tc.getColumnName().length();
				}
				else {
					maxWidth = tc.getAlias().length();
				}
				
				for(Record record : selectedRecords) {
					for(Column column : table.getColumns()) {
						if(tc.getColumnName().equals(column.getName())) {
							if(tc.getTableName() == null || tc.getTableName().equals(column.getBelongToTable().getName())) {
								value = record.getValue(column);
								break;
							}
						}
					}
					
					if(value.toString().length() > maxWidth) {
						maxWidth = value.toString().length();
					}
				}
				maxWidths.put(tc.getColumn(), maxWidth);
			}
			
			
			for(TCPair tc : selectedTCPairs) {				
				for(int i = 0; i < maxWidths.get(tc.getColumn()) + 2; i++) {
					boundary += "-";
				}
				boundary += "+";
				
				if(tc.getAlias() == null) {
					gap = maxWidths.get(tc.getColumn()) - tc.getColumnName().length();
					title += " " + tc.getColumnName();
					for(int i = 0; i < gap; i++) {
						title += " ";
					}
				}
				else {
					gap = maxWidths.get(tc.getColumn()) - tc.getAlias().length();
					title += " " + tc.getAlias();
					for(int i = 0; i < gap; i++) {
						title += " ";
					}
				}
				title += " |";
			}
			System.out.println(boundary + "\n" + title + "\n" + boundary);
			
			for(Record record : selectedRecords) {
				content = "|";
				for(TCPair tc : selectedTCPairs) {
					for(Column column : table.getColumns()) {
						if(tc.getColumnName().equals(column.getName())) {
							if(tc.getTableName() == null || tc.getTableName().equals(column.getBelongToTable().getName())) {
								value = record.getValue(column);
								break;
							}
						}
					}
					
					gap = maxWidths.get(tc.getColumn()) - value.toString().length();
					
					content += " " + value.toString();
					for(int i = 0; i < gap; i++) {
						content += " ";
					}
					content += " |";
				}
				System.out.println(content);
			}
			
			System.out.println(boundary);
		}
		
		else {
			//get the max width for each column
			for(Column column : table.getColumns()) {				
				maxWidth = column.getName().length();
				
				for(Record record : selectedRecords) {
					value = record.getValue(column);
					
					if(value.toString().length() > maxWidth) {
						maxWidth = value.toString().length();
					}
				}
				maxWidths.put(column, maxWidth);
			}
			
			
			for(Column column : table.getColumns()) {
				for(int i = 0; i < maxWidths.get(column) + 2; i++) {
					boundary += "-";
				}
				boundary += "+";
				title += " " + column.getName();
				gap = maxWidths.get(column) - column.getName().length();
				for(int i = 0; i < gap; i++) {
					title += " ";
				}
				title += " |";
			}
			System.out.println(boundary + "\n" + title + "\n" + boundary);
			
			for(Record record : selectedRecords) {
				content = "|";

				for(Column column : table.getColumns()) {
					value = record.getValue(column);
					gap = maxWidths.get(column) - value.toString().length();
					content += " " + value.toString();
					for(int i = 0; i < gap; i++) {
						content += " ";
					}
					content += " |";
				}

				System.out.println(content);
			}
			
			System.out.println(boundary);
		}
	}
	
	
	private void processTCPair(TCPair tc, Database db) throws QueryException {
		Cursor cursor = null;
		DatabaseEntry key = null;
		DatabaseEntry data = new DatabaseEntry();
		
		String tableName = tc.getTableName();
		String columnName = tc.getColumnName();
		boolean fromTableExists = false;
		Table t = null;
		
		try {
			cursor = db.openCursor(null, null);
			
			//when tableName is specified
			if(tableName != null) {
				//get the proper table
				for(String[] fromTableNames : fromTablesName) {
					if(fromTableNames[1] != null) { //alias exists
						if(tableName.equals(fromTableNames[1])) {
							key = new DatabaseEntry(fromTableNames[0].getBytes("UTF-8"));
							tc.setTableName(fromTableNames[0]);
							fromTableExists = true;
							break;
						}
					}
					else {
						if(tableName.equals(fromTableNames[0])) {
							key = new DatabaseEntry(tableName.getBytes("UTF-8"));
							fromTableExists = true;
							break;
						}
					}
				}
				
				if(fromTableExists) {
					OperationStatus searchTable = cursor.getSearchKey(key, data, LockMode.DEFAULT);
					t = deserialize(data);
					tc.setTable(t);
				}
				else { //the table name doesn't exist
					throw new QueryException(Messages.SELECT_COLUMN_RESOLVE_ERROR, tableName + "." + columnName);
				}
				
				//get the proper column
				boolean columnNameExists = false;
				for(Column column : t.getColumns()) {
					if(columnName.equals(column.getName())) {
						tc.setColumn(column);
						columnNameExists = true;
					}
				}
				if(!columnNameExists) { //the column name doesn't exist
					throw new QueryException(Messages.SELECT_COLUMN_RESOLVE_ERROR, tableName + "." + columnName);
				}
			}
			
			//when tableName is not specified
			else {
				boolean columnNameExists = false;
				for(Column column : table.getColumns()) {
					if(columnName.equals(column.getName())) {
						if(columnNameExists) { //the column name is ambiguous
							throw new QueryException(Messages.SELECT_COLUMN_RESOLVE_ERROR, columnName);
						}
						else{
							tc.setColumn(column);
							columnNameExists = true;
						}
					}
				}
				if(!columnNameExists) { //the column name doesn't exist
					throw new QueryException(Messages.SELECT_COLUMN_RESOLVE_ERROR, columnName);
				}
			}
		}
		catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		finally {
			cursor.close();
		}
	}

}
