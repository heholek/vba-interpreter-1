package org.siphon.visualbasic.interpreter.opcodes;

import org.siphon.visualbasic.interpreter.Interpreter;
import org.siphon.visualbasic.runtime.LogicalExpr;
import org.siphon.visualbasic.runtime.Value;
import org.siphon.visualbasic.runtime.VbValue;

public class UnaryOperation implements Opcode {
	public static enum UnaryOperations {
		NOT,
	}
	
	protected UnaryOperations op;
	
	public UnaryOperation(UnaryOperations op) {
		this.op = op;
	}
	
	@Override
	public Integer apply(Interpreter interpreter) {
		Value value = interpreter.pop();
		
		if (!(value instanceof VbValue)) {
			throw new UnsupportedOperationException("Need a VbValue");
		}
		
		VbValue val = (VbValue) value;
		
		Value r;
		
		switch (this.op) {
		case NOT:
			r = LogicalExpr.not(val);
			break;
		default:
			throw new UnsupportedOperationException("Don't know this unary expression");
		}
		
		interpreter.push(r);
		return null;
	}
	
	@Override
	public String toString() {
		return this.op.name();
	}
	
	public static final UnaryOperation NOT = new UnaryOperation(UnaryOperations.NOT);

}
