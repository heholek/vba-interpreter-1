package org.siphon.visualbasic.interpreter.opcodes;

import org.siphon.visualbasic.interpreter.Interpreter;
import org.siphon.visualbasic.interpreter.value.Value;
import org.siphon.visualbasic.runtime.VbValue;

public class PushOpcode implements Opcode {
	protected VbValue value;
	
	public PushOpcode(VbValue value) {
		this.value = value;
	}

	@Override
	public Integer apply(Interpreter interpreter) {
		interpreter.push(value);
		return null;
	}

	@Override
	public String toString() {
		return "PUSH " + value.toString();
	}

}
