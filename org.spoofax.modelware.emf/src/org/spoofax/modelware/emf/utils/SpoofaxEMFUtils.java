package org.spoofax.modelware.emf.utils;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.spoofax.interpreter.core.Interpreter;
import org.spoofax.interpreter.core.InterpreterErrorExit;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.core.InterpreterExit;
import org.spoofax.interpreter.core.UndefinedStrategyException;
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
	
	//TODO: instantiate a new interpreter for each language rather than reusing the existing one
	//LanguageRegistry should be split up in SpoofaxEMFLanguageRegistry and SoofaxGMFLanguageRegistry
	public static IStrategoTerm invokeStrategy(StrategoObserver observer, IStrategoTerm input, String strategy) throws InterpreterErrorExit, InterpreterExit, UndefinedStrategyException, InterpreterException {
		observer.getLock().lock();
		Interpreter itp = observer.getRuntime();
		IStrategoTerm result = null;
		try {
			IStrategoTerm current = itp.current();
			itp.setCurrent(input);
			itp.invoke(strategy);
			result = itp.current();
			itp.setCurrent(current);
		}
		finally {
			observer.getLock().unlock();
		}
		
		// make sure that origin information is propagated
		if (OriginAttachment.getOrigin(input) != null) {
			ImploderOriginTermFactory factory = new ImploderOriginTermFactory(SpoofaxEMFUtils.termFactory);
			factory.makeLink(result, input);
		}

		return result;
	}
	
	// TODO: use StrategoTextChangeCalculator instead
	public static String calculateTextReplacement(IStrategoTerm newTree, FileState fileState){
		SGLRParseController controller = fileState.getParseController();
		Descriptor descriptor = fileState.getDescriptor();
		File file = SourceAttachment.getFile(controller.getCurrentAst());
		
		try {
			StrategoObserver observer = descriptor.createService(StrategoObserver.class, controller);
			IStrategoTerm textreplace = construct_textual_change_4_0.instance.invoke(
					observer.getRuntime().getCompiledContext(), 
					termFactory.makeTuple(fileState.getCurrentAst(), newTree), 
					createStrategy(RefactoringFactory.getPPStrategy(descriptor), file, observer),
					createStrategy(RefactoringFactory.getParenthesizeStrategy(descriptor), file, observer),
					createStrategy(RefactoringFactory.getOverrideReconstructionStrategy(descriptor), file, observer),
					createStrategy(RefactoringFactory.getResugarStrategy(descriptor), file, observer)
				);
			return ((IStrategoString) textreplace.getSubterm(2)).stringValue();
		} catch (BadDescriptorException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	private static Strategy createStrategy(final String sname, final File file, final StrategoObserver observer) {
		return new Strategy() {
			@Override
			public IStrategoTerm invoke(Context context, IStrategoTerm current) {
				if (sname!=null)
					return observer.invokeSilent(sname, current, file);
				return null;
			}
		};
	}
	
	public static void setEditorContent(final EditorState editor, final String content) {
		System.out.println(editor.getDocument().get());
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				editor.getDocument().set(content);
				System.out.println(editor.getDocument().get());
			}
		});
		System.out.println(editor.getDocument().get());
	}
}
