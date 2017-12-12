package org.siphon.visualbasic.interpreter.value;

import org.siphon.visualbasic.interpreter.types.FloatingPointType;
import org.siphon.visualbasic.interpreter.types.IntegralType;
import org.siphon.visualbasic.interpreter.types.StringType;
import org.siphon.visualbasic.interpreter.types.Type;

public abstract class Value {
	/**
	 * Convert value to Java string.
	 */
	abstract public String toString();
	
	/**
	 * Get value type.
	 */
	abstract public Type getType();
	
	/**
	 * Get the value as Java object.
	 */
//	abstract public Object asJavaObject();
	
	/**
	 * Cast value to another type.
	 */
	public Value cast(Type type) {
		if (type instanceof StringType) {
			return new StringValue(toString());
		}
		else if (type instanceof IntegralType) {
			long v;
			if (getType() instanceof StringType) {
				v = Long.valueOf(toString());
			}
			else if (getType() instanceof FloatingPointType) {
				v = (long) ((FloatingPointValue) this).asJavaDouble();
			}
			else if (getType() instanceof IntegralType) {
				v = ((IntegralValue) this).asJavaLong();
			}
			else {
				throw new UnsupportedOperationException("Cast from this type is not supported");
			}
			
			if (type.equals(IntegralType.LONGLONG)) {
				return new LongLongValue(v);
			}
			else if (type.equals(IntegralType.LONG)) {
				return new LongValue((int) v);
			}
			else {
				return new IntegerValue((short) v);
			}
		}
		else {
			throw new UnsupportedOperationException("Cast to this type is not supported");
		}
	}
	
	public static Value valueOf(Object obj) {
		if (obj instanceof String) {
			return new StringValue((String) obj);
		}
		else if (obj instanceof Short) {
			return new IntegerValue((Short) obj);
		}
		else if (obj instanceof Integer) {
			return new LongValue((Integer) obj);
		}
		else if (obj instanceof Long) {
			return new LongLongValue((Long) obj);
		}
		else if (obj instanceof Double) {
			return new DoubleValue((Double) obj);
		}
		else {
			throw new UnsupportedOperationException("Don't know how to convert this Java value to VBA value");
		}
	}
}
