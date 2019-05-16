package davinDBMS.query;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

import davinDBMS.entity.*;

public class DeleteQuery extends DMLQuery {
	private ArrayList<Record> deleteRecords = new ArrayList<Record>();

	public void apply(Database db) {
		try {
			check(db);
			delete(db);
			
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
				table = deserialize(data); //read the table
				//set fromTables and fromTablesName (for 'evaluateBoolExp' and 'processTCPairOperands' methods)
				fromTables.add(table);
				String[] tArr = new String[2];
				tArr[0] = table.getName();
				fromTablesName.add(tArr);
			}
		}
		catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		finally {
			cursor.close();
		}
	}
	
	
	private void delete(Database db) throws QueryException {
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
		
		
		//check referential integrity and delete the records
		int cntDelete = selectedRecords.size();
		boolean cancelDelete = false;
		int cntCancelDelete = 0;
		Column referredByColumn = null;
		
		for(Record record : selectedRecords) { //record by record
			for(Column column : table.getColumns()) { //for each column of this table
				cancelDelete = false;
				
				//find the column referring to this column
				if(column.getReferredByTables().size() > 0) {
					for(Table referredByTable : column.getReferredByTables()) {
						referredByColumn = null;
						for(Column col : referredByTable.getColumns()) {
							if(col.getReferToColumn() != null && col.getReferToColumn().getName().equals(column.getName())
									&& col.getReferToTable().getName().equals(table.getName())) {
								referredByColumn = col;

								//at least one of the column referring to this column is not nullable
								if(!referredByColumn.canBeNull()) {
									for(Record rec : referredByTable.getRecords()) {
										if(rec.getValue(referredByColumn).equals(record.getValue(column))) {
											cancelDelete = true;
											break;
										}
									}
									if(cancelDelete) {
										break;
									}
								}
							}
						}
						if(cancelDelete) {
							break;
						}
					}
					if(cancelDelete) {
						break;
					}
				}
			}
			
			if(cancelDelete) {
				cntCancelDelete++;
				continue;
			}
			else {
				//replace the value of the column referring to this column as null value
				for(Column column : table.getColumns()) {
					if(column.getReferredByTables().size() > 0) {
						for(Table referredByTable : column.getReferredByTables()) {
							referredByColumn = null;
							for(Column col : referredByTable.getColumns()) {
								if(col.getReferToColumn() != null && col.getReferToColumn().getName().equals(column.getName())
										&& col.getReferToTable().getName().equals(table.getName())) {
									referredByColumn = col;
									
									for(Record rec : referredByTable.getRecords()) {
										//if the value of the column referring to this column
										if(rec.getValue(referredByColumn).equals(record.getValue(column))) {
											rec.putValue(referredByColumn, new Value("null"));
										}
									}
								}
							}
							updateTable(db, referredByTable);
							
							//update the content of tables which are referred by 'referredByTable' in DB
							for(Column col : referredByTable.getColumns()) { //for each column of 'referredByTable' table
								Table referToTable = col.getReferToTable();
								if(referToTable != null) { //if there is a table referred by this column	
									updateTable(db, referToTable); //update the content of referred table in DB
								}
							}
						}
					}
				}
				
				//delete the record from this table
				deleteRecords.add(record);
			}
		}
		
		table.removeRecords(deleteRecords);
		updateTable(db, table);
		
		System.out.printf(Messages.DELETE_RESULT.getMessage(), cntDelete-cntCancelDelete);
		System.out.println();
		if(cntCancelDelete > 0) {
			System.out.printf(Messages.DELETE_REFERENTIAL_INTEGRITY_ERROR.getMessage(), cntCancelDelete);
			System.out.println();
		}
	}

}
