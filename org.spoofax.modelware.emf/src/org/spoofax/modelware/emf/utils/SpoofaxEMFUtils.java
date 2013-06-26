package org.spoofax.modelware.emf.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.emf.common.util.EMap;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.spoofax.interpreter.core.InterpreterErrorExit;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.core.InterpreterExit;
import org.spoofax.interpreter.core.UndefinedStrategyException;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.imploder.ImploderOriginTermFactory;
import org.spoofax.terms.AbstractTermFactory;
import org.spoofax.terms.TermFactory;
import org.spoofax.terms.attachments.OriginAttachment;
import org.strategoxt.imp.generator.construct_textual_change_4_0;
import org.strategoxt.imp.runtime.EditorState;
import org.strategoxt.imp.runtime.FileState;
import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;
import org.strategoxt.imp.runtime.dynamicloading.Descriptor;
import org.strategoxt.imp.runtime.dynamicloading.RefactoringFactory;
import org.strategoxt.imp.runtime.parser.SGLRParseController;
import org.strategoxt.imp.runtime.services.StrategoObserver;
import org.strategoxt.imp.runtime.stratego.SourceAttachment;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class SpoofaxEMFUtils {

	public static AbstractTermFactory termFactory = new TermFactory();

	public static UniversalEditor findSpoofaxEditor(IPath path) {
		return (UniversalEditor) findEditor(path, SpoofaxEMFConstants.IMP_EDITOR_ID);
	}

	public static IEditorPart findEditor(IPath path, String editorID) {
		IFile file = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(path);

		if (file == null)
			return null;

		IEditorInput editorInput = new FileEditorInput(file);

		Collection<IWorkbenchPage> pages = getAllWorkbenchPages();
		Iterator<IWorkbenchPage> it = pages.iterator();

		while (it.hasNext()) {
			IWorkbenchPage page = it.next();
			IEditorReference[] editors = page.findEditors(editorInput, null, IWorkbenchPage.MATCH_INPUT);
			for (int i = 0; i < editors.length; i++) {
				if (editors[i].getId().equals(editorID)) {
					return editors[i].getEditor(false);
				}
			}
		}

		return null;
	}

	public static Collection<IWorkbenchPage> getAllWorkbenchPages() {
		Collection<IWorkbenchPage> result = new LinkedList<IWorkbenchPage>();
		for (IWorkbenchWindow window : PlatformUI.getWorkbench().getWorkbenchWindows()) {
			result.addAll(Arrays.asList(window.getPages()));
		}
		return result;
	}

	public static FileState getEditorOrFileState(IPath path) {
		try {
			IEditorPart part = findSpoofaxEditor(path);
			if (part != null) {
				return EditorState.getEditorFor(part);
			}
			else {
				return FileState.getFile(path, null);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	// hold map (fileState -> analyzedAST+adjustedAST)
	// if analyzedAST is changed since last call, update analyzedAST+adjustedAST

	public static Hashtable<FileState, AnalyzedAdjustedPair> analyzedAdjustedPairs = new Hashtable<FileState, AnalyzedAdjustedPair>();

	public static IStrategoTerm getAdjustedAST(FileState fileState) {
		if (fileState.getCurrentAst() == null) { // empty document
			return null;
		}

		IStrategoTerm result = null;
		try {
			IStrategoTerm analyzedAST = fileState.getCurrentAnalyzedAst();

			// hack to avoid race condition on start-up: wait till file is analyzed
			while (analyzedAST == null) {
				Thread.sleep(25);
				analyzedAST = fileState.getCurrentAnalyzedAst();
			}

			AnalyzedAdjustedPair analyzedAdjustedPair = analyzedAdjustedPairs.get(fileState);
			if (analyzedAdjustedPair != null && analyzedAdjustedPair.getAnalyzedAST() == analyzedAST) {
				result = analyzedAdjustedPair.getAdjustedAST();
			}
			else {
				result = adjustTree2Model(analyzedAST, fileState);

				// hack to avoid race condition on start-up: wait till adjust-tree-to-model strategy has been loaded
				while (result == null) {
					Thread.sleep(25);
					result = getAdjustedAST(fileState);
				}

				analyzedAdjustedPairs.put(fileState, new AnalyzedAdjustedPair(analyzedAST, result));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	public static IStrategoTerm adjustTree2Model(IStrategoTerm input, FileState fileState) {
		return adjustmentHelper(input, fileState, SpoofaxEMFConstants.ADJUST_TREE_2_MODEL_STRATEGY);
	}

	public static IStrategoTerm adjustModel2Tree(IStrategoTerm input, FileState fileState) {
		return adjustmentHelper(input, fileState, SpoofaxEMFConstants.ADJUST_MODEL_2_TREE_STRATEGY);
	}

	private static IStrategoTerm adjustmentHelper(IStrategoTerm input, FileState fileState, String strategy) {
		StrategoObserver observer = null;
		try {
			observer = fileState.getDescriptor().createService(StrategoObserver.class, fileState.getParseController());
		}
		catch (BadDescriptorException e) {
			e.printStackTrace();
		}

		try {
			return invokeStrategy(observer, input, strategy);
		}
		catch (UndefinedStrategyException e) {
			// continue without adjustment
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return input;
	}

	public static IStrategoTerm invokeStrategy(StrategoObserver observer, IStrategoTerm input, String strategy) throws InterpreterErrorExit, InterpreterExit, UndefinedStrategyException, InterpreterException {
		IStrategoTerm result = null;
		
		observer.getLock().lock();
		try {
			result = observer.invokeSilent(strategy, input);
		} finally {
			observer.getLock().unlock();
		}
		
		// make sure that origin information is propagated
		if (OriginAttachment.getOrigin(input) != null) {
			ImploderOriginTermFactory factory = new ImploderOriginTermFactory(termFactory);
			factory.makeLink(result, input);
		}

		return result;
	}

	// TODO: use StrategoTextChangeCalculator instead
	public static String calculateTextReplacement(IStrategoTerm newTree, FileState fileState) {
		SGLRParseController controller = fileState.getParseController();
		Descriptor descriptor = fileState.getDescriptor();
		File file = SourceAttachment.getFile(controller.getCurrentAst());

		try {
			StrategoObserver observer = descriptor.createService(StrategoObserver.class, controller);
			IStrategoTerm textreplace = construct_textual_change_4_0.instance.invoke(observer.getRuntime().getCompiledContext(), termFactory.makeTuple(fileState.getCurrentAst(), newTree), createStrategy(RefactoringFactory.getPPStrategy(descriptor), file, observer), createStrategy(RefactoringFactory.getParenthesizeStrategy(descriptor), file, observer), createStrategy(RefactoringFactory.getOverrideReconstructionStrategy(descriptor), file, observer),
					createStrategy(RefactoringFactory.getResugarStrategy(descriptor), file, observer));
			return ((IStrategoString) textreplace.getSubterm(2)).stringValue();
		}
		catch (BadDescriptorException e) {
			e.printStackTrace();
		}

		return null;
	}

	private static Strategy createStrategy(final String sname, final File file, final StrategoObserver observer) {
		return new Strategy() {
			@Override
			public IStrategoTerm invoke(Context context, IStrategoTerm current) {
				if (sname != null)
					return observer.invokeSilent(sname, current, file);
				return null;
			}
		};
	}

	public static void setEditorContent(final EditorState editor, final String content) {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				editor.getDocument().set(content);
			}
		});
	}

	// not used at the moment
	public static List<Integer> term2path(IStrategoTerm term, IStrategoTerm root) {
		return term2pathHelper(term, root, new ArrayList<Integer>());
	}

	// not used at the moment
	private static List<Integer> term2pathHelper(IStrategoTerm term, IStrategoTerm root, List<Integer> parentPath) {
		if (term == root) {
			return parentPath;
		}
		else {
			IStrategoTerm[] subterms = root.getAllSubterms();
			for (int i = 0; i < subterms.length; i++) {
				List<Integer> subtermParentPath = new ArrayList<Integer>();
				subtermParentPath.addAll(parentPath);
				subtermParentPath.add(i);

				List<Integer> subtermResult = term2pathHelper(term, subterms[i], subtermParentPath);
				if (subtermResult != null) {
					return subtermResult;
				}
			}
		}

		return null;
	}

	public static boolean isSome(IStrategoTerm term) {
		return term.getTermType() == IStrategoAppl.APPL && ((IStrategoAppl) term).getConstructor().equals("Some");
	}

	public static boolean isNone(IStrategoTerm term) {
		return term.getTermType() == IStrategoAppl.APPL && ((IStrategoAppl) term).getConstructor().equals("Some");
	}

	public static EStructuralFeature index2feature(EClass eClass, int index) {
		EMap<String, String> index2name = eClass.getEAnnotation(SpoofaxEMFConstants.SPOOFAX_TERM2FEATURE_ANNO).getDetails();
		return eClass.getEStructuralFeature(index2name.get(Integer.toString(index)));
	}
	
	public static int feature2index(EClass eClass, EStructuralFeature eFeature) {
		EMap<String, String> index2name = eClass.getEAnnotation(SpoofaxEMFConstants.SPOOFAX_TERM2FEATURE_ANNO).getDetails();
		return Integer.parseInt(getKeyByValue(index2name, eFeature.getName()));
	}
	
	private static <T, E> T getKeyByValue(EMap<T, E> map, E value) {
		for (Entry<T, E> entry : map.entrySet()) {
			if (value.equals(entry.getValue())) {
				return entry.getKey();
			}
		}
		return null;
	}
}

class AnalyzedAdjustedPair {

	IStrategoTerm analyzedAST;
	IStrategoTerm adjustedAST;

	public AnalyzedAdjustedPair(IStrategoTerm analyzedAST, IStrategoTerm adjustedAST) {
		this.analyzedAST = analyzedAST;
		this.adjustedAST = adjustedAST;
	}

	public IStrategoTerm getAnalyzedAST() {
		return analyzedAST;
	}

	public void setAnalyzedAST(IStrategoTerm analyzedAST) {
		this.analyzedAST = analyzedAST;
	}

	public IStrategoTerm getAdjustedAST() {
		return adjustedAST;
	}

	public void setAdjustedAST(IStrategoTerm adjustedAST) {
		this.adjustedAST = adjustedAST;
	}
}
