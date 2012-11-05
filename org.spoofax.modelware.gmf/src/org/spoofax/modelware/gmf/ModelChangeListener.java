package org.spoofax.modelware.gmf;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.util.EContentAdapter;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.spoofax.modelware.gmf.BridgeEvent;

/**
 * @author Oskar van Rest
 */
public class ModelChangeListener extends EContentAdapter {

	private EditorPair editorPair;
	private boolean debouncer;

	public ModelChangeListener(EditorPair editorPair) {
		this.editorPair = editorPair;
		editorPair.registerObserver(new Debouncer());
	}

	public void notifyChanged(Notification n) {
		super.notifyChanged(n);

		if (debouncer) {
			return;
		}
		
		if (n.getEventType() != Notification.REMOVING_ADAPTER) {
			Bridge.getInstance().model2Term(editorPair);
			
			//TODO: put this elsewhere
			ISelectionProvider selectionProvider = editorPair.getDiagramEditor().getSite().getSelectionProvider();
			selectionProvider.setSelection(selectionProvider.getSelection());
		}
	}
	
	private class Debouncer implements EditorPairObserver {

		@Override
		public void notify(BridgeEvent event) {
			if (event == BridgeEvent.PreTerm2Model) {
				debouncer = true;
			}
			if (event == BridgeEvent.PostTerm2Model) {
				debouncer = false;
			}
		}
	}
}