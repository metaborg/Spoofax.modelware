package org.spoofax.modelware.gmf.resource;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.spoofax.modelware.emf.resource.SpoofaxResource;
import org.spoofax.modelware.gmf.EditorPair;
import org.spoofax.modelware.gmf.GMFBridge;
import org.spoofax.modelware.gmf.GMFBridgeUtil;

public class SpoofaxGMFResource extends SpoofaxResource {

	public SpoofaxGMFResource(URI uri) {
		super(uri);
	}

	/**
	 * @override
	 */
	protected void doLoad(InputStream inputStream, Map<?, ?> options) {
		super.doLoad(inputStream, options);
		
		EditorPair editorPair = GMFBridge.getInstance().getEditorPair(filePath.toString());
		if (editorPair != null) {
			editorPair.loadSemanticModel();
		}
	}
	
	/**
	 * @override
	 */
	protected void doSave(OutputStream outputStream, Map<?, ?> options) {
		final IEditorPart textEditor = GMFBridgeUtil.findTextEditor(filePath);
		
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
