package org.siphon.visualbasic.interpreter.value;

import org.siphon.visualbasic.interpreter.types.IntegralType;

public class LongLongValue extends IntegralValue {
	protected long value;
	
	public LongLongValue(long value) {
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
		return IntegralType.LONGLONG;
	}
}
