package org.spoofax.modelware.gmf.editorservices;

import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IOperationHistoryListener;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.commands.operations.OperationHistoryEvent;
import org.eclipse.core.commands.operations.OperationHistoryFactory;
import org.eclipse.text.undo.DocumentUndoManagerRegistry;
import org.eclipse.text.undo.IDocumentUndoManager;
import org.spoofax.modelware.gmf.EditorPair;
import org.spoofax.modelware.gmf.EditorPairEvent;
import org.spoofax.modelware.gmf.EditorPairObserver;

/**
 * 
 * 
 * @author oskarvanrest
 */
public class UndoRedo implements IOperationHistoryListener {

	private EditorPair editorPair;
	private boolean composite;
	private boolean disable;

	public UndoRedo(EditorPair editorPair) {
		this.editorPair = editorPair;
//		editorPair.registerObserver(new EndCompoundChangeOnSynchonization());
	}

	
	@Override
	public void historyNotification(OperationHistoryEvent event) {
//		IUndoableOperation operation = event.getOperation();
//		
//		System.out.println(event.getEventType() + " " + event.getOperation().hashCode() + " " + event.getOperation().getLabel());
//		
//		
//		if (event.getEventType() == OperationHistoryEvent.OPERATION_ADDED) {
//			
//			if (disable) {
//				operation.dispose();
//				System.out.println("operation disposed...........................");
//			}
//			
//			if (composite) {
//				System.out.println("composite");
//			}
//			else {
//				System.out.println("close and open");
//				OperationHistoryFactory.getOperationHistory().closeOperation(true, true, IOperationHistory.EXECUTE);
//				CompositeOperation hello = new CompositeOperation("Model change");
//				hello.compose(operation);
//				OperationHistoryFactory.getOperationHistory().openOperation(hello, IOperationHistory.EXECUTE);
//			}
//			
//		}
	}

	//TODO rename
	private class EndCompoundChangeOnSynchonization implements EditorPairObserver {

		@Override
		public void notify(EditorPairEvent event) {

			System.out.println(event.toString());
			
			if (event == EditorPairEvent.PreUndo) {
				disable = true;
			}
			else if (event == EditorPairEvent.PostUndo) {
				disable = false;
			}
			
			if (event == EditorPairEvent.PreTerm2Model || event == EditorPairEvent.PreModel2Term) {
				endCompoundChange();
				composite = true;
			}
			
			if (event == EditorPairEvent.PreRender) {
				OperationHistoryFactory.getOperationHistory().closeOperation(true, true, IOperationHistory.EXECUTE);
				composite = false;
			}
		}
	}
	
	private void endCompoundChange() {
		IDocumentUndoManager documentUndoManager = DocumentUndoManagerRegistry.getDocumentUndoManager(editorPair.getTextEditor().getDocumentProvider().getDocument(editorPair.getTextEditor().getEditorInput()));
		documentUndoManager.endCompoundChange();
	}

}
