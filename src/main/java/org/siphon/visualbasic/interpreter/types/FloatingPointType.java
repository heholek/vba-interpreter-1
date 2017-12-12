package org.siphon.visualbasic.interpreter.types;

public class FloatingPointType extends NumericType {
	protected String name;
	protected int bits;
	
	protected FloatingPointType(String name, int bits) {
		this.name = name;
		this.bits = bits;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	public int getNumBits() {
		return bits;
	}
	
	public boolean equals(FloatingPointType other) {
		return this.bits == other.bits;
	}

	public static final FloatingPointType SINGLE = new FloatingPointType("Single", 32);
	public static final FloatingPointType DOUBLE = new FloatingPointType("Double", 64);
}
