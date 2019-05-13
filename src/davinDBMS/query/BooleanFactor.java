package davinDBMS.query;

import davinDBMS.entity.*;

public class BooleanFactor {
	public enum PredicateType{
		  COMP, NULL
	}
	private boolean not = false;
	private PredicateType predicateType;
	private String op;
	private CompValue firstOper, secondOper;
	
	
	public void setNot() {
		not = true;
	}
	
	public void setType(PredicateType type) {
		predicateType = type;
	}
	
	public void setOperator(String op) {
		this.op = op;
	}
	
	public void setFirstOperand(CompValue cValue) {
		firstOper = cValue;
	}
	
	public void setSecondOperand(CompValue cValue) {
		secondOper = cValue;
	}
}
