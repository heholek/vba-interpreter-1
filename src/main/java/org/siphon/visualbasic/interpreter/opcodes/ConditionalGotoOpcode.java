package org.siphon.visualbasic.interpreter.opcodes;

import org.siphon.visualbasic.interpreter.Interpreter;
import org.siphon.visualbasic.runtime.Value;
import org.siphon.visualbasic.runtime.VbValue;
import org.siphon.visualbasic.runtime.VbVarType;

public class ConditionalGotoOpcode implements Opcode {
	protected int index;
	
	public ConditionalGotoOpcode(int index) {
		this.index = index;
	}
	
	public ConditionalGotoOpcode() {
		this(-1);
	}
	
	public void setTarget(int index) {
		this.index = index;
	}

	@Override
	public Integer apply(Interpreter interpreter) {
		Value value = interpreter.pop();
		
		if (!(value instanceof VbValue)) {
			throw new UnsupportedOperationException("Need a VbValue");
		}
		
		Boolean condition  = (Boolean) ((VbValue) value).cast(VbVarType.TypeEnum.vbBoolean).toJava();

		if (condition) {
			return this.index;
		}
		else {
			return null;
		}
	}

	@Override
	public String toString() {
		return "CONDITIONAL_GOTO " + (this.index < 0 ? "<undefined>" : String.valueOf(this.index));
	}

}
