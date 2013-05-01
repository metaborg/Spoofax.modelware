package org.spoofax.modelware.gmf;

import org.eclipse.emf.compare.Comparison;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.impl.EPackageRegistryImpl;
import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramEditor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.modelware.emf.Model2Term;
import org.spoofax.modelware.emf.Term2Model;
import org.spoofax.modelware.emf.compare.CompareUtil;
import org.spoofax.terms.TermFactory;
import org.strategoxt.imp.runtime.EditorState;
import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;
import org.strategoxt.imp.runtime.services.ITextReplacer;

/**
 * @author Oskar van Rest
 */
public class Bridge {
	
	private static Bridge instance = null;
	private ITermFactory termFactory = new TermFactory();

	private Bridge() {
	}

	public static Bridge getInstance() {
		if (instance == null) {
			instance = new Bridge();
		}
		return instance;
	}
	
	public void term2Model(final EditorPair editorPair, IStrategoTerm analysedAST) {
		
		final DiagramEditor diagramEditor = editorPair.getDiagramEditor();
		
		editorPair.notifyObservers(BridgeEvent.PreTerm2Model);
		EObject left = new Term2Model(EPackageRegistryImpl.INSTANCE.getEPackage(editorPair.getLanguage().getPackageName())).convert(analysedAST);
		editorPair.notifyObservers(BridgeEvent.PostTerm2Model);
		
		EObject right = BridgeUtil.getSemanticModel(diagramEditor);
		
		if (right == null)
			return;

		editorPair.notifyObservers(BridgeEvent.PreCompare);
		Comparison comparison = CompareUtil.compare(left, right);
		editorPair.notifyObservers(BridgeEvent.PostCompare);
		
		editorPair.notifyObservers(BridgeEvent.PreMerge);
		CompareUtil.merge(comparison, right);

		editorPair.notifyObservers(BridgeEvent.PreRender);
		// Workaround for http://www.eclipse.org/forums/index.php/m/885469/#msg_885469
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				diagramEditor.getDiagramEditPart().addNotify();
				editorPair.notifyObservers(BridgeEvent.PostRender);
			}
		});
	}
	
	public void handleModelChange(EditorPair editorPair) {
		
		final IEditorPart textEditor = editorPair.getTextEditor();
		final DiagramEditor diagramEditor = editorPair.getDiagramEditor();

		IStrategoTerm currentTerm = EditorState.getEditorFor(textEditor).getCurrentAst();
		
		editorPair.notifyObservers(BridgeEvent.PreModel2Term);
		IStrategoTerm newTerm = new Model2Term(termFactory).convert(BridgeUtil.getSemanticModel(diagramEditor));
		editorPair.notifyObservers(BridgeEvent.PostModel2Term);
		
		EditorState editor = EditorState.getEditorFor(textEditor);
		ITextReplacer textReplacer = null;
		try {
			textReplacer = editor.getDescriptor().createService(ITextReplacer.class, editor.getParseController());
		} catch (BadDescriptorException e) {
			e.printStackTrace();
		}

		IStrategoList list = termFactory.makeList(termFactory.makeTuple(currentTerm, newTerm));
		
		editorPair.notifyObservers(BridgeEvent.PreLayoutPreservation);
		textReplacer.replaceText(list);
		editorPair.notifyObservers(BridgeEvent.PostLayoutPreservation);
	}
}