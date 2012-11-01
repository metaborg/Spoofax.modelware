package org.spoofax.modelware.gmf;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramEditor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.modelware.emf.Model2Term;
import org.spoofax.modelware.emf.Term2Model;
import org.spoofax.modelware.emf.compare.CompareUtil;
import org.spoofax.modelware.gmf.editorservices.SaveSynchronization;
import org.spoofax.modelware.gmf.editorservices.UndoRedoSynchronization;
import org.spoofax.terms.TermFactory;
import org.strategoxt.imp.runtime.EditorState;
import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;
import org.strategoxt.imp.runtime.services.ITextReplacer;
import org.strategoxt.lang.Context;

public class GMFBridge {

	private static GMFBridge instance = new GMFBridge();
	public static TermFactory termFactory = new TermFactory();

	private GMFBridge() {
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

	public void term2Model(EditorPair editorPair, IStrategoTerm analysedAST) {
		final DiagramEditor diagramEditor = editorPair.getDiagramEditor();
		
		EObject newModel = new Term2Model(editorPair.getEPackage()).convert(analysedAST);
		EObject currentModel = GMFBridgeUtil.getSemanticModel(diagramEditor);

		if (currentModel == null)
			return;

		CompareUtil.merge(newModel, currentModel);

		// Workaround for http://www.eclipse.org/forums/index.php/m/885469/#msg_885469
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				diagramEditor.getDiagramEditPart().addNotify();
			}
		});
	}
	
	public void model2Term(EditorPair editorPair) {
		final IEditorPart textEditor = editorPair.getTextEditor();
		final DiagramEditor diagramEditor = editorPair.getDiagramEditor();

		IStrategoTerm currentTerm = EditorState.getEditorFor(textEditor).getCurrentAst();
		IStrategoTerm newTerm = new Model2Term(GMFBridge.termFactory).convert(GMFBridgeUtil.getSemanticModel(diagramEditor));

		EditorState editor = EditorState.getEditorFor(textEditor);
		ITextReplacer textReplacer = null;
		try {
			textReplacer = editor.getDescriptor().createService(ITextReplacer.class, editor.getParseController());
		} catch (BadDescriptorException e) {
			e.printStackTrace();
		}

		textReplacer.replaceText(GMFBridge.termFactory.makeList(GMFBridge.termFactory.makeTuple(currentTerm, newTerm)));
	}
}