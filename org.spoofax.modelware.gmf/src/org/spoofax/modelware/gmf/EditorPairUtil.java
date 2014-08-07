package org.spoofax.modelware.gmf;

import org.eclipse.core.resources.IFile;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramEditor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;

/**
 * Utilities, abstracting over some boilerplate code.
 * 
 * @author oskarvanrest
 */
public class EditorPairUtil {

	public static Resource getSemanticModelResource(DiagramEditor diagramEditor) {
		return getResource(diagramEditor, 1);
	}

	public static Resource getNotationModelResource(DiagramEditor diagramEditor) {
		return getResource(diagramEditor, 0);
	}

	private static Resource getResource(DiagramEditor diagramEditor, int i) {
		TransactionalEditingDomain editingDomain = diagramEditor.getEditingDomain();
		ResourceSet diagramEditorResourceSet = editingDomain.getResourceSet();
		return diagramEditorResourceSet.getResources().get(i);
	}

	public static EObject getSemanticModel(DiagramEditor diagramEditor) {
		Resource semanticModelResource = getSemanticModelResource(diagramEditor);
		if (semanticModelResource.getContents().size() != 0)
			return semanticModelResource.getContents().get(0);
		else
			return null;
	}

	public static boolean isInitialised(DiagramEditor diagramEditor) {
		return getSemanticModel(diagramEditor) != null && diagramEditor.getEditorSite().getSelectionProvider() != null;
	}

	public static String getFilePath(IEditorPart editor) {
		return getFile(editor).getLocation() == null ? null : getFile(editor).getLocation().toString();
	}

	public static String getFileExtension(IEditorPart editor) {
		IFile file = getFile(editor);
		if (file != null) {
			return file.getFileExtension();
		}
		return null;
	}

	public static IFile getFile(IEditorPart editor) {
		if (editor.getEditorInput() instanceof IFileEditorInput) {
			return ((IFileEditorInput) editor.getEditorInput()).getFile();
		}
		else {
			return null;
		}
	}
}
