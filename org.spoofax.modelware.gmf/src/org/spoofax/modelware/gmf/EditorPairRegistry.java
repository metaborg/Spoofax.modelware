package org.spoofax.modelware.gmf;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramEditor;
import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;

public class EditorPairRegistry {

	private static EditorPairRegistry instance = new EditorPairRegistry();
	private Map<UniversalEditor, EditorPair> mapT = new HashMap<UniversalEditor, EditorPair>();
	private Map<DiagramEditor, EditorPair> mapD = new HashMap<DiagramEditor, EditorPair>();
	
	private EditorPairRegistry() {
		GMFBridgeUtil.getActivePage().addPartListener(new EditorCloseListener());
	}
	
	public static EditorPairRegistry getInstance() {
		return instance;
	}
	
	public EditorPair remove(IEditorPart editor) {
		EditorPair editorPair = get(editor);
		if (editorPair != null) {		
			mapT.remove(editorPair.getTextEditor());
			mapD.remove(editorPair.getDiagramEditor());	
			
			editorPair.dispose();
		}
		return editorPair;
	}
	
	public EditorPair get(IEditorPart editor) {
		if (editor instanceof UniversalEditor) {
			return mapT.get(editor);
		}
		else {
			return mapD.get(editor);
		}
	}
	
	public EditorPair get(String textFilePath) {
		IEditorPart textEditor = GMFBridgeUtil.findTextEditor(textFilePath);
		return mapT.get(textEditor);
	}
	
	public EditorPair get(String textFilePath, String packageName) {
		UniversalEditor textEditor = GMFBridgeUtil.findTextEditor(textFilePath);
		if (contains(textEditor)) {
			return get(textEditor);
		}
		
		DiagramEditor diagramEditor = GMFBridgeUtil.findDiagramEditor(textFilePath, packageName);
		EPackage ePackage = EPackage.Registry.INSTANCE.getEPackage(packageName);
		
		if (textEditor != null && diagramEditor != null && ePackage != null && GMFBridgeUtil.isInitialised(diagramEditor)) {
			EditorPair editorPair = new EditorPair(textEditor, diagramEditor, ePackage);
			mapT.put(textEditor, editorPair);
			mapD.put(diagramEditor, editorPair);
			return editorPair;
		}
		
		return null;
	}
	
	public Collection<EditorPair> getAll() {
		return mapT.values();
	}
	
	public boolean contains(IEditorPart editor) {
		return (get(editor) != null);
	}
	
	class EditorCloseListener implements IPartListener {

		@Override
		public void partClosed(IWorkbenchPart part) {
			
			 if (part instanceof IEditorPart) {
				 IEditorPart editor = (IEditorPart) part;
				 
				 if (getInstance().contains(editor)) {
					 getInstance().remove(editor);
				 }
			 }
		}
		
		@Override
		public void partActivated(IWorkbenchPart part) {
		}

		@Override
		public void partBroughtToTop(IWorkbenchPart part) {
		}

		@Override
		public void partDeactivated(IWorkbenchPart part) {
		}

		@Override
		public void partOpened(IWorkbenchPart part) {
		}
	}
}


