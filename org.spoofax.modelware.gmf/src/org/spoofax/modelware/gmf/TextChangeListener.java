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

	public TextChangeListener(EditorPair editorPair) {
		this.editorPair = editorPair;
		Thread thread = new Thread(new Timer());
		thread.start();
		editorPair.registerObserver(new Debouncer());
	}

	public void change() {
		if (debounce) {
			return;
		}

		editorPair.doTerm2Model();
	}

	private class Timer implements Runnable {
		public void run() {
			EditorState editorState = EditorState.getEditorFor(editorPair.getTextEditor());
			
			try {
			IStrategoTerm lastAnalyzedAST = editorState.getAnalyzedAst();
			
			while(true) {
				if (editorState.getAnalyzedAst() != lastAnalyzedAST) {
					lastAnalyzedAST = editorState.getAnalyzedAst();
					change();
				}
				else {
					Thread.sleep(25);
				}
			}
			} catch (Exception e) {
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
