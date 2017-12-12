package org.siphon.visualbasic.interpreter.types;

public class StringType extends Type {
	private StringType() {
	}
	
	@Override
	public String getName() {
		return "String";
	}

	public static final StringType STRING = new StringType();
}
