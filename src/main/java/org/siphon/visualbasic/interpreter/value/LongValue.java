package org.siphon.visualbasic.interpreter.value;

import org.siphon.visualbasic.interpreter.types.IntegralType;

public class LongValue extends IntegralValue {
	protected int value;
	
	public LongValue(int value) {
		this.value = value;
	}
	
	public long asJavaLong() {
		return this.value;
	}
	
	public String toString() {
		return String.valueOf(this.value);
	}

	@Override
	public IntegralType getType() {
		return IntegralType.LONG;
	}
}
