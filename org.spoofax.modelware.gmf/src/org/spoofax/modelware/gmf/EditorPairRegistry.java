package org.spoofax.modelware.gmf;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramEditor;
import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;

/**
 * @author Oskar van Rest
 */
public class EditorPairRegistry {

	private static EditorPairRegistry instance = null;
	private Map<UniversalEditor, EditorPair> mapT = new HashMap<UniversalEditor, EditorPair>();
	private Map<DiagramEditor, EditorPair> mapD = new HashMap<DiagramEditor, EditorPair>();
	private HashMap<String, IEditorPart> singleEditors = new HashMap<String, IEditorPart>();

	private EditorPairRegistry() {
		registerOpenEditors();
		installEditorPartListener();
	}

	private void registerOpenEditors() {
		for (IWorkbenchPage page : BridgeUtil.getAllWorkbenchPages()) {
			IEditorReference[] editors = page.getEditorReferences();
			for (int i=0;i<editors.length; i++) {
				registerPart(editors[i].getEditor(false));
			}
		}
	}

	private void installEditorPartListener() {
		EditorPartListener listener = new EditorPartListener();
		for (IWorkbenchPage page : BridgeUtil.getAllWorkbenchPages()) {
			page.addPartListener(listener);
		}
	}

	public static EditorPairRegistry getInstance() {
		if (instance == null) {
			instance = new EditorPairRegistry();
		}
		return instance;
	}

	public void add(EditorPair editorPair) {
		mapT.put(editorPair.getTextEditor(), editorPair);
		mapD.put(editorPair.getDiagramEditor(), editorPair);
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
		} else {
			return mapD.get(editor);
		}
	}

	// public EditorPair get(String textFilePath) {
	// IEditorPart textEditor = BridgeUtil.findTextEditor(textFilePath);
	// return mapT.get(textEditor);
	// }

	// public EditorPair get(String textFilePath, String packageName) {
	// UniversalEditor textEditor = BridgeUtil.findTextEditor(textFilePath);
	// if (contains(textEditor)) {
	// return get(textEditor);
	// }
	//
	// DiagramEditor diagramEditor = BridgeUtil.findDiagramEditor(textFilePath, packageName);
	// EPackage ePackage = EPackage.Registry.INSTANCE.getEPackage(packageName);
	//
	// if (textEditor != null && diagramEditor != null && ePackage != null && BridgeUtil.isInitialised(diagramEditor)) {
	// EditorPair editorPair = new EditorPair(textEditor, diagramEditor, ePackage);
	// mapT.put(textEditor, editorPair);
	// mapD.put(diagramEditor, editorPair);
	// return editorPair;
	// }
	//
	// return null;
	// }

	public Collection<EditorPair> getAll() {
		return mapT.values();
	}

	public boolean contains(IEditorPart editor) {
		return (get(editor) != null);
	}

	private void registerPart(IWorkbenchPart part) {
		if (part instanceof UniversalEditor || part instanceof DiagramEditor) {
			IEditorPart editor = (IEditorPart) part;

			String filePath = BridgeUtil.getFilePath(editor);
			String otherFilePath;

			String extension = BridgeUtil.getFileExtension(editor);
			String otherExtension;

			Language language = LanguageRegistry.getInstance().get(extension);
			if (language == null) {
				return;
			}

			UniversalEditor textEditor = null;
			DiagramEditor diagramEditor = null;

			if (editor instanceof UniversalEditor) {
				textEditor = (UniversalEditor) editor;
				otherExtension = language.getDiagramFileExtension();
			} else {
				diagramEditor = (DiagramEditor) editor;
				otherExtension = language.getTextFileExtension();
			}

			StringBuilder sb = new StringBuilder(filePath);
			sb.replace(filePath.lastIndexOf(extension), filePath.lastIndexOf(extension) + extension.length(), otherExtension);
			otherFilePath = sb.toString();

			if (singleEditors.containsKey(otherFilePath)) {
				if (editor instanceof UniversalEditor) {
					diagramEditor = (DiagramEditor) singleEditors.remove(otherFilePath);
				} else {
					textEditor = (UniversalEditor) singleEditors.remove(otherFilePath);
				}

				EditorPair editorPair = new EditorPair(textEditor, diagramEditor, language);
				EditorPairRegistry.getInstance().add(editorPair);
			} else {
				singleEditors.put(filePath, editor);
			}
			;
		}
	}
	
	class EditorPartListener implements IPartListener {

		
		@Override
		public void partOpened(IWorkbenchPart part) {
			registerPart(part);
		}

		@Override
		public void partClosed(IWorkbenchPart part) {
			if (part instanceof UniversalEditor || part instanceof DiagramEditor) {
				IEditorPart editor = (IEditorPart) part;

				if (EditorPairRegistry.getInstance().contains(editor)) {
					EditorPair editorPair = EditorPairRegistry.getInstance().remove(editor);
					IEditorPart partner = editorPair.getPartner(editor);
					singleEditors.put(BridgeUtil.getFilePath(partner), partner);
				} else {
					singleEditors.remove(BridgeUtil.getFilePath(editor));
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
	}
}
