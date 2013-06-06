package org.spoofax.modelware.emf.origin.model;

import org.spoofax.interpreter.terms.IStrategoTerm;

public abstract class EOrigin {

	private IStrategoTerm origin;

	public EOrigin(IStrategoTerm origin) {
		this.origin = origin;
	}
	
	public IStrategoTerm getOrigin() {
		return origin;
	}
}
