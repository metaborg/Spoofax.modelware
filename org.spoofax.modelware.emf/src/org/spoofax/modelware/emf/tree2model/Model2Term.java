package org.spoofax.modelware.emf.tree2model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.EMap;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.spoofax.interpreter.terms.IStrategoConstructor;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.imp.runtime.Environment;

/**
 * @author Oskar van Rest
 */
public class Model2Term extends AbstractModel2Term {
	
	public Model2Term(ITermFactory factory) {
		super(factory);
	}

	@Override
	public IStrategoTerm convert(EObject object) {
		EClass c = object.eClass();
		EMap<String, String> index2name = c.getEAnnotation("spoofax.term2feature").getDetails();

		IStrategoConstructor constructor = factory.makeConstructor(c.getName(), c.getEStructuralFeatures().size());
		ArrayList<IStrategoTerm> kids = new ArrayList<IStrategoTerm>();

		for (int i = 0; i < index2name.size(); i++) {
			EStructuralFeature eSructuralFeature = c.getEStructuralFeature(index2name.get(Integer.toString(i)));
			kids.add(convert(object, eSructuralFeature));
		}

		return factory.makeAppl(constructor, 
				(IStrategoTerm[]) kids.toArray(new IStrategoTerm[kids.size()]), 
				factory.makeList());
	}

	@Override
	protected IStrategoTerm convert(EObject object, EAttribute attribute) {
		Object value = object.eGet(attribute);

		if (value == null) {
			return null;
		} else {
			return factory.makeString(value.toString());
		}
	}

	@Override
	protected IStrategoTerm convert(EObject object, EReference reference) {
		Object value = object.eGet(reference);

		if (!reference.isMany()) {
			return convertRef((EObject) value, reference);
		} else {
			EList<?> elements = (EList<?>) value;
			ArrayList<IStrategoTerm> results = new ArrayList<IStrategoTerm>();
			
			for (Object element : elements) {
				if (element instanceof EObject) {
					results.add(convertRef((EObject) element, reference));
				}
			}
			
			return factory.makeList(results);
		}
	}
	
	protected IStrategoTerm convertRef(EObject object, EReference reference) {
		if (reference.isContainment()) {
			return convert(object);
		} else {
			if (object != null) {
				Object ID = getIdentifier(object);
				return factory.makeString(ID.toString());
			}
			else {
				return factory.makeString(reference.getName()); //TODO: create support for spoofax.defaultValue annotation
			}
		}
	}
	

	@Override
	protected IStrategoTerm someOrNone(IStrategoTerm term) {
		if (term == null)
			return factory.makeAppl(factory.makeConstructor("None", 0));
		else
			return factory.makeAppl(factory.makeConstructor("Some", 1), term);
	}

	@Override
	protected IStrategoTerm createDefaultValue(EAttribute attribute) {
		String defaultValue = attribute.getDefaultValueLiteral();
		if (defaultValue != null) {
			return factory.makeString(defaultValue);	
		}
		else {
			return factory.makeString(attribute.getName());
		}
	}

	@Override
	protected IStrategoTerm createDefaultValue(EReference reference) {
		EAttribute identifyingAttribute = getIdentifyingAttribute(reference.getEReferenceType());
		
		Environment.logException("Class " + reference.getEType().getInstanceClassName() + " does not provide an identifying attribute (spoofax.def) even though it is referenced."); 
		
		return createDefaultValue(identifyingAttribute);

	}
	
	private Object getIdentifier(EObject eObject) {
		EAttribute eIdentifyingAttribute = getIdentifyingAttribute(eObject.eClass());
		if (eIdentifyingAttribute != null) {
			return eObject.eGet(eIdentifyingAttribute);
		}
		
		return null;
	}
	
	private EAttribute getIdentifyingAttribute(EClass eClass) {
		List<EAttribute> identifyingAttributes = getIdentifyingAttributes(eClass);
		
		if (identifyingAttributes.size() > 0) {
			return identifyingAttributes.get(0);
		}
		else {
			return null;
		}
	}
	
	private List<EAttribute> getIdentifyingAttributes(EClass eClass) {
		ArrayList<EAttribute> result = new ArrayList<EAttribute>();
		for (EAttribute attibute : eClass.getEAllAttributes()) {
			if (attibute.getEAnnotation("spoofax.def") != null) {
				result.add(attibute);
			}
		}
		
		return result;
	}
	
}
