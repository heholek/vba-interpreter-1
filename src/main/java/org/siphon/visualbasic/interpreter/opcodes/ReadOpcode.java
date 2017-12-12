package org.siphon.visualbasic.interpreter.opcodes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.siphon.visualbasic.interpreter.Argument;
import org.siphon.visualbasic.interpreter.Interpreter;
import org.siphon.visualbasic.interpreter.Invokable;
import org.siphon.visualbasic.interpreter.ModuleInstance;
import org.siphon.visualbasic.interpreter.ProcedureScope;
import org.siphon.visualbasic.interpreter.Invokable.InvokableType;
import org.siphon.visualbasic.runtime.ArgumentDecl;
import org.siphon.visualbasic.runtime.Value;
import org.siphon.visualbasic.runtime.VbValue;
import org.siphon.visualbasic.runtime.VbVarType;
import org.siphon.visualbasic.runtime.VbVariable;

/**
 * Read value of a variable or invoke a procedure.
 * @author jzaddach
 *
 */
public class ReadOpcode implements Opcode {

	protected List<Argument> arguments = null;
	
	private ReadOpcode() {
	}
	
	public ReadOpcode(List<Argument> arguments) {
		this.arguments = arguments;
	}

	@Override
	public Integer apply(Interpreter interpreter) {
		Value value = interpreter.pop();
		
		if (value instanceof Invokable) {
			Invokable invokable = (Invokable) value;
			
			
			interpreter.pushScope(new ProcedureScope(invokable, interpreter.getCurrentScope()));
			if (invokable.getType() == InvokableType.FUNCTION) {
				//Insert return value
				//TODO: Now we can't find the function if it's a recursive function ... :(
				interpreter.getCurrentScope().insert(invokable.getName(), new VbVariable(VbVarType.VbVariant, VbValue.Empty));
			}
			
			try {
				invokable.invoke(interpreter, arguments);
			}
			finally {
				interpreter.dropScope();
			}
		}
		else if (value instanceof VbVariable && arguments == null) {
			interpreter.push(((VbVariable) value).value);
		}
		else {
			if (arguments != null) {
				throw new RuntimeException("Tried to invoke something that is not an invokable where a procedure call was required");
			}
			interpreter.push(value.get(interpreter));
			System.err.println("FIXME: READing unknown class " + value.getClass().getName() + " value");
			//throw new UnsupportedOperationException("Trying to read something that is not a variable or a procedure");
		}
		
		return null;
	}

	@Override
	public String toString() {
		return "READ";
	}

	public static final Opcode READ = new ReadOpcode();
}
