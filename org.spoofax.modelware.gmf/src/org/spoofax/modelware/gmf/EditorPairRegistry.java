package org.spoofax.modelware.gmf;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramEditor;
import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.imp.ui.DefaultPartListener;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.spoofax.modelware.emf.Language;
import org.spoofax.modelware.emf.LanguageRegistry;
import org.spoofax.modelware.emf.utils.Utils;

/**
 * Registry that holds the set of active {@link EditorPair}s. A pair is currently created when
 * a textual and graphical editor are opened that belong to the same {@link Language} and both
 * hold a resource with the same file path and file name (i.g. `project1/test.ent` and 
 * `project1/test.ent_diagram`). The {@link EditorPair} is disposed when one of the two editors 
 * is closed.
 * 
 * @author oskarvanrest
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
		for (IWorkbenchPage page : Utils.getAllWorkbenchPages()) {
			IEditorReference[] editors = page.getEditorReferences();
			for (int i=0;i<editors.length; i++) {
				registerPart(editors[i].getEditor(false));
			}
		}
	}

	private void installEditorPartListener() {
		EditorPartListener listener = new EditorPartListener();
		for (IWorkbenchPage page : Utils.getAllWorkbenchPages()) {
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

	public Collection<EditorPair> getAll() {
		return mapT.values();
	}

	public boolean contains(IEditorPart editor) {
		return (get(editor) != null);
	}

	private void registerPart(IWorkbenchPart part) {
		if (part instanceof UniversalEditor || part instanceof DiagramEditor) {
			IEditorPart editor = (IEditorPart) part;

			String filePath = EditorPairUtil.getFilePath(editor);
			String otherFilePath;

			String extension = EditorPairUtil.getFileExtension(editor);
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
	
	class EditorPartListener extends DefaultPartListener {

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
					singleEditors.put(EditorPairUtil.getFilePath(partner), partner);
				} else {
					singleEditors.remove(EditorPairUtil.getFilePath(editor));
				}
			}
		}
	}
}
