package org.spoofax.modelware.gmf;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramEditor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

public class GMFBridgeUtil {

	public static IEditorPart findTextEditor(String filePath, String extension) {
		return findEditor(filePath + "." + extension, "org.eclipse.imp.runtime.impEditor");
	}

	public static DiagramEditor findDiagramEditor(String filePath, String textFileExtension, String packageName) {
		String extension1 = packageName.toLowerCase() + "_diagram";
		String extension2 = textFileExtension + "_diagram";
		String editorID = packageName + ".diagram.part." + packageName + "DiagramEditorID";
		
		DiagramEditor editor = (DiagramEditor) findEditor(filePath + "." + extension1, editorID);
		if (editor == null) {
			return (DiagramEditor) findEditor(filePath + "." + extension2, editorID);
		}
		else {
			return editor;
		}
	}

	private static IEditorPart findEditor(String path, String editorId) {
		IWorkbenchPage page = getActivePage();

		IPath iPath = new Path(path);
		IFile iFile = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(iPath);
		IEditorInput editorInput = new FileEditorInput(iFile);

		IEditorPart editor = null;

		IEditorReference[] editors = page.findEditors(editorInput, null, IWorkbenchPage.MATCH_INPUT);
		for (int i = 0; i < editors.length; i++) {
			if (editors[i].getId().equals(editorId)) {
				editor = editors[i].getEditor(false);
			}
		}

		return editor;
	}

	public static IWorkbenchPage getActivePage() {
		IWorkbench wb = PlatformUI.getWorkbench();
		IWorkbenchWindow win = wb.getWorkbenchWindows()[0]; // TODO: getActiveWorkbenchWindow doesn't work
		return win.getActivePage();
	}

	public static Resource getSemanticModelResource(DiagramEditor diagramEditor) {
		TransactionalEditingDomain editingDomain = diagramEditor.getEditingDomain();
		ResourceSet diagramEditorResourceSet = editingDomain.getResourceSet();
		return diagramEditorResourceSet.getResources().get(1);
	}

	public static EObject getSemanticModel(DiagramEditor diagramEditor) {
		Resource semanticModelResource = getSemanticModelResource(diagramEditor);
		if (semanticModelResource.getContents().size() != 0)
			return semanticModelResource.getContents().get(0);
		else
			return null;
	}
}
