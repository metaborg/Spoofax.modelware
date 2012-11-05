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
import org.spoofax.modelware.emf.Object2Subterm;
import org.spoofax.modelware.gmf.EditorPair;
import org.spoofax.modelware.gmf.BridgeUtil;
import org.strategoxt.imp.runtime.EditorState;

/**
 * @author Oskar van Rest
 */
public class DiagramSelectionChangedListener implements ISelectionChangedListener {

	private EditorPair editorPair;

	public DiagramSelectionChangedListener(EditorPair editorPair) {
		this.editorPair = editorPair;
	}

	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		if (!editorPair.getDebouncer().diagramSelectionAllowed())
			return;

		IEditorPart textEditor = editorPair.getTextEditor();
		IStrategoTerm AST = EditorState.getEditorFor(textEditor).getCurrentAst();
		
		List<EObject> selectedObjects = getSelectedEObjects(event);
		TextSelection textSelection = calculateTextSelection(selectedObjects, AST);
		
		ISelectionProvider selectionProvider = textEditor.getEditorSite().getSelectionProvider();
		selectionProvider.setSelection(textSelection);
	}
	
	private TextSelection calculateTextSelection(List<EObject> selectedObjects, IStrategoTerm AST) {
		int left = Integer.MAX_VALUE;
		int right = Integer.MIN_VALUE;

		EObject root = BridgeUtil.getSemanticModel(editorPair.getDiagramEditor());

		for (int i = 0; i < selectedObjects.size(); i++) {
			if (EcoreUtil.isAncestor(root, selectedObjects.get(i))) { // only take non-phantom nodes into account
				IStrategoTerm selectedTerm = new Object2Subterm().object2subterm(selectedObjects.get(i), AST);

				if (selectedTerm != null) {
					int newLeft = (ImploderAttachment.getLeftToken(selectedTerm).getStartOffset());
					int newRight = (ImploderAttachment.getRightToken(selectedTerm).getStartOffset());

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
}
