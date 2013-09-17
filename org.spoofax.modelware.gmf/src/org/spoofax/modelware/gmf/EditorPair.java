package org.spoofax.modelware.gmf;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.OperationHistoryFactory;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
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
import org.spoofax.modelware.emf.Language;
import org.spoofax.modelware.emf.compare.CompareUtil;
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
import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;
import org.strategoxt.imp.runtime.stratego.SourceAttachment;

/**
 * An {@link EditorPair} holds a textual and a graphical editor and takes care of the synchronization between them. It also registers a set of
 * listeners for the purpose of integrating editor services such as selection sharing and undo-redo functionality.
 * 
 * @author Oskar van Rest
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
	public IStrategoTerm ASTgraph;

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
		IStrategoTerm newASTgraph = new Model2Term(SpoofaxEMFUtils.termFactory).convert(model);

		final IResource resource = SourceAttachment.getResource(this.ASTgraph);
		final IParseController controller = SourceAttachment.getParseController(this.ASTgraph);

		new TermVisitor() {
			@Override
			public void preVisit(IStrategoTerm term) {
				SourceAttachment.putSource(term, resource, controller);
			}
		}.visit(newASTgraph);

		IStrategoTerm newASTtext = SpoofaxEMFUtils.getASTtext(newASTgraph, editorState);
		notifyObservers(EditorPairEvent.PostModel2Term);

		doReplaceText(newASTtext);
	}
	
	public void doReplaceText(IStrategoTerm newASTtext) {
		while(updating) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		EditorState editorState = EditorState.getEditorFor(textEditor);	
		
		timeOfLastModelChange = System.currentTimeMillis();
		if (thread == null || !thread.isAlive()) {
			text = editorState.getDocument().get();			
			thread = new Thread(new Timer());
			thread.start();
		}
		
		IStrategoTerm oldAST = editorState.getParseController().parse(text, new NullProgressMonitor());
		IStrategoTerm oldASTtext = oldAST; // oldASTtext should actually be the analyzed version of oldAST. However, analyzes will result in a term without parent attachments. The layout preservation algorithm needs parent attachments.
		text = SpoofaxEMFUtils.calculateTextReplacement(oldASTtext, newASTtext, editorState);
	}

	private String text;
	private long timeOfLastModelChange;
	private Thread thread;
	private static final long TIMEOUT = 50;
	private boolean updating;
	
	private class Timer implements Runnable {
		public void run() {
			try {
				long different = -1;
				while (different < TIMEOUT) {
					different = System.currentTimeMillis() - timeOfLastModelChange;
					Thread.sleep(Math.max(0, TIMEOUT - different));
				}
				
				final EditorState editorState = EditorState.getEditorFor(textEditor);
				updating = true;
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						try {
							IStrategoTerm ASTtext = editorState.getCurrentAnalyzedAst();
							editorState.getDocument().set(text);
							while (editorState.getCurrentAnalyzedAst() == ASTtext) {
								Thread.sleep(10);
							}
						} catch (BadDescriptorException e) {
							e.printStackTrace();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						updating = false;
					}
				});
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void doTerm2Model() {
		notifyObservers(EditorPairEvent.PreTerm2Model);
		EObject left = new Term2Model(EPackageRegistryImpl.INSTANCE.getEPackage(getLanguage().getNsURI())).convert(ASTgraph);
		notifyObservers(EditorPairEvent.PostTerm2Model);

		EObject right = EditorPairUtil.getSemanticModel(diagramEditor);

		if (right == null)
			return;

		notifyObservers(EditorPairEvent.PreCompare);
		Comparison comparison = CompareUtil.compare(left, right);
		notifyObservers(EditorPairEvent.PostCompare);

		notifyObservers(EditorPairEvent.PreMerge);
		CompareUtil.merge(comparison, right);
		notifyObservers(EditorPairEvent.PostMerge);

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