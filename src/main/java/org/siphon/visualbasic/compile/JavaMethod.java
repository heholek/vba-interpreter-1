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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ClassUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.siphon.visualbasic.ArgumentException;
import org.siphon.visualbasic.ArgumentMode;
import org.siphon.visualbasic.FormModuleDecl;
import org.siphon.visualbasic.Library;
import org.siphon.visualbasic.MethodDecl;
import org.siphon.visualbasic.MethodType;
import org.siphon.visualbasic.ModuleDecl;
import org.siphon.visualbasic.VarDecl;
import org.siphon.visualbasic.VbDecl;
import org.siphon.visualbasic.Visibility;
import org.siphon.visualbasic.interpreter.Argument;
import org.siphon.visualbasic.interpreter.Interpreter;
import org.siphon.visualbasic.runtime.ArgumentDecl;
import org.siphon.visualbasic.runtime.CallFrame;
import org.siphon.visualbasic.runtime.JavaModuleInstance;
import org.siphon.visualbasic.runtime.ModuleInstance;
import org.siphon.visualbasic.runtime.Value;
import org.siphon.visualbasic.runtime.VbRuntimeException;
import org.siphon.visualbasic.runtime.VbValue;
import org.siphon.visualbasic.runtime.VbVarType;
import org.siphon.visualbasic.runtime.VbVariable;
import org.siphon.visualbasic.runtime.framework.VbMethod;
import org.siphon.visualbasic.runtime.framework.VbParam;

import com.sun.jdi.InvalidTypeException;

public class JavaMethod extends MethodDecl {

	private static final Logger logger = LogManager.getLogger(JavaMethod.class);
	public final Method javaMethod;
	private boolean withInterpreter;
	public Object object = null;

	public boolean isWithInterpreter() {
		return withInterpreter;
	}

	public JavaMethod(Library library, ModuleDecl module, Method method, boolean withInterpreter) {
		super(library, module, method.getReturnType() == Void.class ? MethodType.Sub : MethodType.Function);

		this.withInterpreter = withInterpreter;
		this.javaMethod = method;
		this.name = method.getName();
		this.visibility = Visibility.PUBLIC;

		Parameter[] params = method.getParameters();
		if(withInterpreter) {
			Parameter[] newParams = new Parameter[params.length - 2];
			for(int i=2; i< params.length; i++) {
				newParams[i-2] = params[i];
			}
			params = newParams;
		}
		if (params.length > 0) {
			this.arguments = new ArrayList<>();
			
			for (Parameter param : params) {
				VbParam[] vbParams = param.getAnnotationsByType(VbParam.class);
				VbParam vbParam = null;
				if(vbParams.length > 0) vbParam = vbParams[0];
				
				ArgumentDecl arg = new ArgumentDecl();
				arg.methodDecl = this;
				if(vbParam != null){
					arg.setName(vbParam.name());
					arg.mode = vbParam.mode();
					arg.isParamArray = vbParam.paramArray();
					arg.optional = vbParam.optional();
					arg.varType =  new VbVarType(vbParam.type());	//TODO 如果没有赋值，应当取Java类型转换的类型
					switch(arg.varType.vbType){
					case vbInteger:
						arg.defaultValue = VbValue.fromJava(arg.varType.vbType, vbParam.defaultInt());
						break;
					case vbLong:
						arg.defaultValue = VbValue.fromJava(arg.varType.vbType, vbParam.defaultLong());
						break;
					case vbString:
						arg.defaultValue = VbValue.fromJava(arg.varType.vbType, vbParam.defaultString());
						break;
					case vbDouble:
					case vbDecimal:
					case vbCurrency:
						arg.defaultValue = VbValue.fromJava(arg.varType.vbType, vbParam.defaultDouble());
						break;
					case vbBoolean:
						arg.defaultValue = VbValue.fromJava(VbVarType.TypeEnum.vbBoolean, (Object)vbParam.defaultBoolean());
						break;
					case vbSingle:
						arg.defaultValue = VbValue.fromJava(arg.varType.vbType, vbParam.defaultFloat());
						break;
					case vbVariant:
						arg.defaultValue = VbValue.Missing;		// TODO Variant 也应取到默认值
						break;
					default:
						throw new UnsupportedOperationException("TODO");	//TODO 其它类型 Date
					}
				} else{
					arg.setName(param.getName());
					arg.mode = ArgumentMode.ByVal;
					arg.isParamArray = param.isVarArgs();
					arg.varType = VbVarType.javaTypeToVb(param.getType());
				}

				this.arguments.add(arg);
			}
		}
		
		this.returnType = VbVarType.javaTypeToVb(method.getReturnType());
	}

	public JavaMethod(Library lib, ModuleDecl module, Method method, boolean isProperty, boolean withInterpreter) {
		this(lib, module, method, withInterpreter);
		if(isProperty){
			if(method.getName().startsWith("get")){
				this.methodType = MethodType.PropertyGet;
			} else {
				if(this.arguments.get(this.arguments.size() -1).varType.vbType == VbVarType.TypeEnum.vbObject){ 
					this.methodType = MethodType.PropertySet;
				} else {
					this.methodType = MethodType.PropertyLet;
				}
			}
			this.name = method.getName().substring(3);
		}
	}

	public JavaMethod(Library library, JavaModuleDecl module, MethodDecl methodDecl, Method javaMethod, boolean withInterpreter) {
		super(library, module, methodDecl.methodType);
		this.javaMethod = javaMethod;
		this.setName(methodDecl.getName());
		this.visibility = Visibility.PUBLIC;
		this.withInterpreter = withInterpreter; 
		this.returnType = methodDecl.returnType;
		for(ArgumentDecl arg : methodDecl.arguments){
			this.arguments.add(arg);
		}
	}

	public JavaMethod(Library library, JavaClassModuleDecl module, MethodDecl methodDecl, Method javaMethod, boolean withInterpreter) {
		super(library, module, methodDecl.methodType);
		this.javaMethod = javaMethod;
		this.setName(methodDecl.getName());
		this.setVisibility(Visibility.PUBLIC);
		this.withInterpreter = withInterpreter;
		this.returnType = methodDecl.returnType;
		for(ArgumentDecl arg : methodDecl.arguments){
			this.arguments.add(arg);
		}
	}
	
	@Override
	public void invoke(Interpreter interpreter, List<Argument> args) {
		List<VbValue> invokeArgs = new ArrayList<>();
		logger.debug("==== Entering Java {} ====", getName());
		if (getArguments() != null) {
			List<Value> argValues = bindArguments(interpreter, args);
			for (int i = 0; i < argValues.size(); i++) {
				VbValue argValue = argValues.get(i).get(interpreter);
				invokeArgs.add(argValue);
			}
		}
		else {
			throw new UnsupportedOperationException("TODO: Implement argument handling without argument specification");
		}
		
		try {
			Object result = javaMethod.invoke(this.object,
					toJavaArguments(invokeArgs, javaMethod.getParameterTypes()));
			if (methodType == MethodType.Function || methodType == MethodType.PropertyGet) {
				if (result instanceof VbValue) {
					interpreter.push((VbValue) result); 
				}
				else if (result == null) {
					interpreter.push(VbValue.Null);
				}
				else {
					interpreter.push(VbValue.fromJava(result));
				}
			}
		}
		catch (InvocationTargetException ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}
		catch (IllegalAccessException ex) {
			ex.printStackTrace();
		}
		
		logger.debug("==== Exiting Java {} ====", getName());
	}

	private Object[] toJavaArguments(List<VbValue> arguments, Class<?>[] paramTypes) {
		Object[] result = new Object[paramTypes.length];
		for (int i = 0; i < arguments.size(); i++) {
			Class<?> paramType = paramTypes[i];
			VbValue argCall = arguments.get(i);

			if (paramType == VbValue.class) {
				result[i] = argCall;
			} else {
				result[i] = VbValue.vbValueToJava(argCall);
			}
			
			if (result[i] != null && !ClassUtils.isAssignable(result[i].getClass(), paramType, true) && !paramType.equals(VbValue.class)) {
				throw new RuntimeException("Provided argument type " + result[i].getClass().getSimpleName() + " differs from expected argument type " + paramType.getSimpleName());
			}
		}		
		return result;
	}

}
