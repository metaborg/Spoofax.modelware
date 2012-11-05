package org.spoofax.modelware.gmf;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.util.EContentAdapter;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.spoofax.modelware.gmf.EditorPair.BridgeEvent;

/**
 * @author Oskar van Rest
 */
public class ModelChangeListener extends EContentAdapter {

	private EditorPair editorPair;
	private final int debounceConstant = 500;
	private long debouncer;

	public ModelChangeListener(EditorPair editorPair) {
		this.editorPair = editorPair;
		editorPair.registerObserver(new Debouncer());
	}

	public void notifyChanged(Notification n) {
		super.notifyChanged(n);

		if (n.getEventType() != Notification.REMOVING_ADAPTER) {
			if (System.currentTimeMillis() - debouncer < debounceConstant)
				return;
			
			Bridge.getInstance().model2Term(editorPair);
			
			//TODO: move somewhere else
			ISelectionProvider selectionProvider = editorPair.getDiagramEditor().getSite().getSelectionProvider();
			selectionProvider.setSelection(selectionProvider.getSelection());
		}
	}
	
	private class Debouncer implements EditorPairObserver {

		@Override
		public void notify(BridgeEvent event) {
			if (event == BridgeEvent.Term2Model) {
				debouncer = System.currentTimeMillis();
			}
		}
	}
}