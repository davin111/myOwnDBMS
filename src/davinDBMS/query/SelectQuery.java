package davinDBMS.query;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

import davinDBMS.entity.*;

public class SelectQuery extends DMLQuery {
	private ArrayList<String[]> fromTablesName;
	private ArrayList<Table> fromTables = new ArrayList<Table>();
	private ArrayList<ArrayList<BooleanFactor>> boolExp = null;
	
	
	public void setFromTables(ArrayList<String[]> names) {
		fromTablesName = names;
	}
	
	public void setBoolExp(ArrayList<ArrayList<BooleanFactor>> boolExp) {
		this.boolExp = boolExp;
	}
	

	public void apply(Database db) {
		try {
			check(db);
			evaluateBoolExp(db);
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
			for(String[] tableName : fromTablesName) {
				key = new DatabaseEntry(tableName[0].getBytes("UTF-8"));
				
				OperationStatus searchTable = cursor.getSearchKey(key, data, LockMode.DEFAULT);
				
				if (searchTable == OperationStatus.NOTFOUND) { //if the table does not exist
					throw new QueryException(Messages.SELECT_TABLE_EXISTENCE_ERROR);
				}
				else {
					tmpTable = deserialize(data);
					fromTables.add(tmpTable);
					if(table == null) {
						table = tmpTable;
					}
					else {
						table = table.product(tmpTable);
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
	
	
	private void evaluateBoolExp(Database db) {
		Cursor cursor = null;
		DatabaseEntry key = null;
		DatabaseEntry data = new DatabaseEntry();
		
		CompValue[] opers = null;
		String op = null;
		
		try {
			for(ArrayList<BooleanFactor> boolTerm : boolExp) {
				
				for(BooleanFactor boolFact : boolTerm) {
					//evaluate BooleanFactor
					opers = boolFact.getOperands();
					op = boolFact.getOperator();
					
					for(CompValue oper : opers) {
						//TCPair - get the proper column
						if(oper instanceof TCPair) {
							TCPair tcOper = (TCPair) oper;
							String tableName = tcOper.getTableName();
							String columnName = tcOper.getColumnName();
							boolean fromTableExists = false;
							Table t = null;
							
							//when tableName is specified
							if(tableName != null) {
								for(String[] fromTableNames : fromTablesName) {
									if(fromTableNames[1] != null) { //alias exists
										if(tableName.equals(fromTableNames[1])) {
											key = new DatabaseEntry(fromTableNames[0].getBytes("UTF-8"));
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
									tcOper.setTable(t);
								}
								else {
									throw new QueryException(Messages.WHERE_TABLE_NOT_SPECIFIED);
								}
							}
							//when tableName is not specified
							else {
								
							}
						}
						else if(oper instanceof Value){ //Value
							oper = (Value) oper;
						}
						//or null
					}
					
					if(boolFact.getType() == BooleanFactor.PredicateType.COMP) { //comparison_predicate
						
						
						
					}
					else { //null_predicate
						
					}
					
				}
			}
		}
	}

}
