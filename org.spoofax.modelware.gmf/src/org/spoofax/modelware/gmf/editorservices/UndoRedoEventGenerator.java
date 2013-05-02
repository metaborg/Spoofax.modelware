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
		if (event.getOperation().hasContext(editorPair.getTextUndoContext()) || event.getOperation().hasContext(editorPair.getDiagramUndoContext())) {
			switch (event.getEventType()) {
			case OperationHistoryEvent.ABOUT_TO_UNDO:
				editorPair.notifyObservers(BridgeEvent.PreUndo);
				break;
			case OperationHistoryEvent.UNDONE:
				editorPair.notifyObservers(BridgeEvent.PostUndo);
				break;
			case OperationHistoryEvent.ABOUT_TO_REDO:
				editorPair.notifyObservers(BridgeEvent.PreRedo);
				break;
			case OperationHistoryEvent.REDONE:
				editorPair.notifyObservers(BridgeEvent.PostRedo);
				break;
			default:
				break;
			}
		}
	}
}
