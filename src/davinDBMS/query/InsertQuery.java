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
			
			//update the content of referred tables in DB
			for(Column column : table.getColumns()) { //for each column of this table
				Table referToTable = column.getReferToTable();
				if(referToTable != null) { //if there is a table referred by this column	
					updateTable(db, referToTable); //update the content of referred table in DB
				}
			}
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
								
		if(columnsName != null) {
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
			
			//put null value into unspecified columns
			for(Column column : table.getColumns()) {
				if(!columnsName.contains(column.getName())) {
					if(column.canBeNull()) {
						if(record.getValue(column) == null) {
							record.putValue(column, new Value("null"));
						}
					}
					else {
						throw new QueryException(Messages.INSERT_COLUMN_NON_NULLABLE_ERROR, column.getName());
					}
				}
			}
		}
		//when there are no specified the names of columns
		else {
			//if the size of the value list is not equal to the size of actual columns
			if(values.size() != table.getColumns().size()) {
				throw new QueryException(Messages.INSERT_TYPE_MISMATCH_ERROR);
			}
			
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
		
		//primary key check
		if(table.getPrimaryKeys().size() > 0) {
			boolean isSame = false;
			
			for(Record rec : table.getRecords()) {
				isSame = true;
				for(Column priCol : table.getPrimaryKeys()) {
					Value recordValue = rec.getValue(priCol);
					if(!record.getValue(priCol).equals(recordValue)) {
						isSame = false;
						break;
					}
				}
				if(isSame) {
					throw new QueryException(Messages.INSERT_DUPLICATE_PRIMARY_KEY_ERROR);
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
				return;
			}
			else { //if null value is inserted to the non nullable column
				throw new QueryException(Messages.INSERT_COLUMN_NON_NULLABLE_ERROR, column.getName());
			}
		}
		else if(column.getType() == Column.DataType.CHAR && column.getCharLength() < value.getStrVal().length()) {
			value.setVal(value.getStrVal().substring(0, column.getCharLength()));
		}
		
		//foreign key check
		Value recordValue = null;
		boolean referToColumnHasVal = false;
		if(column.isForeignKey()) {
			for(Record rec : column.getReferToTable().getRecords()) {
				recordValue = rec.getValue(column.getReferToColumn());
				if(value.equals(recordValue)) {
					referToColumnHasVal = true;
					break;
				}
			}
			if(!referToColumnHasVal) {
				throw new QueryException(Messages.INSERT_REFERENTIAL_INTEGRITY_ERROR);
			}
		}
	}
}
