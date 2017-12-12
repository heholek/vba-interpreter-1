package org.siphon.visualbasic.runtime.framework.vb;

import org.siphon.visualbasic.ArgumentException;
import org.siphon.visualbasic.runtime.VbRuntimeException;
import org.siphon.visualbasic.runtime.framework.VbMethod;

public class HeadlessCommandButton extends HeadlessControl {
	protected int alignment = 0;
	protected String caption = "";

	@VbMethod(isDefault=true)
	public String getCaption() throws VbRuntimeException, ArgumentException {
		return caption;
	}
	
	@VbMethod(isDefault=true)
	public void setCaption(String caption) throws VbRuntimeException, ArgumentException {
		this.caption = caption;
	}
}
