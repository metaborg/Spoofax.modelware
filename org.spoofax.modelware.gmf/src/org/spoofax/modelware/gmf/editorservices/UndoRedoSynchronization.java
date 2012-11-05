package org.spoofax.modelware.gmf.editorservices;

import java.util.Collection;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IExecutionListener;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.command.CommandStack;
import org.eclipse.emf.compare.diff.merge.service.MergeService;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
import org.eclipse.emf.transaction.RecordingCommand;
import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramEditor;
import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.imp.parser.IModelListener;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.spoofax.modelware.gmf.EditorPair;
import org.spoofax.modelware.gmf.Bridge;
import org.spoofax.modelware.gmf.BridgeUtil;
import org.strategoxt.imp.runtime.EditorState;

/**
 * @author Oskar van Rest
 */
public class UndoRedoSynchronization implements IExecutionListener {

	@Override
	public void notHandled(String commandId, NotHandledException exception) {
	}

	@Override
	public void postExecuteFailure(String commandId, ExecutionException exception) {
	}

	@Override
	public void postExecuteSuccess(String commandId, Object returnValue) {
	}

	@Override
	public void preExecute(String commandId, ExecutionEvent event) {
//		IEditorPart activeEditor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
//		if (activeEditor == null)
//			return;
//		
//		final EditorPair editorPair = GMFBridge.getInstance().getEditorPairs().get(activeEditor);
//		if (editorPair == null) {
//			return;
//		}
//		
//		CommandStack diagramCommmandStack = editorPair.getDiagramEditor().getEditingDomain().getCommandStack();

//		if (activeEditor instanceof UniversalEditor) {
////			
////			if (commandId.equals("org.eclipse.ui.edit.undo"))
////				//TODO
////			else if (commandId.equals("org.eclipse.ui.edit.redo"))
////				//TODO
//			
//		}
//		else if (activeEditor instanceof DiagramEditor) {
//			editorPair.getDebouncer().text2modelAllowed();
//			
//			UniversalEditor textEditor = (UniversalEditor) activeEditor;
//			event.getCommand();
//		}

		
		
		
		/**
		 * if active editor == texteditor {
		 * 	skip next text2model transformation using the debouncer
		 *  if (commandId = undo) {
		 *   tell diagramEditor to perform undo
		 *  if (commnadId = redo) {
		 *   tell diagramEditor to perform undo
		 * else if active editor == diagramEditor {
		 *   etc... 
		 */
	
		
		
	}
}
