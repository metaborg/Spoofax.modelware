package org.spoofax.modelware.gmf.editorservices;

import org.eclipse.core.commands.operations.IOperationHistoryListener;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.commands.operations.OperationHistoryEvent;
import org.eclipse.text.undo.DocumentUndoManagerRegistry;
import org.eclipse.text.undo.IDocumentUndoManager;
import org.spoofax.modelware.gmf.EditorPair;

/**
 * @author Oskar van Rest
 */
public class UndoRedo implements IOperationHistoryListener {

	private EditorPair editorPair;

//	private IUndoableOperation a;
//	private IUndoableOperation b;
//	private IUndoableOperation c;
//	private IUndoableOperation d;
//	private boolean done;

	public UndoRedo(EditorPair editorPair) {
		this.editorPair = editorPair;
	}

	@Override
	public void historyNotification(OperationHistoryEvent event) {
		IUndoableOperation operation = event.getOperation();

		if (operation.hasContext(getTextUndoContext())) {
			operation.addContext(getDiagramUndoContext());
		}
		else if (operation.hasContext(getDiagramUndoContext())) {
			operation.addContext(getTextUndoContext());
		}
		
		
		System.out.println("operation");
//		if (a == null)
//			a = event.getOperation();
//		else if (b == null)
//			b = event.getOperation();
//		else if (c == null)
//			c = event.getOperation();
//		else if (d == null)
//			d = event.getOperation();
//
//		if (d != null && !done) {
//			done = true;
//			DualEditorCompositeOperation e = new DualEditorCompositeOperation("Model change");
//			e.addContext(getTextUndoContext());
//			e.addContext(getDiagramUndoContext());
//			e.add(b);
//			e.add(c);
//			e.add(d);
//
//			OperationHistoryFactory.getOperationHistory().add(e);
//
//			System.out.println("can execute: " + e.canExecute());
//			try {
//				e.execute(new NullProgressMonitor(), null);
//			} catch (ExecutionException e1) {
//				e1.printStackTrace();
//			}
//		}



//		if (lastActiveEditor == editorPair.getTextEditor()) {
//			if (textualEditorUndoContext == null) {
//				textualEditorUndoContext = event.getOperation().getContexts()[0];
//			}
//		} else if (lastActiveEditor == editorPair.getDiagramEditor()) {
//			if (textualEditorUndoContext != null) {
//				boolean hello = false;
//				for (int i = 0; i < event.getOperation().getContexts().length; i++) {
//
//					if (event.getOperation().getContexts()[i].matches(textualEditorUndoContext)) {
//						hello = true;
//
//					}
//
//				}
//				if (!hello) {
//					event.getOperation().addContext(textualEditorUndoContext);
//				}
//			}
//		}

		// if (lastActiveEditor == editorPair.getTextEditor()) {
		// // hack

		//
		// switch (event.getEventType()) {
		// case OperationHistoryEvent.OPERATION_ADDED:
		// editorPair.notifyObservers(BridgeEvent.PostTextLayoutChange);
		// break;
		// case OperationHistoryEvent.ABOUT_TO_UNDO:
		// editorPair.notifyObservers(BridgeEvent.PreTextUndo);
		// break;
		// case OperationHistoryEvent.ABOUT_TO_REDO:
		// editorPair.notifyObservers(BridgeEvent.PreTextRedo);
		// break;
		// default:
		// break;
		// }
		// } else {
		// switch (event.getEventType()) {
		// case OperationHistoryEvent.OPERATION_ADDED:
		// editorPair.notifyObservers(BridgeEvent.PostDiagramLayoutChange);
		// break;
		// case OperationHistoryEvent.ABOUT_TO_UNDO:
		// editorPair.notifyObservers(BridgeEvent.PreDiagramUndo);
		// break;
		// case OperationHistoryEvent.ABOUT_TO_REDO:
		// editorPair.notifyObservers(BridgeEvent.PreDiagramRedo);
		// break;
		// default:
		// break;
		// }
		// }
		
		
		
	}

	private IUndoContext getTextUndoContext() {
		IDocumentUndoManager documentUndoManager = DocumentUndoManagerRegistry.getDocumentUndoManager(editorPair.getTextEditor().getDocumentProvider().getDocument(editorPair.getTextEditor().getEditorInput()));
		return documentUndoManager.getUndoContext();
	}
	
	private IUndoContext getDiagramUndoContext() {
		return 	editorPair.getDiagramEditor().getDiagramEditDomain().getDiagramCommandStack().getUndoContext();
	}
	
//	private class ModelChange implements EditorPairObserver {
//
//		@Override
//		public void notify(BridgeEvent event) {
//			
//			System.out.println(event.toString());
//			if (event == BridgeEvent.PreDiagramSelection) {
//				debounce = true;
//			}
//			if (event == BridgeEvent.PostDiagramSelection) {
//				debounce = false;
//			}
//		}
//	}
	
}
