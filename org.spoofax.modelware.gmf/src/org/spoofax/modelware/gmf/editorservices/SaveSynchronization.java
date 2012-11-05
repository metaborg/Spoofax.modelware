package org.spoofax.modelware.gmf.editorservices;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IExecutionListener;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.spoofax.modelware.gmf.Bridge;
import org.spoofax.modelware.gmf.EditorPair;

/**
 * @author Oskar van Rest
 */
public class SaveSynchronization implements IExecutionListener {

	private EditorPair editorPair;
	
	public SaveSynchronization(EditorPair editorPair) {
		this.editorPair = editorPair;
	}
	
	@Override
	public void notHandled(String commandId, NotHandledException exception) {
	}

	@Override
	public void postExecuteFailure(String commandId, ExecutionException exception) {
	}

	@Override
	public void postExecuteSuccess(String commandId, Object returnValue) {
	}

	/**
	 * Save the diagram editor when the text editor is saved. 
	 */
	@Override
	public void preExecute(String commandId, ExecutionEvent event) {
		if (commandId.equals("org.eclipse.ui.file.save")) {
			if (Bridge.getInstance().getLastActiveEditor() == editorPair.getTextEditor()) {
				editorPair.getDiagramEditor().doSave(new NullProgressMonitor());
			}
		}
		else if (commandId.equals("org.eclipse.ui.file.saveAll")) {
			editorPair.getDiagramEditor().doSave(new NullProgressMonitor());
		}
	}
}
