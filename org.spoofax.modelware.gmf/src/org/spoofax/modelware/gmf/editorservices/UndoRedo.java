package org.spoofax.modelware.gmf.editorservices;

import org.eclipse.core.commands.operations.IOperationHistoryListener;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.commands.operations.OperationHistoryEvent;
import org.eclipse.text.undo.DocumentUndoManagerRegistry;
import org.eclipse.text.undo.IDocumentUndoManager;
import org.spoofax.modelware.gmf.EditorPair;
import org.spoofax.modelware.gmf.EditorPairEvent;
import org.spoofax.modelware.gmf.EditorPairObserver;

/**
 * Under construction...
 * 
 * @author oskarvanrest
 */
public class UndoRedo implements IOperationHistoryListener {

	private EditorPair editorPair;

	public UndoRedo(EditorPair editorPair) {
		this.editorPair = editorPair;
		editorPair.registerObserver(new EndCompoundChangeOnSynchonization());
	}

	
	@Override
	public void historyNotification(OperationHistoryEvent event) {
		IUndoableOperation operation = event.getOperation();
		
		System.out.println(event.getEventType() + " " + event.getOperation().hashCode() + " " + event.getOperation().getLabel());
		
		if (event.getEventType() == OperationHistoryEvent.OPERATION_ADDED) {
			
		}
	}

	//TODO rename
	private class EndCompoundChangeOnSynchonization implements EditorPairObserver {

		@Override
		public void notify(EditorPairEvent event) {
			
			if (event == EditorPairEvent.PreTerm2Model || event == EditorPairEvent.PreModel2Term) {
				endCompoundChange();
			}
		}
	}
	
	private void endCompoundChange() {
		IDocumentUndoManager documentUndoManager = DocumentUndoManagerRegistry.getDocumentUndoManager(editorPair.getTextEditor().getDocumentProvider().getDocument(editorPair.getTextEditor().getEditorInput()));
		documentUndoManager.endCompoundChange();
	}

}
