package org.siphon.visualbasic.interpreter.opcodes;

import org.siphon.visualbasic.interpreter.Interpreter;

public class EndWithOpcode implements Opcode {

	private EndWithOpcode() {
	}

	@Override
	public Integer apply(Interpreter interpreter) {
		interpreter.dropWith();
		return null;
	}
	
	public static final Opcode END_WITH = new EndWithOpcode();
}
