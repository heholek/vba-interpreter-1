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

import org.siphon.visualbasic.ArgumentException;
import org.siphon.visualbasic.runtime.VbRuntimeException;
import org.siphon.visualbasic.runtime.framework.VbMethod;

public class HeadlessTextBox extends HeadlessControl {

	protected int alignment = 0;
	protected String text = "";

	@VbMethod
	private void setAlignment(Integer alignment) {
		switch(alignment) {
		case 0:
			alignment = 0;
			break;
		case 1:
			alignment = 1;
			break;
		case 2:
			alignment = 2;
			break;
		}
	}
	
	@VbMethod
	private Integer getAlignment() {
		return alignment;
	}

	@VbMethod(isDefault=true)
	public String getText() throws VbRuntimeException, ArgumentException {
		return text;
	}
	
	@VbMethod(isDefault=true)
	public void setText(String text) throws VbRuntimeException, ArgumentException {
		this.text = text;
	}

}
