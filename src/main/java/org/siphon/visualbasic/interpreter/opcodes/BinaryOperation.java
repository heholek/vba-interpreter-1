package org.siphon.visualbasic.interpreter.opcodes;

import java.util.Stack;

import org.siphon.visualbasic.interpreter.Interpreter;
import org.siphon.visualbasic.interpreter.value.StringValue;
import org.siphon.visualbasic.runtime.LogicalExpr;
import org.siphon.visualbasic.runtime.MathExpr;
import org.siphon.visualbasic.runtime.Value;
import org.siphon.visualbasic.runtime.VbString;
import org.siphon.visualbasic.runtime.VbValue;
import org.siphon.visualbasic.runtime.VbVarType.TypeEnum;
import org.siphon.visualbasic.runtime.framework.vba.Math;

public class BinaryOperation implements Opcode {
	public static enum BinaryOperations {
		EXP,
		MUL,
		DIV,
		MOD,
		ADD,
		SUB,
		NEG,
		EQ,
		NE,
		LT,
		GT,
		LE,
		GE,
		IDIV,
		AND,
		OR,
		XOR,
		EQV,
		IMP,
		CAT,
		POW,
		
		LIKE,
		IS,
	}
	
	protected BinaryOperations op;
	
	public BinaryOperation(BinaryOperations op) {
		this.op = op;
	}
	
	public Integer apply(Interpreter interpreter) {
		VbValue a = interpreter.pop().get(interpreter);
		VbValue b = interpreter.pop().get(interpreter);
		VbValue r;
		
		if (a.isVariant()) {
			a = (VbValue) a.value;
		}
		
		if (b.isVariant()) {
			b = (VbValue) b.value;
		}
		
		switch (op) {
		case CAT:
			r = VbValue.fromJava(a.cast(TypeEnum.vbString).toJava().toString() + b.cast(TypeEnum.vbString).toJava().toString());
			break;
		case ADD:
			if (a.isString() || b.isString()) {
				if (a.isNull() || a.isEmpty()) {
					a = new VbString("");
				}
				if (b.isNull() || a.isEmpty()) {
					b = new VbString("");
				}
				r = VbValue.fromJava(a.cast(TypeEnum.vbString).toJava().toString() + b.cast(TypeEnum.vbString).toJava().toString());
			}
			else {
				r = MathExpr.add(a, b);
			}
			break;
		case SUB:
			r = MathExpr.subtract(a, b);
			break;
		case MUL:
			r = MathExpr.multi(a, b);
			break;
		case DIV:
			r = MathExpr.div(a, b);
			break;
		case IDIV:
			r = MathExpr.idiv(a, b);
			break;
		case AND:
			r = LogicalExpr.and(a, b);
			break;
		case OR:
			r = LogicalExpr.or(a,  b);
			break;
		case XOR:
			r = LogicalExpr.xor(a, b);
			break;
		case IMP:
			r = LogicalExpr.imp(a, b);
			break;
		case EQV:
			r = LogicalExpr.eqv(a, b);
			break;
		default:
			throw new UnsupportedOperationException("The opcode is not supported");
		}
		
		interpreter.push(r);
		return null;
	}

	@Override
	public String toString() {
		return op.name();
	}
	
	public static final Opcode AND = new BinaryOperation(BinaryOperations.AND);
	public static final Opcode ADD = new BinaryOperation(BinaryOperations.ADD);
	public static final Opcode SUB = new BinaryOperation(BinaryOperations.SUB);
	public static final Opcode MUL = new BinaryOperation(BinaryOperations.MUL);
	public static final Opcode DIV = new BinaryOperation(BinaryOperations.DIV);
	public static final Opcode IDIV = new BinaryOperation(BinaryOperations.IDIV);
	public static final Opcode MOD = new BinaryOperation(BinaryOperations.MOD);
	public static final Opcode LIKE = new BinaryOperation(BinaryOperations.LIKE);
	public static final Opcode EQ = new BinaryOperation(BinaryOperations.EQ);
	public static final Opcode GE = new BinaryOperation(BinaryOperations.GE);
	public static final Opcode LE = new BinaryOperation(BinaryOperations.LE);
	public static final Opcode GT = new BinaryOperation(BinaryOperations.GT);
	public static final Opcode LT = new BinaryOperation(BinaryOperations.LT);
	public static final Opcode NE = new BinaryOperation(BinaryOperations.NE);
	public static final Opcode IS = new BinaryOperation(BinaryOperations.IS);
	public static final Opcode OR = new BinaryOperation(BinaryOperations.OR);
	public static final Opcode XOR = new BinaryOperation(BinaryOperations.XOR);
	public static final Opcode EQV = new BinaryOperation(BinaryOperations.EQV);
	public static final Opcode IMP = new BinaryOperation(BinaryOperations.IMP);
	public static final Opcode POW = new BinaryOperation(BinaryOperations.POW);
	public static final Opcode CAT = new BinaryOperation(BinaryOperations.CAT);
}
