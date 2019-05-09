package davinDBMS.query;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Iterator;

import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

import davinDBMS.entity.*;

public class InsertQuery extends DMLQuery {
	private ArrayList<String> columnsName = null;
	
	
	public void setColumnsName(ArrayList<String> columnsName) {
		this.columnsName = columnsName;
	}
	
	
	public void apply(Database db) {
		try {
			check(db);
			insert(db);
		}
		catch (QueryException qe) {
			System.out.println(qe);
		}
	}

	public void check(Database db) throws QueryException {
		Cursor cursor = null;
		
		try {
			cursor = db.openCursor(null, null);
			
			DatabaseEntry key = new DatabaseEntry(table.getName().getBytes("UTF-8"));
			DatabaseEntry data = new DatabaseEntry();
			
			OperationStatus searchTable = cursor.getSearchKey(key, data, LockMode.DEFAULT);
			
			if (searchTable == OperationStatus.NOTFOUND) { //if the table does not exist
				throw new QueryException(Messages.NO_SUCH_TABLE);
			}
			else {
				table = deserialize(data);
			}
		}
		catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			}
		finally {
			cursor.close();
		}
	}
	
	
	private void insert(Database db) throws QueryException {
		boolean columnExists = false;
		Value value = null;
		Record record = new Record();
				
		//if the size of the value list is not equal to the size of actual columns
		if(values.size() != table.getColumns().size()) {
			throw new QueryException(Messages.INSERT_TYPE_MISMATCH_ERROR);
		}
				
		if(columnsName != null) {
			//if the size of the column name list is not equal to the size of actual columns
			if(columnsName.size() != table.getColumns().size()) {
				throw new QueryException(Messages.INSERT_TYPE_MISMATCH_ERROR);
			}
					
			Iterator<Value> iter = values.iterator();
			for(String columnName : columnsName) {
				value = iter.next();
				columnExists = false;
						
				//find the column mentioned in the column name list
				for(Column column : table.getColumns()) {
					if(columnName.equals(column.getName())) {
						columnExists = true;
							
						if(column.getType() == value.getType() || value.isNull()) {
							checkColumnWithValue(column, value);
							record.putValue(column, value);
						}
						else { //type mismatch
							throw new QueryException(Messages.INSERT_TYPE_MISMATCH_ERROR);
						}
								
						break;
					}
				}
				if(!columnExists) { //if the column mentioned in the column name list does not exist
					throw new QueryException(Messages.INSERT_COLUMN_EXISTENCE_ERROR, columnName);
				}
			}	
		}
		else {
			Iterator<Value> iter = values.iterator();
			for(Column column : table.getColumns()) {
				value = iter.next();
				if(column.getType() == value.getType() || value.isNull()) {
					checkColumnWithValue(column, value);
					record.putValue(column, value);
				}
				else { //type mismatch
					throw new QueryException(Messages.INSERT_TYPE_MISMATCH_ERROR);
				}
			}
		}
				
		table.addRecord(record);
		updateTable(db, table);
		System.out.printf(Messages.INSERT_RESULT.getMessage());
		System.out.println();
	}
	
	
	private void checkColumnWithValue(Column column, Value value) throws QueryException {
		if(value.isNull()) {
			if(column.canBeNull()) {
				value.setType(column.getType());
			}
			else { //if null value is inserted to the non nullable column
				throw new QueryException(Messages.INSERT_COLUMN_NON_NULLABLE_ERROR, column.getName());
			}
		}
		else if(column.getType() == Column.DataType.CHAR && column.getCharLength() < value.getStrVal().length()) {
			value.setVal(value.getStrVal().substring(0, column.getCharLength()));
		}
		
		//primary key check
		Value recordValue = null;
		if(column.isPrimaryKey()) {
			for(Record rec : table.getRecords()) {
				recordValue = rec.getValue(column);
				if(value.equals(recordValue)) {
					throw new QueryException(Messages.INSERT_DUPLICATE_PRIMARY_KEY_ERROR);
				}
			}
		}
		
		//foreign key check
		boolean referToColumnHasVal = false;
		if(column.isForeignKey()) {
			for(Record rec : column.getReferToTable().getRecords()) {
				recordValue = rec.getValue(column.getReferToColumn());
				if(value.equals(recordValue)) {
					referToColumnHasVal = true;
				}
			}
			if(!referToColumnHasVal) {
				throw new QueryException(Messages.INSERT_REFERENTIAL_INTEGRITY_ERROR);
			}
		}
	}
}
