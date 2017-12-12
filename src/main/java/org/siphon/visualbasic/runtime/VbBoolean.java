package org.siphon.visualbasic.runtime;

public class VbBoolean extends VbValue {

	public VbBoolean(boolean value) {
		super(VbVarType.VbBoolean, value ? -1 : 0);
	}

}
