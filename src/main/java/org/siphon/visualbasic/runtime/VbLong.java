package org.siphon.visualbasic.runtime;

import org.siphon.visualbasic.OverflowException;

public class VbLong extends VbIntegral {
	public static final VbLong ZERO = new VbLong(0);
	
	public VbLong(int value) {
		super(VbVarType.VbLong, value);
		// TODO Auto-generated constructor stub
	}
	
	public VbLong(long value) {
		this((int) value);
		if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
			throw new OverflowException();
		}
	}

}
