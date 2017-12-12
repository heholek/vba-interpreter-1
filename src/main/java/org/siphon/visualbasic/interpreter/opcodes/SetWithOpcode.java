package org.siphon.visualbasic.interpreter.opcodes;

import org.siphon.visualbasic.interpreter.Interpreter;
import org.siphon.visualbasic.runtime.Value;

public class SetWithOpcode implements Opcode {

	private SetWithOpcode() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public Integer apply(Interpreter interpreter) {
		Value value = interpreter.pop();
		interpreter.getCurrentScope().pushWith(value.get(interpreter));
		return null;
	}

	@Override
	public String toString() {
		return "SET_WITH";
	}
	
	public static final Opcode SET_WITH = new SetWithOpcode();
}
