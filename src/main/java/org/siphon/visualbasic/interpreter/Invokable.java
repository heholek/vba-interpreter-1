package org.siphon.visualbasic.interpreter;

import java.util.List;

import org.siphon.visualbasic.runtime.ArgumentDecl;
import org.siphon.visualbasic.runtime.Value;

public interface Invokable {
	public static enum InvokableType {
		SUB,
		FUNCTION,
		PROPERTY_GET,
		PROPERTY_SET,
		PROPERTY_LET,
		RULE,
	}
	
	public void invoke(Interpreter interpreter, List<Argument> arguments);
	
	public Value getStaticVariable(String name);
	
	public InvokableType getType();
	
	public String getName();
	
	public List<ArgumentDecl> getArguments();
}
