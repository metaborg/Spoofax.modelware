package org.spoofax.modelware.emf.tree2model;

import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;

/**
 * @author Oskar van Rest
 */
public abstract class AbstractTerm2Model {
	
	protected final String term2featureAnno = "spoofax.term2feature";
	protected final String defAnno = "spoofax.def";
	
	public EObject convert(IStrategoTerm term) {
		EObject result = (EObject) convert(term, null);
		setReferences();
		return result;
	}
	
	protected Object convert(IStrategoTerm term, EStructuralFeature feature) {
		
		switch (term.getTermType()) {

		case IStrategoTerm.APPL:
			return convert((IStrategoAppl) term, feature);
			
		case IStrategoTerm.STRING: 
			return convert((IStrategoString) term, feature);

		case IStrategoTerm.LIST: 
			return convert((IStrategoList) term, feature);

		default:		
			return null;
		}
	}
	
	protected abstract Object convert(IStrategoAppl term, EStructuralFeature feature);

	protected abstract Object convert(IStrategoString term, EStructuralFeature feature);
	
	protected abstract List<Object> convert(IStrategoList term, EStructuralFeature feature);

	protected abstract void setReferences();
}
