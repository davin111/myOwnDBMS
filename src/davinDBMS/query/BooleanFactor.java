package davinDBMS.query;

import java.util.ArrayList;

import davinDBMS.entity.*;

public class BooleanFactor {
	public enum PredicateType{
		  COMP, NULL
	}
	
	private boolean not = false;
	private PredicateType predicateType;
	private String op = null;
	private CompValue[] operands = new CompValue[2];
	
	public boolean isPar = false;
	private ArrayList<ArrayList<BooleanFactor>> boolExp;
	
	
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
	
	
	public void setBoolExp(ArrayList<ArrayList<BooleanFactor>> boolExp) {
		isPar = true;
		this.boolExp = boolExp;
	}
	
	public boolean isPar() {
		return isPar;
	}
	
	public ArrayList<ArrayList<BooleanFactor>> getBoolExp(){
		return boolExp;
	}
}
