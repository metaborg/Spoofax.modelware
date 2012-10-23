package org.spoofax.modelware.gmf;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramEditor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.FileEditorInput;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.modelware.emf.Term2Model;
import org.spoofax.modelware.emf.compare.CompareUtil;
import org.strategoxt.lang.Context;

public class GMFBridge {

	private static GMFBridge instance = new GMFBridge();

	private Map<String, EditorPair> editorPairs;

	private GMFBridge() {
		editorPairs = new HashMap<String, EditorPair>();
		GMFBridgeUtil.getActivePage().addPartListener(new GMFBridgePartListener());
	}

	public static GMFBridge getInstance() {
		return instance;
	}

	public IStrategoTerm synchronize(Context context, IStrategoTerm analysedAST, IStrategoString filePath, IStrategoString textFileExtension, IStrategoString packageName) {
		EditorPair editorPair = getEditorPair(context, filePath.stringValue(), textFileExtension.stringValue(), packageName.stringValue());
		
		if (editorPair != null && editorPair.getDebouncer().text2modelAllowed())
			term2Model(editorPair, analysedAST);
		
		return analysedAST;
	}

	private EditorPair getEditorPair(Context context, String filePath, String textFileExtension, String packageName) {
		String key = filePath; // TODO: combine filePath + textFileExtension + packageName instead? What if multiple text editors use the same path (but different extensions)?

		if (editorPairs.containsKey(key)) {
			return editorPairs.get(key);
		} else {
			IEditorPart textEditor = GMFBridgeUtil.findTextEditor(filePath, textFileExtension);
			DiagramEditor diagramEditor = GMFBridgeUtil.findDiagramEditor(filePath, textFileExtension, packageName);
			EPackage ePackage = EPackage.Registry.INSTANCE.getEPackage(packageName);
			
			if (!(textEditor == null || diagramEditor == null || ePackage == null)) {
				EditorPair editorPair = new EditorPair(textEditor, diagramEditor, context, ePackage);
				editorPairs.put(key, editorPair);
				return editorPair;
			}
		}
		
		return null;
	}

	private void term2Model(final EditorPair editorPair, IStrategoTerm analysedAST) {
		EObject newModel = new Term2Model(editorPair.getEPackage()).convert(analysedAST);
		EObject currentModel = GMFBridgeUtil.getSemanticModel(editorPair.getDiagramEditor());

		if (currentModel == null)
			return;
		
		CompareUtil.merge(newModel, currentModel);

		// Workaround for http://www.eclipse.org/forums/index.php/m/885469/#msg_885469
		// TODO remove once the people of GMF fixed it
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				editorPair.getDiagramEditor().getDiagramEditPart().addNotify();
			}
		});
	}

	public EditorPair removeEditorPair(String key) {
		EditorPair editorPair = editorPairs.remove(key);
		if (editorPair != null) {
			editorPair.dispose();
		}
		return editorPair;
	}
}

class GMFBridgePartListener implements IPartListener {

	@Override
	public void partActivated(IWorkbenchPart part) {
	}

	@Override
	public void partBroughtToTop(IWorkbenchPart part) {
	}

	@Override
	public void partClosed(IWorkbenchPart part) {
		if (part instanceof IEditorPart) {
			IEditorPart editorPart = (IEditorPart) part;
			if (editorPart.getEditorInput() instanceof FileEditorInput) {
				FileEditorInput input = (FileEditorInput) editorPart.getEditorInput();
				String path = input.getPath().toString();
				String key = path.substring(0, path.lastIndexOf("."));
				GMFBridge.getInstance().removeEditorPair(key);
			}
		}
	}

	@Override
	public void partDeactivated(IWorkbenchPart part) {
	}

	@Override
	public void partOpened(IWorkbenchPart part) {
	}
}