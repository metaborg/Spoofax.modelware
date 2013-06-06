package org.spoofax.modelware.emf.origin.model;

import java.util.ArrayList;
import java.util.Iterator;

import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;

public class EListOrigin extends ESlotOrigin {

	private ArrayList<EValueOrigin> eValueOrigins;
	
	public EListOrigin(IStrategoList origin) {
		super(origin);
		
		eValueOrigins = new ArrayList<EValueOrigin>();

		Iterator<IStrategoTerm> it = origin.iterator();
		while (it.hasNext()) {
			IStrategoTerm subterm = it.next();

			switch (subterm.getTermType()) {

			case IStrategoTerm.STRING:
				eValueOrigins.add(new EValueOrigin((IStrategoString) subterm));
				break;

			default:
				break;
			}
		}
	}	
}
