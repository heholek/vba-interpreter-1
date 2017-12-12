package org.siphon.visualbasic.runtime;

import org.siphon.visualbasic.VbDecl;

public class AbstractScope implements Scope {
	@Override
	public VbVariable locate(VbDecl decl) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Scope getParent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isGlobalScope() {
		// TODO Auto-generated method stub
		return false;
	}

}
