package org.spoofax.modelware.gmf;

import org.eclipse.core.commands.operations.IOperationHistoryListener;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.commands.operations.OperationHistoryEvent;
import org.eclipse.core.commands.operations.OperationHistoryFactory;
import org.eclipse.emf.workspace.EMFCommandOperation;
import org.eclipse.gmf.runtime.common.core.command.CompositeCommand;
import org.eclipse.gmf.runtime.emf.commands.core.command.CompositeTransactionalCommand;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.text.undo.DocumentUndoManager;
import org.eclipse.text.undo.DocumentUndoManagerRegistry;
import org.eclipse.text.undo.IDocumentUndoManager;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.properties.PropertySheet;
import org.spoofax.modelware.gmf.EditorPair.BridgeEvent;
import org.strategoxt.imp.runtime.EditorState;

/**
 * @author Oskar van Rest
 */
public class OperationalHistoryListener implements IOperationHistoryListener {

	private EditorPair editorPair;
	private IDocumentUndoManager undoManager;
	private boolean debouncer = false;

	public OperationalHistoryListener(EditorPair editorPair) {
		this.editorPair = editorPair;

		editorPair.registerObserver(new StartNewCompoundChangeWhenModelChanges());

		IDocument document = EditorState.getEditorFor(editorPair.getTextEditor()).getDocument();
		undoManager = DocumentUndoManagerRegistry.getDocumentUndoManager(document);
	}

	@Override
	public void historyNotification(OperationHistoryEvent event) {
		if (debouncer) {
			debouncer = false;
			return;
		}

		IWorkbench wb = PlatformUI.getWorkbench();
		IWorkbenchWindow win = wb.getWorkbenchWindows()[0];
//		System.out.println(win.getSelectionService().getSelection().getClass().toString());
		
//		StructuredSelection ss = (StructuredSelection) win.getSelectionService().
//		System.out.println("size = " + ss.size());
		
//		System.out.println(BridgeUtil.getActivePage().getActivePart().getClass().toString());
//		if (BridgeUtil.getActivePage().getActivePart() instanceof PropertySheet) {
//			(BridgeUtil.getActivePage().STATE_MAXIMIZED
//			PropertySheet p = (PropertySheet) BridgeUtil.getActivePage().getActivePart() ;
//			p.getSite().getSelectionProvider().
////			editorPair.getDiagramEditor().getEditorSite().getSelectionProvider().
//			
//			StructuredSelection ss = (StructuredSelection) p.getSite().getSelectionProvider().getSelection();
//			System.out.println(p.toString());
//			
//		
//		}
//		
//		if (event.getOperation() instanceof CompositeCommand || event.getOperation() instanceof CompositeTransactionalCommand) {
//			if (BridgeUtil.getActiveEditor() == editorPair.getDiagramEditor()) {
//			switch (event.getEventType()) {
//			case OperationHistoryEvent.OPERATION_ADDED:
//				editorPair.notifyObservers(BridgeEvent.DiagramLayoutChange);
//				break;
//			case OperationHistoryEvent.ABOUT_TO_REDO:
//				editorPair.notifyObservers(BridgeEvent.DiagramRedo);
//				break;
//			case OperationHistoryEvent.ABOUT_TO_UNDO:
//				editorPair.notifyObservers(BridgeEvent.DiagramUndo);
//				break;
//			default:
//				break;
//			}
//			}
//		} else {
//			if (BridgeUtil.getActiveEditor() == editorPair.getTextEditor()) {
//				switch (event.getEventType()) {
//				case OperationHistoryEvent.OPERATION_ADDED:
//					editorPair.notifyObservers(BridgeEvent.TextLayoutChange);
//					break;
//				case OperationHistoryEvent.ABOUT_TO_REDO:
//					editorPair.notifyObservers(BridgeEvent.TextRedo);
//					break;
//				case OperationHistoryEvent.ABOUT_TO_UNDO:
//					editorPair.notifyObservers(BridgeEvent.TextUndo);
//					break;
//				default:
//					break;
//				}
//			}
//		}
	}

	private class StartNewCompoundChangeWhenModelChanges implements EditorPairObserver {

		@Override
		public void notify(BridgeEvent event) {
			// start new compound change when Term2Model transformation is executed, so that we can always go back to this particular state.
			if (event == BridgeEvent.Term2Model) {
				undoManager.endCompoundChange();
				undoManager.beginCompoundChange();
			}
			// don't generate a TextLayoutChange notification as a result of a model2term notification.
			else if (event == BridgeEvent.Model2Term) {
				debouncer = true;
			}
		}
	}
}
