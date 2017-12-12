package org.siphon.visualbasic.interpreter.types;

public class IntegralType extends NumericType {
	protected long min;
	protected long max;
	protected int bits;
	protected String name;
	
	public IntegralType(String name, int bits, long min, long max) {
		this.name = name;
		this.bits = bits;
		this.min = min;
		this.max = max;
	}
	
	public long getMin() {return this.min;}
	public long getMax() {return this.max;}
	public int getNumBits() {return this.bits;}
	public String getName() {return this.name;}
	
	public boolean equals(IntegralType other) {
		return this.bits == other.bits;
	}
	
	public static final IntegralType INTEGER = new IntegralType("Integer", 16, Short.MIN_VALUE, Short.MAX_VALUE);
	public static final IntegralType LONG = new IntegralType("Long", 32, Integer.MIN_VALUE, Integer.MAX_VALUE);
	public static final IntegralType LONGLONG = new IntegralType("LongLong", 64, Long.MIN_VALUE, Long.MAX_VALUE);
}
