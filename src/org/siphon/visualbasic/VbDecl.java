package org.siphon.visualbasic;

public class VbDecl {
	public String name;
	public Visibility visibility = Visibility.PRIVATE;
	protected final Library library;
	
	public Library getLibrary() {
		return this.library;
	}
	
	public VbDecl(Library library){
		this.library = library;
	}
	
	public static final VbDecl AMBIGUOUS = new VbDecl(null);
	public static final VbDecl COMPILER_UNKNOWN = new VbDecl(null);
	
	public String upperCaseName(){
		return name.toUpperCase();
	}
}
