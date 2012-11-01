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
import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

public class GMFBridgeUtil {

	private static String IMPEditorID = "org.eclipse.imp.runtime.impEditor";
	
	public static UniversalEditor findTextEditor(String path) {
		return (UniversalEditor) findEditor(path, IMPEditorID);
	}
	
	public static UniversalEditor findTextEditor(IPath path) {
		return (UniversalEditor) findEditor(path, IMPEditorID);
	}
	
	public static DiagramEditor findDiagramEditor(String textfilePath, String packageName) {
		String editorID = packageName + ".diagram.part." + packageName + "DiagramEditorID";
		
		String diagramfileExtension = packageName.toLowerCase() + "_diagram";
		String diagramfilePath = textfilePath.substring(0, textfilePath.lastIndexOf(".")) + diagramfileExtension;
		DiagramEditor editor = (DiagramEditor) findEditor(diagramfilePath, editorID);
		
		if (editor != null)
			return editor;
		
		diagramfilePath = textfilePath + "_diagram";
		return (DiagramEditor) findEditor(diagramfilePath, editorID);
	}

	private static IEditorPart findEditor(String path, String editorID) {
		return findEditor(new Path(path), editorID);
	}
	
	private static IEditorPart findEditor(IPath path, String editorID) {
		IFile file = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(path);
		
		if (file == null)
			return null;
		
		IEditorInput editorInput = new FileEditorInput(file);
		IWorkbenchPage page = getActivePage();
		
		IEditorPart editor = null;
		IEditorReference[] editors = page.findEditors(editorInput, null, IWorkbenchPage.MATCH_INPUT);
		for (int i = 0; i < editors.length; i++) {
			if (editors[i].getId().equals(editorID)) {
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
		if (getSemanticModel(diagramEditor) != null && diagramEditor.getEditorSite().getSelectionProvider() != null)
			return true;
		else
			return false;
	}
}
