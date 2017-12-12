package org.siphon.visualbasic.runtime;

import org.siphon.visualbasic.interpreter.Interpreter;

public abstract class Value {
	abstract public VbValue get(Interpreter interpreter);
	abstract public void set(Interpreter interpreter, VbValue value);
}
