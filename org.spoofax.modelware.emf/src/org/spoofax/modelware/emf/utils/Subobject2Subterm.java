package org.spoofax.modelware.emf.utils;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.stratego.StrategoTermPath;
import org.strategoxt.lang.Context;

/**
 * Given an EObject contained by some root object (i.e. "the model"), find the corresponding StrategoTerm contained by some root term (i.e.
 * "the AST"). Calculation is done based on the containment hierarchy.
 * 
 * @author oskarvanrest
 */
public class Subobject2Subterm {

	private static Context context = new Context();

	public static IStrategoTerm object2subterm(EObject eObject, IStrategoTerm AST) {
		List<Integer> path = object2path(eObject, new LinkedList<Integer>());
		IStrategoList strategoTermPath = StrategoTermPath.toStrategoPath(path);
		return StrategoTermPath.getTermAtPath(context, AST, strategoTermPath);
	}

	private static List<Integer> object2path(EObject eObject, List<Integer> result) {
		if (eObject.eContainer() == null) {
			return result;

		}
		else {
			int position = SpoofaxEMFUtils.feature2index(eObject.eContainer().eClass(), eObject.eContainingFeature());

			if (eObject.eContainingFeature().getLowerBound() == 0 && eObject.eContainingFeature().getUpperBound() == 1) {
				result.add(0, 0);
			}
			if (eObject.eContainingFeature().getUpperBound() == -1) {
				EList<?> list = (EList<?>) eObject.eContainer().eGet(eObject.eContainingFeature());
				int positionInList = list.indexOf(eObject);
				result.add(0, positionInList);
			}
			result.add(0, position);

			return object2path(eObject.eContainer(), result);
		}
	}
}
