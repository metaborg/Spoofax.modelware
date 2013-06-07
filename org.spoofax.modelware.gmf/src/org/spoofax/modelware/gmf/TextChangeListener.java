package org.spoofax.modelware.gmf;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.modelware.emf.utils.SpoofaxEMFUtils;
import org.strategoxt.imp.runtime.EditorState;

/**
 * TODO: add analyzed AST change listener to Spoofax instead of this hack
 * 
 * @author oskarvanrest
 */
public class TextChangeListener {

	private EditorPair editorPair;
	private boolean debounce;
	private boolean active;

	public TextChangeListener(EditorPair editorPair) {
		this.editorPair = editorPair;
		Thread thread = new Thread(new Timer());
		active = true;
		thread.start();
		editorPair.registerObserver(new Debouncer());
	}

	private class Timer implements Runnable {
		public void run() {
			EditorState editorState = EditorState.getEditorFor(editorPair.getTextEditor());

			try {
				while (active) {
					IStrategoTerm newAdjustedAST = SpoofaxEMFUtils.getAdjustedAST(editorState);
					
					if (newAdjustedAST != editorPair.adjustedAST) {
						editorPair.adjustedAST = newAdjustedAST;
						editorPair.notifyObservers(EditorPairEvent.PostAnalyze);
					}
					
					Thread.sleep(25);
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void dispose() {
		active = false;
	}

	private class Debouncer implements EditorPairObserver {

		@Override
		public void notify(EditorPairEvent event) {
			
			if (event == EditorPairEvent.PostAnalyze) {
				if (debounce) {
					editorPair.notifyObservers(EditorPairEvent.PostLayoutPreservation);
				}
				else {
					editorPair.doTerm2Model();
				}
			}
			
			if (event == EditorPairEvent.PreLayoutPreservation) {
				debounce = true;
			}
			else if (event == EditorPairEvent.PostLayoutPreservation) {
				debounce = false;
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
