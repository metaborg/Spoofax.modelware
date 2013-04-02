package org.spoofax.modelware.gmf;

import org.eclipse.core.commands.operations.IOperationHistoryListener;
import org.eclipse.core.commands.operations.OperationHistoryEvent;
import org.eclipse.ui.IEditorPart;

/**
 * @author Oskar van Rest
 */
public class OperationalEventsGenerator implements IOperationHistoryListener {

	private EditorPair editorPair;

	public OperationalEventsGenerator(EditorPair editorPair) {
		this.editorPair = editorPair;
	}

	@Override
	public void historyNotification(OperationHistoryEvent event) {
		IEditorPart editor = Bridge.getInstance().getLastActiveEditor();
		
		if (editor != editorPair.getTextEditor() && editor != editorPair.getDiagramEditor()) {
			return;
		}

		if (editor == editorPair.getTextEditor()) {
			// hack
			if (event.getOperation().getLabel().equals("Typing")) {
				event.getOperation().addContext(editorPair.getDiagramEditor().getDiagramEditDomain().getDiagramCommandStack().getUndoContext());
			}
			
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
}
