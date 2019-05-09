package davinDBMS.query;

import java.util.ArrayList;

import davinDBMS.entity.*;

public abstract class DMLQuery extends Query {
	//Record record;
	ArrayList<Value> values;
	
	public void setValues(ArrayList<Value> values) {
		this.values = values;
	}
}
