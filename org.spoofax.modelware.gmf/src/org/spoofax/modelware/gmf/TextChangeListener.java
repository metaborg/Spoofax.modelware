package org.spoofax.modelware.gmf;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.compare.Comparison;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.impl.EPackageRegistryImpl;
import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramEditor;
import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.imp.parser.IModelListener;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ide.ResourceUtil;
import org.spoofax.interpreter.core.Interpreter;
import org.spoofax.interpreter.core.InterpreterErrorExit;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.core.InterpreterExit;
import org.spoofax.interpreter.core.UndefinedStrategyException;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.IStrategoTuple;
import org.spoofax.modelware.emf.Term2Model;
import org.spoofax.modelware.emf.compare.CompareUtil;
import org.strategoxt.imp.runtime.EditorState;
import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;
import org.strategoxt.imp.runtime.dynamicloading.Descriptor;
import org.strategoxt.imp.runtime.parser.SGLRParseController;
import org.strategoxt.imp.runtime.services.StrategoObserver;

/**
 * Listens for text changes and performs a text-to-model transformation if parsing of the text 
 * results in a changed AST.
 * 
 * @author oskarvanrest
 */
public class TextChangeListener implements IModelListener {

	private EditorPair editorPair;
	private IStrategoTerm lastAST;
	private long timeOfLastChange;
	private Thread thread;
	private static final long keyStrokeTimeout = 700;
	private boolean debouncer;

	public TextChangeListener(EditorPair editorPair) {
		this.editorPair = editorPair;
		editorPair.registerObserver(new Debouncer());
	}

	@Override
	public AnalysisRequired getAnalysisRequired() {
		return AnalysisRequired.NONE;
	}

	@Override
	public void update(IParseController controller, IProgressMonitor monitor) {	
		if (debouncer) {
			debouncer = false;
			setLastAST();
			return;
		}
		
		timeOfLastChange = System.currentTimeMillis();
		if (thread == null || !thread.isAlive()) {
			thread = new Thread(new Timer());
			thread.start();
		}
	}
	
	private void setLastAST() {
		UniversalEditor textEditor = editorPair.getTextEditor();
		EditorState activeEditor = EditorState.getEditorFor(textEditor);
		lastAST = activeEditor.getCurrentAst();
	}

	private void executeTerm2Model() {
		UniversalEditor textEditor = editorPair.getTextEditor();
		EditorState activeEditor = EditorState.getEditorFor(textEditor);
		IStrategoTerm AST = activeEditor.getCurrentAst();

		if (lastAST != null && lastAST.equals(AST)) {
			return;
		} else {
			setLastAST();
		}

		IResource resource = ResourceUtil.getResource(textEditor.getEditorInput());
		Descriptor descriptor = activeEditor.getDescriptor();
		SGLRParseController parseController = activeEditor.getParseController();
		StrategoObserver observer = null;

		try {
			observer = descriptor.createService(StrategoObserver.class, null);
		} catch (BadDescriptorException e) {
			e.printStackTrace();
		}

		observer.getLock().lock();
		try {
			editorPair.notifyObservers(EditorPairEvent.PreParse);
			observer.update(parseController, new NullProgressMonitor());
		} finally {
			observer.getLock().unlock();
			editorPair.notifyObservers(EditorPairEvent.PostParse);
		}
		
		IStrategoTerm analysedAST = observer.getResultingAst(resource);
		if (analysedAST instanceof IStrategoTuple && analysedAST.getSubtermCount()>0 && analysedAST.getSubterm(0) instanceof IStrategoAppl) {
			analysedAST = analysedAST.getSubterm(0); 
		}
		if (analysedAST instanceof IStrategoAppl) {
			doTerm2Model(analysedAST);
		}
	}
	
	public void doTerm2Model(IStrategoTerm tree) {
		
		EditorState editorState = EditorState.getEditorFor(editorPair.getTextEditor());

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
		
		final DiagramEditor diagramEditor = editorPair.getDiagramEditor();
		
		editorPair.notifyObservers(EditorPairEvent.PreTerm2Model);
		EObject left = new Term2Model(EPackageRegistryImpl.INSTANCE.getEPackage(editorPair.getLanguage().getPackageName())).convert(tree);
		editorPair.notifyObservers(EditorPairEvent.PostTerm2Model);
		
		EObject right = EditorPairUtil.getSemanticModel(diagramEditor);
		
		if (right == null)
			return;

		editorPair.notifyObservers(EditorPairEvent.PreCompare);
		Comparison comparison = CompareUtil.compare(left, right);
		editorPair.notifyObservers(EditorPairEvent.PostCompare);
		
		editorPair.notifyObservers(EditorPairEvent.PreMerge);
		CompareUtil.merge(comparison, right);

		editorPair.notifyObservers(EditorPairEvent.PreRender);
		// Workaround for http://www.eclipse.org/forums/index.php/m/885469/#msg_885469
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				diagramEditor.getDiagramEditPart().addNotify();
				editorPair.notifyObservers(EditorPairEvent.PostRender);
			}
		});
	}

	private class Timer implements Runnable {
		public void run() {
			try {
				long different = -1;
				while (different < keyStrokeTimeout) {
					different = System.currentTimeMillis() - timeOfLastChange;
					Thread.sleep(Math.max(0, keyStrokeTimeout - different));
				}
				executeTerm2Model();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private class Debouncer implements EditorPairObserver {

		@Override
		public void notify(EditorPairEvent event) {			
			if (event == EditorPairEvent.PreLayoutPreservation) {
				debouncer = true;
			}
			
			else if (event == EditorPairEvent.PreUndo) {
				debouncer = true;				
			}
			else if (event == EditorPairEvent.PreRedo) {
				debouncer = true;				
			}
		}
	}
}
