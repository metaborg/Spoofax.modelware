package org.spoofax.modelware.gmf;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.util.EContentAdapter;
import org.eclipse.jface.viewers.ISelectionProvider;

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
			
			GMFBridge.getInstance().model2Term(editorPair);
			
			ISelectionProvider selectionProvider = editorPair.getDiagramEditor().getSite().getSelectionProvider();
			selectionProvider.setSelection(selectionProvider.getSelection());
		}
	}
}