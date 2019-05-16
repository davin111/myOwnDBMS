package davinDBMS.query;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

import davinDBMS.entity.*;

public abstract class DMLQuery extends Query {
	ArrayList<Value> values;
	ArrayList<ArrayList<BooleanFactor>> boolExp = null;
	ArrayList<Record> selectedRecords = new ArrayList<Record>();
	ArrayList<String[]> fromTablesName = new ArrayList<String[]>();
	ArrayList<Table> fromTables = new ArrayList<Table>();
	
	
	public void setValues(ArrayList<Value> values) {
		this.values = values;
	}
	
	public void setBoolExp(ArrayList<ArrayList<BooleanFactor>> boolExp) {
		this.boolExp = boolExp;
	}
	
	
	Value.ThreeValue evaluateBoolExp(Record record, ArrayList<ArrayList<BooleanFactor>> boolExp, Database db) throws QueryException {		
		CompValue[] opers = null;
		String op = null;
		Value.ThreeValue boolExpValue = null, boolTermValue = null, boolFactValue = null;
		
		for(ArrayList<BooleanFactor> boolTerm : boolExp) {
			boolTermValue = null;
			for(BooleanFactor boolFact : boolTerm) {
				boolFactValue = null;
				//if parenthesized boolExp
				if(boolFact.isPar()) {
					boolFactValue = evaluateBoolExp(record, boolFact.getBoolExp(), db); //recursive call
				}
				
				//evaluate BooleanFactor
				else {
					opers = boolFact.getOperands();
					op = boolFact.getOperator();
						
					for(CompValue oper : opers) {
						//TCPair - need to get the proper column
						if(oper instanceof TCPair) {
							processTCPairOperands((TCPair)oper, db);
						}
					}
			
					if(record == null) {
						continue;
					}
					//comparison_predicate
					if(boolFact.getType() == BooleanFactor.PredicateType.COMP) {
						Value[] values = new Value[2];
					
						for(int i = 0; i < 2; i++) {
							if(opers[i] instanceof TCPair) {
								TCPair tcOper = (TCPair)opers[i];
								for(Column column : table.getColumns()) {
									if(tcOper.getColumnName().equals(column.getName())) {
										if(tcOper.getTableName() == null || tcOper.getTableName().equals(column.getBelongToTable().getName())) {
											values[i] = record.getValue(column);
											break;
										}
									}
								}
							}
							else if(opers[i] instanceof Value){
								values[i] = ((Value)opers[i]);
							}
						}
	
						boolFactValue = values[0].compare(values[1], op);
					}
						
					//null_predicate
					else {
						Value value = null;
						TCPair tcOper = (TCPair)opers[0];
						for(Column column : table.getColumns()) {
							if(tcOper.getColumnName().equals(column.getName())) {
								if(tcOper.getTableName() == null || tcOper.getTableName().equals(column.getBelongToTable().getName())) {
									value = record.getValue(column);
									break;
								}
							}
						}
								
						if(op.equals("is null")){
							if(value.isNull()) {
								boolFactValue = Value.ThreeValue.TRUE;
							}
							else {
								boolFactValue = Value.ThreeValue.FALSE;
							}
						}
						else { //"is not null"
							if(!value.isNull()) {
								boolFactValue = Value.ThreeValue.TRUE;
							}
							else {
								boolFactValue = Value.ThreeValue.FALSE;
							}
						}
					}
					
					
					if(boolFact.isNot()) {
						if(boolFactValue == Value.ThreeValue.TRUE) {
							boolFactValue = Value.ThreeValue.FALSE;
						}
						else if(boolFactValue == Value.ThreeValue.FALSE) {
							boolFactValue = Value.ThreeValue.TRUE;
						}
					}
				}
				
				
				if(boolFactValue == Value.ThreeValue.FALSE) {
					boolTermValue = Value.ThreeValue.FALSE;
					break;
				}
				else if(boolFactValue == Value.ThreeValue.UNKNOWN) {
					boolTermValue = Value.ThreeValue.UNKNOWN;
				}
				else { //boolFactValue is TRUE
					if(boolTermValue == null) {
						boolTermValue = Value.ThreeValue.TRUE;
					}
				}
			}
			
			if(boolTermValue == Value.ThreeValue.TRUE) {
				return Value.ThreeValue.TRUE;
			}
			else if(boolTermValue == Value.ThreeValue.UNKNOWN) {
				boolExpValue = Value.ThreeValue.UNKNOWN;
			}
			else { //boolTermValue is FALSE
				if(boolExpValue == null) {
					boolExpValue = Value.ThreeValue.FALSE;
				}
			}
		}
		
		return Value.ThreeValue.FALSE;
	}
	
	
	void processTCPairOperands(TCPair tcOper, Database db) throws QueryException {
		Cursor cursor = null;
		DatabaseEntry key = null;
		DatabaseEntry data = new DatabaseEntry();
		
		String tableName = null;
		if(tcOper.getAlias() == null) {
			tableName = tcOper.getTableName();
		}
		else {
			tableName = tcOper.getAlias();
		}
		String columnName = tcOper.getColumnName();
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
							tcOper.setAlias(tableName);
							tcOper.setTableName(fromTableNames[0]);
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
				
				//get the proper column
				boolean columnNameExists = false;
				for(Column column : t.getColumns()) {
					if(columnName.equals(column.getName())) {
						tcOper.setColumn(column);
						columnNameExists = true;
					}
				}
				if(!columnNameExists) {
					throw new QueryException(Messages.WHERE_COLUMN_NOT_EXIST);
				}
			}
			
			//when tableName is not specified
			else {
				boolean columnNameExists = false;
				for(Column column : table.getColumns()) {
					if(columnName.equals(column.getName())) {
						if(columnNameExists) {
							throw new QueryException(Messages.WHERE_AMBIGUOUS_REFERENCE);
						}
						else{
							tcOper.setColumn(column);
							columnNameExists = true;
						}
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
}
