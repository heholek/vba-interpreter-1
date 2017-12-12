package org.siphon.visualbasic.interpreter;

import org.siphon.visualbasic.interpreter.types.Type;
import org.siphon.visualbasic.interpreter.types.VariantType;
import org.siphon.visualbasic.interpreter.value.Value;

public class Variable extends Value {
	protected Type type;
	protected Value value;
	
	public Variable(Type type) {
		this.type = type;
	}
	
	public Variable(Value value) {
		this.type = value.getType();
		this.value = value;
	}
	
	public void assign(Value value) {
		if (this.type instanceof VariantType) {
			this.value = value;
		}
		else {
			this.value = value.cast(this.type);
		}
	}
	
	public Value get() {
		return this.value;
	}
	
	public Type getType() {
		return this.type;
	}

	@Override
	public String toString() {
		return this.value.toString() + " (" + this.type.toString() + ")";
	}
}
