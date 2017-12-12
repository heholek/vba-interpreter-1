package org.siphon.visualbasic.runtime;

import org.siphon.visualbasic.OverflowException;

public class VbInteger extends VbIntegral {

	public static final VbInteger ZERO = new VbInteger(0);
	public static final short MIN_VALUE = Short.MIN_VALUE;
	public static final short MAX_VALUE = Short.MAX_VALUE;
	
	public VbInteger(short value) {
		super(VbVarType.VbInteger, Integer.valueOf(value));
	}
	
	public VbInteger(int value) {
		this((short) value);
		
		if (value > MAX_VALUE || value < MIN_VALUE) {
			throw new OverflowException();
		}
	}
	
	public static boolean canHold(long value) {
		return value >= MIN_VALUE && value <= MAX_VALUE;
	}

}
