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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.ParserRuleContext;
import org.siphon.visualbasic.ClassModuleDecl;
import org.siphon.visualbasic.ClassTypeDecl;
import org.siphon.visualbasic.EventDecl;
import org.siphon.visualbasic.MethodDecl;
import org.siphon.visualbasic.MethodType;
import org.siphon.visualbasic.ModuleDecl;
import org.siphon.visualbasic.OverflowException;
import org.siphon.visualbasic.SourceLocation;
import org.siphon.visualbasic.VarDecl;
import org.siphon.visualbasic.VbDecl;
import org.siphon.visualbasic.interpreter.Argument;
import org.siphon.visualbasic.interpreter.opcodes.ConditionalGotoOpcode;
import org.siphon.visualbasic.interpreter.opcodes.DropOpcode;
import org.siphon.visualbasic.interpreter.opcodes.EndWithOpcode;
import org.siphon.visualbasic.interpreter.opcodes.GotoLabelOpcode;
import org.siphon.visualbasic.interpreter.opcodes.GotoOpcode;
import org.siphon.visualbasic.interpreter.opcodes.OnErrorOpcode;
import org.siphon.visualbasic.interpreter.opcodes.Opcode;
import org.siphon.visualbasic.interpreter.opcodes.PushOpcode;
import org.siphon.visualbasic.interpreter.opcodes.ReadOpcode;
import org.siphon.visualbasic.interpreter.opcodes.SetWithOpcode;
import org.siphon.visualbasic.interpreter.opcodes.StatementOpcode;
import org.siphon.visualbasic.interpreter.opcodes.UnaryOperation;
import org.siphon.visualbasic.interpreter.opcodes.WriteOpcode;
import org.siphon.visualbasic.runtime.ArrayDef;
import org.siphon.visualbasic.runtime.ArrayDef.RankAsStatement;
import org.siphon.visualbasic.runtime.Statement;
import org.siphon.visualbasic.runtime.VbBoolean;
import org.siphon.visualbasic.runtime.VbValue;
import org.siphon.visualbasic.runtime.VbVarType;
import org.siphon.visualbasic.runtime.statements.AssignStatement;
import org.siphon.visualbasic.runtime.statements.EvalAssignableStatement;
import org.siphon.visualbasic.runtime.statements.ExitMethodStatement;
import org.siphon.visualbasic.runtime.statements.ForEachStatement;
import org.siphon.visualbasic.runtime.statements.ForNextStatement;
import org.siphon.visualbasic.runtime.statements.GoSubStatement;
import org.siphon.visualbasic.runtime.statements.GotoStatement;
import org.siphon.visualbasic.runtime.statements.IfNotGotoStatement;
import org.siphon.visualbasic.runtime.statements.LiteralStatement;
import org.siphon.visualbasic.runtime.statements.LogicalStatements;
import org.siphon.visualbasic.runtime.statements.MidStatment;
import org.siphon.visualbasic.runtime.statements.NewStatement;
import org.siphon.visualbasic.runtime.statements.OnErrorStatement;
import org.siphon.visualbasic.runtime.statements.PrintStatement;
import org.siphon.visualbasic.runtime.statements.RaiseEventStatement;
import org.siphon.visualbasic.runtime.statements.RedimStatement;
import org.siphon.visualbasic.runtime.statements.ResumeStatement;
import org.siphon.visualbasic.runtime.statements.ReturnStatement;
import org.siphon.visualbasic.runtime.statements.SelectCaseConditionStatement;
import org.siphon.visualbasic.runtime.statements.SelectCaseStatement;
import org.siphon.visualbasic.runtime.statements.SelectCaseStatement.ComparisonCondition;
import org.siphon.visualbasic.runtime.statements.SelectCaseStatement.Switcher;
import org.siphon.visualbasic.runtime.statements.StopStatement;
import org.siphon.visualbasic.runtime.statements.UndeterminedLabelGotoStatement;
import org.siphon.visualbasic.runtime.statements.VariableStatement;
import org.siphon.visualbasic.runtime.statements.WithStatement;

import vba.VbaParser.AmbiguousIdentifierContext;
import vba.VbaParser.ArgCallContext;
import vba.VbaParser.BlockContext;
import vba.VbaParser.BlockIfThenElseContext;
import vba.VbaParser.BlockStmtContext;
import vba.VbaParser.CaseCondIsContext;
import vba.VbaParser.CaseCondSelectionContext;
import vba.VbaParser.CaseCondToContext;
import vba.VbaParser.CaseCondValueContext;
import vba.VbaParser.CloseStmtContext;
import vba.VbaParser.ConstStmtContext;
import vba.VbaParser.DoLoopStmtContext;
import vba.VbaParser.ExitStmtContext;
import vba.VbaParser.ExplicitCallStmtContext;
import vba.VbaParser.ForEachStmtContext;
import vba.VbaParser.ForNextStmtContext;
import vba.VbaParser.GetStmtContext;
import vba.VbaParser.GoSubStmtContext;
import vba.VbaParser.GoToStmtContext;
import vba.VbaParser.IfBlockStmtContext;
import vba.VbaParser.IfConditionStmtContext;
import vba.VbaParser.IfElseBlockStmtContext;
import vba.VbaParser.IfElseIfBlockStmtContext;
import vba.VbaParser.IfThenElseStmtContext;
import vba.VbaParser.ImplicitCallStmt_InBlockContext;
import vba.VbaParser.ImplicitCallStmt_InStmtContext;
import vba.VbaParser.InlineIfThenElseContext;
import vba.VbaParser.InputStmtContext;
import vba.VbaParser.LetStmtContext;
import vba.VbaParser.LineInputStmtContext;
import vba.VbaParser.LineLabelContext;
import vba.VbaParser.LoadStmtContext;
import vba.VbaParser.MidStmtContext;
import vba.VbaParser.OnErrorStmtContext;
import vba.VbaParser.OpenStmtContext;
import vba.VbaParser.PrintStmtContext;
import vba.VbaParser.PutStmtContext;
import vba.VbaParser.RaiseEventStmtContext;
import vba.VbaParser.RedimStmtContext;
import vba.VbaParser.RedimSubStmtContext;
import vba.VbaParser.ResumeStmtContext;
import vba.VbaParser.ReturnStmtContext;
import vba.VbaParser.SC_CaseContext;
import vba.VbaParser.SC_CondContext;
import vba.VbaParser.SC_SelectionContext;
import vba.VbaParser.SelectCaseStmtContext;
import vba.VbaParser.SetStmtContext;
import vba.VbaParser.StopStmtContext;
import vba.VbaParser.SubscriptContext;
import vba.VbaParser.SubscriptsContext;
import vba.VbaParser.ValueStmtContext;
import vba.VbaParser.VariableStmtContext;
import vba.VbaParser.WhileWendStmtContext;
import vba.VbaParser.WithStmtContext;
import vba.VbaParser.WriteStmtContext;

public class BlockCompiler {

	private static class Exits {
		public List<GotoStatement> forStatements = new ArrayList<>();
		public List<GotoStatement> doStatements = new ArrayList<>();
	}

	private BlockContext block;
	private MethodDecl method;
	private Compiler compiler;

	List<Statement> result = new ArrayList<>();
	List<Opcode> opcodes = new ArrayList<>(); 
	Map<String, Integer> labels = new HashMap<>();
	int innerLabelId = 0;
	private final ModuleDecl module;
	
	private List<UndeterminedLabelGotoStatement> undeterminedLableGotoStaments = new ArrayList<>();

	public BlockCompiler(BlockContext block, MethodDecl method, Compiler compiler) {
		this.block = block;
		this.method = method;
		this.module = method.getModule();
		this.compiler = compiler;
	}
	
	public BlockCompiler(MethodDecl method, Compiler compiler) {
		this.method = method;
		this.module = method.getModule();
		this.compiler = compiler;
	}

	/*
	 * 	lineLabel					*
	| appactivateStmt				-	激活一应用程序窗口。
	| attributeStmt					-	不知是什么
	| beepStmt						-	通过计算机喇叭发出一个声调。
	| chdirStmt						-	改变当前的目录或文件夹。
	| chdriveStmt					-	改变当前的驱动器。
	| closeStmt						-	关闭 Open 语句所打开的输入/输出 (I/O) 文件。
	| constStmt						*
	| dateStmt						-	设置当前系统日期。
	| deleteSettingStmt				-	在 Windows 注册表中，从应用程序项目里删除区域或注册表项设置。
	| deftypeStmt					-	在模块级别上，为变量和传给过程的参数，设置缺省数据类型，以及为其名称以指定的字符开头的 Function 和 Property Get 过程，设置返回值类型。
	| doLoopStmt					*
	| endStmt						-	这种语句并不存在
	| eraseStmt						w	重新初始化大小固定的数组的元素，以及释放动态数组的存储空间。
	| errorStmt						w	模拟错误的发生。Error 语句获得的支持是向后兼容的。在新的代码中，特别是在建立对象时，要使用 Err 对象的 Raise 方法产生运行时错误。
	| exitStmt						*
	| explicitCallStmt				*
	| filecopyStmt					-
	| forEachStmt					*
	| forNextStmt					*
	| getStmt						-	将一个已打开的磁盘文件读入一个变量之中。通常用 Put 将 Get 读出的数据写入一个文件。
	| goSubStmt						*
	| goToStmt						*
	| ifThenElseStmt				*
	| inputStmt						-	从已打开的顺序文件中读出数据并将数据指定给变量。
	| killStmt						-	从磁盘中删除文件。
	| letStmt						*
	| lineInputStmt					-	从已打开的顺序文件中读出一行并将它分配给 String变量。
	| loadStmt						-	加载窗口
	| lockStmt						-	对于用 Open 语句打开的全部文件或一部分文件，其它进程所进行的控制访问。
	| lsetStmt						w	在一字符串变量中将一字符串往左对齐，或是将一用户定义类型变量复制到另一用户自定义类型变量。
	| midStmt						*	
	| mkdirStmt						-	创建一个新的目录或文件夹。
	| nameStmt						-	重新命名一个文件、目录、或文件夹。
	| onErrorStmt					*
	| onGoToStmt
	| onGoSubStmt
	| openStmt						-	对文件做任何 I/O 操作之前都必须先打开文件。Open 语句分配一个缓冲区供文件进行 I/O 之用，并决定缓冲区所使用的访问方式。
	| printStmt						-	将格式化显示的数据写入顺序文件中。
	| putStmt						-	将一个变量的数据写入磁盘文件中。
	| raiseEventStmt				*
	| randomizeStmt					-	VB 里实际上用的是 Math.Randomize Sub
	| redimStmt						*	
	| resetStmt						-	关闭所有用 Open 语句打开的磁盘文件。
	| resumeStmt					*
	| returnStmt					*
	| rmdirStmt						-	删除一个存在的目录或文件夹。
	| rsetStmt						w	在一字符串变量中将一字符串往右对齐。
	| savepictureStmt				-
	| saveSettingStmt				-
	| seekStmt						-	在 Open 语句打开的文件中，设置下一个读/写操作的位置。
	| selectCaseStmt				*
	| sendkeysStmt					-	将一个或多个按键消息发送到活动窗口，就如同在键盘上进行输入一样。
	| setattrStmt					- 	为一个文件设置属性信息。
	| setStmt						*
	| stopStmt						* 	可以在过程中的任何地方放置 Stop 语句，使用 Stop 语句，就相当于在程序代码中设置断点。
	| timeStmt						- 	设置系统时间。
	| unloadStmt					-	结束窗口
	| unlockStmt					- 	对于用 Open 语句打开的全部文件或一部分文件，其它进程所进行的控制访问。
	| variableStmt					*
	| whileWendStmt					*
	| widthStmt						-	将一个输出行的宽度指定给用 Open 语句打开的文件。
	| withStmt						*
	| writeStmt						-	将数据写入顺序文件。
	| implicitCallStmt_InBlock		*
	 */
	public List<Statement> compile() {

		compileBlock(block);

		// 确定标签后设置 goto 点
		for (UndeterminedLabelGotoStatement stmt : undeterminedLableGotoStaments) {
			if (labels.containsKey(stmt.label)) {
				Collections.replaceAll(stmt.owner, stmt, stmt.toGotoStatement(labels.get(stmt.label)));
			} else {
				module.addCompileException(new CompileException(stmt.getSourceLocation(), CompileException.LABEL_NOT_DEFINED, stmt.label));
			}
		}

		return result;
	}

	private void compileBlock(BlockContext block) {
		compileBlock(block, null, null);
	}
	
	private void compileBlock(BlockContext block, List<GotoStatement> forExits, List<GotoStatement> doWhileExits) {
		if (block == null)
			return;
		for (BlockStmtContext st : block.blockStmt()) {
			ParserRuleContext c = (ParserRuleContext) st.getChild(0);
			try {
				compileStmt(c, forExits, doWhileExits);
			} catch (CompileException e) {
				module.addCompileException(e);
			}
		}
	}

	Statement compileBlockStatement(ParserRuleContext blockStatement) throws CompileException {
		compileStmt(blockStatement, null, null);
		return result.get(0);
	}

	
	private void compileStmt(ParserRuleContext ast, List<GotoStatement> forExits, List<GotoStatement> doWhileExits)
			throws CompileException 
	{
		opcodes.add(new StatementOpcode(method.getModule().sourceLocation(ast)));
		if (ast instanceof VariableStmtContext) {
			List<VarDecl> vars = compiler.compileVarDecl((VariableStmtContext) ast, method.getModule(), method);
			for (VarDecl v : vars) {
				result.add(new VariableStatement(module.sourceLocation(ast), v));
			}

		} else if (ast instanceof ConstStmtContext) {
			compiler.compileConstDecl((ConstStmtContext) ast, method.getModule(), method);

		} else if (ast instanceof LetStmtContext) {
			/*
			 * letStmt : (LET WS)? implicitCallStmt_InStmt WS? (EQ | PLUS_EQ | MINUS_EQ) WS? valueStmt;
			 */
			LetStmtContext let = (LetStmtContext) ast;
			ValueStmtContext value = let.valueStmt();
			ImplicitCallStmt_InStmtContext implicitCallStmt_InStmt = let.implicitCallStmt_InStmt();
			compiler.compileValueStatement(value, method, opcodes);
			compiler.compileImplicitCallStmt(implicitCallStmt_InStmt, method, true, opcodes);
			opcodes.add(WriteOpcode.WRITE);
//			
//
//			ensureAssignable(assignee.getVarType(), valueStmt.getVarType(), "let");
//			assert assignee.getStatement() instanceof EvalAssignableStatement;
//			result.add(new AssignStatement(module.sourceLocation(ast), (EvalAssignableStatement) assignee.getStatement(),
//					valueStmt.getStatement()));
		} else if (ast instanceof SetStmtContext) {
			/*
			setStmt : SET WS implicitCallStmt_InStmt WS? EQ WS? valueStmt;
			*/
			SetStmtContext set = (SetStmtContext) ast;
			compiler.compileValueStatement(set.valueStmt(), method, opcodes);
			compiler.compileImplicitCallStmt(set.implicitCallStmt_InStmt(), method, true, opcodes);
			opcodes.add(WriteOpcode.WRITE);

		} else if (ast instanceof IfThenElseStmtContext) {
			IfThenElseStmtContext ifThenElse = (IfThenElseStmtContext) ast;
			if (ifThenElse instanceof InlineIfThenElseContext) {
				/*
				 * IF WS ifConditionStmt WS THEN WS blockStmt (WS ELSE WS blockStmt)?
				 */
				InlineIfThenElseContext inlineIfThenElse = (InlineIfThenElseContext) ifThenElse;
				BlockStmtContext ifBlock = inlineIfThenElse.blockStmt(0);
				BlockStmtContext elseBlock = inlineIfThenElse.blockStmt(1);
				compileIfThenElse(inlineIfThenElse.ifConditionStmt(), Arrays.asList(new BlockStmtContext[] { ifBlock }), null,
						Arrays.asList(new BlockStmtContext[] { elseBlock }), forExits, doWhileExits);

			} else if (ifThenElse instanceof BlockIfThenElseContext) {
				/*
				 * ifThenElseStmt : 
						IF WS ifConditionStmt WS THEN WS blockStmt (WS ELSE WS blockStmt)?	# inlineIfThenElse
						| ifBlockStmt ifElseIfBlockStmt* ifElseBlockStmt? END_IF			# blockIfThenElse
					;
				 */
				BlockIfThenElseContext blockIfThenElseContext = (BlockIfThenElseContext) ifThenElse;
				List<IfElseIfBlockStmtContext> elseIfStmts = blockIfThenElseContext.ifElseIfBlockStmt();
				IfElseBlockStmtContext elseStmt = blockIfThenElseContext.ifElseBlockStmt();

				IfBlockStmtContext ifBlock = blockIfThenElseContext.ifBlockStmt();
				IfConditionStmtContext ifBlockCondition = ifBlock.ifConditionStmt();
				List<BlockStmtContext> ifStatements = ifBlock.block().blockStmt();
				List<BlockStmtContext> elseStatements = elseStmt != null ? elseStmt.block().blockStmt() : null;

				compileIfThenElse(ifBlockCondition, ifStatements, elseIfStmts, elseStatements, forExits, doWhileExits);
			}

		} else if (ast instanceof SelectCaseStmtContext) {
			compileSelectCase((SelectCaseStmtContext) ast, forExits, doWhileExits);

		} else if (ast instanceof ForNextStmtContext) {
			compileForNext((ForNextStmtContext) ast, forExits, doWhileExits);

		} else if (ast instanceof ForEachStmtContext) {
			compileForEach((ForEachStmtContext) ast, forExits, doWhileExits);

		} else if (ast instanceof DoLoopStmtContext) {
			compileDoLoop((DoLoopStmtContext) ast, forExits, doWhileExits);

		} else if (ast instanceof WhileWendStmtContext) {
			compileWhileWend((WhileWendStmtContext) ast, forExits, doWhileExits);

		} else if (ast instanceof ExitStmtContext) {
			ExitStmtContext exit = (ExitStmtContext) ast;
			compileExitStmt(exit, forExits, doWhileExits);

		} else if (ast instanceof LineLabelContext) {
			String label = ((LineLabelContext) ast).ambiguousIdentifier().getText().toUpperCase();
			if (labels.containsKey(label)) {
				module.addCompileException(ast, CompileException.AMBIGUOUS_LABEL, ast);
			} else {
				labels.put(label, result.size());
			}

		} else if (ast instanceof WithStmtContext) {
			compileWithStmt((WithStmtContext) ast, forExits, doWhileExits);

		} else if (ast instanceof GoToStmtContext) {
			GoToStmtContext gotoStmt = (GoToStmtContext) ast;
			String label = gotoStmt.ambiguousIdentifier().getText().toUpperCase();
			if (!labels.containsKey(label)) {
				UndeterminedLabelGotoStatement stmt = new UndeterminedLabelGotoStatement(module.sourceLocation(ast), label,
						result);
				result.add(stmt);
				undeterminedLableGotoStaments.add(stmt);
			} else {
				result.add(new GotoStatement(module.sourceLocation(ast), labels.get(label)));
			}

		} else if (ast instanceof GoSubStmtContext) {
			GoSubStmtContext gosubStmt = (GoSubStmtContext) ast;
			String label = gosubStmt.ambiguousIdentifier().getText().toUpperCase();
			if (!labels.containsKey(label)) {
				UndeterminedLabelGotoStatement stmt = new UndeterminedLabelGotoStatement.GoSub(module.sourceLocation(ast),
						label, result);
				result.add(stmt);
				undeterminedLableGotoStaments.add(stmt);
			} else {
				result.add(new GoSubStatement(module.sourceLocation(ast), labels.get(label)));
			}

		} else if (ast instanceof ReturnStmtContext) {
			result.add(new ReturnStatement(module.sourceLocation(ast)));

		} else if (ast instanceof OnErrorStmtContext) {
			compileOnErrorStmt((OnErrorStmtContext) ast);

		} else if (ast instanceof ResumeStmtContext) {
			compileResumeStmt((ResumeStmtContext) ast);

		} else if (ast instanceof RaiseEventStmtContext) {
			compileRaiseEventStmt((RaiseEventStmtContext) ast);

		} else if (ast instanceof PrintStmtContext) {
			//TODO: This is not the original intention of print, debugging
			//https://docs.microsoft.com/en-us/office/vba/language/reference/user-interface-help/printstatement
			throw new UnsupportedOperationException("Current implementation of print is incorrect");
			//PrintStmtContext printStmtContext = (PrintStmtContext) ast;
			//ValueStmtContext vs = printStmtContext.outputList().outputList_Expression(0).valueStmt();
			//result.add(
			//		new PrintStatement(module.sourceLocation(ast), compiler.compileValueStatement(vs, method).getStatement()));

		} else if (ast instanceof ExplicitCallStmtContext) {
			Statement statement = compileExplicitCallStmt((ExplicitCallStmtContext) ast);
			result.add(statement);

		} else if (ast instanceof ImplicitCallStmt_InBlockContext) {
			compileImplicitCallInBlockStmt((ImplicitCallStmt_InBlockContext) ast);
			
		} else if(ast instanceof StopStmtContext){
			result.add(new StopStatement(module.sourceLocation(ast)));
			
		} else if(ast instanceof RedimStmtContext){
			compileRedimStmt((RedimStmtContext)ast);
			
		} else if(ast instanceof MidStmtContext){
			compileMidStmt((MidStmtContext)ast);
			
		} else if(ast instanceof LoadStmtContext) {
			compileLoadStmt((LoadStmtContext)ast);
			
		} else if (ast instanceof OpenStmtContext) {
			compileOpenStmt((OpenStmtContext)ast);
		} 
		else if (ast instanceof WriteStmtContext) {
			compileWriteStmt((WriteStmtContext)ast);
		}
		else if (ast instanceof CloseStmtContext) {
			compileCloseStmt((CloseStmtContext)ast); 
		}
		else if (ast instanceof GetStmtContext) {
			compileGetStmt((GetStmtContext)ast);
		}
		else if (ast instanceof PutStmtContext) {
			compilePutStmt((PutStmtContext)ast);
		}
		else if (ast instanceof InputStmtContext) {
			compileInputStmt((InputStmtContext)ast);
		}
		else if (ast instanceof LineInputStmtContext) {
			compileLineInputStmt((LineInputStmtContext)ast);
		}
		else {
			throw new ImpossibleException("Don't know how to parse '" + ast.getText() + "' at " + ast.getStart().getInputStream().getSourceName() + ":" + ast.getStart().getLine() + ":" + ast.getStart().getCharPositionInLine());
		}
	}
	
	private void compileOpenStmt(OpenStmtContext ast) throws CompileException {
		//TODO
		throw new UnsupportedOperationException("Not implemented");
	}
	
	private void compileWriteStmt(WriteStmtContext ast) throws CompileException {
		//TODO
		throw new UnsupportedOperationException("Not implemented");
	}
	
	private void compileCloseStmt(CloseStmtContext ast) throws CompileException {
		//TODO
		throw new UnsupportedOperationException("Not implemented");
	}
	
	private void compileGetStmt(GetStmtContext ast) throws CompileException {
		//TODO
		throw new UnsupportedOperationException("Not implemented");		
	}
	
	private void compilePutStmt(PutStmtContext ast) throws CompileException {
		//TODO
		throw new UnsupportedOperationException("Not implemented");		
	}
	
	private void compileInputStmt(InputStmtContext ast) throws CompileException {
		//TODO
		throw new UnsupportedOperationException("Not implemented");		
	}
	
	private void compileLineInputStmt(LineInputStmtContext ast) throws CompileException {
		//TODO
		throw new UnsupportedOperationException("Not implemented");	
	}

	private void compileMidStmt(MidStmtContext ast) throws CompileException {
		throw new UnsupportedOperationException("Not implemented");	
//		VbDecl varDecl = compiler.findMemberDeclInScope(ast.ambiguousIdentifier(), method, false);
//		compiler.mustBeVariable(varDecl);
//		compiler.mustBeStringType(varDecl);
//		
//		ValueStatementDesc start = compiler.compileValueStatement(ast.valueStmt(0), method, opcodes);
//		compiler.mustBeBaseType(start.getVarType(), start.getAst());
//		
//		ValueStatementDesc length = null;
//		if(ast.valueStmt().size() == 3){
//			length = compiler.compileValueStatement(ast.valueStmt(1), method, opcodes);
//			compiler.mustBeBaseType(length.getVarType(), length.getAst());
//		}
//		
//		ValueStmtContext strStmt = ast.valueStmt(ast.valueStmt().size() - 1);
//		ValueStatementDesc str = compiler.compileValueStatement(strStmt, method, opcodes);
//		compiler.mustBeBaseType(str.getVarType(), strStmt);
//		
//		result.add(new MidStatment(module.sourceLocation(ast), (VarDecl) varDecl, start.getStatement(), length == null ? null : length.getStatement(), str.getStatement()));
	}
	
	private void compileLoadStmt(LoadStmtContext ast) {
		// TODO
	}

	private void compileRedimStmt(RedimStmtContext ast) throws CompileException {
		boolean preserve = ast.PRESERVE() != null;
		for(RedimSubStmtContext sub : ast.redimSubStmt()){
			VbDecl decl = compiler.findMemberDeclInScope(sub.ambiguousIdentifier(), method, false);
			compiler.mustBeArrayType(decl);
			VarDecl varDecl = (VarDecl) decl;
			if(varDecl.varType.vbType != VbVarType.TypeEnum.vbVariant && varDecl.varType.vbType != VbVarType.TypeEnum.vbArray){
				throw module.newCompileException(sub.ambiguousIdentifier(), CompileException.SYNTAX_ERROR, sub.ambiguousIdentifier(), " is not array or variant");
			}
			
			RankAsStatement[] ranks = parseArrayRanks(module, method, sub.subscripts());
			
			VbVarType type = null;
			if(sub.asTypeClause() != null){
				if(preserve){
					throw module.newCompileException(sub.asTypeClause(), CompileException.SYNTAX_ERROR, sub.asTypeClause(), "redim preserve cannot follow with type clause");
				}
				if(varDecl.varType.vbType != VbVarType.TypeEnum.vbVariant){
					throw module.newCompileException(sub.asTypeClause(), CompileException.SYNTAX_ERROR, sub.asTypeClause(), "array is not variant, cannot change type");
				}
				type = compiler.parseType(sub.asTypeClause(), null, module);
			}
			
			RedimStatement statement = new RedimStatement(module.sourceLocation(sub), (VarDecl)decl, ranks, preserve, type);
			result.add(statement);
		}
	}
	
	private ArrayDef.RankAsStatement[] parseArrayRanks(ModuleDecl module, MethodDecl method, SubscriptsContext constSubscriptsContext)
			throws CompileException {
		throw new UnsupportedOperationException("Not implemented");	
//		ArrayDef.RankAsStatement[] ranks = null;
//		if (constSubscriptsContext != null) {
//			List<SubscriptContext> subscripts = constSubscriptsContext.subscript();
//			ranks = new ArrayDef.RankAsStatement[subscripts.size()];
//			int i = 0;
//			for (SubscriptContext subscript : subscripts) {
//				if (subscript.valueStmt().size() == 1) {
//					int from = module.getArrayBase();
//					ValueStatementDesc to;
//					ValueStmtContext s = subscript.valueStmt(0);
//					try {
//						to = compiler.compileValueStatement(s, method, opcodes);
//						compiler.mustBeNumberType(to.getVarType());
//						ranks[i++] = new ArrayDef.RankAsStatement(new LiteralStatement(VbValue.fromJava(from)), to.getStatement());
//					} catch (OverflowException e) {
//						module.addCompileException(s, CompileException.OVERFLOW, s);
//					}
//				} else {
//					ValueStmtContext s = subscript.valueStmt(0);
//					ValueStmtContext e = subscript.valueStmt(1);
//					try {
//						ValueStatementDesc from = compiler.compileValueStatement(s, method, opcodes);
//						ValueStatementDesc to = compiler.compileValueStatement(e, method, opcodes);
//						compiler.mustBeNumberType(from.getVarType());
//						compiler.mustBeNumberType(to.getVarType());
//						try {
//							ranks[i++] = new ArrayDef.RankAsStatement(from.getStatement(), to.getStatement());
//						} catch (OverflowException e1) {
//							module.addCompileException(s, CompileException.OVERFLOW, e);
//						}
//					} catch (OverflowException e1) {
//						module.addCompileException(s, CompileException.OVERFLOW, s);
//					}
//				}
//			}
//		}
//		return ranks;
	}

	private void ensureAssignable(VbVarType varType, VbVarType varType2, String mode) {
		// TODO
	}

	private void compileRaiseEventStmt(RaiseEventStmtContext stmt) throws CompileException {
		/*
		 * RAISEEVENT WS ambiguousIdentifier (WS? LPAREN WS? (argsCall WS?)? RPAREN)?;
		 */
		if (module instanceof ClassModuleDecl == false) {
			module.addCompileException(stmt.RAISEEVENT(), CompileException.MUST_IN_CLASSMODULE, stmt.RAISEEVENT());
		}

		ClassModuleDecl classModuleDecl = (ClassModuleDecl) module;

		String eventName = stmt.ambiguousIdentifier().getText();
		EventDecl event = classModuleDecl.events.get(eventName.toUpperCase());
		if (event == null) {
			module.addCompileException(stmt.ambiguousIdentifier(), CompileException.NOT_MEMBER_TYPE,
					stmt.ambiguousIdentifier());
		}
		List<ValueStatementDesc> args = null;
		if (stmt.argsCall() != null) {
			Compiler.noNamedArgs(stmt.argsCall(), module);
			args = compiler.compileArgsCall(stmt.argsCall(), event.arguments, method);
		}
		Compiler.checkArgs(event.arguments, args, module, stmt.argsCall());
		RaiseEventStatement raise = new RaiseEventStatement(module.sourceLocation(stmt), event, compiler.toStatements(args, module));
		result.add(raise);
	}

	private void compileResumeStmt(ResumeStmtContext ast) {
		// resumeStmt : RESUME (WS (NEXT | ambiguousIdentifier))?;
		SourceLocation sourceLocation = module.sourceLocation(ast);
		if (ast.NEXT() != null) {
			result.add(new ResumeStatement.Next(sourceLocation));
		} else if (ast.ambiguousIdentifier() != null) {
			String label = ast.ambiguousIdentifier().getText().toUpperCase();
			if (label.equals("0")) {
				result.add(new ResumeStatement.Resume(sourceLocation));
			} else {
				if (!labels.containsKey(label)) {
					UndeterminedLabelGotoStatement stmt = new ResumeStatement.GotoLabel(sourceLocation, label, result);
					undeterminedLableGotoStaments.add(stmt);
					result.add(stmt);
				} else {
					result.add(new ResumeStatement.Goto(sourceLocation, labels.get(label)));
				}
			}
		} else {
			result.add(new ResumeStatement.Resume(sourceLocation));
		}
	}

	private void compileOnErrorStmt(OnErrorStmtContext ast) {
		// onErrorStmt : (ON_ERROR | ON_LOCAL_ERROR) WS (GOTO WS ambiguousIdentifier | RESUME WS NEXT);
		if (ast.RESUME() != null) {
			opcodes.add(new OnErrorOpcode(true));
		}
		else {
			opcodes.add(new OnErrorOpcode(ast.ambiguousIdentifier().getText().toUpperCase()));
		}
	}

	private void compileWithStmt(WithStmtContext ast, List<GotoStatement> forExits, List<GotoStatement> doWhileExits)
			throws CompileException {
		/*
		 	withStmt : 
				WITH WS (implicitCallStmt_InStmt | (NEW WS type)) endOfStatement 
				block? 
				END_WITH
			;
		 */

		Statement statement = null;
		VbVarType varType = null;
		if (ast.implicitCallStmt_InStmt() != null) {
			compiler.compileImplicitCallStmt(ast.implicitCallStmt_InStmt(), method, false, false, opcodes);
		} else { // new type
			throw new UnsupportedOperationException("Not implemented");
//			varType = compiler.parseType(ast.type(), null, method.getModule());
//			compiler.mustBeClassType(varType.typeDecl, method.getModule(), ast.type());
//			statement = new NewStatement(module.sourceLocation(ast.type()), (ClassTypeDecl) varType.typeDecl);
		}
		
		opcodes.add(SetWithOpcode.SET_WITH);
		compileBlock(ast.block(), forExits, doWhileExits);
		opcodes.add(EndWithOpcode.END_WITH);
	}

	private Statement compileExplicitCallStmt(ExplicitCallStmtContext ast) throws CompileException {
		/*
			explicitCallStmt :  CALL WS implicitCallStmt_InStmt;
		*/
		compiler.compileImplicitCallStmt(ast.implicitCallStmt_InStmt(), method, false,
				false, opcodes);
		throw new UnsupportedOperationException("Not implemented");
//		EvalAssignableStatement stmt = (EvalAssignableStatement) vsd.getStatement();
//		return stmt.apply();
	}

	// 没有 call 开始的
	private void compileImplicitCallInBlockStmt(ImplicitCallStmt_InBlockContext ast) throws CompileException {
		
		
		/*
			implicitCallStmt_InBlock : implicitCallStmt_InStmt (WS argsCall)?;
		 */
		List<Argument> argsList = new ArrayList<Argument>();
		if (ast.argsCall() != null) {
			List<ArgCallContext> args = ast.argsCall().argCall();
			Collections.reverse(args);
			for (ArgCallContext arg : args) {
				if (arg.ambiguousIdentifier() != null) {
					argsList.add(new Argument(arg.ambiguousIdentifier().getText()));
				}
				else {
					argsList.add(Argument.UNNAMED_ARGUMENT);
				}
				
				new ValueStatementCompiler(compiler).compileValueStatement(arg.valueStmt(), method, opcodes);
			}
		}
		
		compiler.compileImplicitCallStmt(ast.implicitCallStmt_InStmt(), method, false,
				false, opcodes);
		
		opcodes.add(new ReadOpcode(argsList));
		opcodes.add(DropOpcode.DROP);
		
		
//		if (ast.argsCall() != null) { // TODO 编译检查
//			List<ValueStatementDesc> args = compiler.compileArgsCall(ast.argsCall(), null, method); // TODO check whether is method/property
//			ic.bind(method.getModule().sourceLocation(ast.argsCall()), compiler.toStatements(args, module));
//		}
//		return ic.apply();
	}


	private void compileWhileWend(WhileWendStmtContext doLoop, List<GotoStatement> forExits, List<GotoStatement> doWhileExits)
			throws CompileException {
		/*
		 * whileWendStmt : 
			WHILE WS valueStmt endOfStatement 
			block?
			WEND
		;
		 */
		ValueStmtContext vs = doLoop.valueStmt();
		
		int begin = opcodes.size() - 1; //Index of the statement opcode
		compiler.compileValueStatement(vs, method, opcodes);
		opcodes.add(UnaryOperation.NOT);
		ConditionalGotoOpcode exit = new ConditionalGotoOpcode();
		opcodes.add(exit);
		
		compileBlock(doLoop.block(), forExits, doWhileExits);
		opcodes.add(new PushOpcode(VbBoolean.TRUE));
		opcodes.add(new ConditionalGotoOpcode(begin));
		exit.setTarget(opcodes.size());
	}

	private void shouldBeCondition(VbVarType condition) {
	}

	private void compileDoLoop(DoLoopStmtContext doLoop, List<GotoStatement> forExits, List<GotoStatement> doWhileExitsPrev)
			throws CompileException {
		/*
		 * doLoopStmt :
			DO endOfStatement 
			block?
			LOOP
			|
			DO WS (WHILE | UNTIL) WS valueStmt endOfStatement
			block?
			LOOP
			| 
			DO endOfStatement
			block
			LOOP WS (WHILE | UNTIL) WS valueStmt
		 */
		throw new UnsupportedOperationException("Not implemented");
//		List<GotoStatement> doWhileExits = new ArrayList<>();
//
//		ValueStmtContext vs = doLoop.valueStmt();
//		ValueStatementDesc condition = vs != null ? compiler.compileValueStatement(vs, method, opcodes) : null;
//		if (condition != null)
//			shouldBeCondition(condition.getVarType());
//		boolean atBottom = false;
//		boolean until = false;
//		if (condition != null) {
//			atBottom = (doLoop.children.indexOf(condition) == doLoop.children.size() - 1);
//			until = (doLoop.UNTIL() != null);
//		}
//
//		int begin = result.size();
//		if (!atBottom) {
//			GotoStatement exit = null;
//			if (!until) {
//				exit = new IfNotGotoStatement(module.sourceLocation(doLoop.WHILE()), condition.getStatement());
//			} else {
//				exit = new IfNotGotoStatement(module.sourceLocation(doLoop.UNTIL()),
//						new LogicalStatements.NotStatement(module.sourceLocation(doLoop.UNTIL()), condition.getStatement()));
//			}
//			result.add(exit);
//			doWhileExits.add(exit);
//		}
//		if (doLoop.block() != null) {
//			for (BlockStmtContext st : doLoop.block().blockStmt()) {
//				ParserRuleContext c = (ParserRuleContext) st.getChild(0);
//				compileStmt(c, forExits, doWhileExits);
//			}
//		}
//		if (condition == null || !atBottom) {
//			result.add(new GotoStatement(module.sourceLocation(doLoop.DO()), begin));
//		} else if (atBottom) {
//			if (until) {
//				result.add(new IfNotGotoStatement(module.sourceLocation(doLoop.UNTIL()), condition.getStatement())
//						.setNextStatement(begin));
//			} else {
//				result.add(new IfNotGotoStatement(module.sourceLocation(doLoop.WHILE()), new LogicalStatements.NotStatement(
//						condition.getStatement().getSourceLocation(), condition.getStatement())).setNextStatement(begin));
//			}
//		}
//
//		for (GotoStatement exit : doWhileExits) {
//			exit.setNextStatement(result.size());
//		}

	}

	private void compileExitStmt(ExitStmtContext exit, List<GotoStatement> forExits, List<GotoStatement> doWhileExits) {
		if (exit.EXIT_SUB() != null) {
			if (method.methodType != MethodType.Sub) {
				module.addCompileException(exit.EXIT_SUB(), CompileException.SYNTAX_ERROR, exit.EXIT_SUB(), " not allowed");
			} else {
				result.add(new ExitMethodStatement(module.sourceLocation(exit)));
			}
		} else if (exit.EXIT_FUNCTION() != null) {
			if (method.methodType != MethodType.Function) {
				module.addCompileException(exit.EXIT_FUNCTION(), CompileException.SYNTAX_ERROR, exit.EXIT_FUNCTION(),
						" not allowed");
			} else {
				result.add(new ExitMethodStatement(module.sourceLocation(exit)));
			}
		} else if (exit.EXIT_RULE() != null) {
			if (method.methodType != MethodType.Rule) {
				module.addCompileException(exit.EXIT_RULE(), CompileException.SYNTAX_ERROR, exit.EXIT_RULE(),
						" not allowed");
			} else {
				result.add(new ExitMethodStatement(module.sourceLocation(exit)));
			}
		} else if (exit.EXIT_PROPERTY() != null) {
			if (method.methodType == MethodType.PropertyGet || method.methodType == MethodType.PropertyLet
					|| method.methodType == MethodType.PropertySet) {
				result.add(new ExitMethodStatement(module.sourceLocation(exit)));
			} else {
				module.addCompileException(exit.EXIT_PROPERTY(), CompileException.SYNTAX_ERROR, exit.EXIT_PROPERTY(),
						" not allowed");
			}
		} else if (exit.EXIT_FOR() != null) {
			if (forExits == null) {
				module.addCompileException(exit.EXIT_FUNCTION(), CompileException.SYNTAX_ERROR, exit.EXIT_FOR(),
						" not in for loop");
			} else {
				GotoStatement exitFor = new GotoStatement(module.sourceLocation(exit));
				forExits.add(exitFor);
				result.add(exitFor);
			}
		} else if (exit.EXIT_DO() != null) {
			if (doWhileExits == null) {
				module.addCompileException(exit.EXIT_DO(), CompileException.SYNTAX_ERROR, exit.EXIT_DO(), " not in for loop");
			} else {
				doWhileExits.add(new GotoStatement(module.sourceLocation(exit)));
			}
		}
	}

	private void compileForNext(ForNextStmtContext forNext, List<GotoStatement> forExitsPrev, List<GotoStatement> doWhileExits)
			throws CompileException {
		/*
		 * msdn： counter 必要参数。用做循环计数器的数值变量。这个变量不能是布尔或数组元素。 
		 * 但是实际上在vb里用数组元素也可以正确运行。
		 * 虽然如此，还是只当简单变量来做比较方便，实际上也很少用数组元素当 counter。
		 * 
		 * 	forNextStmt : 
				FOR WS ambiguousIdentifier typeHint? (WS asTypeClause)? WS? EQ WS? valueStmt WS TO WS valueStmt (WS STEP WS valueStmt)? endOfStatement 
				block?
				NEXT (WS ambiguousIdentifier)?
			; 
		 */
		throw new UnsupportedOperationException("Not implemented");
//		ValueStatementDesc initValue = compiler.compileValueStatement(forNext.valueStmt(0), method, opcodes);
//		ValueStatementDesc endValue = compiler.compileValueStatement(forNext.valueStmt(1), method, opcodes);
//		ValueStatementDesc step = null;
//		if (forNext.valueStmt().size() == 3) {
//			step = compiler.compileValueStatement(forNext.valueStmt(2), method, opcodes);
//			compiler.mustBeNumberType(step.getVarType());
//		} else {
//			VbValue v = new VbInteger(1);
//			step = new ValueStatementDesc().setStatement(new LiteralStatement(v)).setVarType(VbVarType.VbInteger);
//		}
//
//		VarDecl var;
//		AmbiguousIdentifierContext iterName = forNext.ambiguousIdentifier(0);
//		var = (VarDecl) compiler.findMemberDeclInScope(iterName, method, false);
//		compiler.mustBeNumberType(var.varType);
//		ForNextStatement forStatement = new ForNextStatement(var, initValue.getStatement(), step.getStatement(),
//				endValue.getStatement());
//
//		// LetStatment init = new LetStatment();
//		// init.valueStatement = initValue;
//		// init.assignee = new VariableStatement(var);
//		// result.add(init);
//
//		result.add(forStatement.initStatement(initValue.getStatement().getSourceLocation()));
//
//		int beginLine = result.size();
//		List<GotoStatement> forExits = new ArrayList<>();
//		if (forNext.block() != null) {
//			for (BlockStmtContext st : forNext.block().blockStmt()) {
//				ParserRuleContext c = (ParserRuleContext) st.getChild(0);
//				compileStmt(c, forExits, null);
//			}
//		}
//		result.add(forStatement.nextStatement(module.sourceLocation(forNext.NEXT()), beginLine));
//		for (GotoStatement exit : forExits) {
//			exit.setNextStatement(result.size());
//		}
	}

	private void compileForEach(ForEachStmtContext forEach, List<GotoStatement> forExitsPrev, List<GotoStatement> doWhileExits)
			throws CompileException {
		/*
			forEachStmt : 
				FOR WS EACH WS ambiguousIdentifier typeHint? WS IN WS valueStmt endOfStatement
				block?
				NEXT (WS ambiguousIdentifier)?
			;
		*/
		throw new UnsupportedOperationException("Not implemented");
//		VarDecl var;
//		AmbiguousIdentifierContext iterName = forEach.ambiguousIdentifier(0);
//		var = (VarDecl) compiler.findMemberDeclInScope(iterName, method, false);
//
//		ValueStatementDesc collection = compiler.compileValueStatement(forEach.valueStmt(), method, opcodes);
//		compiler.mustBeArrayOrCollection(collection);
//		ForEachStatement forStatement = new ForEachStatement(module.sourceLocation(forEach), var, collection.getStatement());
//
//		result.add(forStatement.initStatement(module.sourceLocation(forEach.FOR())));
//
//		int beginLine = result.size();
//		result.add(forStatement.advanceNext(module.sourceLocation(forEach.IN())));
//
//		List<GotoStatement> forExits = new ArrayList<>();
//		if (forEach.block() != null) {
//			for (BlockStmtContext st : forEach.block().blockStmt()) {
//				ParserRuleContext c = (ParserRuleContext) st.getChild(0);
//				compileStmt(c, forExits, null);
//			}
//		}
//		SourceLocation sl = module.sourceLocation(forEach.NEXT());
//		result.add(new IfNotGotoStatement(sl, forStatement.hasNext(sl)).setNextStatement(result.size() + 2));
//		result.add(new GotoStatement(sl, beginLine));
//		for (GotoStatement exit : forExits) {
//			exit.setNextStatement(result.size());
//		}
	}

	private void compileSelectCase(SelectCaseStmtContext ast, List<GotoStatement> forExits, List<GotoStatement> doWhileExits)
			throws CompileException {
		/*
		 * selectCaseStmt : 
				SELECT WS CASE WS valueStmt endOfStatement 
				sC_Case*
				END_SELECT
			;
			
			sC_Case : 
				CASE WS sC_Cond endOfStatement
				block?
			;
			
			// ELSE first, so that it is not interpreted as a variable call
			sC_Cond :
			    ELSE                                                            # caseCondElse
			    | sC_Selection (WS? ',' WS? sC_Selection)*                      # caseCondSelection
			;
			
			sC_Selection :
			    IS WS? comparisonOperator WS? valueStmt                       # caseCondIs
			    | valueStmt WS TO WS valueStmt                                # caseCondTo
			    | valueStmt                                                   # caseCondValue
			;
		 */
		throw new UnsupportedOperationException("Not implemented");
//		List<GotoStatement> lsGotoEnd = new ArrayList<>();
//
//		ValueStatementDesc vsd = compiler.compileValueStatement(ast.valueStmt(), method, opcodes);
//		compiler.mustBeNumberOrStringType(vsd.getVarType());
//		Statement vs = vsd.getStatement();
//		SelectCaseStatement selectCaseStatement = new SelectCaseStatement(module.sourceLocation(ast), vs);
//		result.add(selectCaseStatement);
//
//		int index = 0;
//		for (SC_CaseContext kase : ast.sC_Case()) {
//			Switcher switcher = new Switcher();
//			List<SelectCaseConditionStatement> conds = new ArrayList<>();
//
//			SC_CondContext cond = kase.sC_Cond();
//			if (cond instanceof CaseCondSelectionContext) {
//				for (SC_SelectionContext selection : ((CaseCondSelectionContext) cond).sC_Selection()) {
//					if (selection instanceof CaseCondValueContext) {
//						CaseCondValueContext v = (CaseCondValueContext) selection;
//						vsd = compiler.compileValueStatement(v.valueStmt(), method, opcodes);
//						compiler.mustBeNumberOrStringType(vsd.getVarType());
//						conds.add(new SelectCaseStatement.SingleValueCondition(vsd.getStatement()));
//					} else if (selection instanceof CaseCondIsContext) {
//						CaseCondIsContext s = (CaseCondIsContext) selection;
//						vsd = compiler.compileValueStatement(s.valueStmt(), method, opcodes);
//						compiler.mustBeNumberOrStringType(vsd.getVarType());
//						conds.add(new ComparisonCondition(vsd.getStatement(), s.comparisonOperator().getText()));
//					} else if (selection instanceof CaseCondToContext) {
//						CaseCondToContext c = (CaseCondToContext) selection;
//						vsd = compiler.compileValueStatement(c.valueStmt(0), method, opcodes);
//						compiler.mustBeNumberType(vsd.getVarType());
//						Statement from = vsd.getStatement();
//						vsd = compiler.compileValueStatement(c.valueStmt(1), method, opcodes);
//						compiler.mustBeNumberType(vsd.getVarType());
//						Statement to = vsd.getStatement();
//						conds.add(new SelectCaseStatement.BetweenCondition(from, to));
//					}
//				}
//				switcher.conditionStatements = (SelectCaseConditionStatement[]) conds
//						.toArray(new SelectCaseConditionStatement[conds.size()]);
//				switcher.nextStatementIndex = result.size();
//				selectCaseStatement.switchers.add(switcher);
//			} else { // caseCondElse
//				selectCaseStatement.defaultStatementIndex = result.size();
//			}
//
//			if(kase.block() != null) {
//				for (BlockStmtContext st : kase.block().blockStmt()) {
//					ParserRuleContext c = (ParserRuleContext) st.getChild(0);
//					compileStmt(c, forExits, doWhileExits);
//				}
//			}
//
//			index++;
//			if (index != ast.sC_Case().size()) {
//				GotoStatement gotoEnd = new GotoStatement(module.sourceLocation(ast.END_SELECT()), -2);
//				result.add(gotoEnd); // GOTO END
//				lsGotoEnd.add(gotoEnd);
//			}
//		}
//
//		for (GotoStatement g : lsGotoEnd) {
//			g.setNextStatement(result.size());
//		}
//		if (selectCaseStatement.defaultStatementIndex == 0)
//			selectCaseStatement.defaultStatementIndex = result.size();
	}

	private void compileIfThenElse(IfConditionStmtContext ifBlockCondition, List<BlockStmtContext> ifStatements,
			List<IfElseIfBlockStmtContext> elseIfStmts, List<BlockStmtContext> elseStatements, List<GotoStatement> forExits,
			List<GotoStatement> doWhileExits) throws CompileException {
		throw new UnsupportedOperationException("Not implemented");
//		ValueStatementDesc vsd = compiler.compileValueStatement(ifBlockCondition.valueStmt(), method, opcodes);
//		shouldBeCondition(vsd.getVarType());
//		Statement condition = vsd.getStatement();
//		List<GotoStatement> gotos = new ArrayList<>();
//		IfNotGotoStatement ifNotGoto = new IfNotGotoStatement(module.sourceLocation(ifBlockCondition), condition);
//		result.add(ifNotGoto);
//
//		// if branch		// TODO Auto-generated method stub
//		for (BlockStmtContext st : ifStatements) {
//			ParserRuleContext c = (ParserRuleContext) st.getChild(0);
//			compileStmt(c, forExits, doWhileExits);
//		}
//		if (elseIfStmts != null && elseIfStmts.isEmpty() == false || elseStatements != null) {
//			GotoStatement toEndIf = new GotoStatement(module.sourceLocation(ifBlockCondition), -2);
//			gotos.add(toEndIf);
//			result.add(toEndIf);
//		}
//
//		// elseif branch
//		if (elseIfStmts != null) {
//			int i = 0;
//			for (IfElseIfBlockStmtContext elseIf : elseIfStmts) {
//				ifNotGoto.setNextStatement(result.size());
//
//				vsd = compiler.compileValueStatement(elseIf.ifConditionStmt().valueStmt(), method, opcodes);
//				shouldBeCondition(vsd.getVarType());
//				condition = vsd.getStatement();
//				ifNotGoto = new IfNotGotoStatement(module.sourceLocation(elseIf.ELSEIF()), condition);
//				result.add(ifNotGoto);
//				gotos.add(ifNotGoto);
//
//				for (BlockStmtContext st : elseIf.block().blockStmt()) {
//					ParserRuleContext c = (ParserRuleContext) st.getChild(0);
//					compileStmt(c, forExits, doWhileExits);
//				}
//
//				i++;
//				if (i < elseIfStmts.size() || elseStatements != null) {
//					GotoStatement toEndIf = new GotoStatement(module.sourceLocation(elseIf.ELSEIF()), -2);
//					gotos.add(toEndIf);
//					result.add(toEndIf);
//				}
//			}
//		}
//
//		// else branch
//		if (!isStatementsEmpty(elseStatements)) {
//			ifNotGoto.setNextStatement(result.size());
//
//			for (BlockStmtContext st : elseStatements) {
//				ParserRuleContext c = (ParserRuleContext) st.getChild(0);
//				compileStmt(c, forExits, doWhileExits);
//			}
//
//		} else {
//			ifNotGoto.setNextStatement(result.size());
//		}
//
//		for (GotoStatement gotoStmt : gotos) {
//			if (gotoStmt.getNextStatement() == -2) {
//				gotoStmt.setNextStatement(result.size());
//			}
//		}
	}

	private boolean isStatementsEmpty(List<BlockStmtContext> statements) {
		return statements == null || statements.isEmpty() || (statements.size() == 1 && statements.get(0) == null);
	}

}
