package org.spoofax.modelware.gmf.editorservices;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.imp.parser.IModelListener;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.text.undo.DocumentUndoManager;
import org.eclipse.text.undo.DocumentUndoManagerRegistry;
import org.eclipse.text.undo.IDocumentUndoManager;
import org.strategoxt.imp.runtime.EditorState;

/**
 * @author Oskar van Rest
 */
public class UndoRedoSynchronisation implements IModelListener {

	@Override
	public AnalysisRequired getAnalysisRequired() {
		return AnalysisRequired.NONE;
	}

	@Override
	public void update(IParseController controller, IProgressMonitor monitor) {
//		EditorState e = EditorState.getActiveEditor();

	
	}

}
