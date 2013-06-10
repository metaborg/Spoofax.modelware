package org.spoofax.modelware.emf.tree2model;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

/**
 * @author Oskar van Rest
 */
public abstract class AbstractModel2Term {

	protected final ITermFactory factory;

	public AbstractModel2Term(ITermFactory factory) {
		this.factory = factory;
	}
	
	public abstract IStrategoTerm convert(EObject eObject);

	protected IStrategoTerm convert(EObject object, EStructuralFeature feature) {
		IStrategoTerm result = null;
		
		if (feature instanceof EAttribute) {
			result = convert(object, (EAttribute) feature);
		} else {
			result = convert(object, (EReference) feature);
		}
		
		if (feature.getLowerBound() == 0 && feature.getUpperBound() == 1) {
			return someOrNone(result);
		}
		else if (result == null) {
			return createDefaultValue(feature);
		}
		else {
			return result;
		}
	}
	protected abstract IStrategoTerm convert(EObject eObject, EAttribute eAttribute);
	
	protected abstract IStrategoTerm convert(EObject eObject, EReference eReference);
	
	protected abstract IStrategoTerm someOrNone(IStrategoTerm term);
	
	protected IStrategoTerm createDefaultValue(EStructuralFeature feature) {
		if (feature instanceof EAttribute) {
			return createDefaultValue((EAttribute) feature);
		} else {
			return createDefaultValue((EReference) feature);
		}
	}
	protected abstract IStrategoTerm createDefaultValue(EAttribute attribute);
	
	protected abstract IStrategoTerm createDefaultValue(EReference reference);
}
