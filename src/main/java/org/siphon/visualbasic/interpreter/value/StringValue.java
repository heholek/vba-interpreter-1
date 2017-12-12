package org.siphon.visualbasic.interpreter.value;

import org.siphon.visualbasic.interpreter.types.StringType;
import org.siphon.visualbasic.interpreter.types.Type;

public class StringValue extends Value {

	protected String value;
	
	public StringValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return this.value;
	}

	@Override
	public Type getType() {
		return StringType.STRING;
	}

	@Override
	public Value cast(Type type) {
		// TODO Auto-generated method stub
		return null;
	}
}
