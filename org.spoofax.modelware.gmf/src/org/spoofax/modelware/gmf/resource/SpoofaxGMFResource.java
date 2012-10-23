package org.spoofax.modelware.gmf.resource;

import java.io.OutputStream;
import java.util.Map;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.spoofax.modelware.emf.resource.SpoofaxResource;
import org.spoofax.modelware.gmf.GMFBridgeUtil;

public class SpoofaxGMFResource extends SpoofaxResource {

	public SpoofaxGMFResource(URI uri) {
		super(uri);
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
