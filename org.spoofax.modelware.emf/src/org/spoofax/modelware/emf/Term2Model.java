package org.spoofax.modelware.emf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.EMap;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.validation.internal.service.IClientContext;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.Environment;

/**
 * @author Oskar van Rest
 */
public class Term2Model extends AbstractTerm2Model {

	private ArrayList<Object[]> references;
	private EObject root;
	protected final EPackage MM;

	public Term2Model(EPackage MM) {
		this.MM = MM;
	}

	@Override
	public EObject convert(IStrategoTerm term) {
		references = new ArrayList<Object[]>();
		root = (EObject) convert((IStrategoAppl) term, null);
		convertReferences();
		return root;
	}

	@Override
	protected Object convert(IStrategoString term, EStructuralFeature feature) {
		if (feature instanceof EAttribute) {
			EAttribute eAttribute = (EAttribute) feature;
			EDataType eDataType = eAttribute.getEAttributeType();
			return EcoreUtil.createFromString(eDataType, term.stringValue());
		}

		return null;
	}

	@Override
	protected List<Object> convert(IStrategoList term, EStructuralFeature feature) {

		if (term.isEmpty())
			return new LinkedList<Object>();
		else {
			List<Object> result = convert(term.tail(), feature);
			result.add(0, convert(term.head(), feature));
			return result;
		}
	}

	@Override
	protected Object convert(IStrategoAppl term, EStructuralFeature feature) {
		String constructor = term.getConstructor().getName();

		if (constructor.equals("Some")) {
			return convert(term.getSubterm(0), feature);
		}
		if (constructor.equals("None")) {
			return null;
		}

		EClass c = (EClass) MM.getEClassifier(constructor);
		EMap<String, String> index2name = c.getEAnnotation("StrategoTerm.index").getDetails();

		EObject result = MM.getEFactoryInstance().create(c);
		for (int i = 0; i < term.getSubtermCount(); i++) {
			EStructuralFeature f = c.getEStructuralFeature(index2name.get(Integer.toString(i)));

			if (f instanceof EReference && !((EReference) f).isContainment()) {
				Object[] reference = { term.getSubterm(i), result, f };
				references.add(reference);
			} else {
				Object value = convert(term.getSubterm(i), f);
				result.eSet(f, value);
			}
		}

		return result;
	}

	protected void convertReferences() {
		for (int i = 0; i < references.size(); i++) {
			IStrategoTerm term = (IStrategoTerm) references.get(i)[0];
			EObject source = (EObject) references.get(i)[1];
			EReference eReference = (EReference) references.get(i)[2];

			switch (term.getTermType()) {

			case IStrategoTerm.STRING:
				setReference((IStrategoString) term, source, eReference);
				break;

			case IStrategoTerm.LIST:
				// TODO not supported by Spoofax (also see Spoofax/530)
				break;

			default:
				break;

			}
		}
	}

	private void setReference(IStrategoString term, EObject source, EReference eReference) {
		if (
				term.getAnnotations().size() == 0 || 
				!(term.getAnnotations().head().isList()) || 
				((IStrategoList) term.getAnnotations().head()).size() <= 1) {
			Environment.logException("The analysed AST does not provide an index for reference " + term.toString() + ", or, the index is not in the expected format. Most likely, there is something wrong with the name binding specification of your language.");
			return;
		}
		
		IStrategoList path = ((IStrategoList) term.getAnnotations().head()).tail();
		List<EObject> defs = findDefs(path, eReference.getEType());

		if (defs.size() >= 1) {
			EObject firstDef = defs.get(0);
			source.eSet(eReference, firstDef);
		}
	}

	
	


	private Set<EClass> getSubtypes(EPackage pkg, EClass eType) {
		Set<EClass> result = new HashSet<EClass> ();
		List<EClassifier> classifiers = pkg.getEClassifiers();

		Iterator<EClassifier> it = classifiers.iterator();
		while(it.hasNext()) {
			EClassifier nextClassifier = (EClassifier) it.next();
			if (nextClassifier instanceof EClass && ((EClass) nextClassifier).getEAllSuperTypes().contains(eType)) {
					result.add((EClass) nextClassifier);
			}
		}
		return result;
	}


	private Set<EClass> getEAllContainedTypes(EObject container, EReference reference) {
		Set<EClass> result = new HashSet<EClass> ();
		EClass containerEClass = container.eClass();
		return getEAllContainedTypes(containerEClass, reference, result);
	}
	
	private Set<EClass> getEAllContainedTypes(EClass containerEClass, EReference reference, Set<EClass> result) {	
		if (reference.isContainment() && (containerEClass.getEAllReferences().contains(reference))) {
			
			EClass eType = (EClass) reference.getEType();
			Set<EClass> types = getSubtypes(containerEClass.getEPackage(), eType);
			types.add(eType);
			result.addAll(types);
		
			Iterator<EClass> it = types.iterator();
			while(it.hasNext()) {
				EClass type = it.next();				
				EList<EReference> refs = type.getEAllContainments();
				
				for (int i=0; i<refs.size(); i++) {
					EReference ref = refs.get(i);
					if (!result.contains(ref.getEType())) {
						result.addAll(getEAllContainedTypes(type, ref, result));
					}
				}
			}
		}
		
		return result;
	}
	
	
	/**
	 * Returns all EObjects of type 'type' with containment hierarchy 'path'
	 */
	private List<EObject> findDefs(IStrategoList path, EClassifier targetType) {
		List<EObject> defs = new LinkedList<EObject>();

		findDefs(path, defs, targetType);

		Iterator<EObject> it = defs.iterator();
		while (it.hasNext()) {
			EObject eObject = it.next();
			if (!targetType.isInstance(eObject)) {
				it.remove();
			}
		}
		
		return defs;
	}
	
	private void findDefs(IStrategoList path, List<EObject> defs, EClassifier targetType) {
		if (path.size() == 1) {
			defs.add(root);
		} else {
			findDefs(path.tail(), defs, targetType);

			IStrategoString childID = (IStrategoString) path.head();
			ListIterator<EObject> it = defs.listIterator();
			while (it.hasNext()) {
				EObject eObject = it.next();
				it.remove();
				findDefs(eObject, childID, it, targetType);
			}

		}
	}

	@SuppressWarnings("unchecked")
	private void findDefs(EObject object, IStrategoString childID, ListIterator<EObject> it, EClassifier targetType) {
		EList<EReference> containmentRefs = object.eClass().getEAllContainments();
		EList<EObject> childsToConsider = new BasicEList<EObject>();
		
		for (EReference ref : containmentRefs) {
			if (getEAllContainedTypes(object, ref).contains(targetType)) {
				Object refValue = object.eGet(ref);
				if (refValue instanceof EList<?>) {
					childsToConsider.addAll((Collection<? extends EObject>) refValue);
				}
				else if (refValue instanceof EObject) {
					childsToConsider.add((EObject) refValue);
				}
			}
		}
		
		for (EObject child : childsToConsider) {
			String eChildID = EcoreUtil.getID(child);

			if (eChildID == null) {
				findDefs(child, childID, it, targetType);
			} else if (eChildID.equals(childID.stringValue())) {
				it.add(child);
			}
		}
	}
}
