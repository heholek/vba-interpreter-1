package org.siphon.visualbasic.interpreter;

import java.util.HashMap;
import java.util.Map;

import org.siphon.visualbasic.runtime.Value;
import org.siphon.visualbasic.runtime.VbValue;

public class ModuleInstance extends Value {
	Map<String, Value> members = new HashMap<>();
	
	public void addMember(String name, Value member) {
		this.members.put(name.toUpperCase(), member);
	}
	
	public Value getMember(String name) {
		return this.members.get(name.toUpperCase());
	}
	
	public boolean hasMember(String name) {
		return this.members.containsKey(name.toUpperCase());
	}

	@Override
	public VbValue get(Interpreter interpreter) {
		throw new UnsupportedOperationException("Can't get the value of a module instance");
	}

	@Override
	public void set(Interpreter interpreter, VbValue value) {
		throw new UnsupportedOperationException("Can't assign to a module instance");		
	}
}
