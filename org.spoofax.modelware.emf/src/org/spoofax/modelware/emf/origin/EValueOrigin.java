package org.spoofax.modelware.emf.origin;

import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;

public abstract class EValueOrigin extends ESlotOrigin {

	protected IStrategoAppl someOrNone;
	
	protected EValueOrigin(IStrategoTerm origin) {
		this(origin, null);
	}
	
	protected EValueOrigin(IStrategoTerm origin, IStrategoAppl someOrNone) {
		super(origin);
		this.someOrNone = someOrNone;
	}
	
	public IStrategoAppl getSomeOrNone() {
		return someOrNone;
	}
}
