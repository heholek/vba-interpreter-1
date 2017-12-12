package org.siphon.visualbasic.interpreter.value;

import org.siphon.visualbasic.interpreter.types.Type;

public abstract class IntegralValue extends NumericValue {
	/**
	 * Get this value as a Java long.
	 * @return
	 */
	abstract public long asJavaLong();
	
	/**
	 * Get the string representation of this value
	 */
	abstract public String toString();
	
	/**
	 * Get the bit width of this type.
	 */
	abstract public Type getType();
}
