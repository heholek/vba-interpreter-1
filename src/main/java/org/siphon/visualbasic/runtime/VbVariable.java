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
package org.siphon.visualbasic.runtime;

import java.util.List;

import org.siphon.visualbasic.ArgumentMode;
import org.siphon.visualbasic.ClassModuleDecl;
import org.siphon.visualbasic.ClassTypeDecl;
import org.siphon.visualbasic.Interpreter;
import org.siphon.visualbasic.MethodDecl;
import org.siphon.visualbasic.OverflowException;
import org.siphon.visualbasic.SourceLocation;
import org.siphon.visualbasic.VarDecl;
import org.siphon.visualbasic.VbDecl;
import org.siphon.visualbasic.runtime.framework.Enums.VbCallType;

public class VbVariable extends Value {

	public VbVarType varType;		// 即 varDecl.varType
	
	public VbValue value;
	
	public VarDecl varDecl;

	private boolean readonly = false;
	
	public boolean isReadonly() {
		return readonly;
	}

	public VbVariable setReadonly(boolean readonly) {
		this.readonly = readonly;
		return this;
	}

	public static final VbVariable ME = new VbVariable((VarDecl) null, null).setReadonly(true);
	
	public VbVariable(VarDecl varDecl, VbValue value) {
		this.varDecl = varDecl;
		if(varDecl != null) this.varType = varDecl.varType;
		this.value = value;
	}
	
	public VbVariable(VbVarType type, VbValue value) {
		this.varType = type;
		this.value = value;
	}
	
	@Override
	public String toString() {
		return String.format("(VAR %s type:%s value: %s)", varDecl.getName(), varType.vbType, value);
	}

	public void assign(VbValue value, Interpreter interpreter, CallFrame frame, SourceLocation sourceLocation) throws VbRuntimeException {
		if(this.varType.vbType == VbVarType.TypeEnum.vbObject && value != null && !value.isObject()) {
			VbDecl defaultMember = this.varType.getDefaultMember(VbCallType.VbLet);
			if(defaultMember != null) {
				MethodDecl let = (MethodDecl) defaultMember;
				interpreter.callMethod((ModuleInstance) this.value.value, (List<Statement>) null, let, value);
				return;
			}
		}
		
		if(this.isReadonly()){
			throw new VbRuntimeException(VbRuntimeException.OBJECT_DOES_NOT_SUPPORT_THIS_ACTION, sourceLocation);
		}
		
		if(value.isObject()){
			if(this.varType.vbType != VbVarType.TypeEnum.vbObject && this.varType.vbType != VbVarType.TypeEnum.vbVariant){
				throw new VbRuntimeException(VbRuntimeException.TYPE_MISMATCH, sourceLocation);
			} 
			if(this.varDecl.withEvents){
				removeEventHandlers(this.value, interpreter, frame);
				
				this.setObjectValue(value, sourceLocation);
				
				((ModuleInstance) this.value.value).bindEventHandlers(this.varDecl, frame.module, interpreter, frame);
			} else {
				this.setObjectValue(value, sourceLocation);
			}
		} else {	// not object type
			if(this.varType.vbType == VbVarType.TypeEnum.vbObject){
				throw new VbRuntimeException(VbRuntimeException.TYPE_MISMATCH, sourceLocation);
			}
			try {
				if(this.varDecl instanceof ArgumentDecl){
					ArgumentDecl argDecl = (ArgumentDecl) this.varDecl;
					if(argDecl.mode == ArgumentMode.ByRef){	// TODO ByRef 不一定是这样实现的
						VbValue val = VbValue.cast(value, this.varType.vbType);
//						if(value.varType.vbType != this.varType.vbType){
//							throw new VbRuntimeException(VbRuntimeException.TYPE_MISMATCH, sourceLocation);
//						}
						this.value = val;
					} else {
						if(value.isObject() && this.varType.isBaseType()){
							value = interpreter.evalDefaultMember(value, frame, sourceLocation);
						}
						this.value = VbValue.cast(value, this.varType.vbType);
					}
				} else {
					if(value.isObject() && this.varType.isBaseType()){
						value = interpreter.evalDefaultMember(value, frame, sourceLocation);
					}
					this.value = VbValue.cast(value, this.varType.vbType);
				}
			} catch (OverflowException e) {
				throw new VbRuntimeException(VbRuntimeException.OVERFLOW, sourceLocation);
			} catch (ClassCastException e) {
				throw new VbRuntimeException(VbRuntimeException.TYPE_MISMATCH, sourceLocation);
			} catch(NullValueException e){
				throw new VbRuntimeException(VbRuntimeException.NULL_USE_IS_INVALID, sourceLocation);
			}
		}
	}
	
	private void setObjectValue(VbValue value, SourceLocation sourceLocation) throws VbRuntimeException {
		if(value.varType.vbType == VbVarType.TypeEnum.vbVariant){
			value = (VbValue) value.value;
		}
		if(this.varType.vbType == VbVarType.TypeEnum.vbVariant){
			this.value = value;
			return;
		}
		
		ClassModuleDecl myClass = this.varType.getClassModuleDecl();
		if(myClass == null){		// dim as Object | IUnknown
			this.value = value;
			return;
		}
		
		ModuleInstance inst = (ModuleInstance) value.value;
		ClassModuleDecl instCls = (ClassModuleDecl) inst.getModuleDecl();
		ClassModuleDecl currCls = value.varType.getClassModuleDecl();
		if(instCls == myClass){
			if(currCls == myClass){
				this.value = value;
			} else {
				VbVarType t = new VbVarType(VbVarType.TypeEnum.vbObject, new ClassTypeDecl(instCls.getLibrary(), instCls), null, null);
				this.value = new VbValue(t, inst);
			}
		} else if(instCls.isImplementFrom(myClass)){
			VbVarType t = new VbVarType(VbVarType.TypeEnum.vbObject, instCls.getImplementorTypeDecl(myClass), null, null);	// 转为为能实现本类型的值
			this.value = new VbValue(t, inst);
		} else {
			throw new VbRuntimeException(VbRuntimeException.TYPE_MISMATCH, sourceLocation);
		}
	}

	private void removeEventHandlers(VbValue value, Interpreter interpreter, CallFrame frame) {
		if(value == null || value.varType.vbType != VbVarType.TypeEnum.vbObject || value.value == null) return;
		
		ModuleInstance instance = (ModuleInstance) value.value;
		instance.removeEventListener(frame.module);
	}

	@Override
	public VbValue get(org.siphon.visualbasic.interpreter.Interpreter interpreter) {
		return this.value;
	}

	@Override
	public void set(org.siphon.visualbasic.interpreter.Interpreter interpreter, VbValue value) {
		this.value = value;
	}

}
