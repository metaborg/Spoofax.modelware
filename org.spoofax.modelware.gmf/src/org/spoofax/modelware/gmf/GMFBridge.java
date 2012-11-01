package org.spoofax.modelware.gmf;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.modelware.emf.Term2Model;
import org.spoofax.modelware.emf.compare.CompareUtil;
import org.spoofax.modelware.gmf.editorservices.SaveSynchronization;
import org.spoofax.modelware.gmf.editorservices.UndoRedoSynchronization;
import org.spoofax.terms.StrategoAppl;
import org.spoofax.terms.TermFactory;
import org.strategoxt.lang.Context;

public class GMFBridge {

	private static GMFBridge instance = new GMFBridge();
	public static TermFactory termFactory = new TermFactory();

	private GMFBridge() {
		GMFBridgeUtil.getActivePage().addPartListener(new GMFBridgePartListener());
		ICommandService service = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
		service.addExecutionListener(new SaveSynchronization());
		service.addExecutionListener(new UndoRedoSynchronization());
	}

	public static GMFBridge getInstance() {
		return instance;
	}

	public IStrategoTerm synchronize(Context context, IStrategoTerm analysedAST, IStrategoString textFilePath, IStrategoString packageName) {
		EditorPair editorPair = EditorPairRegistry.getInstance().get(textFilePath.stringValue(), packageName.stringValue());

		if (editorPair == null || !(analysedAST instanceof IStrategoAppl)) {
			return analysedAST;
		}

		if (editorPair != null && analysedAST instanceof IStrategoAppl) {
			if (editorPair.getLastAST() == null || !editorPair.getLastAST().equals(analysedAST)) {
				if (editorPair.getDebouncer().text2modelAllowed()) {
					term2Model(editorPair, analysedAST);
				}
			}
			editorPair.setLastAST(analysedAST);
		}
		
		return analysedAST;
	}

	private void term2Model(final EditorPair editorPair, IStrategoTerm analysedAST) {
		if (!(analysedAST instanceof StrategoAppl))
			return;

		EObject newModel = new Term2Model(editorPair.getEPackage()).convert(analysedAST);
		EObject currentModel = GMFBridgeUtil.getSemanticModel(editorPair.getDiagramEditor());

		if (currentModel == null)
			return;

		CompareUtil.merge(newModel, currentModel);

		// Workaround for http://www.eclipse.org/forums/index.php/m/885469/#msg_885469
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				editorPair.getDiagramEditor().getDiagramEditPart().addNotify();
			}
		});
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
		// if (part instanceof IEditorPart) {
		// IEditorPart editor = (IEditorPart) part;
		// EditorPairs editorPairs = GMFBridge.getInstance().getEditorPairs();
		//
		// if (editorPairs.containsEditor(editor)) {
		// editorPairs.re
		// }
		//
		//
		//
		// GMFBridge.getInstance().getEditorPairs().get(editor);
		// if (editorPart.getEditorInput() instanceof FileEditorInput) {
		// FileEditorInput input = (FileEditorInput) editorPart.getEditorInput();
		// String key = input.getPath().toString();
		// GMFBridge.getInstance().getEditorPairs().remove(key);
		// }
		// }
	}

	@Override
	public void partDeactivated(IWorkbenchPart part) {
	}

	@Override
	public void partOpened(IWorkbenchPart part) {
	}
}