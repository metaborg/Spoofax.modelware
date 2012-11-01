package org.spoofax.modelware.gmf;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramEditor;
import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.ui.IEditorPart;

public class EditorPairRegistry {

	private static EditorPairRegistry instance = new EditorPairRegistry();
	private Map<UniversalEditor, EditorPair> mapT = new HashMap<UniversalEditor, EditorPair>();
	private Map<DiagramEditor, EditorPair> mapD = new HashMap<DiagramEditor, EditorPair>();
	
	private EditorPairRegistry() {
		
	}
	
	public static EditorPairRegistry getInstance() {
		return instance;
	}
	
	public EditorPair remove(IEditorPart editor) {
		EditorPair editorPair = get(editor);
		if (editorPair != null) {
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
}
