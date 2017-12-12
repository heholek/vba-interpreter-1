package org.siphon.visualbasic.interpreter.opcodes;

import org.siphon.visualbasic.interpreter.Interpreter;

public class GotoLabelOpcode implements Opcode {

	protected String label = null;
	
	public GotoLabelOpcode(String label) {
		this.label = label;
	}
	
	public GotoLabelOpcode() {
		this(null);
	}
	
	public void setLabel(String label) {
		this.label = label;
	}
	
	@Override
	public Integer apply(Interpreter interpreter) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();

	}

	@Override
	public String toString() {
		return "GOTO " + (this.label == null ? "<undefined>" : this.label);
	}
}
