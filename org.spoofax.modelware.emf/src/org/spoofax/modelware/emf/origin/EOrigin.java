package org.spoofax.modelware.emf.origin;

import org.eclipse.emf.ecore.EObject;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.modelware.emf.utils.SpoofaxEMFUtils;

public abstract class EOrigin {

	private IStrategoTerm origin;
	
	protected EOrigin(IStrategoTerm origin) {
		this.origin = origin;
	}
	
	public IStrategoTerm getOrigin() {
		return origin;
	}
	
	public static EObjectOrigin constructEOrigin(EObject eObject, IStrategoTerm origin) {
		if (SpoofaxEMFUtils.isSome(origin) && origin.getSubterm(0) instanceof IStrategoAppl) {
			return new EObjectOrigin(eObject, (IStrategoAppl) origin.getSubterm(0), (IStrategoAppl) origin);
		}
		else if (origin.getSubterm(0) instanceof IStrategoAppl){
			return new EObjectOrigin(eObject, (IStrategoAppl) origin);
		}
		
		return null;
	}
}
