package org.siphon.visualbasic.interpreter.types;

public class ProcedureType extends Type {

	private ProcedureType() {
		
	}
	
	@Override
	public String getName() {
		return "Procedure";
	}

	public static final ProcedureType PROCEDURE = new ProcedureType();
}
