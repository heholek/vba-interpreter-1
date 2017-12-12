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
package org.siphon.visualbasic.compile;

import static org.siphon.visualbasic.compile.Compiler.getArgumentDeclsOfMethodOrProperty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.siphon.visualbasic.ConstDecl;
import org.siphon.visualbasic.MethodDecl;
import org.siphon.visualbasic.ModuleDecl;
import org.siphon.visualbasic.ModuleMemberDecl;
import org.siphon.visualbasic.PropertyDecl;
import org.siphon.visualbasic.SourceLocation;
import org.siphon.visualbasic.VarDecl;
import org.siphon.visualbasic.VbDecl;
import org.siphon.visualbasic.VbWithDecl;
import org.siphon.visualbasic.interpreter.Argument;
import org.siphon.visualbasic.interpreter.opcodes.BinaryOperation;
import org.siphon.visualbasic.interpreter.opcodes.ElemOpcode;
import org.siphon.visualbasic.interpreter.opcodes.FindOpcode;
import org.siphon.visualbasic.interpreter.opcodes.Opcode;
import org.siphon.visualbasic.interpreter.opcodes.PushOpcode;
import org.siphon.visualbasic.interpreter.opcodes.PushWith;
import org.siphon.visualbasic.interpreter.opcodes.ReadOpcode;
import org.siphon.visualbasic.interpreter.value.Value;
import org.siphon.visualbasic.runtime.ArgumentDecl;
import org.siphon.visualbasic.runtime.VbString;
import org.siphon.visualbasic.runtime.VbValue;
import org.siphon.visualbasic.runtime.VbVarType;
import org.siphon.visualbasic.runtime.VbVarType.TypeEnum;
import org.siphon.visualbasic.runtime.statements.EvalAssignableStatement;
import org.siphon.visualbasic.runtime.statements.LiteralStatement;

import vba.VbaParser.ArgCallContext;
import vba.VbaParser.ArgsCallContext;
import vba.VbaParser.DictionaryCallContext;
import vba.VbaParser.ICS_followElementContext;
import vba.VbaParser.ImplicitCallStmt_InStmtContext;
import vba.VbaParser.LiteralContext;
import vba.VbaParser.MemberCallContext;
import vba.VbaParser.SubscriptsContext;
import vba.VbaParser.ValueStmtContext;
import vba.VbaParser.VsBinaryContext;
import vba.VbaParser.VsICSContext;
import vba.VbaParser.VsLiteralContext;
import vba.VbaParser.VsStructContext;
import vba.VbaParser.VsTypeOfContext;
import vba.VbaParser.VsUnaryContext;

public class ValueStatementCompiler {

	private final Compiler compiler;
	private final Map<String, Opcode> BINARY_OPERATORS = Map.ofEntries(
			Map.entry("AND", BinaryOperation.AND),
			Map.entry("&", BinaryOperation.CAT),
			Map.entry("+", BinaryOperation.ADD),
			Map.entry("-", BinaryOperation.SUB),
			Map.entry("*", BinaryOperation.MUL),
			Map.entry("/", BinaryOperation.DIV),
			Map.entry("\\", BinaryOperation.IDIV),
			Map.entry("MOD", BinaryOperation.MOD),
			Map.entry("LIKE", BinaryOperation.LIKE),
			Map.entry("=", BinaryOperation.EQ),
			Map.entry(">=", BinaryOperation.GE),
			Map.entry("<=", BinaryOperation.LE),
			Map.entry(">", BinaryOperation.GT),
			Map.entry("<", BinaryOperation.LT),
			Map.entry("<>", BinaryOperation.NE),
			Map.entry("IS", BinaryOperation.IS),
			Map.entry("OR", BinaryOperation.OR),
			Map.entry("XOR", BinaryOperation.XOR),
			Map.entry("EQV", BinaryOperation.EQV),
			Map.entry("IMP", BinaryOperation.IMP),
			Map.entry("^", BinaryOperation.POW)
	);
			

	public ValueStatementCompiler(Compiler compiler) {
		this.compiler = compiler;
	}

	/*
	 * valueStmt : 
	literal 												# vsLiteral		*
	| implicitCallStmt_InStmt 								# vsICS			*
	| LPAREN WS? valueStmt (WS? ',' WS? valueStmt)* RPAREN 	# vsStruct		* 	//  t = (a, b, c) ??? 不理解这种东西是什么 
	| NEW WS? (baseType | complexType) 						# vsNew			*
	| typeOfStmt 											# vsTypeOf		*
	| midStmt 												# vsMid			*
	| ADDRESSOF WS? valueStmt 								# vsAddressOf
	
	| valueStmt WS? IS WS? valueStmt 						# vsIs			*
	| valueStmt WS? LIKE WS? valueStmt 						# vsLike		*
	| valueStmt WS? GEQ WS? valueStmt 						# vsGeq			*
	| valueStmt WS? LEQ WS? valueStmt 						# vsLeq			*
	| valueStmt WS? GT WS? valueStmt 						# vsGt			*
	| valueStmt WS? LT WS? valueStmt 						# vsLt			*
	| valueStmt WS? NEQ WS? valueStmt 						# vsNeq			*
	| valueStmt WS? EQ WS? valueStmt 						# vsEq			*
	
	| valueStmt WS? AMPERSAND WS? valueStmt 				# vsAmp			*
	| MINUS WS? valueStmt 									# vsNegation	*
	| PLUS WS? valueStmt 									# vsPlus		*
	| valueStmt WS? PLUS WS? valueStmt 						# vsAdd			*
	| valueStmt WS? MOD WS? valueStmt 						# vsMod			*
	| valueStmt WS? DIV WS? valueStmt 						# vsDiv			*
	| valueStmt WS? MULT WS? valueStmt 						# vsMult		*
	| valueStmt WS? MINUS WS? valueStmt 					# vsMinus		*
	| valueStmt WS? POW WS? valueStmt 						# vsPow			*
	
	| valueStmt WS? IMP WS? valueStmt 						# vsImp			*
	| valueStmt WS? EQV WS? valueStmt 						# vsEqv			*
	| valueStmt WS? XOR WS? valueStmt 						# vsXor			*
	| valueStmt WS? OR WS? valueStmt 						# vsOr			*
	| valueStmt WS? AND WS? valueStmt 						# vsAnd			*
	| NOT WS? valueStmt 									# vsNot			*
	 */
	void compileValueStatement(ValueStmtContext valueStmt, MethodDecl method, List<Opcode> opcodes) throws CompileException {
		if (valueStmt instanceof VsBinaryContext) {
			VsBinaryContext binary = (VsBinaryContext) valueStmt;
			compileValueStatement(binary.val2, method, opcodes);
			compileValueStatement(binary.val1, method, opcodes);
			opcodes.add(BINARY_OPERATORS.get(binary.op.getText().toUpperCase()));
		}
		else if (valueStmt instanceof VsUnaryContext) {
			throw new UnsupportedOperationException("Unary operators are not implemented");
		}
		else if (valueStmt instanceof VsICSContext) {
			compileImplicitCallStmt(((VsICSContext) valueStmt).implicitCallStmt_InStmt(), method, false, opcodes);

		} else if (valueStmt instanceof VsLiteralContext) {
			LiteralContext literal = ((VsLiteralContext) valueStmt).literal();
			VbValue value = compiler.parseLiteral(literal, method.getModule());
			opcodes.add(new PushOpcode(value));

		} else if (valueStmt instanceof VsStructContext) {
			throw new UnsupportedOperationException("VsStructContext is not implemented");
			//VsStructContext struct = (VsStructContext) valueStmt; // TODO 不理解 struct 的确切含义
			//return compileValueStatement(struct.valueStmt(0), method);
			
		} else if(valueStmt instanceof VsTypeOfContext){
			throw new UnsupportedOperationException("VsTypeOfContext is not implemented");
//			TypeOfIsStmtContext op = ((VsTypeOfContext) valueStmt).typeOfIsStmt();
//			ValueStatementDesc v1 = compileValueStatement(op.valueStmt(), method);
//			ClassTypeDecl patternType = compiler.findClassTypeDecl(op.type().getText().toUpperCase(), op.type(), method.getModule());
//			VbVarType varType = v1.getVarType();
//			compiler.mustBeOject(varType, op.valueStmt());
//			if(varType.vbType == VbVarType.TypeEnum.vbVariant || (varType.vbType == VbVarType.TypeEnum.vbObject && varType.typeDecl == null)){
//				result.setStatement(new TypeOfIsStatement(v1.getStatement(), patternType, method.getModule().sourceLocation(valueStmt)));
//			} else {
//				ClassModuleDecl clsLeft = varType.getClassModuleDecl();
//				ClassModuleDecl clsRight = varType.getClassModuleDecl();
//				boolean match = (clsLeft == clsRight || clsLeft.isImplementFrom(clsRight));
//				result.setStatement(new LiteralStatement(VbValue.fromJava(match)));
//			}
		} else {
			throw new ImpossibleException();
		}
	}

//	private ValueStatementDesc compileNewStatement(MethodDecl method, VsNewContext vsNew) throws CompileException {
//		/*
//		 * NEW WS? complexType 						# vsNew
//		 */
//		String name = null;
//		name = vsNew.complexType().getText().toUpperCase();
//		ClassTypeDecl typeDecl = compiler.findClassTypeDecl(name, vsNew, method.getModule());
//
//		ValueStatementDesc result = new ValueStatementDesc();
//		result.setStatement(new NewStatement(method.getModule().sourceLocation(vsNew), typeDecl)).setAst(vsNew);
//		return result;
//	}

	/**
	 * 
	 * @param implicitCallStmt
	 * @param method
	 * @param asAssignee
	 *            是否作为赋值的左值。对于左值应生成 LocateVarStatement 不应生成 GetStatement。
	 * @return
	 * @throws CompileException
	 */
	void compileImplicitCallStmt(ImplicitCallStmt_InStmtContext implicitCallStmt, MethodDecl method,
			boolean asAssignee, List<Opcode> opcodes) throws CompileException {
		this.compileImplicitCallStmt(implicitCallStmt, method, asAssignee, true, opcodes);
	}

	void compileImplicitCallStmt(ImplicitCallStmt_InStmtContext implicitCallStmt, MethodDecl method,
			boolean asAssignee, boolean apply, List<Opcode> opcodes) throws CompileException {
		/*
		 *  implicitCallStmt_InStmt : iCS_startSymbol iCS_followElement*;
			iCS_startSymbol :
				  ambiguousIdentifier typeHint?
				| memberCall		// in with block
				| dictionaryCall	// in with block
			;
			
			iCS_followElement: memberCall | dictionaryCall | LPAREN argsCall? RPAREN;
			
			memberCall : '.' ambiguousIdentifier;
			
			dictionaryCall : '!' ambiguousIdentifier;
		 */
		
		ParserRuleContext first = (ParserRuleContext) implicitCallStmt.iCS_startElement().getChild(0);
		List<ICS_followElementContext> followers = implicitCallStmt.iCS_followElement();
		List<Opcode> curOpcodes = new ArrayList<>();
		
		if (first instanceof MemberCallContext || first instanceof DictionaryCallContext) {
			curOpcodes.add(PushWith.PUSH_WITH);
			curOpcodes.add(new PushOpcode(VbValue.fromJava(first.getChild(1).getText())));
			curOpcodes.add(ElemOpcode.ELEM);
		}
		else {
			curOpcodes.add(new PushOpcode(VbValue.fromJava(first.getText().toUpperCase())));
			curOpcodes.add(FindOpcode.FIND_LVALUE);
		}
		
		for (int depth = 0; depth < followers.size(); depth++) {
			ICS_followElementContext follower = followers.get(depth);
			if (follower.memberCall() != null) {
				curOpcodes.add(new PushOpcode(VbValue.fromJava((follower.memberCall().ambiguousIdentifier().getText().toUpperCase()))));
				curOpcodes.add(ElemOpcode.ELEM);
			} else if(follower.dictionaryCall() != null){
				curOpcodes.add(new PushOpcode(VbValue.fromJava((follower.dictionaryCall().ambiguousIdentifier().getText().toUpperCase()))));
				curOpcodes.add(ElemOpcode.ELEM);
			} else if (follower.argsCall() != null){	// argsCall
				List<ArgCallContext> args = follower.argsCall().argCall();
				List<Argument> argsList = new ArrayList<Argument>();
				Collections.reverse(args);
				for (ArgCallContext arg : args) {
					if (arg.ambiguousIdentifier() != null) {
						argsList.add(new Argument(arg.ambiguousIdentifier().getText()));
					}
					else {
						argsList.add(Argument.UNNAMED_ARGUMENT);
					}
					
					compileValueStatement(arg.valueStmt(), method, opcodes);
				}
				
				opcodes.addAll(curOpcodes);
				opcodes.add(new ReadOpcode(argsList));
				curOpcodes.clear();
				return;
			}
		}
		
		opcodes.addAll(curOpcodes);
		return;
	}

	/*
	 *  implicitCallStmt_InStmt : iCS_startSymbol iCS_followElement*;
		iCS_startSymbol :
			  ambiguousIdentifier typeHint?
			| memberCall		// in with block
			| dictionaryCall	// in with block
		;
		
		iCS_followElement: memberCall | dictionaryCall | LPAREN argsCall? RPAREN;
		
		memberCall : '.' ambiguousIdentifier;
		
		dictionaryCall : '!' ambiguousIdentifier;
	 */
	private ValueStatementDesc compileDeepMember(VbDecl base, SourceLocation baseLocation, MethodDecl method,
			List<ParserRuleContext> thrumb, boolean asAssignee, ParserRuleContext implicitCallStmt, boolean apply)
			throws CompileException {
		
		EvalAssignableStatement result = null;

		VbVarType type = null;
		if (base instanceof ConstDecl) {
			VbValue c = ((ConstDecl) base).constValue; 
			if (thrumb.size() > 0 && !c.isObject()) {
				throw method.getModule().newCompileException(thrumb.get(0), CompileException.UNKNOWN_MEMBER, thrumb.get(0));
			}
			if (asAssignee && thrumb.size() == 0) {
				throw method.getModule().newCompileException(implicitCallStmt, CompileException.CANNOT_MODIFY_CONST, implicitCallStmt);
			}
			if (c.isObject()) { // const cannot be array or object or udt, but sometimes come from bindObject can be Object
				result = new EvalAssignableStatement(method.getModule().sourceLocation(implicitCallStmt), c);
				type = c.varType;
			} else {
				return new ValueStatementDesc().setStatement(new LiteralStatement(c)).setVarType(type).setAst(implicitCallStmt);
			}
		} else if (base instanceof VarDecl) {
			VarDecl vd = (VarDecl) base;
			type = vd.varType;
			if (vd.methodDecl != null && vd == vd.methodDecl.result) {
				// maybe recursive call
				if (vd.methodDecl.arguments.size() > 0 && thrumb.size() > 0
						&& nextIsArguments(vd.methodDecl.arguments, type, thrumb.get(0))) {
					base = vd.methodDecl;
				}
			}
		} 
		else if (base instanceof ModuleDecl) {
			ModuleDecl md = (ModuleDecl) base;
			type = new VbVarType(TypeEnum.vbObject, null, null, md.getClass());
		}
			
		if (base instanceof VbWithDecl) {
			result = new EvalAssignableStatement(baseLocation, new EvalAssignableStatement.With());
			type = new VbVarType(TypeEnum.vbVariant);
		}
		else {
			result = new EvalAssignableStatement(baseLocation, base);
		}

		boolean afterFunctionCalled = false;
		for (int i = 0; i < thrumb.size(); i++) {
			ParserRuleContext ast = thrumb.get(i);
			
			afterFunctionCalled = false;
			if(base instanceof MethodDecl || base instanceof PropertyDecl){
				ModuleMemberDecl m = (ModuleMemberDecl) base;
				if(ast instanceof ArgsCallContext){
					List<ValueStatementDesc> lsArgCalls = compiler.compileArgsCall((ArgsCallContext) ast, getArgumentDeclsOfMethodOrProperty(base) , method);
					compiler.checkMethodOrPropertyCall(base, lsArgCalls, method.getModule(), ast);
					result = result.bind(method.getModule().sourceLocation(ast), compiler.toStatements(lsArgCalls, method.getModule()));  // base 已经 bind 过了
					type = getReturnType(m);
					base = null;
					afterFunctionCalled = true;
				} else if(ast instanceof MemberCallContext || ast instanceof DictionaryCallContext){
					List<ValueStatementDesc> lsArgCalls = Collections.emptyList();
					compiler.checkMethodOrPropertyCall(base, lsArgCalls, method.getModule(), ast);	
					// 0参数，不需要 bind
					type = getReturnType(m);
					base = null;
					i--; 	// wait next turn
					afterFunctionCalled = m instanceof MethodDecl || (m instanceof PropertyDecl && ((PropertyDecl)m).isReadonly());
				}
			} /*else if (base instanceof ModuleDecl) {
				if (ast instanceof MemberCallContext) {
					result = new EvalAssignableStatement(baseLocation, new EvalAssignableStatement.With());
				}
			}*/ else if(type.vbType == TypeEnum.vbVariant || (type.vbType == TypeEnum.vbObject && type.typeDecl == null)){
				if(ast instanceof ArgsCallContext){
					List<ValueStatementDesc> lsArgCalls = compiler.compileArgsCall((ArgsCallContext) ast, getArgumentDeclsOfMethodOrProperty(base) , method);
					result = result.bind(method.getModule().sourceLocation(ast), compiler.toStatements(lsArgCalls, method.getModule()));
				} else if(ast instanceof MemberCallContext){
					MemberCallContext mc = (MemberCallContext) ast;
					result = result.bind(method.getModule().sourceLocation(ast), new EvalAssignableStatement.Member(mc.ambiguousIdentifier().getText().toUpperCase()));
				} else if(ast instanceof DictionaryCallContext){
					DictionaryCallContext dictCall = (DictionaryCallContext) ast;
					result = result.bind(method.getModule().sourceLocation(ast), new EvalAssignableStatement.DictionaryCall(dictCall.ambiguousIdentifier().getText().toUpperCase()));
				}
				
			} else if(type.vbType == VbVarType.TypeEnum.vbArray){
				if(ast instanceof ArgsCallContext){
					List<ValueStatementDesc> lsArgCalls = compiler.compileArgsCall((ArgsCallContext) ast, getArgumentDeclsOfMethodOrProperty(base) , method);
					this.checkArrayOrCollectionCall(type, lsArgCalls, method.getModule(), ast);
					Compiler.noNamedArgs((ArgsCallContext) ast, method.getModule());
					result = result.bind(method.getModule().sourceLocation(ast), compiler.toStatements(lsArgCalls, method.getModule()));
					type = type.arrayDef.baseType;
					base = null;
				} else if(ast instanceof MemberCallContext){
					throw method.getModule().newCompileException(ast, CompileException.SYNTAX_ERROR, " is an array");
				} else if(ast instanceof DictionaryCallContext){
					throw method.getModule().newCompileException(ast, CompileException.SYNTAX_ERROR, " is an array");
				}
				
			} else if(type.vbType == VbVarType.TypeEnum.vbObject){
				if(ast instanceof ArgsCallContext){
					if(type.hasDefaultMember()){
						base = type.getDefaultMember();	// enter default member
						result = result.bind(method.getModule().sourceLocation(ast), base);
						i--;
					} else {
						throw method.getModule().newCompileException(ast, CompileException.DEFAULT_MEMBER_REQUIRED, ast);
					}
				} else if(ast instanceof MemberCallContext){
					MemberCallContext mc = (MemberCallContext) ast;
					String name = mc.ambiguousIdentifier().getText().toUpperCase();

					ModuleMemberDecl member = (ModuleMemberDecl) type.typeDecl.getMember(name);
					if (member instanceof VarDecl) {
						VarDecl vd = (VarDecl) member;
						type = vd.varType;
						result = result.bind(method.getModule().sourceLocation(ast), vd);
						
					} else if (member instanceof MethodDecl || member instanceof PropertyDecl) {
						result = result.bind(method.getModule().sourceLocation(ast), member);
						base = member;
						afterFunctionCalled = member instanceof MethodDecl || (member instanceof PropertyDecl && ((PropertyDecl)member).isReadonly());
					} else if (member == null) {
						throw method.getModule().newCompileException(mc.ambiguousIdentifier(), CompileException.MEMBER_NOT_EXIST, mc.ambiguousIdentifier().getText());
					}
				} else if(ast instanceof DictionaryCallContext){
					if(type.isDictionary()){
						DictionaryCallContext dictCall = (DictionaryCallContext) ast;
						ModuleMemberDecl member = type.getDictionaryMember();
						result = result.bind(method.getModule().sourceLocation(ast), member);
						
						List<ValueStatementDesc> lsArgCalls = new ArrayList<>();
						ValueStatementDesc desc = new ValueStatementDesc();
						desc.setAst(ast).setStatement(new LiteralStatement(new VbString(dictCall.ambiguousIdentifier().getText()))).setVarType(VbVarType.VbString);
						lsArgCalls.add(desc);
						result.bind(method.getModule().sourceLocation(dictCall.ambiguousIdentifier()), compiler.toStatements(lsArgCalls, method.getModule()));
						
						base = null;
						type = getReturnType(member);
						
						afterFunctionCalled = member instanceof MethodDecl || (member instanceof PropertyDecl && ((PropertyDecl)member).isReadonly());
					} else {
						throw method.getModule().newCompileException(ast, CompileException.DEFAULT_MEMBER_REQUIRED, ast);
					}
				}
				
			} else if(type.vbType == VbVarType.TypeEnum.vbUserDefinedType){
				if(ast instanceof ArgsCallContext){
					throw method.getModule().newCompileException(ast, CompileException.SYNTAX_ERROR, " is a udt");
				} else if(ast instanceof MemberCallContext){
					String name = ((MemberCallContext) ast).ambiguousIdentifier().getText().toUpperCase();
					if (!type.typeDecl.hasMember(name))
						throw method.getModule().newCompileException(ast, CompileException.UNKNOWN_MEMBER, ast);

					VarDecl member = (VarDecl) type.typeDecl.getMember(name);
					result = result.bind(method.getModule().sourceLocation(ast), member);
					type = member.varType;
					base = null;
				} else if(ast instanceof DictionaryCallContext){
					throw method.getModule().newCompileException(ast, CompileException.SYNTAX_ERROR, " is a array");
				}
			} else {	// base type
				throw method.getModule().newCompileException(ast, CompileException.SYNTAX_ERROR, " is base type");
			}
		}

		if (asAssignee) {
			if (afterFunctionCalled) {
				throw method.getModule().newCompileException(implicitCallStmt, CompileException.FUNCTION_CANNOT_ASSIGN,
						implicitCallStmt);
			} else {
				return new ValueStatementDesc().setStatement(result).setVarType(type).setAst(implicitCallStmt);
			}
		} else {
			if (apply) {
//				MemberAtLocation last = result.peek();
//				if(last.member instanceof MethodDecl || last.member instanceof PropertyDecl){
//					compiler.checkMethodOrPropertyCall((VbDecl) last.member, Collections.emptyList(), method.getModule(), thrumb.get(thrumb.size()-1));
//				}
				return new ValueStatementDesc().setStatement(result.apply()).setVarType(type).setAst(implicitCallStmt);
			} else {
				return new ValueStatementDesc().setStatement(result).setVarType(type).setAst(implicitCallStmt);
			}
		}
	}
	
	
	
	
	private VbVarType getReturnType(ModuleMemberDecl member) {
		if(member instanceof MethodDecl){
			return ((MethodDecl) member).returnType;
		} else {
			PropertyDecl pd = (PropertyDecl) member;
			return pd.getReturnType();
		}
	}

	void checkArrayOrCollectionCall(VbVarType varType, List<ValueStatementDesc> argCalls, ModuleDecl module,
			ParserRuleContext ast) {
		// TODO
	}
	

	private boolean nextIsArguments(List<ArgumentDecl> arguments, VbVarType returnType, ParserRuleContext next) {
		boolean result = true;
		if (next instanceof SubscriptsContext) {
			int sz = ((SubscriptsContext) next).subscript().size();
			if (arguments.size() == 0) {
				if (sz == 0) {
					//
				} else if (returnType.vbType == VbVarType.TypeEnum.vbArray || returnType.vbType == VbVarType.TypeEnum.vbVariant) {
					result = false;
				} else { // TODO wrong arguments check soon
					//
				}
			}
		} else if (next instanceof ArgsCallContext) {
			int sz = ((ArgsCallContext) next).argCall().size();
			if (arguments.size() == 0) {
				if (sz == 0) {
					//
				} else if (returnType.vbType == VbVarType.TypeEnum.vbArray || returnType.vbType == VbVarType.TypeEnum.vbVariant) {
					result = false;
				} else { // TODO wrong arguments check soon
					//
				}
			}
		} else {
			result = false;
		}
		return result;
	}
}
