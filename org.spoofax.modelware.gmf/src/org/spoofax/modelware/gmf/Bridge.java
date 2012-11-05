package org.spoofax.modelware.gmf;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.impl.EPackageRegistryImpl;
import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramEditor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
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
	
	private static Bridge instance = new Bridge();
	private ITermFactory termFactory = new TermFactory();
	private IEditorPart lastActiveEditor;

	private Bridge() {
		installLastActiveEditorListener();
	}

	public static Bridge getInstance() {
		return instance;
	}

	private void installLastActiveEditorListener() {
		LastActiveEditorListener listener = new LastActiveEditorListener();
		for (IWorkbenchPage page : BridgeUtil.getAllWorkbenchPages()) {
			page.addPartListener(listener);
		}
	}
	
	public IEditorPart getLastActiveEditor() {
		return lastActiveEditor;
	}
	
	public void term2Model(EditorPair editorPair, IStrategoTerm analysedAST) {
		
		final DiagramEditor diagramEditor = editorPair.getDiagramEditor();
		
		EObject newModel = new Term2Model(EPackageRegistryImpl.INSTANCE.getEPackage(editorPair.getLanguage().getPackageName())).convert(analysedAST);
		EObject currentModel = BridgeUtil.getSemanticModel(diagramEditor);

		if (currentModel == null)
			return;

		editorPair.notifyObservers(BridgeEvent.PreTerm2Model);
		CompareUtil.merge(newModel, currentModel);
		editorPair.notifyObservers(BridgeEvent.PostTerm2Model);

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
		IStrategoTerm newTerm = new Model2Term(termFactory).convert(BridgeUtil.getSemanticModel(diagramEditor));

		EditorState editor = EditorState.getEditorFor(textEditor);
		ITextReplacer textReplacer = null;
		try {
			textReplacer = editor.getDescriptor().createService(ITextReplacer.class, editor.getParseController());
		} catch (BadDescriptorException e) {
			e.printStackTrace();
		}

		editorPair.notifyObservers(BridgeEvent.PreModel2Term);
		textReplacer.replaceText(termFactory.makeList(termFactory.makeTuple(currentTerm, newTerm)));
		editorPair.notifyObservers(BridgeEvent.PostModel2Term);
	}
	
	private class LastActiveEditorListener implements IPartListener {

		@Override
		public void partActivated(IWorkbenchPart part) {
			if (part instanceof IEditorPart) {
				lastActiveEditor = (IEditorPart) part;
			}
		}

		@Override
		public void partBroughtToTop(IWorkbenchPart part) {
		}

		@Override
		public void partClosed(IWorkbenchPart part) {
		}

		@Override
		public void partDeactivated(IWorkbenchPart part) {
		}

		@Override
		public void partOpened(IWorkbenchPart part) {
		}
		
	}
}