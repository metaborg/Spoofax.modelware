package org.spoofax.modelware.gmf;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.OperationHistoryFactory;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.transaction.ResourceSetChangeEvent;
import org.eclipse.emf.transaction.ResourceSetListenerImpl;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramEditor;
import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.text.undo.DocumentUndoManagerRegistry;
import org.eclipse.text.undo.IDocumentUndoManager;
import org.eclipse.ui.IEditorPart;
import org.spoofax.modelware.gmf.editorservices.DiagramSelectionChangedListener;
import org.spoofax.modelware.gmf.editorservices.TextSelectionChangedListener;
import org.spoofax.modelware.gmf.editorservices.UndoRedo;
import org.spoofax.modelware.gmf.editorservices.UndoRedoEventGenerator;
import org.spoofax.modelware.gmf.resource.SpoofaxGMFResource;

/**
 * @author Oskar van Rest
 */
public class EditorPair {

	Collection<EditorPairObserver> observers;
	
	private UniversalEditor textEditor;
	private DiagramEditor diagramEditor;
	private Language language;
	
	private EObject semanticModel;
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
		// order of execution of the one above and below is essential
		OperationHistoryFactory.getOperationHistory().addOperationHistoryListener(new UndoRedo(this));
		
		SpoofaxGMFResource resource = (SpoofaxGMFResource) diagramEditor.getEditingDomain().getResourceSet().getResources().get(1);
		textEditor.addOnSaveListener(resource.new SaveSynchronization(this));
		
		TransactionalEditingDomain editingDomain = TransactionUtil.getEditingDomain(BridgeUtil.getSemanticModel(diagramEditor));
		editingDomain.addResourceSetListener(new MergeFinishedEventGenerator(this));
		//observers.add(new SpoofaxModelwareBenchmarker());
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
				editorPair.notifyObservers(BridgeEvent.PostMerge);
			}
		}
		
		private class MergeStartListener implements EditorPairObserver {

			@Override
			public void notify(BridgeEvent event) {
				if (event == BridgeEvent.PreMerge) {
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
		//TODO textchangelistener
		BridgeUtil.getSemanticModel(diagramEditor).eAdapters().remove(semanticModelContentAdapter);
		diagramEditor.getEditorSite().getSelectionProvider().removeSelectionChangedListener(GMFSelectionChangedListener);
		textEditor.getSite().getSelectionProvider().removeSelectionChangedListener(spoofaxSelectionChangedListener);
	}
	
	public void loadSemanticModel() {
		if (semanticModel != null && semanticModel.eAdapters().contains(semanticModelContentAdapter))
			semanticModel.eAdapters().remove(semanticModelContentAdapter);
			
		semanticModel = BridgeUtil.getSemanticModel(diagramEditor);
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
		else return null;
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
	
	public void notifyObservers(BridgeEvent event) {
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
}