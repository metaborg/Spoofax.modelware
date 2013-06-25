package org.spoofax.modelware.emf.utils;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.EMap;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.spoofax.interpreter.terms.IStrategoInt;
import org.spoofax.interpreter.terms.IStrategoList;

/**
 * Given a StrategoTerm contained by some root term (i.e. "the AST"), find the corresponding EObject contained by
 * some root object (i.e. "the model"). Calculation is done based on the containment hierarchy.
 *
 * @author oskarvanrest
 */
public class Subterm2Subobject {

	//TODO: fix deprecated, spoofax.term2feature, and doc.
	public static EObject path2object(IStrategoList adjustedASTSelection, EObject root) {
		EObject current = root;

		for (int i = 0; i < adjustedASTSelection.size(); i++) {
			EClass eClass = current.eClass();
			EMap<String, String> index2name = eClass.getEAnnotation("spoofax.term2feature").getDetails();
			EStructuralFeature feature = eClass.getEStructuralFeature(index2name.get(adjustedASTSelection.get(i).toString()));
			if (feature.getLowerBound() == 0 && feature.getUpperBound() == 1) {
				i++; // ignore Some(...)
			}
			if (feature.getUpperBound() == -1) { // list
				if (i + 1 < adjustedASTSelection.size()) {
					current = (EObject) ((EList<?>) current.eGet(feature)).get(((IStrategoInt) adjustedASTSelection.get(i + 1)).intValue());
					i++;
				}
			} else {
				current = (EObject) current.eGet(feature);
			}
		}

		return current;
	}
}
