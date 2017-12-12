package org.siphon.visualbasic.interpreter.opcodes;

import org.siphon.visualbasic.interpreter.Interpreter;

public class PushWith implements Opcode {

	private PushWith() {
	}

	@Override
	public Integer apply(Interpreter interpreter) {
		interpreter.push(interpreter.getCurrentScope().getWith());
		return null;
	}

	@Override
	public String toString() {
		return "PUSH <WITH>";
	}
	
	public static final Opcode PUSH_WITH = new PushWith();

}
