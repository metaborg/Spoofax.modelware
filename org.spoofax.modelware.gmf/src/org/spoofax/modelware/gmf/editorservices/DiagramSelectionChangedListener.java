package org.spoofax.modelware.gmf.editorservices;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.gef.EditPart;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.imploder.ImploderAttachment;
import org.spoofax.modelware.emf.utils.Subobject2Subterm;
import org.spoofax.modelware.gmf.EditorPairEvent;
import org.spoofax.modelware.gmf.EditorPair;
import org.spoofax.modelware.gmf.EditorPairUtil;
import org.spoofax.modelware.gmf.EditorPairObserver;
import org.spoofax.terms.attachments.OriginAttachment;

/**
 * Listens for changes in the set of selected graphical elements and selects the corresponding set 
 * of textual elements upon such a change.
 * 
 * @author oskarvanrest
 */
public class DiagramSelectionChangedListener implements ISelectionChangedListener {

	private EditorPair editorPair;
	private boolean debounce;

	public DiagramSelectionChangedListener(EditorPair editorPair) {
		this.editorPair = editorPair;
		editorPair.registerObserver(new Debouncer());
	}

	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		assert editorPair.adjustedAST != null;
		
		if (debounce) {
			debounce = false;
			return;
		}

		IEditorPart textEditor = editorPair.getTextEditor();
		
		List<EObject> selectedObjects = getSelectedEObjects(event);
		
		TextSelection textSelection = calculateTextSelection(selectedObjects, editorPair.adjustedAST);
		
		ISelectionProvider selectionProvider = textEditor.getEditorSite().getSelectionProvider();
		
		editorPair.notifyObservers(EditorPairEvent.PreDiagram2TextSelection);
		selectionProvider.setSelection(textSelection);
		editorPair.notifyObservers(EditorPairEvent.PostDiagram2TextSelection);
	}
		
	private TextSelection calculateTextSelection(List<EObject> selectedObjects, IStrategoTerm AST) {
		int left = Integer.MAX_VALUE;
		int right = Integer.MIN_VALUE;

		EObject root = EditorPairUtil.getSemanticModel(editorPair.getDiagramEditor());

		for (int i = 0; i < selectedObjects.size(); i++) {
			if (EcoreUtil.isAncestor(root, selectedObjects.get(i))) { // only take non-phantom nodes into account
				IStrategoTerm selectedTerm = Subobject2Subterm.object2subterm(selectedObjects.get(i), AST);

				if (selectedTerm != null) {
					IStrategoTerm originTerm = OriginAttachment.getOrigin(selectedTerm);
					int newLeft = (ImploderAttachment.getLeftToken(originTerm).getStartOffset());
					int newRight = (ImploderAttachment.getRightToken(originTerm).getEndOffset()) + 1;

					if (newLeft < left) {
						left = newLeft;
					}
					if (newRight > right) {
						right = newRight;
					}
				}
			}
		}
		
		if (left != Integer.MAX_VALUE && right != Integer.MIN_VALUE) {
			return new TextSelection(left, right - left);
		} else {
			return new TextSelection(0, 0);
		}
	}

	private List<EObject> getSelectedEObjects(SelectionChangedEvent event) {
		ISelection selection = event.getSelection();
		LinkedList<EObject> result = new LinkedList<EObject>();

		if (!(selection instanceof StructuredSelection)) {
			return result;
		}

		List<?> selectedParts = ((StructuredSelection) selection).toList();

		for (Object o : selectedParts) {
			if (o instanceof EditPart) {
				EditPart editPart = (EditPart) o;
				if (editPart.getModel() instanceof View) {
					View view = (View) editPart.getModel();
					if (!(view instanceof Diagram) && view.getElement() != null) {
						result.add(view.getElement());
					}
				}
			}
		}

		return result;
	}
	
	private class Debouncer implements EditorPairObserver {

		@Override
		public void notify(EditorPairEvent event) {
			
			// debouncing of diagram selections by user
			if (event == EditorPairEvent.PreText2DiagramSelection) {
				debounce = true;
			}
			
			// debouncing of diagram selections during model merging
			if (event == EditorPairEvent.PreMerge) {
				debounce = true;
			}
			if (event == EditorPairEvent.PostMerge) {
				debounce = false;
			}
		}
	}
}
