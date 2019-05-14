package davinDBMS.query;

import davinDBMS.entity.*;

public class BooleanFactor {
	public enum PredicateType{
		  COMP, NULL
	}
	public enum ThreeValue{
		TRUE, FALSE, UNKNOWN
	}
	private boolean not = false;
	private PredicateType predicateType;
	private String op = null;
	private CompValue[] operands = null;
	public ThreeValue val;
	
	
	public void setNot() {
		not = true;
	}
	
	public boolean isNot() {
		return not;
	}
	
	public void setType(PredicateType type) {
		predicateType = type;
	}
	
	public PredicateType getType() {
		return predicateType;
	}
	
	public void setOperator(String op) {
		this.op = op;
	}
	
	public String getOperator() {
		return op;
	}
	
	public void setFirstOperand(CompValue cValue) {
		operands[0] = cValue;
	}
	
	public void setSecondOperand(CompValue cValue) {
		operands[1] = cValue;
	}
	
	public CompValue[] getOperands() {
		return operands;
	}
}
