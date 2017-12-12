package org.siphon.visualbasic;

public class VbWithDecl extends VbDecl {
	protected VbDecl decl;
	
	public VbWithDecl(VbDecl decl) {
		super(null);
		this.decl = decl;
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public Library getLibrary() {
		return decl.getLibrary();
	}
	
	@Override
	public String getName() {
		return decl.getName();
	}
	
	public VbDecl getWrappedDecl() {
		return decl;
	}
}
