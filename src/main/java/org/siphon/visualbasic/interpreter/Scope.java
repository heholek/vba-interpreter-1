package org.siphon.visualbasic.interpreter;

import org.siphon.visualbasic.runtime.Value;

public interface Scope {
	public Value find(String name);
	
	public void insert(String name, Value var);
	
	public void setParent(Scope scope);
	
	public Scope getParent();
	
	public void pushWith(Value value);
	
	public void dropWith();
	
	public Value getWith();
	
	public void setErrorHandler(OnErrorHandler handler);
	
	public OnErrorHandler getErrorHandler();
	
	public void setReturnValue(Value value);
	
	public Value getReturnValue();
	
	public void setStackDepthAtStatementStart(int stackDepth);
	
	public int getStackDepthAtStatementStart();
}
