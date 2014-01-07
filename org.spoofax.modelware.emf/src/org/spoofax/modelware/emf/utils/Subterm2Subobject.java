package org.spoofax.modelware.emf.utils;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.spoofax.interpreter.terms.IStrategoInt;
import org.spoofax.interpreter.terms.IStrategoList;

/**
 * Given a StrategoTerm contained by some root term (i.e. "the AST"), find the corresponding EObject contained by
 * some root object (i.e. "the model"). Calculation is done based on the containment hierarchy.
 *
 * @author Oskar van Rest
 */
public class Subterm2Subobject {

	public static EObject path2object(IStrategoList adjustedASTSelection, EObject root) {
		EObject current = root;
		
		for (int i = 1; i < adjustedASTSelection.size(); i++) {
			if (current == null) {
				return null; // object not (yet) in model
			}

			i++;
			
			EStructuralFeature feature = Utils.getFeature(current.eClass(), ((IStrategoInt) adjustedASTSelection.getAllSubterms()[i]).intValue());
			
			if (!(feature instanceof EReference && ((EReference) feature).isContainment())) {
				return null; // ignore attributes and cross-references
			}
			
			i++;
			if (feature.getLowerBound() == 0 && feature.getUpperBound() == 1) {
				i++; // ignore Some(...)
			}
			if (feature.getUpperBound() == -1) { // list
				if (i + 1 < adjustedASTSelection.size()) {
					EList<?> list = (EList<?>) current.eGet(feature);
					int index = ((IStrategoInt) adjustedASTSelection.getAllSubterms()[i+1]).intValue();
					if (index >= list.size()) {
						return null;
					}
					current = (EObject) ((EList<?>) current.eGet(feature)).get(index);
					i++;
				}
			} else {
				current = (EObject) current.eGet(feature);
			}
		}

		return current;
	}
}
