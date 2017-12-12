package org.siphon.visualbasic.runtime;

import org.siphon.visualbasic.VbDecl;

public interface Scope {
	/**
	 * Find a variable in this scope.
	 * @param decl Declaration of the variable to find.
	 * @return A variable or null if the variable isn't found.
	 */
	public VbVariable locate(VbDecl decl);
	
	/**
	 * Get the parent scope. 
	 * @return The parent scope of null if this is the global scope.
	 */
	public Scope getParent();
	
	/**
	 * Check if this is the global scope.
	 * @return true if this is the global scope, false otherwise.
	 */
	public boolean isGlobalScope();
}
