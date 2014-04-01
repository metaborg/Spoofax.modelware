package org.spoofax.modelware.gmf.editorservices;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.gef.EditPart;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.editors.text.TextEditor;
import org.spoofax.modelware.emf.editorservices.TextSelectionUtil;
import org.spoofax.modelware.emf.utils.Utils;
import org.spoofax.modelware.gmf.EditorPair;
import org.spoofax.modelware.gmf.EditorPairEvent;
import org.spoofax.modelware.gmf.EditorPairObserver;
import org.spoofax.modelware.gmf.EditorPairUtil;
import org.strategoxt.imp.runtime.EditorState;

/**
 * Listens for changes in the set of selected graphical elements and selects the corresponding set 
 * of textual elements upon such a change.
 * 
 * @author Oskar van Rest
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
		assert editorPair.ASTgraph != null;
		
		if (!editorPair.isDiagramToTextSelectionEnabled()) {
			return;
		}
		
		if (debounce) {
			debounce = false;
			return;
		}

		TextEditor textEditor = editorPair.getTextEditor();
		List<EObject> selectedObjects = getSelectedEObjects(event);
		EObject root = EditorPairUtil.getSemanticModel(editorPair.getDiagramEditor());
		TextSelection selection = TextSelectionUtil.calculateTextSelection(Utils.getObserver(EditorState.getEditorFor(editorPair.getTextEditor())), selectedObjects, root, editorPair.ASTgraph);
		
		editorPair.notifyObservers(EditorPairEvent.PreDiagram2TextSelection);
		TextSelectionUtil.setTextSelection(textEditor, selection);
		editorPair.notifyObservers(EditorPairEvent.PostDiagram2TextSelection);
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
