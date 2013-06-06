package org.spoofax.modelware.emf.origin.model;

import java.util.ArrayList;
import java.util.Iterator;

import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;

public class EObjectOrigin extends EOrigin {

	private ArrayList<ESlotOrigin> eSlotOrigins;

	public EObjectOrigin(IStrategoAppl origin) {
		super(origin);

		eSlotOrigins = new ArrayList<ESlotOrigin>();

		Iterator<IStrategoTerm> it = origin.iterator();
		while (it.hasNext()) {
			IStrategoTerm subterm = it.next();

			switch (subterm.getTermType()) {

			case IStrategoTerm.STRING:
				eSlotOrigins.add(new EValueOrigin((IStrategoString) subterm));
				break;

			case IStrategoTerm.LIST:
				eSlotOrigins.add(new EListOrigin((IStrategoList) subterm));
				break;

			default:
				break;
			}
		}
	}
}
