package org.spoofax.modelware.gmf;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.OperationHistoryFactory;
import org.eclipse.emf.compare.Comparison;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.impl.EPackageRegistryImpl;
import org.eclipse.emf.transaction.ResourceSetChangeEvent;
import org.eclipse.emf.transaction.ResourceSetListenerImpl;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramEditor;
import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.text.undo.DocumentUndoManagerRegistry;
import org.eclipse.text.undo.IDocumentUndoManager;
import org.eclipse.ui.IEditorPart;
import org.spoofax.interpreter.core.Interpreter;
import org.spoofax.interpreter.core.InterpreterErrorExit;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.core.InterpreterExit;
import org.spoofax.interpreter.core.UndefinedStrategyException;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.modelware.emf.compare.CompareUtil;
import org.spoofax.modelware.emf.tree2model.Model2Term;
import org.spoofax.modelware.emf.tree2model.Term2Model;
import org.spoofax.modelware.emf.utils.SpoofaxEMFUtils;
import org.spoofax.modelware.gmf.editorservices.DiagramSelectionChangedListener;
import org.spoofax.modelware.gmf.editorservices.TextSelectionChangedListener;
import org.spoofax.modelware.gmf.editorservices.UndoRedo;
import org.spoofax.modelware.gmf.editorservices.UndoRedoEventGenerator;
import org.spoofax.modelware.gmf.resource.SpoofaxGMFResource;
import org.strategoxt.imp.runtime.EditorState;
import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;
import org.strategoxt.imp.runtime.dynamicloading.Descriptor;
import org.strategoxt.imp.runtime.dynamicloading.RefactoringFactory;
import org.strategoxt.imp.runtime.services.StrategoObserver;
import org.strategoxt.imp.runtime.services.StrategoTextChangeCalculator;

/**
 * An {@link EditorPair} holds a textual and a graphical editor and takes care of the
 * synchronization between them. It also registers a set of listeners for the purpose of integrating
 * editor services such as selection sharing and undo-redo functionality.
 * 
 * @author oskarvanrest
 */
public class EditorPair {

	Collection<EditorPairObserver> observers;

	private UniversalEditor textEditor;
	private DiagramEditor diagramEditor;
	private Language language;

	public static EObject semanticModel;
	private ModelChangeListener semanticModelContentAdapter;
	private DiagramSelectionChangedListener GMFSelectionChangedListener;
	private TextSelectionChangedListener spoofaxSelectionChangedListener;

	public EditorPair(UniversalEditor textEditor, DiagramEditor diagramEditor, Language language) {
		this.observers = new ArrayList<EditorPairObserver>();

		this.textEditor = textEditor;
		this.diagramEditor = diagramEditor;
		this.language = language;

		loadSemanticModel();
		addSelectionChangeListeners();
		textEditor.addModelListener(new TextChangeListener(this));

		OperationHistoryFactory.getOperationHistory().addOperationHistoryListener(new UndoRedoEventGenerator(this));
		// note: order of execution of the statement above and the one below is essential
		OperationHistoryFactory.getOperationHistory().addOperationHistoryListener(new UndoRedo(this));

		SpoofaxGMFResource resource = (SpoofaxGMFResource) diagramEditor.getEditingDomain().getResourceSet().getResources().get(1);
		textEditor.addOnSaveListener(resource.new SaveSynchronization(this));

		TransactionalEditingDomain editingDomain = TransactionUtil.getEditingDomain(EditorPairUtil.getSemanticModel(diagramEditor));
		editingDomain.addResourceSetListener(new MergeFinishedEventGenerator(this));
	}

	/**
	 * Generates a 'PostMerge' event once model merging is finished.
	 */
	private class MergeFinishedEventGenerator extends ResourceSetListenerImpl {

		private boolean merging = false;
		private EditorPair editorPair;

		public MergeFinishedEventGenerator(EditorPair editorPair) {
			this.editorPair = editorPair;
			this.editorPair.registerObserver(new MergeStartListener());
		}

		@Override
		public void resourceSetChanged(ResourceSetChangeEvent event) {
			if (merging) {
				merging = false;
				editorPair.notifyObservers(EditorPairEvent.PostMerge);
			}
		}

		private class MergeStartListener implements EditorPairObserver {

			@Override
			public void notify(EditorPairEvent event) {
				if (event == EditorPairEvent.PreMerge) {
					merging = true;
				}
			}
		}
	}

	private void addSelectionChangeListeners() {
		diagramEditor.getEditorSite().getSelectionProvider().addSelectionChangedListener(GMFSelectionChangedListener = new DiagramSelectionChangedListener(this));
		textEditor.getSite().getSelectionProvider().addSelectionChangedListener(spoofaxSelectionChangedListener = new TextSelectionChangedListener(this));
	}

	public void dispose() {
		// TODO textchangelistener, TextToModelOnSave
		EditorPairUtil.getSemanticModel(diagramEditor).eAdapters().remove(semanticModelContentAdapter);
		diagramEditor.getEditorSite().getSelectionProvider().removeSelectionChangedListener(GMFSelectionChangedListener);
		textEditor.getSite().getSelectionProvider().removeSelectionChangedListener(spoofaxSelectionChangedListener);
	}

	// TODO refactor
	public void loadSemanticModel() {
		if (semanticModel != null && semanticModel.eAdapters().contains(semanticModelContentAdapter))
			semanticModel.eAdapters().remove(semanticModelContentAdapter);

		semanticModel = EditorPairUtil.getSemanticModel(diagramEditor);
		if (semanticModel != null)
			semanticModel.eAdapters().add(semanticModelContentAdapter = new ModelChangeListener(this));
	}

	public UniversalEditor getTextEditor() {
		return textEditor;
	}

	public DiagramEditor getDiagramEditor() {
		return diagramEditor;
	}

	public IEditorPart getPartner(IEditorPart editor) {
		if (editor == textEditor)
			return diagramEditor;
		else if (editor == diagramEditor)
			return textEditor;
		else
			return null;
	}

	public Language getLanguage() {
		return language;
	}

	public void registerObserver(EditorPairObserver observer) {
		observers.add(observer);
	}

	public void unregisterObserver(EditorPairObserver observer) {
		observers.remove(observer);
	}

	public void notifyObservers(EditorPairEvent event) {
		for (EditorPairObserver observer : observers) {
			observer.notify(event);
		}
	}

	public IUndoContext getTextUndoContext() {
		IDocumentUndoManager documentUndoManager = DocumentUndoManagerRegistry.getDocumentUndoManager(textEditor.getDocumentProvider().getDocument(textEditor.getEditorInput()));
		return documentUndoManager.getUndoContext();
	}

	public IUndoContext getDiagramUndoContext() {
		return diagramEditor.getDiagramEditDomain().getDiagramCommandStack().getUndoContext();
	}

	public void doModelToTerm() {
		EditorState editor = EditorState.getEditorFor(textEditor);

		notifyObservers(EditorPairEvent.PreModel2Term);
		IStrategoTerm newTree = new Model2Term(EditorPairUtil.termFactory).convert(EditorPairUtil.getSemanticModel(diagramEditor));
		newTree = SpoofaxEMFUtils.adjustModel2Tree(editor, newTree);
		notifyObservers(EditorPairEvent.PostModel2Term);


//		IStrategoList list = EditorPairUtil.termFactory.makeList(EditorPairUtil.termFactory.makeTuple(oldTree, newTree));

//		 StrategoTextChangeCalculator changeCalculator = null;
//		try {
//			changeCalculator = createTextChangeCalculator(editorState.getDescriptor());
//		}
//		catch (BadDescriptorException e) {
//			e.printStackTrace();
//		}
//		
//		 notifyObservers(EditorPairEvent.PreLayoutPreservation);
//		 Collection<TextFileChange> changes = changeCalculator.getFileChanges(list, observer);
//		 notifyObservers(EditorPairEvent.PostLayoutPreservation);
//		 if (changes.size() > 0) {
//		 System.out.println("testje: " + changes.iterator().next().getEdit().toString());
//		 }
		
//		 Display.getDefault().syncExec(new Runnable() {
//		 public void run() {
//		 editorState.getDocument().set(result);
//		 }
//		 });

		// TODO: fix Spoofax/676, then replace code below with code above and remove
		// TextReplacer.java from Spoofax runtime

		notifyObservers(EditorPairEvent.PreLayoutPreservation);
		String replacement = SpoofaxEMFUtils.calculateTextReplacement(newTree, editor);
		notifyObservers(EditorPairEvent.PostLayoutPreservation);
		SpoofaxEMFUtils.setEditorContent(editor, replacement);
	}

	private static StrategoTextChangeCalculator createTextChangeCalculator(Descriptor d) throws BadDescriptorException {
		String ppStrategy = RefactoringFactory.getPPStrategy(d);
		String parenthesize = RefactoringFactory.getParenthesizeStrategy(d);
		String overrideReconstruction = RefactoringFactory.getOverrideReconstructionStrategy(d);
		String resugar = RefactoringFactory.getResugarStrategy(d);
		StrategoTextChangeCalculator textChangeCalculator = new StrategoTextChangeCalculator(ppStrategy, parenthesize, overrideReconstruction, resugar);
		return textChangeCalculator;
	}

	public void doTerm2Model(IStrategoTerm tree) {

		EditorState editorState = EditorState.getEditorFor(textEditor);

		try {
			StrategoObserver observer = editorState.getDescriptor().createService(StrategoObserver.class, editorState.getParseController());
			Interpreter itp = observer.getRuntime();
			itp.setCurrent(tree);
			itp.invoke("adjust-tree-to-model");
			tree = itp.current();
		}
		catch (UndefinedStrategyException e) {
			// continue without adjustment
		}
		catch (BadDescriptorException e) {
			e.printStackTrace();
		}
		catch (InterpreterErrorExit e) {
			e.printStackTrace();
		}
		catch (InterpreterExit e) {
			e.printStackTrace();
		}
		catch (InterpreterException e) {
			e.printStackTrace();
		}

		notifyObservers(EditorPairEvent.PreTerm2Model);
		EObject left = new Term2Model(EPackageRegistryImpl.INSTANCE.getEPackage(getLanguage().getPackageName())).convert(tree);
		notifyObservers(EditorPairEvent.PostTerm2Model);

		EObject right = EditorPairUtil.getSemanticModel(diagramEditor);

		if (right == null)
			return;

		notifyObservers(EditorPairEvent.PreCompare);
		Comparison comparison = CompareUtil.compare(left, right);
		notifyObservers(EditorPairEvent.PostCompare);

		notifyObservers(EditorPairEvent.PreMerge);
		CompareUtil.merge(comparison, right);

		notifyObservers(EditorPairEvent.PreRender);
		// Workaround for
		// http://www.eclipse.org/forums/index.php/m/885469/#msg_885469
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				diagramEditor.getDiagramEditPart().addNotify();
				notifyObservers(EditorPairEvent.PostRender);
			}
		});
	}
}