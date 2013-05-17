package org.spoofax.modelware.gmf;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.util.EContentAdapter;
import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramEditor;
import org.eclipse.imp.editor.UniversalEditor;
import org.spoofax.interpreter.core.Interpreter;
import org.spoofax.interpreter.core.InterpreterErrorExit;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.core.InterpreterExit;
import org.spoofax.interpreter.core.UndefinedStrategyException;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.modelware.emf.Model2Term;
import org.strategoxt.imp.runtime.EditorState;
import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;
import org.strategoxt.imp.runtime.services.ITextReplacer;
import org.strategoxt.imp.runtime.services.StrategoObserver;

/**
 * Listens for changes in GMF's semantic model and perform a model-to-text
 * transformation upon such a change.
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
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void doModelToTerm() {

		final UniversalEditor textEditor = editorPair.getTextEditor();
		final DiagramEditor diagramEditor = editorPair.getDiagramEditor();

		IStrategoTerm oldTree = EditorState.getEditorFor(textEditor).getCurrentAst();

		editorPair.notifyObservers(EditorPairEvent.PreModel2Term);
		IStrategoTerm newTree = new Model2Term(EditorPairUtil.termFactory).convert(EditorPairUtil.getSemanticModel(diagramEditor));
		editorPair.notifyObservers(EditorPairEvent.PostModel2Term);
		
		EditorState editorState = EditorState.getEditorFor(textEditor);

		try {
			StrategoObserver observer = editorState.getDescriptor().createService(StrategoObserver.class, editorState.getParseController());
			Interpreter itp = observer.getRuntime();
			itp.setCurrent(newTree);
			itp.invoke("adjust-model-to-tree");
			newTree = itp.current();
		}
		catch (UndefinedStrategyException e) {
			// continue without adjustment
		}
		catch (BadDescriptorException e) {
			e.printStackTrace();
		}
		catch (InterpreterErrorExit e) {
			e.printStackTrace();
		}
		catch (InterpreterExit e) {
			e.printStackTrace();
		}
		catch (InterpreterException e) {
			e.printStackTrace();
		}
		
		ITextReplacer textReplacer = null;
		try {
			textReplacer = editorState.getDescriptor().createService(ITextReplacer.class, editorState.getParseController());
		}
		catch (BadDescriptorException e) {
			e.printStackTrace();
		}

		IStrategoList list = EditorPairUtil.termFactory.makeList(EditorPairUtil.termFactory.makeTuple(oldTree, newTree));

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