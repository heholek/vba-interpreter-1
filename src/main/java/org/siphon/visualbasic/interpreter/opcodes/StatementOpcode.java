package org.siphon.visualbasic.interpreter.opcodes;

import org.siphon.visualbasic.SourceLocation;
import org.siphon.visualbasic.interpreter.Interpreter;

/**
 * This opcode signals the beginning of a new statement (new line).
 * Used to set debug information (current line) in the interpreter.
 * @author jzaddach
 *
 */
public class StatementOpcode implements Opcode {
	protected SourceLocation location;
	
	public StatementOpcode(SourceLocation location) {
		this.location = location;
	}

	@Override
	public Integer apply(Interpreter interpreter) {
		interpreter.setCurrentLocation(this.location);
		interpreter.getCurrentScope().setStackDepthAtStatementStart(interpreter.getStack().size());
		return null;
	}

	@Override
	public String toString() {
		return "STATEMENT " + this.location.getSourceFile() + ":" + this.location.getLine();
	}

}
