package org.spoofax.modelware.gmf.editorservices.selectionsharing;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.gef.EditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramEditPart;
import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramEditor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.modelware.emf.Subterm2Object;
import org.spoofax.modelware.gmf.EditorPair;
import org.spoofax.modelware.gmf.GMFBridgeUtil;
import org.strategoxt.imp.runtime.EditorState;

public class TextSelectionChangedListener implements ISelectionChangedListener {

	private EditorPair editorPair;

	public TextSelectionChangedListener(EditorPair editorPair) {
		this.editorPair = editorPair;
	}

	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		if (illegalSelection())
			return;
		if (!editorPair.getDebouncer().textSelectionAllowed())
			return;

		IStrategoTerm selection = EditorState.getActiveEditor().getSelectionAst(true);
		DiagramEditor diagramEditor = editorPair.getDiagramEditor();
		if (selection == null) {
			diagramEditor.getSite().getSelectionProvider().setSelection(new StructuredSelection());
			return;
		}

		List<IStrategoAppl> selectedIStrategoAppls = filterIStrategoAppls(selection);

		EObject root = GMFBridgeUtil.getSemanticModel(diagramEditor);
		List<EObject> eObjectsToSelect = strategoApplToEObject(selectedIStrategoAppls, root);
		eObjectsToSelect = addAllContents(eObjectsToSelect);
		List<EditPart> editPartsToSelect = eObjectsToEditPart(eObjectsToSelect, diagramEditor.getDiagramEditPart());

		diagramEditor.getSite().getSelectionProvider().setSelection(new StructuredSelection(editPartsToSelect));
	}

	private boolean illegalSelection() {
		try {
			EditorState.getActiveEditor().getSelectionAst(true);
		} catch (Exception e) {
			return true; // this happens when you select text, delete it, and choose 'undo'
		}
		return false;
	}

	private List<EObject> addAllContents(List<EObject> objectsToSelect) {
		List<EObject> result = new LinkedList<EObject>();
		TreeIterator<Object> it = EcoreUtil.getAllContents(objectsToSelect);

		while (it.hasNext()) {
			Object object = it.next();
			if (object instanceof EObject) {
				result.add((EObject) object);
			}
		}

		return result;
	}

	private List<EditPart> eObjectsToEditPart(List<EObject> objects, DiagramEditPart diagramEditPart) {
		List<EditPart> result = new LinkedList<EditPart>();

		for (EObject eObject : objects) {
			EditPart editPart = diagramEditPart.findEditPart(diagramEditPart, eObject);
			if (editPart != null) {
				result.add(editPart);
			}
		}

		return result;
	}

	private List<EObject> strategoApplToEObject(List<IStrategoAppl> appls, EObject root) {
		List<EObject> result = new LinkedList<EObject>();

		for (int i = 0; i < appls.size(); i++) {
			try {
				EObject eObject = new Subterm2Object().subterm2object(appls.get(i), root);
				result.add(eObject);
			} catch (Exception e) {
				// Exception occurs when selected term has no model correspondence, which is the case if the text has not yet been parsed.
				// TODO: Exception occurs when text2model and model2text transformations have been customized.
			}
		}

		return result;
	}

	private List<IStrategoAppl> filterIStrategoAppls(IStrategoTerm selection) {
		List<IStrategoAppl> result = new LinkedList<IStrategoAppl>();

		switch (selection.getTermType()) {
		case IStrategoTerm.APPL:
			IStrategoAppl appl = (IStrategoAppl) selection;
			if (appl.getConstructor().getName().equals("Some")) {
				if (appl.getSubterm(0).getTermType() == IStrategoTerm.APPL) {
					result.add((IStrategoAppl) appl.getSubterm(0));
				}
			} else {
				result.add((IStrategoAppl) selection);
			}
			break;
		case IStrategoTerm.LIST:
			IStrategoList list = (IStrategoList) selection;
			for (int i = 0; i < ((IStrategoList) selection).size(); i++) {
				if (list.head().getTermType() == IStrategoTerm.APPL) {
					result.add((IStrategoAppl) list.head());
				}
				list = list.tail();
			}
			break;
		default:
			break;
		}

		return result;
	}
}
