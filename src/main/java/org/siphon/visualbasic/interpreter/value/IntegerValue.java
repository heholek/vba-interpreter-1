package org.siphon.visualbasic.interpreter.value;

import org.siphon.visualbasic.interpreter.types.IntegralType;
import org.siphon.visualbasic.interpreter.types.Type;

public class IntegerValue extends IntegralValue {
	protected short value;
	
	public IntegerValue(short value) {
		this.value = value;
	}
	
	public long asJavaLong() {
		return this.value;
	}
	
	public String toString() {
		return String.valueOf(this.value);
	}

	@Override
	public Type getType() {
		return IntegralType.INTEGER;
	}
}
