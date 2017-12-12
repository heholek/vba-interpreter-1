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

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.siphon.visualbasic.ArgumentException;
import org.siphon.visualbasic.ClassModuleDecl;
import org.siphon.visualbasic.ClassTypeDecl;
import org.siphon.visualbasic.Interpreter;
import org.siphon.visualbasic.ModuleMemberDecl;
import org.siphon.visualbasic.NumberRange;
import org.siphon.visualbasic.OverflowException;
import org.siphon.visualbasic.PropertyDecl;
import org.siphon.visualbasic.SourceLocation;
import org.siphon.visualbasic.VarDecl;
import org.siphon.visualbasic.VbTypeDecl;
import org.siphon.visualbasic.compile.ImpossibleException;
import org.siphon.visualbasic.compile.JavaClassModuleDecl;
import org.siphon.visualbasic.compile.JavaMethod;
import org.siphon.visualbasic.compile.JavaModuleDecl;
import org.siphon.visualbasic.interpreter.Argument;
import org.siphon.visualbasic.runtime.VbVarType.TypeEnum;
import org.siphon.visualbasic.runtime.framework.vb.HeadlessTextBox;

import thirdparty.org.apache.poi.poifs.macros.VBAObject;

import org.apache.bcel.classfile.JavaClass;

public class VbValue extends Value {

	public final static VbValue Empty = new VbValue(VbVarType.VbVariant, new VbValue(VbVarType.VbEmpty, null));

	public final static VbValue Nothing = new VbValue(new VbVarType(VbVarType.TypeEnum.vbObject), null);
	
	public final static VbValue Null = new VbValue(new VbVarType(VbVarType.TypeEnum.vbVariant), new VbValue(VbVarType.VbNull, null));
	
	public final static VbValue Missing = new VbValue(new VbVarType(VbVarType.TypeEnum.vbVariant), new VbValue(VbVarType.VbError, 448));

	public static final VbValue TRUE = new VbBoolean(true);
	
	public static final VbValue FALSE = new VbBoolean(false);

	public final VbVarType varType;

	public Object value;

	public VbValue(VbVarType varType, Object value) {
		super();
		assert value instanceof VbValue == false || varType.vbType == VbVarType.TypeEnum.vbVariant;
		this.varType = varType;
		this.value = value;
		
		VbVarType.TypeEnum vbType = varType.vbType;
		if(vbType.getNumVal() > 0 && vbType.getNumVal() < VbVarType.NumberRange.length){
			NumberRange range = VbVarType.NumberRange[vbType.getNumVal()];
			if(range != null){
				range.checkRange((Number) value);
			}
		}
	}
	
	public VbValue cast(VbVarType.TypeEnum varType) throws OverflowException {
		return VbValue.cast(this, varType);
	}

	public static VbValue cast(VbValue value, VbVarType.TypeEnum varType) throws OverflowException {
		if (value.varType.vbType == varType)
			return value;
		
		switch (varType) {
		case vbInteger:
			return CInt(value);
		case vbString:
			return CStr(value);
		case vbLong:
			return CLng(value);
		case vbSingle:
			return CSng(value);
		case vbDouble:
			return CDbl(value);
		case vbDate:
			return CDate(value);
		case vbBoolean:
			return CBool(value);
		case vbByte:
			return CByte(value);
		case vbDecimal:
			return CDec(value);
		case vbCurrency:
			return CCur(value);
		case vbVariant:
			return value;
		default:
			throw new ClassCastException(String.format("%s cannot cast to %s", value, varType));
		}
	}

	public static VbValue CSng(VbValue value) {
		if(value.isNull()){
			throw new NullValueException();
		}
		if (value.varType.vbType == VbVarType.TypeEnum.vbSingle)
			return value;

		VbValue v = CDbl(value);
		return new VbValue(VbVarType.VbSingle, ((Number) v.value).floatValue());
	}

	public static VbValue CStr(VbValue value) {
		/*
		 * Boolean 含有 True或 False 的字符串 
			Date 含有系统中短日期格式日期的字符串 
			Null 一个运行时错误 
			Empty 一个零长度字符串 ("") 
			Error 包含单词 Error 以及错误号的字符串 			实际运行得到的是错误号
			其他数值 含有数值的字符串 
		 */
		if(value.isNull()){
			throw new NullValueException();
		}
		
		if(value.varType.vbType == VbVarType.TypeEnum.vbVariant){
			value = (VbValue) value.value;
		}
		switch (value.varType.vbType) {
		case vbString:
			return value;
		case vbBoolean:
			return new VbString((Integer) value.value == 0 ? "False" : "True");
		case vbDate:
			double d = (Double)value.value;
			if(d >=0 && d < 1){	// time part only
				DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM);
				Date date = doubleToDate((Double)value.value);
				LocalTime ld = date.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
				return new VbString(ld.format(formatter));
			} else {
				DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM);
				Date date = doubleToDate((Double)value.value);
				LocalDateTime ld = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
				return new VbString(ld.format(formatter));
			}
		case vbEmpty:
			return new VbString("");
		
		case vbNull:
			return new VbString("Null");

		case vbError:
			return new VbString("Error " + value.value);

		default:
			if (value.value instanceof Number) {
				if(value.varType.vbType == VbVarType.TypeEnum.vbCurrency){
					return new VbString(String.valueOf(currencyToDouble((Long)value.value)));
				} else {
					Number n = (Number) value.value;
					String s;
					if(n.longValue() == n.doubleValue()) {
						s = String.valueOf(n.longValue());
					} else {
						s = String.valueOf(n);
					}
					return new VbString(s);
				}
			}
			throw new ClassCastException("cannot cast to string");
		}
	}

	public static VbValue CByte(VbValue value) throws OverflowException {
		if(value.isNull()){
			throw new NullValueException();
		}
		if(value.varType.vbType == VbVarType.TypeEnum.vbVariant){
			value = (VbValue) value.value;
		}
		if(value.varType.vbType == VbVarType.TypeEnum.vbByte){
			return value;
		}
		Integer i = null;
		if(value.value instanceof Integer){
			i = (Integer) value.value;
		} else {
			VbValue v = CInt(value);
			i = (Integer) v.value;
		}
		if (i < 0 || i > 255) {
			throw new OverflowException();
		} else {
			i = i & 0xff;
			return new VbValue(VbVarType.VbByte, i);
		}
	}

	public static VbValue CBool(VbValue value) {
		if(value.isNull()){
			throw new NullValueException();
		}
		if (value.varType.vbType == VbVarType.TypeEnum.vbBoolean) {
			return value;
		}

		Object v = value.value;
		if (v instanceof Number) {		// include date
			return new VbBoolean(((Number) v).intValue() != 0);
		} else if (value.varType.vbType == VbVarType.TypeEnum.vbString) {
			String s = (String) v;
			if ("TRUE".equalsIgnoreCase(s)) {
				return new VbBoolean(true);
			} else if ("FALSE".equalsIgnoreCase(s)) {
				return new VbBoolean(false);
			} else {
				value = CDbl(value);
				return CBool(value);
			}
		} else if (value.varType.vbType == VbVarType.TypeEnum.vbEmpty) {
			return FALSE;
		} else {
			throw new ClassCastException("cannot cast to boolean");
		}
	}

	public static VbValue CDate(VbValue value) {
		if(value.isNull()){
			throw new NullValueException();
		}
		if (value.varType.vbType == VbVarType.TypeEnum.vbVariant) {
			value = (VbValue) value.value;
		}
		
		Object v = value.value;
		if (v instanceof Number) {
			if(value.varType.vbType == VbVarType.TypeEnum.vbCurrency){
				v = currencyToDouble((Long)v);
			}
			return new VbValue(VbVarType.VbDate, ((Number) v).doubleValue());
		} else if (value.varType.vbType == VbVarType.TypeEnum.vbString) {
			try {
				String s = (String) v;
				Date date = parseDate(s);
				return new VbValue(VbVarType.VbDate, dateToDouble(date));
			} catch (Exception e) {
				value = CDbl(value);
				return CDate(value);
			}
		} else if (value.varType.vbType == VbVarType.TypeEnum.vbEmpty) {
			return VbVarType.VbDate.crateDefaultValue();
		} else {
			value = CDbl(value);
			return CDate(value);
		}
	}

	public static Date parseDate(String s) {
		FormatStyle[] styles = new FormatStyle[] { FormatStyle.SHORT, FormatStyle.MEDIUM, FormatStyle.LONG, FormatStyle.FULL };
		for (int i = 0; i < styles.length; i++) {
			try {
				DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(styles[i]);
				LocalDate lcd = LocalDate.parse(s, formatter);
				return Date.from(lcd.atStartOfDay(ZoneId.systemDefault()).toInstant());
			} catch (Exception ex) {

			}
		}
		SimpleDateFormat[] formats = new SimpleDateFormat[] { new SimpleDateFormat("HH:mm:ss") };
		for (int i = 0; i < formats.length; i++) {
			try {
				return formats[i].parse(s);
			} catch (Exception e) {
			}
		}
		throw new ClassCastException("cannot cast to date");
	}

	public static Date doubleToDate(double value) {
		long l = (long) (value * 86400000) + VbVarType.DATE_OFFSET;
		return new Date(l);
	}

	public static VbValue CDbl(VbValue value) {
		if(value.isNull()){
			throw new NullValueException();
		}
		if(value.varType.vbType == VbVarType.TypeEnum.vbVariant){
			value = (VbValue) value.value;
		}
		if (value.value instanceof Number) {	// include date
			Number n = (Number) value.value;
			if(value.varType.vbType == VbVarType.TypeEnum.vbCurrency){
				n = currencyToDouble((Long)value.value);
				return new VbValue(VbVarType.VbDouble, n);
			} else {
				return new VbValue(VbVarType.VbDouble, n.doubleValue());
			}
		} else if (value.varType.vbType == VbVarType.TypeEnum.vbString) {
			String s = (String) value.value;
			String j = s.replace("&H", "0x").replace("&O", "0");
			return new VbValue(VbVarType.VbDouble, Double.parseDouble(j));

		} else if (value.varType.vbType == VbVarType.TypeEnum.vbEmpty) {
			return new VbValue(VbVarType.VbDouble, 0.0);
		} else {
			throw new ClassCastException("cannot cast to double");
		}
	}

	public static VbValue CDec(VbValue value) {
		if(value.isNull()){
			throw new NullValueException();
		}
		if(value.varType.vbType == VbVarType.TypeEnum.vbVariant){
			value = (VbValue) value.value;
		}
		if (value.value instanceof Number) {
			if (value.value instanceof BigDecimal) {
				return new VbValue(VbVarType.VbDecimal, value.value);
			} else if(value.varType.vbType == VbVarType.TypeEnum.vbCurrency){
				double r = currencyToDouble((Long)value.value);
				return new VbValue(VbVarType.VbDecimal, new BigDecimal(r));
			} else {
				double r = ((Number) (value.value)).doubleValue();
				return new VbValue(VbVarType.VbDecimal, new BigDecimal(r));
			}
		} else if (value.varType.vbType == VbVarType.TypeEnum.vbString) {
			String s = (String) value.value;
			String j = s.replace("&H", "0x").replace("&O", "0");
			return new VbValue(VbVarType.VbDecimal, new BigDecimal(j));

		} else {
			value = CDbl(value);
			return CDec(value);
		}
	}

	public static VbValue CCur(VbValue value) {
		if(value.isNull()){
			throw new NullValueException();
		}
		if(value.varType.vbType == VbVarType.TypeEnum.vbVariant){
			value = (VbValue) value.value;
		}
		if(value.varType.vbType == VbVarType.TypeEnum.vbCurrency){
			return value;
		}
		if (value.value instanceof Number) {
			if (value.value instanceof Double) {
				return new VbValue(VbVarType.VbCurrency, doubleToCurrency((Double) value.value));
			} else if(value.value instanceof Integer){
				return new VbValue(VbVarType.VbCurrency, (Integer)(value.value) * 10000L);
			} else if(value.value instanceof Long){
				return new VbValue(VbVarType.VbCurrency, (Long)(value.value) * 10000L);
			} else {
				double r = ((Number) (value.value)).doubleValue();
				return new VbValue(VbVarType.VbCurrency, doubleToCurrency(r));
			}
		} else if (value.varType.vbType == VbVarType.TypeEnum.vbString) {
			String s = (String) value.value;
			String j = s.replace("&H", "0x").replace("&O", "0");
			return new VbValue(VbVarType.VbCurrency, doubleToCurrency(Double.parseDouble(j) * 1000));
		} else {
			value = CDbl(value);
			return CCur(value);
		}
	}

	public static long doubleToCurrency(double value) {
		return (long)(value * 10000);
	}
	
	public static double currencyToDouble(long value) {
		return value / 10000.0;
	}

	public static VbValue CLng(VbValue value) {
		if(value.isNull()){
			throw new NullValueException();
		}
		if(value.varType.vbType == VbVarType.TypeEnum.vbVariant){
			value = (VbValue) value.value;
		}
		if (value.varType.vbType == VbVarType.TypeEnum.vbString) {
			value = CDbl(value);
		}
		if (value.value instanceof Number) {	// include date, boolean
			Number n = (Number) value.value;
			if (n instanceof Long) {
				if(value.varType.vbType == VbVarType.TypeEnum.vbCurrency){
					return new VbLong((Long)n / 10000);
				} else {
					return new VbLong((long) n);
				}
			} else if (n instanceof Integer) {
				return new VbLong(n.longValue());
			} else {
				double v = n.doubleValue();
				double remain = Math.abs(v % 1);
				long i = (long) v;
				// 当小数部分恰好为 0.5 时，Cint 和 CLng 函数会将它转换为最接近的偶数值。例如，0.5 转换为 0、1.5 转换为 2。Cint 和 CLng 函数不同于 Fix 和 Int 函数，Fix 和 Int 函数会将小数部分截断而不是四舍五入。并且 Fix 和 Int 函数总是返回与传入的数据类型相同的值。
				if (remain == 0.5) {
					if (Math.abs(i % 2) == 1) {
						v += Math.signum(v) * 0.1;
						i = (long) Math.round(v);
					}
				}
				return new VbLong(i);
			}
		} else if (value.varType.vbType == VbVarType.TypeEnum.vbEmpty) {
			return VbLong.ZERO;
		} else {
			throw new ClassCastException("cannot cast to long");
		}
	}

	public static VbValue CInt(VbValue value) {
		if(value.isNull()){
			throw new NullValueException();
		}
		if(value.varType.vbType == VbVarType.TypeEnum.vbVariant){
			value = (VbValue) value.value;
		}
		if (value.varType.vbType == VbVarType.TypeEnum.vbString) {
			value = CDbl(value);
		}
		if (value.value instanceof Number) {	// include date, boolean
			Number n = (Number) value.value;
			if (n instanceof Integer) {
				return new VbInteger((Integer) n);
			} else if (n instanceof Long) {
				if(value.varType.vbType == VbVarType.TypeEnum.vbCurrency){
					return new VbInteger((int)((Long)n / 10000));
				} else {
					return new VbInteger(n.intValue());
				}
			} else {
				double v = n.doubleValue();
				double remain = Math.abs(v % 1);
				int i = (int) v;
				// 当小数部分恰好为 0.5 时，Cint 和 CLng 函数会将它转换为最接近的偶数值。例如，0.5 转换为 0、1.5 转换为 2。Cint 和 CLng 函数不同于 Fix 和 Int 函数，Fix 和 Int 函数会将小数部分截断而不是四舍五入。并且 Fix 和 Int 函数总是返回与传入的数据类型相同的值。
				if (remain == 0.5) {
					if (Math.abs(i % 2) == 1) {
						v += Math.signum(v) * 0.1;
						i = (int) Math.round(v);
					}
				}
				return new VbInteger(i);
			}
		} else if (value.varType.vbType == VbVarType.TypeEnum.vbEmpty) {
			return new VbInteger(0);
		} else {
			throw new ClassCastException("cannot cast to int");
		}
	}

	static double dateToDouble(Date date) {
		double v = (date.getTime() - VbVarType.DATE_OFFSET) / 86400000.0;
		return v;
	}

	public static VbValue fromJava(TypeEnum vbType, Object object) throws OverflowException {
		VbValue old = fromJava(object);
		if (old.varType.vbType == vbType)
			return old;
		return cast(old, vbType);
	}

	@Override
	public String toString() {
		if(this.varType.vbType == VbVarType.TypeEnum.vbVariant){
			VbValue v = (VbValue) this.value;
			return String.format("(VbValue Variant/%s %s)", v.varType, v.value);
		} else if(this.varType.vbType == VbVarType.TypeEnum.vbDate){
			return String.format("(VbValue %s %s)", this.varType, doubleToDate((Double)this.value));
		} else {
			return String.format("(VbValue %s %s)", this.varType, this.value);
		} 
	}
	public static VbValue fromJava(Object obj) {
		return fromJava(obj, true, null);
	}
	
	public static VbValue fromJava(Object object, boolean autoCreateJavaModuleDecl) {
		return fromJava(object, autoCreateJavaModuleDecl, null);
	}
	
	public static VbValue fromJava(Object object, VbVarType suggest) {
		return fromJava(object, false, suggest);
	}
	
	public static VbValue fromJava(Object obj, boolean autoCreateJavaModuleDecl, VbVarType suggest) {
		if (obj instanceof VbValue)
			return (VbValue) obj;

		if (obj == null) {
			return null;
		}
		
		if(obj instanceof VbBoundObject) {
			JavaModuleInstance instance = ((VbBoundObject) obj).getVbModuleInstance();
			if(instance != null) {
				if(suggest != null) {
					VbValue val = new VbValue(suggest, instance);
					return val;
				} else {
					return instance.asVbValue();
				}
			}
		}
		
		if (obj instanceof Integer) {
			return new VbInteger((Integer) obj);
		} else if (obj instanceof Long) {
			return new VbLong((Long) obj);
		} else if (obj instanceof Float) {
			return new VbValue(VbVarType.VbSingle, obj);
		} else if (obj instanceof Double) {
			return new VbValue(VbVarType.VbDouble, obj);
		} else if (obj instanceof BigDecimal) {
			return new VbValue(VbVarType.VbDecimal, obj);
		} else if (obj instanceof Date) {
			return new VbValue(VbVarType.VbDate, dateToDouble((Date) obj));
		} else if (obj instanceof Byte) {
			return new VbValue(VbVarType.VbByte, Byte.toUnsignedInt((Byte) obj));
		} else if (obj instanceof String) {
			return new VbString((String) obj);
		} else if (obj instanceof Boolean) {
			return new VbBoolean((Boolean)obj);

		} else {
			if(suggest != null 
					&& suggest.getClassModuleDecl() instanceof JavaClassModuleDecl 
					&& ((JavaClassModuleDecl)suggest.getClassModuleDecl()).getJavaClass().isAssignableFrom(obj.getClass())){
				
				JavaModuleInstance instance = new JavaModuleInstance(suggest.getClassModuleDecl(), obj);
				VbValue val = new VbValue(suggest, instance);
				return val;
			} else if(autoCreateJavaModuleDecl){
				JavaClassModuleDecl decl = new JavaClassModuleDecl(null, null, obj.getClass());
				ClassTypeDecl ct = new ClassTypeDecl(null, decl);
				VbVarType vt = new VbVarType(VbVarType.TypeEnum.vbObject, ct, null, obj.getClass());
				JavaModuleInstance instance = new JavaModuleInstance(decl, obj);
				VbValue val = new VbValue(vt, instance);
				return val;
			} else {
				return new VbValue(new VbVarType(VbVarType.TypeEnum.vbObject, ClassTypeDecl.JAVA_OBJECT_TYPE, null, obj.getClass()), obj);
			}
		}
	}

	public static boolean isTrue(VbValue value) {
		if(value.isNull()) return false;
		
		Object v = value.getExactValue();
		if(v instanceof Number){
			Number n = (Number) v;
			return MathExpr.eqZero(n) == false;
		} else {
			return isTrue(CBool(value));
		}
	}

	public Object toJava() {
		switch (this.varType.vbType) {
		case vbInteger:
		case vbLong:
		case vbString:
		case vbDouble:
		case vbSingle:
		case vbByte:
		case vbDecimal:
			return this.value;
		case vbCurrency:
			return currencyToDouble((Long)this.value);
		case vbBoolean:
			return (Integer)this.value != 0;
		case vbDate:
			return doubleToDate(((Double)this.value));
		case vbArray:
			VbArray arr = (VbArray) this;
			List<VbValue> ls = arr.toList();
			List<Object> ls2 = new ArrayList<Object>();
			for(VbValue obj : ls){
				ls2.add(obj.toJava());		// 无法获取泛型容器的元素类型
			}
			return ls2;
		case vbUserDefinedType:
			throw new UnsupportedOperationException();		
		case vbObject:
			if(this.varType.typeDecl == null) return null;
			if(this.value == null){
				return null;
			} 
			if(this.varType.typeDecl == ClassTypeDecl.JAVA_OBJECT_TYPE){
				return ((JavaModuleInstance)this.value).getInstance();
			} else if(this.varType.getClassTypeDecl().classModule instanceof JavaClassModuleDecl){
				return ((JavaModuleInstance)this.value).getInstance();
			} else {
				return this;
			}
		case vbNull:
		case vbEmpty:
		case vbError:
		case vbDataObject:
			return this.value;
		case vbVariant:
			VbValue v = (VbValue) this.value;
			return v.toJava();
		}
		throw new ImpossibleException();
	}

	public static Object vbValueToJava(Object object) {
		if (object instanceof VbValue) {
			return ((VbValue) object).toJava();
		} else {
			return object;
		}
	}

	public ModuleInstance ensureInstanceInited(Interpreter interpreter, CallFrame frame, SourceLocation sourceLocation)
			throws VbRuntimeException {
		ModuleInstance instance = (ModuleInstance) this.value;
		if (instance == ModuleInstance.WAIT_NEW) {
			ClassTypeDecl classDecl = (ClassTypeDecl) this.varType.typeDecl;
			if(classDecl.classModule instanceof JavaClassModuleDecl){
				JavaClassModuleDecl jcmd = (JavaClassModuleDecl) classDecl.classModule;
				this.value = (instance = new JavaModuleInstance(jcmd, jcmd.newInstance(interpreter, frame, sourceLocation)));
			} else {
				this.value = (instance = new ModuleInstance(classDecl.classModule));
				instance.initializeClass(interpreter, frame);
			}
			return instance;
		} else if (instance == null) {
			throw new VbRuntimeException(VbRuntimeException.OBJECT_VARIABLE_OR_WITH_BLOCK_VARIABLE_HAS_NOT_BEEN_SET_YET, sourceLocation);
		} else {
			return instance;
		}
	}

	public static VbValue fromJava(Object instance, VbVarType varType, JavaClassModuleDecl javaClassModuleDecl) {
		return new VbValue(varType, new JavaModuleInstance(javaClassModuleDecl, instance));
	}

	public boolean isVariant() {
		return this.varType.vbType == VbVarType.TypeEnum.vbVariant;
	}
	
	public boolean isMissing(){
		if(this.varType.vbType == VbVarType.TypeEnum.vbError && this.value.equals(448)) return true;
		
		if(this.varType.vbType == VbVarType.TypeEnum.vbVariant){
			VbValue v = ((VbValue)this.value);
			return v.varType.vbType == VbVarType.TypeEnum.vbError && v.value.equals(448);
		}
		return false;
	}

	public boolean isString() {
		if(this.varType.vbType == VbVarType.TypeEnum.vbString) return true;
		if(this.varType.vbType == VbVarType.TypeEnum.vbVariant){
			return ((VbValue)this.value).varType.vbType == VbVarType.TypeEnum.vbString;
		}
		return false;
	}

	public Object getExactValue() {
		if(this.varType.vbType == VbVarType.TypeEnum.vbVariant){
			return ((VbValue)this.value).value;
		} else {
			return this.value;
		}
	}
	
	public TypeEnum getExactVbType() {
		if(this.varType.vbType == VbVarType.TypeEnum.vbVariant){
			return ((VbValue)this.value).varType.vbType;
		} else {
			return this.varType.vbType;
		}
	}

	public boolean isEmpty() {
		return this.varType.vbType == VbVarType.TypeEnum.vbEmpty 
				|| this.varType.vbType == VbVarType.TypeEnum.vbVariant && (this.value == null || ((VbValue)this.value).varType.vbType == VbVarType.TypeEnum.vbEmpty);
	}

	public boolean isNull() {
		return this.varType.vbType == VbVarType.TypeEnum.vbNull 
				|| this.varType.vbType == VbVarType.TypeEnum.vbVariant && ((VbValue)this.value).varType.vbType == VbVarType.TypeEnum.vbNull ;
	}

	public boolean isError() {
		return this.varType.vbType == VbVarType.TypeEnum.vbError 
				|| this.varType.vbType == VbVarType.TypeEnum.vbVariant && ((VbValue)this.value).varType.vbType == VbVarType.TypeEnum.vbError;
	}

	public boolean isObject() {
		return this.varType.vbType == VbVarType.TypeEnum.vbObject 
				|| this.varType.vbType == VbVarType.TypeEnum.vbVariant && ((VbValue)this.value).varType.vbType == VbVarType.TypeEnum.vbObject;
	}

	public Object toJava(Class<?> type) {
		if(VbValue.class.isAssignableFrom(type)){
			return this;
		}
		if(CharSequence.class.isAssignableFrom(type)){
			return CStr(this).value;
		} else if(type == Object.class){
			return this.toJava();
		} 
		if(type.isArray() && this.varType.vbType != VbVarType.TypeEnum.vbArray){
			throw new ClassCastException();
		}
		
		switch(this.varType.vbType){
		case vbInteger:
		case vbByte:
		case vbLong:
		case vbDecimal:
		case vbDouble:
		case vbSingle:
		case vbBoolean:
		case vbEmpty:
			if(ClassUtils.isAssignable(type, Number.class)){
				if(type == Integer.class || type == int.class){
					return ((Number)this.value).intValue();
				} else if(type == Long.class || type == long.class){
					return ((Number)this.value).longValue();
				} else if(type == Double.class || type == double.class){
					return ((Number)this.value).doubleValue();
				} else if(type == Float.class || type == float.class){
					return ((Number)this.value).floatValue();
				} else if(type == Short.class || type == short.class){
					return ((Number)this.value).shortValue();
				} else if(type == BigDecimal.class){
					if(this.value instanceof BigDecimal){
						return this.value;
					} else {
						return new BigDecimal(((Number)this.value).doubleValue());
					}
				} 
			} else if(type == Boolean.class || type == boolean.class){
				return ((Number)this.value).intValue() != 0;
			} 
			break;
		case vbDate:
			if(type == Date.class){
				return this.toJava();
			}
		case vbCurrency:
			return (CDbl(this)).toJava(type);
		case vbString:
			if(ClassUtils.isAssignable(type, Number.class)){
				return CDbl(this).toJava(type);
			} else if(type == Boolean.class || type == boolean.class){
				return CBool(this).toJava();
			} 
		case vbNull:
			return null;
		case vbObject:
			if(this.value == null){
				return null;
			} 
			if(this.varType.typeDecl == ClassTypeDecl.JAVA_OBJECT_TYPE){
				if(type.isAssignableFrom(this.value.getClass())){
					return this.value;
				} 
			} else if(this.varType.getClassTypeDecl().classModule instanceof JavaClassModuleDecl){
				if(type.isAssignableFrom(this.varType.javaClass)){
					return this.value;
				}
			} else {
				// throw new ClassCastException();
			}
			break;
		case vbArray:
			VbArray arr = (VbArray) this;
			List<VbValue> ls = arr.toList();
			if(type.isArray()){
				Class<?> ctype = type.getComponentType();
				Object a = Array.newInstance(ctype, ls.size());
				for(int i=0; i<ls.size(); i++){
					Array.set(a, i, ls.get(i).toJava(ctype));
				}
				return a;
			} else if(Iterable.class.isAssignableFrom(type)){
				List<Object> ls2 = new ArrayList<Object>();
				for(VbValue obj : ls){
					ls2.add(obj.toJava());		// 无法获取泛型容器的元素类型
				}
				return ls2;
			} else if(type == Iterator.class){
				return ls.stream().map(v -> v.toJava()).iterator();
			}
			break;
		case vbUserDefinedType:
			// throw new ClassCastException();
		}
		throw new ClassCastException();
	}

	public int getSimilarity(Class<?> type) {
		if(this.varType.vbType == VbVarType.TypeEnum.vbObject){
			if(this.value == null) return 90;
			ClassTypeDecl decl = this.varType.getClassTypeDecl();
			if(decl == ClassTypeDecl.JAVA_OBJECT_TYPE){
				Class<? extends Object> c = this.value.getClass();
				if(c == type){
					return 100;
				} else if(c.isAssignableFrom(type)){
					return 90;
				} else {
					return -50;
				}
			}
		}
		return this.varType.getSimilarity(type);
	}

	public ModuleInstance getInstance() {
		return (ModuleInstance) this.value;
	}

	@Override
	public VbValue get(org.siphon.visualbasic.interpreter.Interpreter interpreter) {
		if (this.varType.vbType == TypeEnum.vbObject) {
			//FIXME: This is hackish
			if (this.value instanceof JavaModuleInstance) {
				JavaModuleInstance jmi = (JavaModuleInstance) this.value;
				if (jmi.getModuleDecl() instanceof ClassModuleDecl) {
					ClassModuleDecl decl = (ClassModuleDecl) jmi.getModuleDecl();
					ModuleMemberDecl defaultMember = decl.getDefaultMember();
					if (defaultMember instanceof PropertyDecl) {
						PropertyDecl propertyDecl = (PropertyDecl) defaultMember;
						if (propertyDecl.get instanceof JavaMethod) {
							 ((JavaMethod) propertyDecl.get).object = jmi.getInstance();
						}
						propertyDecl.get.invoke(interpreter, new ArrayList<Argument>());
						Value value = interpreter.pop();
						if (value instanceof VbValue) {
							return (VbValue) value;
						}
						else {
							throw new RuntimeException("Default member " + defaultMember.getName() + " of class " + decl.getName() + " returned something that is not a VbValue (" + value.getClass().getName() + ")");
						}
					}
					return this;
				}
			}
		}
		return this;
	}

	@Override
	public void set(org.siphon.visualbasic.interpreter.Interpreter interpreter, VbValue value) {
		throw new UnsupportedOperationException("A VbValue cannot be assigned to");
	}
	
}
