package org.siphon.visualbasic.interpreter.opcodes;

import org.siphon.visualbasic.interpreter.Interpreter;
import org.siphon.visualbasic.interpreter.types.StringType;
import org.siphon.visualbasic.runtime.Value;
import org.siphon.visualbasic.runtime.VbValue;
import org.siphon.visualbasic.runtime.VbVarType;
import org.siphon.visualbasic.runtime.VbVarType.TypeEnum;
import org.siphon.visualbasic.runtime.VbVariable;

public class FindOpcode implements Opcode {
	public enum ValueType
	{
		LValue,
		RValue,
	};
	
	protected ValueType valueType;
	
	private FindOpcode(ValueType valueType) {
		this.valueType = valueType;
	}

	@Override
	public Integer apply(Interpreter interpreter) {
		Value value = interpreter.pop();
		
		if (!(value instanceof VbValue) || ((VbValue) value).getExactVbType() != TypeEnum.vbString) {
			throw new UnsupportedOperationException("Expected a string type"); 
		}
		
		String name = ((VbValue) value).toJava().toString().toUpperCase();
		
		Value var = interpreter.getCurrentScope().find(name);
		
		if (var == null) {
			var = new VbVariable(VbVarType.VbVariant, VbValue.Empty);
			interpreter.getCurrentScope().insert(name, var);
		}
		
		interpreter.push(var);
		return null;
	}

	@Override
	public String toString() {
		return "FIND " + this.valueType.name();
	}
	
	public static final Opcode FIND_LVALUE = new FindOpcode(ValueType.LValue);
	public static final Opcode FIND_RVALUE = new FindOpcode(ValueType.RValue);

}
