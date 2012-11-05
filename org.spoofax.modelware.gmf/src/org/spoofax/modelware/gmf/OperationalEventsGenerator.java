package org.spoofax.modelware.gmf;

import org.eclipse.core.commands.operations.IOperationApprover;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IOperationHistoryListener;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.commands.operations.OperationHistoryEvent;
import org.eclipse.core.commands.operations.OperationHistoryFactory;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.undo.DocumentUndoManagerRegistry;
import org.eclipse.ui.IEditorPart;
import org.strategoxt.imp.runtime.EditorState;

/**
 * @author Oskar van Rest
 */
public class OperationalEventsGenerator implements IOperationHistoryListener {

	private EditorPair editorPair;

	public OperationalEventsGenerator(EditorPair editorPair) {
		this.editorPair = editorPair;
		
//		IOperationHistory history = OperationHistoryFactory.getOperationHistory();
//		IDocument document = EditorState.getEditorFor(editorPair.getTextEditor()).getDocument();
//		IUndoContext context = DocumentUndoManagerRegistry.getDocumentUndoManager(document).getUndoContext();
//		IOperationApprover approver = new BridgeOperationApprover();
//		history.addOperationApprover(approver);
	}

	@Override
	public void historyNotification(OperationHistoryEvent event) {
		IEditorPart editor = Bridge.getInstance().getLastActiveEditor();
		
		if (editor != editorPair.getTextEditor() && editor != editorPair.getDiagramEditor()) {
			return;
		}
		
		// disable Undo/Redo for now. TODO: create own Undo Context.
		IUndoContext[] contexts = event.getOperation().getContexts();
		for (int i=0; i<contexts.length;i++) {
			event.getOperation().removeContext(contexts[i]);
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
	
	private class BridgeOperationApprover implements IOperationApprover {

		@Override
		public IStatus proceedRedoing(IUndoableOperation operation, IOperationHistory history, IAdaptable info) {
			System.out.println("proceedRedoing");
			return Status.OK_STATUS;
		}

		@Override
		public IStatus proceedUndoing(IUndoableOperation operation, IOperationHistory history, IAdaptable info) {
			System.out.println("proceedUndoing");
			return Status.OK_STATUS;
		}
		
	}
}
