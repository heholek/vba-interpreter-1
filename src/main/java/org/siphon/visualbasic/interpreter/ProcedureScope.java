package org.siphon.visualbasic.interpreter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.siphon.visualbasic.runtime.Value;
import org.siphon.visualbasic.runtime.VbVariable;

public class ProcedureScope implements Scope {
	protected Invokable procedure;
	protected Scope parent;
	protected Map<String, Value> variables = new HashMap<>();
	protected Stack<Value> curWith = new Stack<>();
	protected OnErrorHandler errorHandler = null;
	protected Value returnValue = null;
	protected int stackDepthAtStatementStart = 0;
	
	public ProcedureScope(Invokable procedure, Scope parent) {
		this.procedure = procedure;
		this.parent = parent;
	}

	@Override
	public Value find(String name) {
		if (variables.containsKey(name.toUpperCase())) {
			return variables.get(name.toUpperCase());
		}
		else if (procedure.getStaticVariable(name) != null) {
			return procedure.getStaticVariable(name);
		}
		else {
			return this.parent.find(name);
		}
	}

	@Override
	public Scope getParent() {
		return this.parent;
	}

	@Override
	public void insert(String name, Value var) {
		this.variables.put(name.toUpperCase(), var);
	}

	@Override
	public void pushWith(Value value) {
		this.curWith.push(value);
	}

	@Override
	public void dropWith() {
		this.curWith.pop();
	}

	@Override
	public Value getWith() {
		if (!this.curWith.empty()) {
			return this.curWith.peek();
		}
		else {
			return this.parent.getWith();
		}
	}

	@Override
	public void setParent(Scope scope) {
		this.parent = scope;
	}

	@Override
	public void setErrorHandler(OnErrorHandler handler) {
		this.errorHandler = handler;	
	}
	
	@Override
	public OnErrorHandler getErrorHandler() {
		return this.errorHandler;
	}

	@Override
	public void setReturnValue(Value value) {
		this.returnValue = value;
	}

	@Override
	public Value getReturnValue() {
		return this.returnValue;
	}
	
	@Override
	public void setStackDepthAtStatementStart(int stackDepth) {
		this.stackDepthAtStatementStart = stackDepth;
	}
	
	@Override
	public int getStackDepthAtStatementStart() {
		return this.stackDepthAtStatementStart;
	}
}
