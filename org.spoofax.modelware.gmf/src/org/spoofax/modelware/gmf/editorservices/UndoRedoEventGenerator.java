package org.spoofax.modelware.gmf.editorservices;

import org.eclipse.core.commands.operations.IOperationHistoryListener;
import org.eclipse.core.commands.operations.OperationHistoryEvent;
import org.spoofax.modelware.gmf.EditorPairEvent;
import org.spoofax.modelware.gmf.EditorPair;

/**
 * 
 * 
 * @author oskarvanrest
 *
 */
public class UndoRedoEventGenerator implements IOperationHistoryListener {
	
	private EditorPair editorPair;
	
	public UndoRedoEventGenerator(EditorPair editorPair) {
		this.editorPair = editorPair;
	}
	
	@Override
	public void historyNotification(OperationHistoryEvent event) {
//		if (event.getOperation().hasContext(editorPair.getTextUndoContext()) || event.getOperation().hasContext(editorPair.getDiagramUndoContext())) {
//			switch (event.getEventType()) {
//			case OperationHistoryEvent.ABOUT_TO_UNDO:
//				editorPair.notifyObservers(EditorPairEvent.PreUndo);
//				break;
//			case OperationHistoryEvent.UNDONE:
//				editorPair.notifyObservers(EditorPairEvent.PostUndo);
//				break;
//			case OperationHistoryEvent.ABOUT_TO_REDO:
//				editorPair.notifyObservers(EditorPairEvent.PreRedo);
//				break;
//			case OperationHistoryEvent.REDONE:
//				editorPair.notifyObservers(EditorPairEvent.PostRedo);
//				break;
//			default:
//				break;
//			}
//		}
	}
}
