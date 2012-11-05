package org.spoofax.modelware.gmf;

/**
 * @author Oskar van Rest
 */
import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.commands.operations.OperationHistoryFactory;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramEditor;
import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.spoofax.modelware.gmf.editorservices.DiagramSelectionChangedListener;
import org.spoofax.modelware.gmf.editorservices.SaveSynchronization;
import org.spoofax.modelware.gmf.editorservices.TextSelectionChangedListener;

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

		OperationHistoryFactory.getOperationHistory().addOperationHistoryListener(new OperationalEventsGenerator(this));

		ICommandService service = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
		service.addExecutionListener(new SaveSynchronization(this));
	}
	
	private void addSelectionChangeListeners() {
		diagramEditor.getEditorSite().getSelectionProvider().addSelectionChangedListener(GMFSelectionChangedListener = new DiagramSelectionChangedListener(this));
		textEditor.getSite().getSelectionProvider().addSelectionChangedListener(spoofaxSelectionChangedListener = new TextSelectionChangedListener(this));
	}

	public void dispose() {
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
//		if (event == BridgeEvent.PostTextLayoutChange || event == BridgeEvent.PostDiagramLayoutChange || event == BridgeEvent.PreDiagramUndo || event == BridgeEvent.PreTextUndo || event == BridgeEvent.PreTextRedo || event == BridgeEvent.PreDiagramRedo) {
			System.out.println(event.toString());
//		}
		
		for (EditorPairObserver observer : observers) {
			observer.notify(event);
		}
	}
}