package org.spoofax.modelware.gmf;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.util.EContentAdapter;
import org.spoofax.modelware.gmf.BridgeEvent;

/**
 * @author Oskar van Rest
 */
public class ModelChangeListener extends EContentAdapter {

	private EditorPair editorPair;
	private boolean debouncer;
	private long timeOfLastChange;
	private static final long timeout = 700;
	private Thread thread;

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
			timeOfLastChange = System.currentTimeMillis();
			if (thread == null || !thread.isAlive()) {
				thread = new Thread(new Timer());
				thread.start();
			}
		}
	}
	
	private class Timer implements Runnable {
		public void run() {
			try {
				long different = -1;
				while (different < timeout) {
					different = System.currentTimeMillis() - timeOfLastChange;
					Thread.sleep(Math.max(0, timeout - different));
				}
				Bridge.getInstance().handleModelChange(editorPair);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private class Debouncer implements EditorPairObserver {

		@Override
		public void notify(BridgeEvent event) {
			if (event == BridgeEvent.PreMerge) {
				debouncer = true;
			}
			if (event == BridgeEvent.PostMerge) {
				debouncer = false;
			}
		}
	}
}