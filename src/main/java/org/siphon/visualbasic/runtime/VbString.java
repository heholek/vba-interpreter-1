package org.siphon.visualbasic.runtime;

import org.siphon.visualbasic.runtime.VbVarType.TypeEnum;

public class VbString extends VbValue {

	public VbString(String value) {
		super(VbVarType.VbString, value);
	}
	
	@Override
	public String toString() {
		return (String) this.value;
	}
	
	public VbValue fromJava(String value) {
		return new VbString(value);
	}

}
