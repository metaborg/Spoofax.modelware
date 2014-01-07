package org.spoofax.modelware.gmf.resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.swt.widgets.Display;
import org.spoofax.modelware.emf.resource.SpoofaxEMFResource;
import org.spoofax.modelware.emf.utils.Utils;
import org.spoofax.modelware.gmf.EditorPair;
import org.spoofax.modelware.gmf.EditorPairRegistry;
import org.strategoxt.imp.runtime.FileState;

/**
 * Extension of Spoofax' EMF resource implementation (SpoofaxEMFResource) that handles save synchronization.
 * Choosing 'save' when either the textual or graphical editor is active, causes resources of both editors to be persisted.
 * 
 * @author oskarvanrest
 */
public class SpoofaxGMFResource extends SpoofaxEMFResource {

	private boolean debouncer;
	
	public SpoofaxGMFResource(URI uri) {
		super(uri);
	}

	/**
	 * @override
	 */
	protected void doLoad(InputStream inputStream, Map<?, ?> options) {
		super.doLoad(inputStream, options);
		
		//TODO: put this elsewhere
		UniversalEditor textEditor = Utils.findSpoofaxEditor(path);
		EditorPair editorPair = EditorPairRegistry.getInstance().get(textEditor);
		if (editorPair != null) {
			editorPair.loadSemanticModel();
		}
	}
	
	/**
	 * @override
	 */
	protected void doSave(OutputStream outputStream, Map<?, ?> options) {
		final UniversalEditor textEditor = Utils.findSpoofaxEditor(path);
		
		if (textEditor == null) {
			FileState filestate = null;
			try {
				filestate = FileState.getFile(path, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (Utils.isDiagramToTextSynchronizationEnabled(filestate)) {
				super.doSave(outputStream, options);
			}
			else {
				try {
					outputStream.write(Files.readAllBytes(Paths.get(path.toOSString())));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		else {
			if (textEditor.isDirty()) {
				Display.getDefault().syncExec((new Runnable() {
					public void run() {
						debouncer = true;
						textEditor.doSave(new NullProgressMonitor());
					}
				}));
			}
			
			try {
				outputStream.write(textEditor.getDocumentProvider().getDocument(textEditor.getEditorInput()).get().getBytes());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public class SaveSynchronization implements IDocumentListener {

		private EditorPair editorPair;

		public SaveSynchronization(EditorPair editorPair) {
			this.editorPair = editorPair;
		}
		
		@Override
		public void documentAboutToBeChanged(DocumentEvent event) {
			if (debouncer) {
				debouncer = false;
			}
			else {
				editorPair.getDiagramEditor().doSave(new NullProgressMonitor());
			}
		}

		@Override
		public void documentChanged(DocumentEvent event) {
		}
	}
}


