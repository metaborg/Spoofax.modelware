package org.spoofax.modelware.gmf.resource;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.swt.widgets.Display;
import org.spoofax.modelware.emf.resource.SpoofaxResource;
import org.spoofax.modelware.gmf.EditorPair;
import org.spoofax.modelware.gmf.EditorPairRegistry;
import org.spoofax.modelware.gmf.BridgeUtil;

/**
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
					textEditor.doSave(new NullProgressMonitor());
				}
			}));
		}
	}
}
