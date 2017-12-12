/*******************************************************************************
 * Copyright (C) 2017 Inshua<inshua@gmail.com>
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/
package org.siphon.visualbasic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.siphon.visualbasic.compile.CompileException;
import org.siphon.visualbasic.interpreter.Argument;
import org.siphon.visualbasic.interpreter.Interpreter;
import org.siphon.visualbasic.interpreter.Invokable;
import org.siphon.visualbasic.interpreter.opcodes.Opcode;
import org.siphon.visualbasic.runtime.ArgumentDecl;
import org.siphon.visualbasic.runtime.Statement;
import org.siphon.visualbasic.runtime.Value;
import org.siphon.visualbasic.runtime.VbValue;
import org.siphon.visualbasic.runtime.VbVarType;
import org.siphon.visualbasic.runtime.VbVariable;

public class MethodDecl extends ModuleMemberDecl implements Invokable {
	public boolean isStatic;
	public List<ArgumentDecl> arguments = new ArrayList<>();
	public final List<Statement> statements = new ArrayList<>();
	public final List<Opcode> opcodes = new ArrayList<>();

	// 仅用来判断是否声明过变量，真正声明变量使用 VariableStatement
	public final Map<String, VarDecl> variables = new HashMap<>();
	protected Map<String, Value> staticVariables = new HashMap<>();
	private static final Logger logger = LogManager.getLogger(MethodDecl.class);

	public VbVarType returnType;

	public ParserRuleContext ast;

	public MethodType methodType;
	public VarDecl result;

	public MethodDecl(Library library, ModuleDecl module, MethodType methodType) {
		super(library, module);
		this.methodType = methodType;
	}

	@Override
	public String toString() {
		String s = "";
		s += visibility + " ";
		if (isStatic)
			s += " static ";
		s += name + " ";
		s += "(" + StringUtils.join(arguments, ", ") + ")";
		s += " As " + returnType;
		return s;
	}
	
	public String toDeclString() {
		String s = "";
		s += visibility + " ";
		if (isStatic)
			s += " static ";
		s += name + " ";
		s += "(" + StringUtils.join(arguments, ", ") + ")";
		s += " As " + returnType;
		
		String sts= "\r\n";
		for(int i = 0; i< statements.size(); i++){
			Statement statement = statements.get(i);
			sts += i + ":\t" + statement + "\r\n";
		}
		s += sts;
		s += "End " + this.name + " \r\n";
		return s;
	}

	public void addVariable(VarDecl decl) {
		String u = decl.name.toUpperCase();
		if (this.variables.containsKey(u)) {
			this.module.addCompileException(decl.ambiguousIdentifier(), CompileException.AMBIGUOUS_IDENTIFIER, decl.ambiguousIdentifier());
		} else {
			this.variables.put(u, decl);
		}
	}

	@Override
	public void invoke(Interpreter interpreter, List<Argument> args) {
		logger.debug("==== Entering {} ====", getName());
		List<Value> argValues = bindArguments(interpreter, args);
		for (int i = 0; i < argValues.size(); i++) {
			Value argValue = argValues.get(i);
			ArgumentDecl decl = getArguments().get(i);
			
			interpreter.getCurrentScope().insert(decl.getName(), argValue.get(interpreter));
		}
		
		if (getType() == InvokableType.FUNCTION) {
			//FIXME: Do something about recursions
			interpreter.getCurrentScope().insert(getName(), new VbVariable(VbVarType.VbVariant, VbValue.Empty));
		}
		
		int stackLevel = interpreter.getStack().size();
		interpreter.execute(opcodes);
		
		if ((getType() == InvokableType.FUNCTION && interpreter.getStack().size() - 1 != stackLevel) || 
			(getType() != InvokableType.FUNCTION && interpreter.getStack().size() != stackLevel)) 
		{
			logger.error("Invokable {} left the stack dirty (level {} at entry, level {} at exit)", getName(), stackLevel, interpreter.getStack().size());
		}
		logger.debug("==== Exiting {} ====", getName());
	}
	
	static class ActualArgument
	{
		ArgumentDecl decl;
		Value value = null;
		
		public ActualArgument(ArgumentDecl decl) {
			this.decl = decl;
		}
	}
	
	
	protected List<Value> bindArguments(Interpreter interpreter, List<Argument> args) {
		List<ActualArgument> actualArguments = new ArrayList<>(); 
		List<Value> result = new ArrayList<>();
		List<Value> argumentValues = new ArrayList<>();
		
		if (args == null) {
			return result;
		}
		
		if (getArguments() == null) {
			throw new UnsupportedOperationException("Can't handle VBA methods without argument specification");
		}
		
		for (ArgumentDecl argumentDecl : getArguments()) {
			actualArguments.add(new ActualArgument(argumentDecl));
		}
		
		if (args.size() > actualArguments.size()) {
			throw new RuntimeException("More values passed to function that expected by declaration");
		}
		
		for (int i = 0; i < args.size(); i++) {
			argumentValues.add(interpreter.pop());
		}
		
		//Assign positional arguments
		for (int i = 0; i < args.size(); i++) {
			Argument arg = args.get(i);
			if (!arg.isNamedArgument()) {
				actualArguments.get(i).value = argumentValues.get(i);
			}
		}
		
		//Assign named arguments
		for (int i = 0; i < args.size(); i++) {
			Argument arg = args.get(i);
			if (arg.isNamedArgument()) {
				for (ActualArgument actualArgument : actualArguments) {
					if (actualArgument.decl.getName().compareToIgnoreCase(arg.getName()) == 0) {
						if (actualArgument.value != null) {
							throw new RuntimeException("Trying to assign to named argument '" + arg.getName() + "' which has already been assigned as positional argument");
						}
						
						actualArgument.value = argumentValues.get(i);
						break;
					}
				}
			}
		}
		
		//Assign default arguments
		for (ActualArgument actualArgument : actualArguments) {
			if (actualArgument.value == null) {
				if (actualArgument.decl.optional) {
					actualArgument.value = actualArgument.decl.defaultValue;
				}
				else {
					throw new RuntimeException("Missing required argument '" + actualArgument.decl.getName() + "'");
				}
			}
			
			//If value is not the same type as the one expected by the function, cast it
			if (actualArgument.value instanceof VbValue && ((VbValue) actualArgument.value).varType.vbType != actualArgument.decl.varType.vbType) {
				actualArgument.value = ((VbValue) actualArgument.value).cast(actualArgument.decl.varType.vbType);
			}

			result.add(actualArgument.value);
		}
		
		return result;
	}

	@Override
	public Value getStaticVariable(String name) {
		return this.staticVariables.getOrDefault(name.toUpperCase(), null);
	}

	@Override
	public InvokableType getType() {
		switch (this.methodType) {
		case Function:
			return InvokableType.FUNCTION;
		case Sub:
			return InvokableType.SUB;
		case PropertyGet:
			return InvokableType.PROPERTY_GET;
		case PropertySet:
			return InvokableType.PROPERTY_SET;
		case PropertyLet:
			return InvokableType.PROPERTY_LET;
		case Rule:
			return InvokableType.RULE;
		}
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public List<ArgumentDecl> getArguments() {
		return arguments;
	}
	
	@Override
	public VbValue get(Interpreter interpreter) {
		invoke(interpreter, null);
		return interpreter.pop().get(interpreter);
	}
}
