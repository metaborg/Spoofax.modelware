package org.spoofax.modelware.gmf;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.impl.EPackageRegistryImpl;
import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramEditor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.modelware.emf.Model2Term;
import org.spoofax.modelware.emf.Term2Model;
import org.spoofax.modelware.emf.compare.CompareUtil;
import org.spoofax.modelware.gmf.EditorPair.BridgeEvent;
import org.spoofax.modelware.gmf.editorservices.SaveSynchronization;
import org.spoofax.modelware.gmf.editorservices.UndoRedoSynchronization;
import org.spoofax.terms.TermFactory;
import org.strategoxt.imp.runtime.EditorState;
import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;
import org.strategoxt.imp.runtime.services.ITextReplacer;

/**
 * @author Oskar van Rest
 */
public class Bridge {
	
	private static Bridge instance = new Bridge();
	private TermFactory termFactory = new TermFactory();

	private Bridge() {
		ICommandService service = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
		service.addExecutionListener(new SaveSynchronization());
		service.addExecutionListener(new UndoRedoSynchronization());
	}

	public static Bridge getInstance() {
		return instance;
	}

	public void term2Model(EditorPair editorPair, IStrategoTerm analysedAST) {
		editorPair.notifyObservers(BridgeEvent.Term2Model);
		
		final DiagramEditor diagramEditor = editorPair.getDiagramEditor();
		
		EObject newModel = new Term2Model(EPackageRegistryImpl.INSTANCE.getEPackage(editorPair.getLanguage().getPackageName())).convert(analysedAST);
		EObject currentModel = BridgeUtil.getSemanticModel(diagramEditor);

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
		editorPair.notifyObservers(BridgeEvent.Model2Term);
		
		final IEditorPart textEditor = editorPair.getTextEditor();
		final DiagramEditor diagramEditor = editorPair.getDiagramEditor();

		IStrategoTerm currentTerm = EditorState.getEditorFor(textEditor).getCurrentAst();
		IStrategoTerm newTerm = new Model2Term(termFactory).convert(BridgeUtil.getSemanticModel(diagramEditor));

		EditorState editor = EditorState.getEditorFor(textEditor);
		ITextReplacer textReplacer = null;
		try {
			textReplacer = editor.getDescriptor().createService(ITextReplacer.class, editor.getParseController());
		} catch (BadDescriptorException e) {
			e.printStackTrace();
		}
		textReplacer.replaceText(termFactory.makeList(termFactory.makeTuple(currentTerm, newTerm)));
	}
}