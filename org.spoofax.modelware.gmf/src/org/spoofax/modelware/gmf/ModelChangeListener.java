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
			Bridge.getInstance().handleModelChange(editorPair);
		}
	}
	
	private class Debouncer implements EditorPairObserver {

		@Override
		public void notify(BridgeEvent event) {
			if (event == BridgeEvent.PreMerge) {
				debouncer = true;
			}
			if (event == BridgeEvent.PostMerge2) {
				debouncer = false;
			}
		}
	}
}