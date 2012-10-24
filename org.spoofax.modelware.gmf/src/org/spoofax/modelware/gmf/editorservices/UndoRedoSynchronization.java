package org.spoofax.modelware.gmf.editorservices;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IExecutionListener;
import org.eclipse.core.commands.NotHandledException;

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
