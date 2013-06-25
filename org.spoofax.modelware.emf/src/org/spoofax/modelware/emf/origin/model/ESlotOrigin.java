package org.spoofax.modelware.emf.origin.model;

import org.spoofax.interpreter.terms.IStrategoTerm;

public abstract class ESlotOrigin extends EOrigin {
	
	public ESlotOrigin(IStrategoTerm origin) {
		super(origin);
	}
	
}


// for each object := annotate it object with it's origin
// for each object := annotate term with origin (if object has origin)