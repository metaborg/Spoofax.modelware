package org.spoofax.modelware.gmf.editorservices;

import org.eclipse.core.commands.operations.IOperationHistoryListener;
import org.eclipse.core.commands.operations.OperationHistoryEvent;
import org.spoofax.modelware.gmf.BridgeEvent;
import org.spoofax.modelware.gmf.EditorPair;

public class UndoRedoEventGenerator implements IOperationHistoryListener {
	
	private EditorPair editorPair;
	
	public UndoRedoEventGenerator(EditorPair editorPair) {
		this.editorPair = editorPair;
	}
	
	@Override
	public void historyNotification(OperationHistoryEvent event) {
		if (event.getOperation().hasContext(editorPair.getTextUndoContext())) {
			switch (event.getEventType()) {
			case OperationHistoryEvent.ABOUT_TO_UNDO:
				editorPair.notifyObservers(BridgeEvent.PreTextUndo);
				break;
			case OperationHistoryEvent.UNDONE:
				editorPair.notifyObservers(BridgeEvent.PostTextUndo);
				break;
			case OperationHistoryEvent.ABOUT_TO_REDO:
				editorPair.notifyObservers(BridgeEvent.PreTextRedo);
				break;
			case OperationHistoryEvent.REDONE:
				editorPair.notifyObservers(BridgeEvent.PostTextRedo);
				break;
			default:
				break;
			}
		}
		else if (event.getOperation().hasContext(editorPair.getDiagramUndoContext())) {
			switch (event.getEventType()) {
			case OperationHistoryEvent.ABOUT_TO_UNDO:
				editorPair.notifyObservers(BridgeEvent.PreDiagramUndo);
				break;
			case OperationHistoryEvent.UNDONE:
				editorPair.notifyObservers(BridgeEvent.PostDiagramUndo);
				break;
			case OperationHistoryEvent.ABOUT_TO_REDO:
				editorPair.notifyObservers(BridgeEvent.PreDiagramRedo);
				break;
			case OperationHistoryEvent.REDONE:
				editorPair.notifyObservers(BridgeEvent.PostDiagramRedo);
				break;
			default:
				break;
			}
		}
	}
}
