package org.spoofax.modelware.emf.origin.model;

import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoString;

/**
 * EDataOrigin represents origin of both attribute and non-containment reference values
 * @author oskarvanrest
 */
public class EDataOrigin extends EValueOrigin {

	protected EDataOrigin(IStrategoString origin) {
		super(origin);
	}

	protected EDataOrigin(IStrategoString origin, IStrategoAppl someOrigin) {
		super(origin, someOrigin);
	}
}
