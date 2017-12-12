package org.siphon.visualbasic.interpreter.opcodes;

import org.siphon.visualbasic.interpreter.Interpreter;

public class DropOpcode implements Opcode {
	
	private DropOpcode() {
		
	}

	@Override
	public Integer apply(Interpreter interpreter) {
		interpreter.pop();
		return null;
	}
	
	@Override
	public String toString() {
		return "DROP";
	}

	public static final Opcode DROP = new DropOpcode();
}
