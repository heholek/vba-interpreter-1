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

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Currency;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.siphon.visualbasic.ClassModuleDecl;
import org.siphon.visualbasic.ClassTypeDecl;
import org.siphon.visualbasic.MethodDecl;
import org.siphon.visualbasic.ModuleMemberDecl;
import org.siphon.visualbasic.NumberRange;
import org.siphon.visualbasic.PropertyDecl;
import org.siphon.visualbasic.UdtDecl;
import org.siphon.visualbasic.VbDecl;
import org.siphon.visualbasic.VbTypeDecl;
import org.siphon.visualbasic.compile.JavaClassModuleDecl;
import org.stringtemplate.v4.compiler.STParser.singleElement_return;

public class VbVarType {

	public static long DATE_OFFSET;
	static {
		try {
			// 取决于时区
			DATE_OFFSET = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("1899-12-30 00:00:00").getTime();
		} catch (ParseException e) {
		}
	}

	public enum TypeEnum {
		vbEmpty(0), vbNull(1), vbInteger(2), vbLong(3), vbSingle(4), vbDouble(5), vbCurrency(6), vbDate(7), vbString(
				8), vbObject(9), vbError(10), vbBoolean(
						11), vbVariant(12), vbDataObject(13), vbDecimal(14), vbByte(17), vbUserDefinedType(36), vbArray(8192);

		private int numVal;

		TypeEnum(int numVal) {
			this.numVal = numVal;
		}

		public int getNumVal() {
			return numVal;
		}
	}
	
	private static final TypeEnum[] BASE_TYPES = new TypeEnum[] {
			TypeEnum.vbEmpty, TypeEnum.vbNull, TypeEnum.vbInteger, TypeEnum.vbLong,
			TypeEnum.vbSingle, TypeEnum.vbDouble, TypeEnum.vbCurrency, TypeEnum.vbDate,
			TypeEnum.vbString, TypeEnum.vbError, TypeEnum.vbBoolean, TypeEnum.vbVariant,
			TypeEnum.vbDecimal, TypeEnum.vbByte,		
	};

	private static final String[] TypeNames = { "Empty", "Null", "Integer", "Long", "Single", "Double", "Currency", "Date",
			"String", "Object", "Error", "Boolean", "Variant", "DataObject", "Decimal", "Unknown15", "Unknown16", "Byte" };

	public static final NumberRange[] NumberRange = { null, null,
			// new NumberRange(Integer.MIN_VALUE, Integer.MAX_VALUE),
			new NumberRange(-32768, 32767), new NumberRange(Long.MIN_VALUE, Long.MAX_VALUE),
			new NumberRange(-Float.MAX_VALUE, Float.MAX_VALUE), new NumberRange(-Double.MAX_VALUE, Double.MAX_VALUE),
			new NumberRange(-Double.MAX_VALUE, Double.MAX_VALUE), // currency
			new NumberRange(-657434, 2958465), // 从 100 年 1 月 1 日到 9999 年 12 月 31 日
			null, null, null, null, // boolean
			null, null, null, // BigDecimal has no range
			null, null, new NumberRange(0, 255) };

	public boolean isBaseType() {
		for (TypeEnum t : BASE_TYPES) {
			if (this.vbType == t) {
				return true;
			}
		}
		
		return false;
	}

	/*
	 * vb 数据类型代码
	 */
	public final TypeEnum vbType;

	/*
	 * 类模块
	 */
	public final VbTypeDecl typeDecl; // 类模块, UDT, Enum

	/*
	 * Java 类型
	 */
	public final Class javaClass;

	/*
	 * 数组类型
	 */
	public final ArrayDef arrayDef;

	public VbVarType(TypeEnum vbType, VbTypeDecl vbTypeDecl, ArrayDef arrayDef, Class javaClass) {
		super();
		this.vbType = vbType;
		this.javaClass = javaClass;
		this.typeDecl = vbTypeDecl;
		this.arrayDef = arrayDef;
	}

	public VbVarType(TypeEnum vbType) {
		this(vbType, null, null, null);
	}

	@Override
	public String toString() {
		String s = getTypeName(this.vbType);
		if (this.typeDecl != null) {
			s += " " + this.typeDecl.toString();
		}
		if (this.vbType == TypeEnum.vbArray) {
			s += "[" + this.arrayDef.toString() + "]";
		}
		return s;
	}

	private static String getTypeName(TypeEnum vbType) {
		if (vbType.getNumVal() >= 0 && vbType.getNumVal() < TypeNames.length) {
			return TypeNames[vbType.getNumVal()];
		} else if (vbType == TypeEnum.vbArray) {
			return "Array";
		} else if (vbType == TypeEnum.vbUserDefinedType) {
			return "UDT";
		} else {
			return "Unknown " + vbType;
		}
	}
	
	public String getTypeName(){
		if (vbType.getNumVal() >= 0 && vbType.getNumVal() < TypeNames.length) {
			return TypeNames[vbType.getNumVal()];
		} else if (vbType == TypeEnum.vbArray) {
			return this.arrayDef.baseType.toString() + "()";
		} else if (vbType == TypeEnum.vbUserDefinedType 
				|| vbType == TypeEnum.vbObject) {
			return this.typeDecl.toString();
		} else {
			throw new UnsupportedOperationException("unknown type");
		}
	}
	
	public final static VbVarType VbEmpty = new VbVarType(TypeEnum.vbEmpty);
	public final static VbVarType VbNull = new VbVarType(TypeEnum.vbNull);
	public final static VbVarType VbInteger = new VbVarType(TypeEnum.vbInteger);
	public static final VbVarType VbLong = new VbVarType(TypeEnum.vbLong);
	public final static VbVarType VbSingle = new VbVarType(TypeEnum.vbSingle);
	public static final VbVarType VbDouble = new VbVarType(TypeEnum.vbDouble);
	public static final VbVarType VbCurrency = new VbVarType(TypeEnum.vbCurrency);
	public static final VbVarType VbDate = new VbVarType(TypeEnum.vbDate);
	public final static VbVarType VbString = new VbVarType(TypeEnum.vbString);
	public static final VbVarType VbBoolean = new VbVarType(TypeEnum.vbBoolean);
	public final static VbVarType VbVariant = new VbVarType(TypeEnum.vbVariant);
	public static final VbVarType VbDecimal = new VbVarType(TypeEnum.vbDecimal);
	public static final VbVarType VbByte = new VbVarType(TypeEnum.vbByte);
	public static final VbVarType VbError = new VbVarType(TypeEnum.vbError);
	public static final VbVarType VbObject = new VbVarType(TypeEnum.vbObject); // TODO 对象基类类型

	/**
	 * 创建基于当前数据类型的数组。
	 * 
	 * @param ranks
	 *            秩。参数声明可以为空。
	 * @return
	 */
	public VbVarType toArrayType(ArrayDef.Rank ranks[]) {
		return new VbVarType(TypeEnum.vbArray, this.typeDecl, new ArrayDef(this, ranks), this.javaClass);
	}

	public static VbVarType javaTypeToVb(Class<?> type) {
		if (type.equals(Void.TYPE)) {
			return null;
		} else if (type == Integer.class) {
			return VbVarType.VbInteger;
		} else if (type.isPrimitive() && type.getSimpleName().equals("short")) {
			return VbVarType.VbInteger;
		} else if (type == Long.class || type.isPrimitive() && type.getSimpleName().equals("long")) {
			return VbVarType.VbLong;
		} else if (type == Float.class) {
			return VbVarType.VbSingle;
		} else if (type == Double.class) {
			return VbVarType.VbDouble;
		} else if (type == String.class) {
			return VbVarType.VbString;
		} else if (type == BigDecimal.class) {
			return VbVarType.VbDecimal;
		} else if (type == Date.class) {
			return VbVarType.VbDate;
		} else if (type == Boolean.class) {
			return VbVarType.VbBoolean;
		} else if (type == Byte.class) {
			return VbVarType.VbByte;
		} else if (type == Object.class) {
			return VbVarType.VbVariant;
		} else if (type == VbValue.class) {
			return VbVarType.VbVariant;
		} else if (type == Short.class) {
			return VbVarType.VbInteger;
		} else {
			// TODO java array to vb array
			throw new UnsupportedOperationException(type + " still not unsupport");
		}
	}

	public VbValue crateDefaultValue() {
		switch (this.vbType) {
		case vbInteger:
			return new VbValue(this, 0);
		case vbLong:
			return new VbValue(this, 0L);
		case vbSingle:
			return new VbValue(this, 0f);
		case vbDouble:
			return new VbValue(this, 0.0);
		case vbCurrency:
			return new VbValue(this, 0L);
		case vbDecimal:
			return new VbValue(this, new BigDecimal(0));

		case vbDate:
			return new VbValue(this, 0.0); // VB 里 Date 存放的是一个 Double，日期部分是整数，时间部分是小数，一天为 1， 其整数从 1899-12-30 开始，比 1970 开始的 ISO 时间小 DATE_OFFSET
		case vbString:
			return new VbValue(this, "");
		case vbVariant:
			return VbValue.Empty; // 类型为 variant 时 null 即 empty
		case vbBoolean:
			return new VbValue(this, 0);
		case vbByte:
			return new VbValue(this, 0);
		case vbArray:
			return new VbArray(this);

		case vbUserDefinedType:
			return new VbValue(this, new UdtInstance((UdtDecl) this.typeDecl));

		case vbObject:
			return new VbValue(this, null); // object 除非主动 new，默认值为 nothing

		default:
			throw new UnsupportedOperationException("wait"); // TODO
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof VbVarType == false)
			return false;
		VbVarType t2 = (VbVarType) obj;
		if (t2.vbType == this.vbType) {
			if (this.vbType == TypeEnum.vbUserDefinedType) {
				return this.typeDecl == t2.typeDecl;
			} else if (this.vbType == TypeEnum.vbObject) {
				ClassTypeDecl cd1 = (ClassTypeDecl) this.typeDecl;
				ClassTypeDecl cd2 = (ClassTypeDecl) t2.typeDecl;
				if (cd1 == null)
					return cd2 == null;
				if (cd2 == null)
					return false;
				return cd1.classModule == cd2.classModule;
			} else {
				return true;
			}
		}
		return false;
	}

	public boolean isDictionary() {
		if (this.vbType == TypeEnum.vbObject && this.typeDecl != null) {
			ClassTypeDecl td = (ClassTypeDecl) typeDecl;
			return td.classModule.getDictionaryMember() != null;
		}
		return false;
	}

	public ModuleMemberDecl getDictionaryMember() {
		if (this.vbType == TypeEnum.vbObject) {
			ClassTypeDecl td = (ClassTypeDecl) typeDecl;
			return td.classModule.getDictionaryMember();
		}
		return null;
	}

	public VbVarType getDictionaryType() {
		if (this.vbType == TypeEnum.vbObject && this.typeDecl != null) {
			ClassTypeDecl td = (ClassTypeDecl) typeDecl;
			ModuleMemberDecl dm = td.classModule.getDictionaryMember();
			if (dm instanceof MethodDecl) {
				return ((MethodDecl) dm).returnType;
			} else if (dm instanceof PropertyDecl) {
				return ((PropertyDecl) dm).getReturnType();
			}
		}
		return null;
	}

	public boolean hasDefaultMember() {
		if (this.vbType == TypeEnum.vbObject && this.typeDecl != null) {
			ClassTypeDecl td = (ClassTypeDecl) typeDecl;
			return td.classModule.getDefaultMember() != null;
		}
		return false;
	}

	public VbDecl getDefaultMember() {
		if (this.vbType == TypeEnum.vbObject) {
			ClassTypeDecl td = (ClassTypeDecl) typeDecl;
			return td.classModule.getDefaultMember();
		}
		return null;
	}
	
	public VbDecl getDefaultMember(int vbCallType) {
		if (this.vbType == TypeEnum.vbObject) {
			ClassTypeDecl td = (ClassTypeDecl) typeDecl;
			return td.classModule.getDefaultMember(vbCallType);
		}
		return null;
	}


	public boolean maybeObject() {
		return this.vbType == TypeEnum.vbObject || this.vbType == TypeEnum.vbVariant;
	}

	public ClassModuleDecl getClassModuleDecl() {
		if (this.vbType == TypeEnum.vbObject) {
			if (this.typeDecl != null) {
				return ((ClassTypeDecl) this.typeDecl).classModule;
			}
		}
		return null;
	}

	public ClassTypeDecl getClassTypeDecl() {
		if (this.vbType == TypeEnum.vbObject) {
			if (this.typeDecl != null) {
				return (ClassTypeDecl) this.typeDecl;
			}
		}
		return null;
	}

	public int getSimilarity(Class<?> type) {
		if (VbValue.class.isAssignableFrom(type)) {
			return 100;
		}
		switch (this.vbType) {
		case vbInteger:
		case vbByte:
		case vbLong:
		case vbDecimal:
		case vbDouble:
		case vbSingle:
		case vbCurrency:
			if (Number.class.isAssignableFrom(type)) {
				if (type == vbNumberTypeToJavaType(this.vbType)) {
					return 100;
				} else {
					return 70;
				}
			} else if (CharSequence.class.isAssignableFrom(type)) {
				return 25;
			} else if (type == Object.class) {
				return 50;
			}
		case vbDate:
			if (type == Date.class) {
				return 100;
			} else if (Number.class.isAssignableFrom(type)) {
				return 20;
			} else if (CharSequence.class.isAssignableFrom(type)) {
				return 35;
			} else if (type == Object.class) {
				return 50;
			}
		case vbBoolean:
			if (type == Boolean.class || type == boolean.class) {
				return 100;
			} else if (Number.class.isAssignableFrom(type)) {
				return 30;
			} else if (CharSequence.class.isAssignableFrom(type)) {
				return 25;
			} else if (type == Object.class) {
				return 50;
			}
		case vbString:
			if (CharSequence.class.isAssignableFrom(type)) {
				return 100;
			} else if (Number.class.isAssignableFrom(type)) {
				return 20;
			} else if (type == Object.class) {
				return 50;
			}
		case vbEmpty:
		case vbNull:
			return 60;
		case vbObject:
			ClassTypeDecl decl = this.getClassTypeDecl();
			if (decl == ClassTypeDecl.JAVA_OBJECT_TYPE) {
				return 10; // should return by VbValue.getSimilarity
			} else if (decl.classModule instanceof JavaClassModuleDecl) {
				if (this.javaClass == type) {
					return 100;
				} else if (type.isAssignableFrom(this.javaClass)) {
					return 90;
				} else {
					return -50;
				}
			}
		case vbArray:
			if (type.isArray()) {
				return (this.arrayDef.baseType).getSimilarity(type.getComponentType());
			} else if (Iterable.class.isAssignableFrom(type) || List.class.isAssignableFrom(type)) {
				return this.arrayDef.baseType.getSimilarity(Object.class);
			}
		case vbVariant:
			return 5;
		case vbUserDefinedType:
			return -50;
		}
		return 0;
	}

	private static Class<?> vbNumberTypeToJavaType(TypeEnum vbType) {
		switch (vbType) {
		case vbInteger:
			return int.class;
		case vbByte:
			return byte.class;
		case vbLong:
			return long.class;
		case vbDecimal:
			return BigDecimal.class;
		case vbDouble:
			return double.class;
		case vbSingle:
			return float.class;
		case vbCurrency:
			return double.class;
		}
		throw new UnsupportedOperationException();
	}

	public boolean isJavaObject() {
		ClassTypeDecl decl = this.getClassTypeDecl();
		if(decl == null) return false;
		if (decl == ClassTypeDecl.JAVA_OBJECT_TYPE) {
			return true;
		} else if (decl.classModule instanceof JavaClassModuleDecl) {
			return true;
		} else {
			return false;
		}
	}
	
	public Class getWrappedJavaClass(){
		ClassTypeDecl decl = this.getClassTypeDecl();
		if(decl == null) return null;
		return ((JavaClassModuleDecl)decl.classModule).getJavaClass();
	}

	public String getProgId() {
		String n = this.getTypeName();
		if("OBJECT".equalsIgnoreCase(n)) {
			ClassModuleDecl cmdcl = this.getClassModuleDecl();
			return cmdcl.getLibrary().getName() + "." + cmdcl.getName(); 
		} else {
			return n;
		}
			
	}

	public boolean isArray() {
		return this.vbType == TypeEnum.vbArray;
	}

}
