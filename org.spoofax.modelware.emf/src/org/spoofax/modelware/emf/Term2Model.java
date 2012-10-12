package org.spoofax.modelware.emf;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

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
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;

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
		IStrategoList path = ((IStrategoList) term.getAnnotations().head()).tail();
		List<EObject> defs = findDefs(path, eReference.getEType());

		if (defs.size() >= 1) {
			EObject firstDef = defs.get(0);
			source.eSet(eReference, firstDef);
		}
	}

	/**
	 * Returns all EObjects of type 'type' with containment hierarchy 'path'
	 */
	private List<EObject> findDefs(IStrategoList path, EClassifier type) {
		List<EObject> defs = new LinkedList<EObject>();

		findDefs(path, defs);

		Iterator<EObject> it = defs.iterator();
		while (it.hasNext()) {
			EObject eObject = it.next();
			if (!type.isInstance(eObject)) {
				it.remove();
			}
		}

		return defs;
	}

	private void findDefs(IStrategoList path, List<EObject> defs) {
		if (path.size() == 1) {
			defs.add(root);
		} else {
			findDefs(path.tail(), defs);

			IStrategoString childID = (IStrategoString) path.head();
			ListIterator<EObject> it = defs.listIterator();
			while (it.hasNext()) {
				EObject eObject = it.next();
				it.remove();
				findDefs(eObject, childID, it);
			}

		}
	}

	private void findDefs(EObject object, IStrategoString childID, ListIterator<EObject> it) {
		for (EObject child : object.eContents()) {
			String eChildID = EcoreUtil.getID(child);

			if (eChildID == null) {
				findDefs(child, childID, it);
			} else if (eChildID.equals(childID.stringValue())) {
				it.add(child);
			}
		}
	}
}
