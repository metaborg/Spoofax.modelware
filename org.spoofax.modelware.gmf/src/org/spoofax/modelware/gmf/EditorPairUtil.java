package org.spoofax.modelware.gmf;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramEditor;
import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.TermFactory;

/**
 * Utilities, abstracting over some boilerplate code.
 * 
 * @author oskarvanrest
 */
public class EditorPairUtil {

	private static String IMPEditorID = "org.eclipse.imp.runtime.impEditor";
	public static ITermFactory termFactory = new TermFactory();

	public static UniversalEditor findTextEditor(IPath path) {
		return (UniversalEditor) findEditor(path, IMPEditorID);
	}

	public static IEditorPart findEditor(IPath path, String editorID) {
		IFile file = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(path);

		if (file == null)
			return null;

		IEditorInput editorInput = new FileEditorInput(file);

		Collection<IWorkbenchPage> pages = getAllWorkbenchPages();
		Iterator<IWorkbenchPage> it = pages.iterator();

		while (it.hasNext()) {
			IWorkbenchPage page = it.next();
			IEditorReference[] editors = page.findEditors(editorInput, null, IWorkbenchPage.MATCH_INPUT);
			for (int i = 0; i < editors.length; i++) {
				if (editors[i].getId().equals(editorID)) {
					return editors[i].getEditor(false);
				}
			}
		}

		return null;
	}

	public static Collection<IWorkbenchPage> getAllWorkbenchPages() {
		Collection<IWorkbenchPage> result = new LinkedList<IWorkbenchPage>();
		for (IWorkbenchWindow window : PlatformUI.getWorkbench().getWorkbenchWindows()) {
			result.addAll(Arrays.asList(window.getPages()));
		}
		return result;
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
		return getSemanticModel(diagramEditor) != null && diagramEditor.getEditorSite().getSelectionProvider() != null;
	}

	public static String getFilePath(IEditorPart editor) {
		return getFile(editor).getLocation().toString();
	}

	public static String getFileExtension(IEditorPart editor) {
		return getFile(editor).getFileExtension();
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
