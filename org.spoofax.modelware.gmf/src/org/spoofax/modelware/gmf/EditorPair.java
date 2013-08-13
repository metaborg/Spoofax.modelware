package org.spoofax.modelware.gmf;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.OperationHistoryFactory;
import org.eclipse.core.resources.IResource;
import org.eclipse.emf.compare.Comparison;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.impl.EPackageRegistryImpl;
import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramEditor;
import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.swt.widgets.Display;
import org.eclipse.text.undo.DocumentUndoManagerRegistry;
import org.eclipse.text.undo.IDocumentUndoManager;
import org.eclipse.ui.IEditorPart;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.modelware.emf.compare.CompareUtil;
import org.spoofax.modelware.emf.origin.model.EObjectOrigin;
import org.spoofax.modelware.emf.tree2model.Model2Term;
import org.spoofax.modelware.emf.tree2model.Term2Model;
import org.spoofax.modelware.emf.utils.SpoofaxEMFUtils;
import org.spoofax.modelware.gmf.editorservices.DiagramSelectionChangedListener;
import org.spoofax.modelware.gmf.editorservices.TextSelectionChangedListener;
import org.spoofax.modelware.gmf.editorservices.UndoRedo;
import org.spoofax.modelware.gmf.editorservices.UndoRedoEventGenerator;
import org.spoofax.modelware.gmf.resource.SpoofaxGMFResource;
import org.spoofax.terms.TermVisitor;
import org.strategoxt.imp.runtime.EditorState;
import org.strategoxt.imp.runtime.stratego.SourceAttachment;

/**
 * An {@link EditorPair} holds a textual and a graphical editor and takes care of the synchronization between them. It also registers a set of
 * listeners for the purpose of integrating editor services such as selection sharing and undo-redo functionality.
 * 
 * @author oskarvanrest
 */
public class EditorPair {

	Collection<EditorPairObserver> observers;

	private UniversalEditor textEditor;
	private DiagramEditor diagramEditor;
	private Language language;

	public EObject semanticModel;
	private ModelChangeListener semanticModelContentAdapter;
	private TextChangeListener textChangeListener;
	private DiagramSelectionChangedListener GMFSelectionChangedListener;
	private TextSelectionChangedListener spoofaxSelectionChangedListener;
	public IStrategoTerm adjustedAST;
	public EObjectOrigin modelOrigin;

	public boolean debounce;

	public EditorPair(UniversalEditor textEditor, DiagramEditor diagramEditor, Language language) {
		this.observers = new ArrayList<EditorPairObserver>();

		this.textEditor = textEditor;
		this.diagramEditor = diagramEditor;
		this.language = language;

		loadSemanticModel();
		addSelectionChangeListeners();
		textChangeListener = new TextChangeListener(this);

		OperationHistoryFactory.getOperationHistory().addOperationHistoryListener(new UndoRedoEventGenerator(this));
		// note: order of execution of the statements above and the one below matters
		OperationHistoryFactory.getOperationHistory().addOperationHistoryListener(new UndoRedo(this));

		SpoofaxGMFResource resource = (SpoofaxGMFResource) diagramEditor.getEditingDomain().getResourceSet().getResources().get(1);
		textEditor.addOnSaveListener(resource.new SaveSynchronization(this));
	}

	private void addSelectionChangeListeners() {
		diagramEditor.getEditorSite().getSelectionProvider().addSelectionChangedListener(GMFSelectionChangedListener = new DiagramSelectionChangedListener(this));
		textEditor.getSite().getSelectionProvider().addSelectionChangedListener(spoofaxSelectionChangedListener = new TextSelectionChangedListener(this));
	}

	public void dispose() {
		// TODO TextToModelOnSave
		textChangeListener.dispose();
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
		EditorState editorState = EditorState.getEditorFor(textEditor);

		notifyObservers(EditorPairEvent.PreModel2Term);
		IStrategoTerm adjustedAST = new Model2Term(SpoofaxEMFUtils.termFactory).convert(model);

		final IResource resource = SourceAttachment.getResource(this.adjustedAST);
		final IParseController controller = SourceAttachment.getParseController(this.adjustedAST);

		new TermVisitor() {
			@Override
			public void preVisit(IStrategoTerm term) {
				SourceAttachment.putSource(term, resource, controller);
				// TODO put origin
				// TODO put desugared origin
			}
		}.visit(adjustedAST);

		IStrategoTerm AST = SpoofaxEMFUtils.getASTtext(adjustedAST, editorState);
		notifyObservers(EditorPairEvent.PostModel2Term);

		notifyObservers(EditorPairEvent.PreLayoutPreservation);
		String replacement = SpoofaxEMFUtils.calculateTextReplacement(AST, editorState);
		SpoofaxEMFUtils.setEditorContent(editorState, replacement);
	}

	public void doTerm2Model() {
		notifyObservers(EditorPairEvent.PreTerm2Model);
		EObject left = new Term2Model(EPackageRegistryImpl.INSTANCE.getEPackage(getLanguage().getPackageName())).convert(adjustedAST);
		notifyObservers(EditorPairEvent.PostTerm2Model);

		EObject right = EditorPairUtil.getSemanticModel(diagramEditor);

		if (debounce) {
			debounce = false;
			return;
		}

		if (right == null)
			return;

		notifyObservers(EditorPairEvent.PreCompare);
		Comparison comparison = CompareUtil.compare(left, right);
		notifyObservers(EditorPairEvent.PostCompare);

		notifyObservers(EditorPairEvent.PreMerge);
		CompareUtil.merge(comparison, right);
		notifyObservers(EditorPairEvent.PostMerge);

		// modelOrigin = EOrigin.constructEOrigin(right, adjustedAST);

		// final TreeIterator<EObject> it = right.eAllContents();
		//
		// new TermVisitor() {
		//
		// @Override
		// public void preVisit(IStrategoTerm term) {
		// if (term.getTermType() == IStrategoTerm.APPL) {
		// System.out.println(term);
		// System.out.println(it.next().getClass().toString());
		// }
		// }
		// }.visit(adjustedAST);

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