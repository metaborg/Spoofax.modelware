package org.spoofax.modelware.gmf;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.util.EContentAdapter;
import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramEditor;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IEditorPart;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.modelware.emf.Model2Term;
import org.strategoxt.imp.runtime.EditorState;
import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;
import org.strategoxt.imp.runtime.services.ITextReplacer;
import org.strategoxt.lang.Context;

public class SemanticModelContentAdapter extends EContentAdapter {

	private EditorPair editorPair;

	public SemanticModelContentAdapter(EditorPair editorPair) {
		this.editorPair = editorPair;
	}

	public void notifyChanged(Notification n) {
		super.notifyChanged(n);

		if (n.getEventType() != Notification.REMOVING_ADAPTER) {
			if (!editorPair.getDebouncer().model2textAllowed())
				return;
			
			IEditorPart textEditor = editorPair.getTextEditor();
			DiagramEditor diagramEditor = editorPair.getDiagramEditor();
			Context context = editorPair.getContext();

			IStrategoTerm currentTerm = EditorState.getEditorFor(textEditor).getCurrentAst();
			IStrategoTerm newTerm = new Model2Term(context.getFactory()).convert(GMFBridgeUtil.getSemanticModel(diagramEditor));

			EditorState editor = EditorState.getEditorFor(textEditor);
			ITextReplacer textReplacer = null;
			try {
				textReplacer = editor.getDescriptor().createService(ITextReplacer.class, editor.getParseController());
			} catch (BadDescriptorException e) {
				e.printStackTrace();
			}

			textReplacer.replaceText(context.getFactory().makeList(context.getFactory().makeTuple(currentTerm, newTerm)));
			
			ISelectionProvider selectionProvider = diagramEditor.getSite().getSelectionProvider();
			selectionProvider.setSelection(selectionProvider.getSelection());
		}
	}
}