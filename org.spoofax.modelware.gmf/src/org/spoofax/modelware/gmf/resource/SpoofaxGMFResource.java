package org.spoofax.modelware.gmf.resource;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.swt.widgets.Display;
import org.spoofax.modelware.emf.resource.SpoofaxResource;
import org.spoofax.modelware.gmf.EditorPair;
import org.spoofax.modelware.gmf.EditorPairRegistry;
import org.spoofax.modelware.gmf.BridgeUtil;
import org.strategoxt.imp.runtime.Environment;

/**
 * Extension of Spoofax' EMF resource implementation (SpoofaxEMFResource) that handles save synchronization.
 * Choosing 'save' when either the textual or graphical editor is active, causes resources of both editors to be persisted.
 * 
 * @author Oskar van Rest
 */
public class SpoofaxGMFResource extends SpoofaxResource {

	public SpoofaxGMFResource(URI uri) {
		super(uri);
	}

	/**
	 * @override
	 */
	protected void doLoad(InputStream inputStream, Map<?, ?> options) {
		super.doLoad(inputStream, options);
		
		//TODO: put this elsewhere
		UniversalEditor textEditor = BridgeUtil.findTextEditor(filePath);
		EditorPair editorPair = EditorPairRegistry.getInstance().get(textEditor);
		if (editorPair != null) {
			editorPair.loadSemanticModel();
		}
	}
	
	/**
	 * @override
	 */
	protected void doSave(OutputStream outputStream, Map<?, ?> options) {
		final UniversalEditor textEditor = BridgeUtil.findTextEditor(filePath);
		
		if (textEditor == null || !textEditor.isDirty()) {
			super.doSave(outputStream, options);
		}
		else {
			Display.getDefault().asyncExec((new Runnable() {
				public void run() {
					if (textEditor.getDocumentProvider() != null) {
						textEditor.doSave(new NullProgressMonitor());
					}
					else {
						Environment.logException("Spoofax.modelware/8.");
					}
				}
			}));
		}
	}
	

	public class SaveSynchronization implements IDocumentListener {

		private EditorPair editorPair;

		public SaveSynchronization(EditorPair editorPair) {
			this.editorPair = editorPair;
		}
		
		@Override
		public void documentAboutToBeChanged(DocumentEvent event) {
			editorPair.getDiagramEditor().doSave(new NullProgressMonitor());
		}

		@Override
		public void documentChanged(DocumentEvent event) {
		}
	}
}


