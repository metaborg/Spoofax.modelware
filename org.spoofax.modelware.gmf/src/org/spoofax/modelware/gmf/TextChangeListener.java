package org.spoofax.modelware.gmf;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.imp.parser.IModelListener;
import org.eclipse.imp.parser.IParseController;
import org.spoofax.modelware.emf.utils.SpoofaxEMFUtils;
import org.strategoxt.imp.runtime.EditorState;

/**
 * Listens for text changes and performs a text-to-model transformation if parsing of the text 
 * results in a changed AST.
 * 
 * @author oskarvanrest
 */
public class TextChangeListener implements IModelListener {

	private EditorPair editorPair;
	private long timeOfLastChange;
	private Thread thread;
	private static final long keyStrokeTimeout = 350;
	private boolean debounce;

	public TextChangeListener(EditorPair editorPair) {
		this.editorPair = editorPair;
		editorPair.registerObserver(new Debouncer());
	}

	@Override
	public AnalysisRequired getAnalysisRequired() {
		return AnalysisRequired.NONE;
	}

	@Override
	public void update(IParseController controller, IProgressMonitor monitor) {	
		if (debounce) {
			return;
		}
		
		timeOfLastChange = System.currentTimeMillis();
		if (thread == null || !thread.isAlive()) {
			thread = new Thread(new Timer());
			thread.start();
		}
	}

	private class Timer implements Runnable {
		public void run() {
			try {
				long different = -1;
				while (different < keyStrokeTimeout) {
					different = System.currentTimeMillis() - timeOfLastChange;
					Thread.sleep(Math.max(0, keyStrokeTimeout - different));
				}
				editorPair.doTerm2Model();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private class Debouncer implements EditorPairObserver {

		@Override
		public void notify(EditorPairEvent event) {			
			if (event == EditorPairEvent.PreLayoutPreservation) {
				debounce = true;
			}
			else if (event == EditorPairEvent.PostLayoutPreservation) {
				debounce = false;
			}
			
			// set editorPair.adjustedTree needed for proper working of selection sharing
			if (event == EditorPairEvent.PostLayoutPreservation) {
				EditorState editorState = EditorState.getEditorFor(editorPair.getTextEditor());
				editorPair.adjustedAST = SpoofaxEMFUtils.getAdjustedAST(editorState);
			}
			
			if (event == EditorPairEvent.PreUndo) {
				debounce = true;				
			}
			else if (event == EditorPairEvent.PreRedo) {
				debounce = true;				
			}
		}
	}
}
