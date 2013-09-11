package org.spoofax.modelware.gmf;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.util.EContentAdapter;
import org.spoofax.modelware.emf.utils.SpoofaxEMFUtils;
import org.spoofax.terms.AbstractTermFactory;
import org.spoofax.terms.TermFactory;

/**
 * Listens for changes in GMF's semantic model and perform a model-to-text
 * transformation upon such a change.
 * 
 * @author Oskar van Rest
 */
public class ModelChangeListener extends EContentAdapter {

	private EditorPair editorPair;
	private boolean debounce;
//	private long timeOfLastChange;
//	private static final long timeout = 100;
//	private Thread thread;

	public ModelChangeListener(EditorPair editorPair) {
		this.editorPair = editorPair;
		editorPair.registerObserver(new Debouncer());
	}

	public void notifyChanged(Notification n) {
		super.notifyChanged(n);
		
		AbstractTermFactory f = SpoofaxEMFUtils.termFactory;
				
		if (debounce) {
			return;
		}
		
		if (n.getEventType() == Notification.SET) {
			editorPair.debounce = true;
			SpoofaxEMFUtils.invokeStrategy(null, editorPair.getTextEditor().getParseController(), null);
//			editorPair.doModelToTerm(EditorPairUtil.getSemanticModel(editorPair.getDiagramEditor()));
		
		}
	}

//	private class Timer implements Runnable {
//		public void run() {
//			try {
//				long different = -1;
//				while (different < timeout) {
//					different = System.currentTimeMillis() - timeOfLastChange;
//					Thread.sleep(Math.max(0, timeout - different));
//				}
//				editorPair.debounce = true;
//				
//				editorPair.doModelToTerm(EditorPairUtil.getSemanticModel(editorPair.getDiagramEditor()));
//			}
//			catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		}
//	}

	private class Debouncer implements EditorPairObserver {

		@Override
		public void notify(EditorPairEvent event) {
			
			if (event == EditorPairEvent.PreMerge) {
				debounce = true;
			}
			else if (event == EditorPairEvent.PostMerge) {
				debounce = false;
			}

			else if (event == EditorPairEvent.PreUndo || event == EditorPairEvent.PreRedo) {
				debounce = true;
			}
			else if (event == EditorPairEvent.PostUndo || event == EditorPairEvent.PostRedo) {
				debounce = false;
			}
		}
	}
}