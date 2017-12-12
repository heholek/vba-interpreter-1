package org.siphon.visualbasic.interpreter.types;

public class VariantType extends Type {

	private VariantType() {
	}

	@Override
	public String getName() {
		return "Variant";
	}
	
	public static final VariantType VARIANT = new VariantType();

}
