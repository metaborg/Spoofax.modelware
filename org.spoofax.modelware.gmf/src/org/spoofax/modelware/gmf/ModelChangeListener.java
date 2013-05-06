package org.spoofax.modelware.gmf;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.util.EContentAdapter;
import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramEditor;
import org.eclipse.ui.IEditorPart;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.modelware.emf.Model2Term;
import org.spoofax.modelware.gmf.EditorPairEvent;
import org.strategoxt.imp.runtime.EditorState;
import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;
import org.strategoxt.imp.runtime.services.ITextReplacer;

/**
 * Listens for changes in GMF's semantic model and perform a model-to-text transformation upon 
 * such a change.
 * 
 * @author oskarvanrest
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
				doModelToTerm();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void doModelToTerm() {
		
		final IEditorPart textEditor = editorPair.getTextEditor();
		final DiagramEditor diagramEditor = editorPair.getDiagramEditor();

		IStrategoTerm currentTerm = EditorState.getEditorFor(textEditor).getCurrentAst();
		
		editorPair.notifyObservers(EditorPairEvent.PreModel2Term);
		IStrategoTerm newTerm = new Model2Term(EditorPairUtil.termFactory).convert(EditorPairUtil.getSemanticModel(diagramEditor));
		editorPair.notifyObservers(EditorPairEvent.PostModel2Term);
		
		EditorState editor = EditorState.getEditorFor(textEditor);
		ITextReplacer textReplacer = null;
		try {
			textReplacer = editor.getDescriptor().createService(ITextReplacer.class, editor.getParseController());
		} catch (BadDescriptorException e) {
			e.printStackTrace();
		}

		IStrategoList list = EditorPairUtil.termFactory.makeList(EditorPairUtil.termFactory.makeTuple(currentTerm, newTerm));
		
		editorPair.notifyObservers(EditorPairEvent.PreLayoutPreservation);
		textReplacer.replaceText(list);
		editorPair.notifyObservers(EditorPairEvent.PostLayoutPreservation);
	}
	
	private class Debouncer implements EditorPairObserver {

		@Override
		public void notify(EditorPairEvent event) {
			if (event == EditorPairEvent.PreMerge) {
				debouncer = true;
			}
			else if (event == EditorPairEvent.PostMerge) {
				debouncer = false;
			}
			
			else if (event == EditorPairEvent.PreUndo) {
				debouncer = true;				
			}
			else if (event == EditorPairEvent.PostUndo) {
				debouncer = false;
			}
			else if (event == EditorPairEvent.PreRedo) {
				debouncer = true;				
			}
			else if (event == EditorPairEvent.PostRedo) {
				debouncer = false;
			}
		}
	}
}