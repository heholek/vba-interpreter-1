package org.siphon.visualbasic.interpreter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.siphon.visualbasic.Library;
import org.siphon.visualbasic.ModuleDecl;
import org.siphon.visualbasic.ModuleMemberDecl;
import org.siphon.visualbasic.SourceLocation;
import org.siphon.visualbasic.VbDecl;
import org.siphon.visualbasic.interpreter.OnErrorHandler.GotoErrorHandler;
import org.siphon.visualbasic.interpreter.OnErrorHandler.NoErrorHandler;
import org.siphon.visualbasic.interpreter.OnErrorHandler.ResumeNextErrorHandler;
import org.siphon.visualbasic.interpreter.opcodes.FindOpcode;
import org.siphon.visualbasic.interpreter.opcodes.Opcode;
import org.siphon.visualbasic.interpreter.opcodes.PushOpcode;
import org.siphon.visualbasic.interpreter.opcodes.ReadOpcode;
import org.siphon.visualbasic.interpreter.opcodes.StatementOpcode;
import org.siphon.visualbasic.runtime.Value;
import org.siphon.visualbasic.runtime.VbValue;

public class Interpreter implements Scope {
	private static final Logger logger = LogManager.getLogger(Interpreter.class);
	protected Stack<Value> stack = new Stack<>();
	protected SourceLocation currentLocation = null;
	protected Scope currentScope = this;
	protected List<Library> libraries = new ArrayList<>();
	protected Map<String, Value> variables = new HashMap<>();
	protected Map<String, ModuleInstance> modules = new HashMap<>();
	protected org.siphon.visualbasic.compile.Compiler compiler;
	
	public Interpreter(org.siphon.visualbasic.compile.Compiler compiler) {	
//		for (Map.Entry<String, List<VbDecl> > decls : compiler.names.indexes.entrySet()) {
//			for (VbDecl decl : decls.getValue()) {
//				if (decl instanceof ModuleDecl) {
//					ModuleInstance moduleInstance = new ModuleInstance();
//					for (Map.Entry<String, VbDecl> member : ((ModuleDecl) decl).members.entrySet()) {
//						if (member.getValue() instanceof ModuleDecl) {
//							moduleInstance.addMember(member.getKey().toUpperCase(), member.getValue());
//						}
//						else if (member.getValue() instanceof )
//					}
//				}
//			}
//		}
		this.compiler = compiler;
		this.add(compiler.getVbaLib());
		this.add(compiler.getVbLib());
	}
	
	public void add(Library library) {
		libraries.add(library);
		
		for (Map.Entry<String, ModuleDecl> module : library.modules.entrySet()) {
			ModuleInstance moduleInstance = new ModuleInstance();
			for (Map.Entry<String, VbValue> value : module.getValue().values.entrySet()) {
				moduleInstance.addMember(value.getKey().toUpperCase(), value.getValue());
			}
			for (Map.Entry<String, VbDecl> decl : module.getValue().members.entrySet()) {
				moduleInstance.addMember(decl.getKey().toUpperCase(), decl.getValue());
			}
			modules.put(module.getKey().toUpperCase(), moduleInstance);
		}
	}
	
	public void addToGlobalNamespace() {}
	
	public Stack<Value> getStack() {
		return this.stack;
	}
	
	public void dropScope() {
		this.currentScope = this.currentScope.getParent();
	}
	
	public void pushScope(Scope scope) {
		scope.setParent(this.currentScope);
		this.currentScope = scope;
	}
	
	public Scope getCurrentScope() {
		return this.currentScope;
	}
	
	public void push(Value value) {
		this.stack.push(value);
	}

	public Value pop() {
		return this.stack.pop();
	}
	
	public void setCurrentLocation(SourceLocation location) {
		this.currentLocation = location;
	}
	
	public Value call(String name, VbValue ...args ) {
		int initialStackSize = this.stack.size();
		Collections.reverse(Arrays.asList(args));
		List<Argument> argDesc = new ArrayList<>();
		for (VbValue arg : args) {
			execute(new PushOpcode(arg));
			argDesc.add(Argument.UNNAMED_ARGUMENT);
		}
		
		execute(new PushOpcode(VbValue.fromJava(name)));
		execute(FindOpcode.FIND_LVALUE);
		execute(new ReadOpcode(argDesc));
		
		Value ret = null;
		if (this.stack.size() > initialStackSize) {
			ret = pop();
		}
		
		return ret;
	}
	
	public Integer execute(Opcode opcode) {
		return opcode.apply(this);
	}
	
	public void execute(List<Opcode> opcodes) {
		int idx = 0;
		while (idx < opcodes.size()) {
			Opcode opcode = opcodes.get(idx);
			logger.debug("{}:\t{}", idx, opcode.toString());
			try {
				Integer result = execute(opcode);
				if (result != null) {
					if (result < 0 || result > opcodes.size()) {
						throw new RuntimeException("The next opcode index is out of bounds");
					}
					
					idx = result.intValue();
				}
				else {
					idx += 1;
				}
			}
			catch (EmptyStackException ex) {
				throw ex;
			}
			catch (UnsupportedOperationException ex) {
				throw ex;
			}
			catch (Exception ex) {
				logger.error("Catching exception during opcode execution:");
				logger.catching(ex);
				if (currentScope.getErrorHandler() instanceof NoErrorHandler) {
					throw ex;
				}
				if (getStack().size() != getCurrentScope().getStackDepthAtStatementStart()) {
					while (getStack().size() > getCurrentScope().getStackDepthAtStatementStart()) {
						pop();
					}
					if (getStack().size() < getCurrentScope().getStackDepthAtStatementStart()) {
						throw new RuntimeException("Too few values on stack");
					}
				}
				if (currentScope.getErrorHandler() instanceof ResumeNextErrorHandler) {
					while (idx < opcodes.size() && !(opcodes.get(idx) instanceof StatementOpcode)) {
						idx += 1;
					}
				}
				else if (currentScope.getErrorHandler() instanceof GotoErrorHandler) {
					GotoErrorHandler errorHandler = (GotoErrorHandler) currentScope.getErrorHandler();
					//TODO: What if Error handler uses label?
					idx = errorHandler.getLine();
				}
			}
		}
	}

	@Override
	public Value find(String name) {
		name = name.toUpperCase();
		if (variables.containsKey(name)) {
			return variables.get(name);
		}
		
		if (modules.containsKey(name)) {
			return modules.get(name);
		}
		
		for (ModuleInstance module : modules.values()) {
			if (module.hasMember(name)) {
				return module.getMember(name);
			}
		}
		
		for (Library library : this.libraries) {
			if (library.names.indexes.containsKey(name)) {
				List<VbDecl> decls = library.names.indexes.get(name);
				if (!decls.isEmpty()) {
					return decls.get(0);
				}
			}
		}
		
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void insert(String name, Value var) {
		variables.put(name.toUpperCase(), var);
	}

	@Override
	public Scope getParent() {
		return null;
	}

	@Override
	public void pushWith(Value value) {
		throw new UnsupportedOperationException("With can't be used in global scope");
	}

	@Override
	public void dropWith() {
		
	}

	@Override
	public Value getWith() {
		return null;
	}

	@Override
	public void setParent(Scope scope) {
		throw new UnsupportedOperationException("Can't set parent scope of global scope");
	}

	@Override
	public void setErrorHandler(OnErrorHandler handler) {
		throw new UnsupportedOperationException("Can't set error handler in global scope");
		
	}
	
	@Override
	public OnErrorHandler getErrorHandler() {
		throw new UnsupportedOperationException("Can't get error handler in global scope");
	}

	@Override
	public void setReturnValue(Value value) {
		throw new UnsupportedOperationException("Can't set a return value in global scope");
		
	}

	@Override
	public Value getReturnValue() {
		throw new UnsupportedOperationException("Can't get a return value in global scope");
	}
	
	@Override
	public void setStackDepthAtStatementStart(int stackDepth) {
		throw new UnsupportedOperationException("Can't set stack depth at statement start in global scope");
	}
	
	@Override
	public int getStackDepthAtStatementStart() {
		throw new UnsupportedOperationException("Can't get stack depth at statement start in global scope");
	}
}
