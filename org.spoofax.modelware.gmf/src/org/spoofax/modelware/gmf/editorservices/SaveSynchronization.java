package org.spoofax.modelware.gmf.editorservices;

import java.util.Collection;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IExecutionListener;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.spoofax.modelware.gmf.EditorPair;
import org.spoofax.modelware.gmf.EditorPairRegistry;

public class SaveSynchronization implements IExecutionListener {

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
			IEditorPart activeEditor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
			if (activeEditor != null && activeEditor instanceof UniversalEditor) {
				EditorPair editorPair = EditorPairRegistry.getInstance().get(activeEditor);
				if (editorPair != null) {
					editorPair.getDiagramEditor().doSave(new NullProgressMonitor());
				}
			}
		}
		else if (commandId.equals("org.eclipse.ui.file.saveAll")) {
			Collection<EditorPair> eps = EditorPairRegistry.getInstance().getAll();
			for (EditorPair ep : eps) {
				ep.getDiagramEditor().doSave(new NullProgressMonitor());
			}
		}
	}
}
