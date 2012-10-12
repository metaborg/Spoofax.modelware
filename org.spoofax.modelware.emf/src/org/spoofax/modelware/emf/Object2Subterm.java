package org.spoofax.modelware.emf;

import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.EMap;
import org.eclipse.emf.ecore.EObject;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.stratego.StrategoTermPath;
import org.strategoxt.lang.Context;

/**
 * @author Oskar van Rest
 */
public class Object2Subterm {

	private static Context context = new Context();
	
	public IStrategoTerm object2subterm(EObject eObject, IStrategoTerm AST) {
		List<Integer> path = object2path(eObject, new LinkedList<Integer>());
		IStrategoList strategoTermPath = StrategoTermPath.toStrategoPath(path);
		return StrategoTermPath.getTermAtPath(context, AST, strategoTermPath);
	}

	private List<Integer> object2path(EObject eObject, List<Integer> result) {
		if (eObject.eContainer() == null) {
			return result;
			
		} else {
			EMap<String, String> index2name = eObject.eContainer().eClass().getEAnnotation("StrategoTerm.index").getDetails();
			int position = Integer.parseInt(getKeyByValue(index2name, eObject.eContainingFeature().getName()));

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

	private <T, E> T getKeyByValue(EMap<T, E> map, E value) {
		for (Entry<T, E> entry : map.entrySet()) {
			if (value.equals(entry.getValue())) {
				return entry.getKey();
			}
		}
		return null;
	}
}
