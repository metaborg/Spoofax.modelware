package org.spoofax.modelware.gmf;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramEditor;
import org.eclipse.ui.IEditorPart;
import org.spoofax.modelware.gmf.editorservices.selectionsharing.DiagramSelectionChangedListener;
import org.spoofax.modelware.gmf.editorservices.selectionsharing.TextSelectionChangedListener;
import org.strategoxt.lang.Context;

public class EditorPair {

	private IEditorPart textEditor;
	private DiagramEditor diagramEditor;
	private Context context;
	private EPackage ePackage;
	private Debouncer debouncer;
	
	private EObject semanticModel;
	
	private SemanticModelContentAdapter semanticModelContentAdapter;
	private DiagramSelectionChangedListener GMFSelectionChangedListener;
	private TextSelectionChangedListener spoofaxSelectionChangedListener;

	public EditorPair(IEditorPart textEditor, DiagramEditor diagramEditor, Context context, EPackage ePackage) {
		this.textEditor = textEditor;
		this.diagramEditor = diagramEditor;
		this.context = context;
		this.ePackage = ePackage;
		this.debouncer = new Debouncer();

		loadSemanticModel();
		diagramEditor.getEditorSite().getSelectionProvider().addSelectionChangedListener(GMFSelectionChangedListener = new DiagramSelectionChangedListener(this));
		textEditor.getSite().getSelectionProvider().addSelectionChangedListener(spoofaxSelectionChangedListener = new TextSelectionChangedListener(this));
	}
	
	public void dispose() {
		GMFBridgeUtil.getSemanticModel(diagramEditor).eAdapters().remove(semanticModelContentAdapter);
		diagramEditor.getEditorSite().getSelectionProvider().removeSelectionChangedListener(GMFSelectionChangedListener);
		textEditor.getSite().getSelectionProvider().removeSelectionChangedListener(spoofaxSelectionChangedListener);
	}
	
	public void loadSemanticModel() {
		if (semanticModel != null && semanticModel.eAdapters().contains(semanticModelContentAdapter))
			semanticModel.eAdapters().remove(semanticModelContentAdapter);
			
		semanticModel = GMFBridgeUtil.getSemanticModel(diagramEditor);
		if (semanticModel != null)
			semanticModel.eAdapters().add(semanticModelContentAdapter = new SemanticModelContentAdapter(this));
	}

	public IEditorPart getTextEditor() {
		return textEditor;
	}

	public DiagramEditor getDiagramEditor() {
		return diagramEditor;
	}

	public Context getContext() {
		return context;
	}

	public EPackage getEPackage() {
		return ePackage;
	}

	public Debouncer getDebouncer() {
		return debouncer;
	}
}