package org.spoofax.modelware.gmf.editorservices;

import org.eclipse.core.commands.operations.IOperationHistoryListener;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.commands.operations.OperationHistoryEvent;
import org.eclipse.core.commands.operations.OperationHistoryFactory;
import org.eclipse.text.undo.DocumentUndoManagerRegistry;
import org.eclipse.text.undo.IDocumentUndoManager;
import org.spoofax.modelware.gmf.BridgeEvent;
import org.spoofax.modelware.gmf.EditorPair;
import org.spoofax.modelware.gmf.EditorPairObserver;

/**
 * @author Oskar van Rest
 */
public class UndoRedo implements IOperationHistoryListener {

	private EditorPair editorPair;
	private IUndoableOperation lastOperation;
	private boolean createCompositeOperation;

	public UndoRedo(EditorPair editorPair) {
		this.editorPair = editorPair;
		editorPair.registerObserver(new EndCompoundChangeOnSynchonization());
	}

	@Override
	public void historyNotification(OperationHistoryEvent event) {
		IUndoableOperation operation = event.getOperation();
		
		if (event.getEventType() == OperationHistoryEvent.ABOUT_TO_UNDO) {
			System.out.println("about to undo " + operation.hashCode() + " (" + operation.getLabel() + ")");
		}
		if (event.getEventType() == OperationHistoryEvent.UNDONE) {
			System.out.println("undone " + operation.hashCode() + " (" + operation.getLabel() + ")");
		}
		
		
		if (event.getEventType() == OperationHistoryEvent.OPERATION_ADDED) {
			System.out.println("adding " + operation.hashCode() + " (" + operation.getLabel() + ")");

			if (operation.hasContext(editorPair.getTextUndoContext())) {
				operation.addContext(editorPair.getDiagramUndoContext());
			} else if (operation.hasContext(editorPair.getDiagramUndoContext())) {
				operation.addContext(editorPair.getTextUndoContext());
			}

			if (createCompositeOperation) {
				createCompositeOperation = false;
				CompositeOperation compositeOperation = new CompositeOperation("Model change");
				compositeOperation.addContext(editorPair.getTextUndoContext());
				compositeOperation.addContext(editorPair.getDiagramUndoContext());
				compositeOperation.add(lastOperation);
				compositeOperation.add(operation);
				OperationHistoryFactory.getOperationHistory().add(compositeOperation);
			}

			lastOperation = operation;
		}
	}

	//TODO rename
	private class EndCompoundChangeOnSynchonization implements EditorPairObserver {

		@Override
		public void notify(BridgeEvent event) {
			System.out.println(event.toString());
			if (event == BridgeEvent.PreTerm2Model || event == BridgeEvent.PreModel2Term) {
				endCompoundChange();
			}

			if (event == BridgeEvent.PreParse || event == BridgeEvent.PreModel2Term) {
				createCompositeOperation = true;
			}
		}
	}
	
	private void endCompoundChange() {
		IDocumentUndoManager documentUndoManager = DocumentUndoManagerRegistry.getDocumentUndoManager(editorPair.getTextEditor().getDocumentProvider().getDocument(editorPair.getTextEditor().getEditorInput()));
		documentUndoManager.endCompoundChange();
	}

}
