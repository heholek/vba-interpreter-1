package org.siphon.visualbasic.interpreter.opcodes;

import org.siphon.visualbasic.PropertyDecl;
import org.siphon.visualbasic.compile.JavaMethod;
import org.siphon.visualbasic.interpreter.Interpreter;
import org.siphon.visualbasic.interpreter.ModuleInstance;
import org.siphon.visualbasic.runtime.JavaModuleInstance;
import org.siphon.visualbasic.runtime.Value;
import org.siphon.visualbasic.runtime.VbValue;
import org.siphon.visualbasic.runtime.VbVarType.TypeEnum;
import org.siphon.visualbasic.runtime.VbVariable;

public class ElemOpcode implements Opcode {

	private ElemOpcode() {
	}

	@Override
	public Integer apply(Interpreter interpreter) {
		Value nameValue = interpreter.pop();
		Value objValue = interpreter.pop();
		
		if (!(nameValue instanceof VbValue) || !((VbValue) nameValue).isString()) {
			throw new RuntimeException("Name is not a VbValue String");
		}
		
		String name = (String) ((VbValue) nameValue).cast(TypeEnum.vbString).toJava();
		
		if (objValue instanceof VbVariable) {
			objValue = objValue.get(interpreter);
		} 
		
		if (objValue instanceof VbValue && ((VbValue) objValue).isVariant()) {
			objValue = (VbValue) ((VbValue) objValue).value;
		}
		
		if (objValue instanceof VbValue && ((VbValue) objValue).value instanceof org.siphon.visualbasic.runtime.ModuleInstance) {
			objValue = (org.siphon.visualbasic.runtime.ModuleInstance) ((VbValue) objValue).value;
		}
		
		if (objValue instanceof org.siphon.visualbasic.runtime.ModuleInstance) {
			org.siphon.visualbasic.runtime.ModuleInstance moduleInstance = (org.siphon.visualbasic.runtime.ModuleInstance) objValue;
			Value value = (Value) moduleInstance.getMember(name);
			if (value instanceof JavaMethod && moduleInstance instanceof JavaModuleInstance) {
				((JavaMethod) value).object = ((JavaModuleInstance) moduleInstance).getInstance();
			}
			else if (value instanceof PropertyDecl && moduleInstance instanceof JavaModuleInstance) {
				if (((PropertyDecl) value).get instanceof JavaMethod) {
					((JavaMethod) ((PropertyDecl) value).get).object = ((JavaModuleInstance) moduleInstance).getInstance();
				}
				if (((PropertyDecl) value).let instanceof JavaMethod) {
					((JavaMethod) ((PropertyDecl) value).let).object = ((JavaModuleInstance) moduleInstance).getInstance();
				}
				if (((PropertyDecl) value).set instanceof JavaMethod) {
					((JavaMethod) ((PropertyDecl) value).set).object = ((JavaModuleInstance) moduleInstance).getInstance();
				}
			}
			else if (value instanceof PropertyDecl && moduleInstance instanceof JavaModuleInstance) {
				
			}
			interpreter.push(value);
			return null;
		}
		else if (objValue instanceof ModuleInstance) {
			ModuleInstance moduleInstance = (ModuleInstance) objValue;
			if (moduleInstance.hasMember(name.toUpperCase())) {
				Value value = moduleInstance.getMember(name.toUpperCase());
				interpreter.push(value);
				return null;
			}
			else {
				throw new RuntimeException("This module doesn't contain the key '" + name.toUpperCase() + "'");
			}
		}
		else {
			throw new UnsupportedOperationException("Not implemented");
		}
	}

	@Override
	public String toString() {
		return "ELEM";
	}
	
	public static final Opcode ELEM = new ElemOpcode();
}
