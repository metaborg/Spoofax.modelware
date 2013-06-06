package org.spoofax.modelware.emf.origin;

import org.eclipse.emf.ecore.EObject;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.modelware.emf.origin.model.EObjectOrigin;

public class ModelOriginUtil {

	public static void makeLink(IStrategoAppl origin, EObject eObject) {
		EObjectOrigin eObjectOrigin = new EObjectOrigin(origin);
		ModelOriginHashtable.INSTANCE.put(eObject, eObjectOrigin);
	}
	
	//TODO
	public static void makeLink(EObject eObject, IStrategoAppl term) {
		EObjectOrigin eObjectOrigin = ModelOriginHashtable.INSTANCE.get(eObject);
		// pre: term and eObjectOrigin have the same structure
		// post: term is assigned origin information
	}
}
