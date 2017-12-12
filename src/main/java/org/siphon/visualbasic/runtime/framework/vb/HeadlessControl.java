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
package org.siphon.visualbasic.runtime.framework.vb;

import java.util.HashMap;
import java.util.Map;

import org.siphon.visualbasic.ArgumentException;
import org.siphon.visualbasic.Interpreter;
import org.siphon.visualbasic.runtime.VbBoundObject;
import org.siphon.visualbasic.runtime.VbInteger;
import org.siphon.visualbasic.runtime.VbRuntimeException;
import org.siphon.visualbasic.runtime.VbValue;
import org.siphon.visualbasic.runtime.VbVarType;
import org.siphon.visualbasic.runtime.framework.VbMethod;
import org.siphon.visualbasic.runtime.framework.stdole.StdFont;

public abstract class HeadlessControl extends VbBoundObject {
	
	protected String name = "";
	
	protected Form form;
	
	protected Integer index = null;		// 控件数组索引  
	
	protected String tag = "";
	
	protected HeadlessControl container = null;
	
	protected Map<String, VbValue> attributes = new HashMap<String, VbValue>();
	
	private StdFont font = new StdFont();
	
	@VbMethod
	public Integer getIndex() {
		return index;
	}

	@VbMethod
	public void setIndex(Integer index) {
		this.index = index;
	}

	private void setLocation(Map<String, VbValue> attrs) {
		if(attrs.containsKey("Top") && attrs.containsKey("Left")) {
			attributes.put("Left", attrs.get("Left"));
			attributes.put("Top", attrs.get("Top"));
		}
	}

	private void setSize(Map<String, VbValue> attrs) {
		if(attrs.containsKey("Width") && attrs.containsKey("Height")) {
			attributes.put("Width", attrs.get("Width"));
			attributes.put("Height", attrs.get("Height"));
		}
	}
	
	private void setFont(Map<String, VbValue> attributes, Interpreter interpreter) throws VbRuntimeException, ArgumentException {
		String name = (String) attributes.get("Name").toJava();
		Number size = (Number) attributes.get("Size").toJava();
		Number charset = (Number) attributes.get("Charset").toJava();
		Number weigth = (Number) attributes .get("Weight").toJava();
		Number underline = (Number) attributes.get("Underline").toJava();
		Number italic = (Number) attributes.get("Italic").toJava();
		Number strikethrough = (Number) attributes.get("Strikethrough").toJava();
        
		font.setName(interpreter, interpreter.getCurrentFrame(), name);
		font.setSize(interpreter, interpreter.getCurrentFrame(), size.doubleValue());
		font.setCharset(interpreter, interpreter.getCurrentFrame(), charset.intValue());
		font.setWeight(interpreter, interpreter.getCurrentFrame(), weigth.intValue());
		font.setUnderline(interpreter, interpreter.getCurrentFrame(), !underline.equals(0));
		font.setItalic(interpreter, interpreter.getCurrentFrame(), !italic.equals(0));
		font.setStrikethrough(interpreter, interpreter.getCurrentFrame(), !strikethrough.equals(0));
	}
	
	@VbMethod
	public Integer getTop() {
		return (Integer) attributes.get("Top").toJava();
	}

	@VbMethod
	public void setTop(Integer top) {
		attributes.put("Top", new VbInteger(top));
	}

	@VbMethod
	public Integer getLeft() {
		return (Integer) attributes.get("Left").toJava();
	}

	@VbMethod
	public void setLeft(Integer left) {
		attributes.put("Left", new VbInteger(left));
	}

	@VbMethod
	public Integer getWidth() {
		return (Integer) attributes.get("Width").toJava();
	}

	@VbMethod
	public void setHeight(Integer height) {
		attributes.put("Height", new VbInteger(height));
	}

	@VbMethod
	public Integer getHeight() {
		return (Integer) attributes.get("Height").toJava();
	}

	@VbMethod
	public void setWidth(Integer width) {
		attributes.put("Width", new VbInteger(width));
	}

	@VbMethod
	public String getTag() {
		return tag;
	}

	@VbMethod
	public void setTag(String tag) {
		this.tag = tag;
	}

	@VbMethod
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@VbMethod("Property Get Font() As StdFont")
	public StdFont getFont() {
		return this.font;
	}
	
	
}
