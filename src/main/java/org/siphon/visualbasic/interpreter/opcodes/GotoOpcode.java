package org.siphon.visualbasic.interpreter.opcodes;

import org.siphon.visualbasic.interpreter.Interpreter;

public class GotoOpcode implements Opcode {
	int index;
	
	public GotoOpcode(int index) {
		this.index = index;
	}
	
	public GotoOpcode() {
		this(-1);
	}
	
	@Override
	public Integer apply(Interpreter interpreter) {
		throw new UnsupportedOperationException("Not implemented");
		
	}

	@Override
	public String toString() {
		return "GOTO " + (this.index == -1 ? "<undefined>" : String.valueOf(this.index));
	}

}
