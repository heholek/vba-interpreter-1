package org.siphon.visualbasic.interpreter.opcodes;

import org.siphon.visualbasic.interpreter.Interpreter;

public interface Opcode {
	abstract public Integer apply(Interpreter interpreter);
	
	abstract public String toString();
}
