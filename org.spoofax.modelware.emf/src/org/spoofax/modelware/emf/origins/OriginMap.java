package org.spoofax.modelware.emf.origins;

import org.eclipse.emf.ecore.EObject;
import org.spoofax.interpreter.terms.IStrategoTerm;

public class OriginMap extends WeakDualMap<EObject, IStrategoTerm>{

	public OriginMap INSTANCE = new OriginMap();
	
	private OriginMap() {
		
	}
}
