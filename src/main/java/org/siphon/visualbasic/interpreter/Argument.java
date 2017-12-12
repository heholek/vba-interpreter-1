package org.siphon.visualbasic.interpreter;

public class Argument {
	boolean namedArgument;
	String name;
	
	public Argument(String name) {
		namedArgument = true;
		this.name = name;
	}
	
	private Argument() {
		namedArgument = false;
	}
	
	public boolean isNamedArgument() {
		return namedArgument;
	}
	
	public String getName() {
		return name;
	}

	public static final Argument UNNAMED_ARGUMENT = new Argument();
}
