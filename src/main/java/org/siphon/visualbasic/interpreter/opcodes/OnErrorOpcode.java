package org.siphon.visualbasic.interpreter.opcodes;

import org.siphon.visualbasic.interpreter.Interpreter;
import org.siphon.visualbasic.interpreter.OnErrorHandler;

public class OnErrorOpcode implements Opcode {
	protected boolean resumeNext = false;
	protected String label = null;
	
	public OnErrorOpcode(String label) {
		this.label = label;
	}
	
	public OnErrorOpcode(boolean resumeNext) {
		this.resumeNext = resumeNext;
	}

	@Override
	public Integer apply(Interpreter interpreter) {
		if (this.resumeNext) {
			interpreter.getCurrentScope().setErrorHandler(OnErrorHandler.RESUME_NEXT_ERROR_HANDLER);
		}
		else if (this.label != null) {
			interpreter.getCurrentScope().setErrorHandler(new OnErrorHandler.GotoErrorHandler(this.label));
		}
		else {
			interpreter.getCurrentScope().setErrorHandler(OnErrorHandler.NO_ERROR_HANDLER);
		}
		return null;
	}

	@Override
	public String toString() {
		String action = this.resumeNext ? "RESUME NEXT" : "GOTO " + this.label;
		return "ON_ERROR " + action;
	}

}
