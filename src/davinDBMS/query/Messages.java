package davinDBMS.query;

public enum Messages { //error and success messages used in this program
  SYNTAX_ERROR("Syntax error"),
  
  CREATE_TABLE_SUCCESS("'%s' table is created"),
  DUPLICATE_COLUMN_DEF_ERROR("Create table has failed: column definition is duplicated"),
  DUPLICATE_PRIMARY_KEY_DEF_ERROR("Create table has failed: primary key definition is duplicated"),
  REFERENCE_TYPE_ERROR("Create table has failed: foreign key references wrong type"),
  REFERENCE_NON_PRIMARY_KEY_ERROR("Create table has failed: foreign key references non primary key column"),
  REFERENCE_COLUMN_EXISTENCE_ERROR("Create table has failed: foreign key references non existing column"),
  REFERENCE_TABLE_EXISTENCE_ERROR("Create table has failed: foreign key references non existing table"),
  NON_EXISTING_COLUMN_DEF_ERROR("Create table has failed: '%s' does not exists in column definition"),
  TABLE_EXISTENCE_ERROR("Create table has failed: table with the same name already exists"),
  
  DROP_SUCCESS("'%s' table is dropped"),
  DROP_REFERENCED_TABLE_ERROR("Drop table has failed: '%s' is referenced by other table"),
  
  SHOW_TABLES_NO_TABLE("There is no table"),
  
  NO_SUCH_TABLE("No such table"),
  
  CHAR_LENGTH_ERROR("Char length should be over 0"),
	
  INSERT_RESULT("The row is inserted"),
  INSERT_DUPLICATE_PRIMARY_KEY_ERROR("Insertion has failed: Primary key duplication"),
  INSERT_REFERENTIAL_INTEGRITY_ERROR("Insertion has failed: Referential integrity violation"),
  INSERT_TYPE_MISMATCH_ERROR("Insertion has failed: Types are not matched"),
  INSERT_COLUMN_EXISTENCE_ERROR("Insertion has failed: '%s' does not exist"),
  INSERT_COLUMN_NON_NULLABLE_ERROR("Insertion has failed: '%s' is not nullable"),
  
  DELETE_RESULT("%d row(s) are deleted"),
  DELETE_REFERENTIAL_INTEGRITY_ERROR("%d row(s) are not deleted due to referential integrity"),
  
  SELECT_TABLE_EXISTENCE_ERROR("Selection has failed: '%s' does not exist"),
  SELECT_COLUMN_RESOLVE_ERROR("Selection has failed: fail to resolve '%s'"),
  
  WHERE_INCOMPARABLE_ERROR("Where clause try to compare incomparable values"),
  WHERE_TABLE_NOT_SPECIFIED("Where clause try to reference tables which are not specified"),
  WHERE_COLUMN_NOT_EXIST("Where clause try to reference non existing column"),
  WHERE_AMBIGUOUS_REFERENCE("Where clause contains ambiguous reference");
  
  
  private String msg;
  
  
  //constructor
  Messages(String msg) {
	  this.msg = msg;
  }
  
  
  //basic accessor
  public String getMessage() {
	  return msg;
  }
}
