package org.spoofax.modelware.gmf;

import org.eclipse.core.commands.operations.IOperationHistoryListener;
import org.eclipse.core.commands.operations.OperationHistoryEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.undo.DocumentUndoManagerRegistry;
import org.eclipse.text.undo.IDocumentUndoManager;
import org.eclipse.ui.IEditorPart;
import org.strategoxt.imp.runtime.EditorState;

/**
 * @author Oskar van Rest
 */
public class OperationalMessageGenerator implements IOperationHistoryListener {

	private EditorPair editorPair;
	private IDocumentUndoManager undoManager;
//	private boolean debouncer = false;

	public OperationalMessageGenerator(EditorPair editorPair) {
		this.editorPair = editorPair;

		editorPair.registerObserver(new StartNewCompoundChangeWhenModelChanges());

		IDocument document = EditorState.getEditorFor(editorPair.getTextEditor()).getDocument();
		undoManager = DocumentUndoManagerRegistry.getDocumentUndoManager(document);
	}

	@Override
	public void historyNotification(OperationHistoryEvent event) {
//		if (debouncer) {
//			debouncer = false;
//			return;
//		}

		IEditorPart editor = Bridge.getInstance().getLastActiveEditor();

		if (editor != editorPair.getTextEditor() && editor != editorPair.getDiagramEditor()) {
			return;
		}

		if (editor == editorPair.getTextEditor()) {
			switch (event.getEventType()) {
			case OperationHistoryEvent.OPERATION_ADDED:
				editorPair.notifyObservers(BridgeEvent.PostTextLayoutChange);
				break;
			case OperationHistoryEvent.ABOUT_TO_UNDO:
				editorPair.notifyObservers(BridgeEvent.PreTextUndo);
				break;
			case OperationHistoryEvent.ABOUT_TO_REDO:
				editorPair.notifyObservers(BridgeEvent.PreTextRedo);
				break;
			default:
				break;
			}
		} else {
			switch (event.getEventType()) {
			case OperationHistoryEvent.OPERATION_ADDED:
				editorPair.notifyObservers(BridgeEvent.PostDiagramLayoutChange);
				break;
			case OperationHistoryEvent.ABOUT_TO_UNDO:
				editorPair.notifyObservers(BridgeEvent.PreDiagramUndo);
				break;
			case OperationHistoryEvent.ABOUT_TO_REDO:
				editorPair.notifyObservers(BridgeEvent.PreDiagramRedo);
				break;
			default:
				break;
			}
		}
	}

	private class StartNewCompoundChangeWhenModelChanges implements EditorPairObserver {

		@Override
		public void notify(BridgeEvent event) {
			// start new compound change when Term2Model transformation is to be executed, so that we can always go back to this particular state.
			if (event == BridgeEvent.PreTerm2Model) {
				undoManager.endCompoundChange();
				undoManager.beginCompoundChange();
			}
			// don't generate a TextLayoutChange notification as a result of a model2term notification.
			// else if (event == BridgeEvent.PreModel2Term) {
			// debouncer = true;
			// }
		}
	}
}
