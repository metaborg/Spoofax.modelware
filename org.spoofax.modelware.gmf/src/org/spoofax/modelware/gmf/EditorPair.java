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

	public static EObject semanticModel; //TODO: static??
	private ModelChangeListener semanticModelContentAdapter;
	private DiagramSelectionChangedListener GMFSelectionChangedListener;
	private TextSelectionChangedListener spoofaxSelectionChangedListener;
	public IStrategoTerm adjustedAST;

	public EditorPair(UniversalEditor textEditor, DiagramEditor diagramEditor, Language language) {
		this.observers = new ArrayList<EditorPairObserver>();

		this.textEditor = textEditor;
		this.diagramEditor = diagramEditor;
		this.language = language;

		loadSemanticModel();
		addSelectionChangeListeners();
		textEditor.addModelListener(new TextChangeListener(this));

		OperationHistoryFactory.getOperationHistory().addOperationHistoryListener(new UndoRedoEventGenerator(this));
		// note: order of execution of the statements above and the one below matters
		OperationHistoryFactory.getOperationHistory().addOperationHistoryListener(new UndoRedo(this));

		SpoofaxGMFResource resource = (SpoofaxGMFResource) diagramEditor.getEditingDomain().getResourceSet().getResources().get(1);
		textEditor.addOnSaveListener(resource.new SaveSynchronization(this));

		TransactionalEditingDomain editingDomain = TransactionUtil.getEditingDomain(EditorPairUtil.getSemanticModel(diagramEditor));
		editingDomain.addResourceSetListener(new MergeFinishedEventGenerator(this));
		
		adjustedAST = SpoofaxEMFUtils.getAdjustedAST(EditorState.getEditorFor(textEditor));
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

	public void doModelToTerm(EObject model) {
		EditorState editor = EditorState.getEditorFor(textEditor);

		notifyObservers(EditorPairEvent.PreModel2Term);
		IStrategoTerm newTree = new Model2Term(SpoofaxEMFUtils.termFactory).convert(model);
		newTree = SpoofaxEMFUtils.adjustModel2Tree(newTree, editor);
		notifyObservers(EditorPairEvent.PostModel2Term);

		notifyObservers(EditorPairEvent.PreLayoutPreservation);
		String replacement = SpoofaxEMFUtils.calculateTextReplacement(newTree, editor);
		notifyObservers(EditorPairEvent.PostLayoutPreservation);
		SpoofaxEMFUtils.setEditorContent(editor, replacement);
	}
	
	public void doTerm2Model() {
		adjustedAST = SpoofaxEMFUtils.getAdjustedAST(EditorState.getEditorFor(textEditor));

		notifyObservers(EditorPairEvent.PreTerm2Model);
		EObject left = new Term2Model(EPackageRegistryImpl.INSTANCE.getEPackage(getLanguage().getPackageName())).convert(adjustedAST);
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
		// Workaround for http://www.eclipse.org/forums/index.php/m/885469/#msg_885469
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				diagramEditor.getDiagramEditPart().addNotify();
				notifyObservers(EditorPairEvent.PostRender);
			}
		});
	}
}