package org.spoofax.modelware.emf.origin.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.modelware.emf.origin.EOriginObserver;
import org.spoofax.modelware.emf.utils.SpoofaxEMFUtils;

public class EObjectOrigin extends EValueOrigin {

	private List<ESlotOrigin> eSlotOrigins = new ArrayList<ESlotOrigin>();

	protected EObjectOrigin(EObject eObject, IStrategoAppl origin) {
		super(origin);

		if (origin == null || eObject == null) {
			return;
		}

		eObject.eAdapters().add(new EOriginObserver(this));
		
		
		EClass eClass = eObject.eClass();
		int subtermCount = origin.getSubtermCount();

		for (int i = 0; i < subtermCount; i++) {
			EStructuralFeature eFeature = SpoofaxEMFUtils.index2feature(eClass, i);
			IStrategoAppl someOrNone = null;
			IStrategoTerm subterm = origin.getSubterm(i);

			if (SpoofaxEMFUtils.isSome(origin) || SpoofaxEMFUtils.isNone(origin)) {
				someOrNone = origin;
			}
			if (SpoofaxEMFUtils.isSome(origin)) {
				subterm = subterm.getSubterm(0);
			}
			
			if (eFeature.getUpperBound() == -1) {
				eSlotOrigins.add(new EListOrigin((EList<?>) eObject.eGet(eFeature), (IStrategoList) subterm));
			}
			else if (eFeature instanceof EReference && ((EReference) eFeature).isContainment()){
				eSlotOrigins.add(new EObjectOrigin((EObject) eObject.eGet(eFeature), (IStrategoAppl) subterm, someOrNone));				
			}
			else {
				eSlotOrigins.add(new EDataOrigin((IStrategoString) subterm, someOrNone));
			}
		}
	}

	protected EObjectOrigin(EObject eObject, IStrategoAppl origin, IStrategoAppl someOrNone) {
		this(eObject, origin);
		this.someOrNone = someOrNone;
	}

	public List<ESlotOrigin> getESlotOrigin() {
		return eSlotOrigins;
	}
}
