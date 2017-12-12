package org.siphon.visualbasic.interpreter.value;

import org.siphon.visualbasic.interpreter.types.FloatingPointType;
import org.siphon.visualbasic.interpreter.types.Type;

public class DoubleValue extends FloatingPointValue {
	protected double value;
	
	public DoubleValue(double value) {
		this.value = value;
	}
	
	@Override
	public String toString() {
		return String.valueOf(this.value);
	}

	@Override
	public double asJavaDouble() {
		return this.value;
	}

	@Override
	public Type getType() {
		return FloatingPointType.DOUBLE;
	}

}
