package org.spoofax.modelware.gmf;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.imp.parser.IModelListener;
import org.eclipse.imp.parser.IParseController;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.IStrategoTuple;
import org.spoofax.modelware.emf.utils.SpoofaxEMFUtils;
import org.strategoxt.imp.runtime.EditorState;
import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;

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
	private boolean debouncer;

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
		if (debouncer) {
			debouncer = false;
			return;
		}
		
		timeOfLastChange = System.currentTimeMillis();
		if (thread == null || !thread.isAlive()) {
			thread = new Thread(new Timer());
			thread.start();
		}
	}
	
	private void executeTerm2Model() {
		EditorState editorState = EditorState.getEditorFor(editorPair.getTextEditor());

		IStrategoTerm analysedAST = null;
		try {
			analysedAST = editorState.getAnalyzedAst();
		}
		catch (BadDescriptorException e) {
			e.printStackTrace();
		}
		
		if (analysedAST instanceof IStrategoTuple && analysedAST.getSubtermCount()>0 && analysedAST.getSubterm(0) instanceof IStrategoAppl) {
			analysedAST = analysedAST.getSubterm(0); 
		}
		if (analysedAST instanceof IStrategoAppl) {
			editorPair.doTerm2Model(analysedAST);
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
				executeTerm2Model();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private class Debouncer implements EditorPairObserver {

		@Override
		public void notify(EditorPairEvent event) {			
			if (event == EditorPairEvent.PreLayoutPreservation) {
				debouncer = true;
			}
			// set editorPair.adjustedTree which needs to correspond to the changed model for proper working of selection sharing
			if (event == EditorPairEvent.PostLayoutPreservation) {
				EditorState editorState = EditorState.getEditorFor(editorPair.getTextEditor());
				
//				IStrategoTerm analysedAST = null;
//				try {
//					analysedAST = editorState.getAnalyzedAst();
//				}
//				catch (BadDescriptorException e) {
//					e.printStackTrace();
//				}
//				analysedAST = SpoofaxEMFUtils.adjustTree2Model(analysedAST, editorState);
//				editorPair.adjustedTree = analysedAST;
			}
			
			else if (event == EditorPairEvent.PreUndo) {
				debouncer = true;				
			}
			else if (event == EditorPairEvent.PreRedo) {
				debouncer = true;				
			}
		}
	}
}
