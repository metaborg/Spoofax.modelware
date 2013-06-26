package org.spoofax.modelware.emf.origin.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.IStrategoAppl;

public class EListOrigin extends ESlotOrigin {

	private List<EValueOrigin> eValueOrigins = new ArrayList<EValueOrigin>();
	
	protected EListOrigin(EList<?> eList, IStrategoList origin) {
		super(origin);

		int subtermCount = origin.size();
		for (int i=0; i<subtermCount; i++) {
			IStrategoTerm subterm = origin.getSubterm(i);
			
			switch (subterm.getTermType()) {

			case IStrategoTerm.STRING:
				eValueOrigins.add(new EDataOrigin((IStrategoString) subterm));
				break;

			case IStrategoTerm.APPL:
				eValueOrigins.add(new EObjectOrigin((EObject) eList.get(i), (IStrategoAppl) subterm));
				break;
				
			default:
				break;
			}
		}
	}
	
	public List<EValueOrigin> getEValueOrigins() {
		return eValueOrigins;
	}
}
