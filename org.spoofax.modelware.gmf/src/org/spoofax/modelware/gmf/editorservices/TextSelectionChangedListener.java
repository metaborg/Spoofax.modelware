package org.spoofax.modelware.gmf.editorservices;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gef.EditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramEditPart;
import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramEditor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.modelware.emf.utils.Subterm2Subobject;
import org.spoofax.modelware.emf.utils.Utils;
import org.spoofax.modelware.gmf.EditorPair;
import org.spoofax.modelware.gmf.EditorPairEvent;
import org.spoofax.modelware.gmf.EditorPairObserver;
import org.spoofax.modelware.gmf.EditorPairUtil;
import org.strategoxt.imp.runtime.EditorState;
import org.strategoxt.imp.runtime.services.StrategoObserver;
import org.strategoxt.imp.runtime.stratego.StrategoTermPath;

/**
 * Listens for changes in the set of selected textual elements and selects the corresponding set of
 * graphical elements upon such a change.
 */
public class TextSelectionChangedListener implements ISelectionChangedListener {

	private EditorPair editorPair;
	private boolean debounce;

	public TextSelectionChangedListener(EditorPair editorPair) {
		this.editorPair = editorPair;
		editorPair.registerObserver(new Debouncer());
	}

	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		if (!editorPair.isTextToDiagramSelectionEnabled()) {
			return;
		}
		
		if (debounce) {
			return;
		}

		if (!isValidSelection()) {
			return;
		}

		IStrategoTerm selection = EditorState.getEditorFor(editorPair.getTextEditor()).getSelectionAst(true);
		DiagramEditor diagramEditor = editorPair.getDiagramEditor();

		editorPair.notifyObservers(EditorPairEvent.PreText2DiagramSelection);
		
		if (selection == null) {
			diagramEditor.getSite().getSelectionProvider().setSelection(new StructuredSelection());
		}
		else {
			EObject root = EditorPairUtil.getSemanticModel(diagramEditor);
			List<EObject> eObjectsToSelect = strategoApplToEObjects(selection, root);
			eObjectsToSelect = addAllContents(eObjectsToSelect);
			List<EditPart> editPartsToSelect = eObjectsToEditPart(eObjectsToSelect, diagramEditor.getDiagramEditPart());
			diagramEditor.getSite().getSelectionProvider().setSelection(new StructuredSelection(editPartsToSelect));
		}
	}

	/**
	 * Selection is not valid when user deletes text and chooses to undo the change
	 */
	private boolean isValidSelection() {
		try {
			EditorState.getActiveEditor().getSelectionAst(true);
		}
		catch (Exception e) {
			return false;
		}
		return true;
	}

	private List<EObject> addAllContents(List<EObject> objectsToSelect) {
		List<EObject> result = new LinkedList<EObject>();
		result.addAll(objectsToSelect);
		
		for (EObject objectToSelect : objectsToSelect) {
			TreeIterator<EObject> it = objectToSelect.eAllContents();

			while (it.hasNext()) {
				result.add(it.next());
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

	private List<EObject> strategoApplToEObjects(IStrategoTerm selection, EObject root) {
		List<EObject> result = new LinkedList<EObject>();
		
		if (selection instanceof IStrategoList) {
			IStrategoTerm[] subterms = selection.getAllSubterms();
			for (int i=0; i<subterms.length; i++) {
				result.addAll(strategoApplToEObjects(subterms[i], root));
			}
		}
		else {
			IStrategoList adjustedASTSelection = null;
			StrategoObserver observer = Utils.getObserver(EditorState.getEditorFor(editorPair.getTextEditor()));
			observer.getLock().lock();
			try {
				adjustedASTSelection = StrategoTermPath.getTermPathWithOrigin(observer.getRuntime().getCompiledContext(), editorPair.ASTgraph, selection);
			}
			finally {
				observer.getLock().unlock();
			}
			if (adjustedASTSelection != null) {
				EObject eObject = Subterm2Subobject.path2object(adjustedASTSelection, root);
				if (eObject != null) {
					result.add(eObject);
				}
			}
		}

		return result;
	}

	private class Debouncer implements EditorPairObserver {

		@Override
		public void notify(EditorPairEvent event) {
			if (event == EditorPairEvent.PreDiagram2TextSelection) {
				debounce = true;
			}
			if (event == EditorPairEvent.PostDiagram2TextSelection) {
				debounce = false;
			}
		}
	}
}
