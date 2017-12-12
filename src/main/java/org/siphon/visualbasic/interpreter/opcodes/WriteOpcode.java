package org.siphon.visualbasic.interpreter.opcodes;

import java.util.ArrayList;
import java.util.List;

import org.siphon.visualbasic.PropertyDecl;
import org.siphon.visualbasic.interpreter.Argument;
import org.siphon.visualbasic.interpreter.Interpreter;
import org.siphon.visualbasic.interpreter.Variable;
import org.siphon.visualbasic.runtime.Value;
import org.siphon.visualbasic.runtime.VbValue;
import org.siphon.visualbasic.runtime.VbVariable;

public class WriteOpcode implements Opcode {

	private WriteOpcode() {
	}

	@Override
	public Integer apply(Interpreter interpreter) {
		Value var = interpreter.pop();
		
		if (var instanceof VbVariable) {
			Value val = interpreter.pop().get(interpreter);
			if (val instanceof VbValue) {
				((VbVariable) var).value = ((VbValue) val);
			}
			else {
				throw new UnsupportedOperationException("Trying to write something that is not a VbValue");
			}
		}
		else if (var instanceof PropertyDecl) {
			PropertyDecl property = (PropertyDecl) var;
			if (property.let != null) {
				List<Argument> args = new ArrayList<>();
				args.add(Argument.UNNAMED_ARGUMENT);
				property.let.invoke(interpreter, args);
			}
		}
		else {
			throw new UnsupportedOperationException("Trying to write to something that is not a variable");
		}
		
		return null;
	}

	@Override
	public String toString() {
		return "WRITE";
	}

	public static final Opcode WRITE = new WriteOpcode();
}
