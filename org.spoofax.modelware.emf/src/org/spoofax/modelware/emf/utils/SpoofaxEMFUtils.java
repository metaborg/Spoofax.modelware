package org.spoofax.modelware.emf.utils;

import java.io.File;

import org.eclipse.swt.widgets.Display;
import org.spoofax.interpreter.core.Interpreter;
import org.spoofax.interpreter.core.InterpreterErrorExit;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.core.InterpreterExit;
import org.spoofax.interpreter.core.UndefinedStrategyException;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.AbstractTermFactory;
import org.spoofax.terms.TermFactory;
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
	
	public static IStrategoTerm adjustTree2Model(FileState fileState, IStrategoTerm input) {
		return adjustmentHelper(fileState, input, SpoofaxEMFConstants.ADJUST_TREE_2_MODEL_STRATEGY);
	}
	
	public static IStrategoTerm adjustModel2Tree(FileState fileState, IStrategoTerm input) {
		return adjustmentHelper(fileState, input, SpoofaxEMFConstants.ADJUST_MODEL_2_TREE_STRATEGY);
	}
	
	private static IStrategoTerm adjustmentHelper(FileState fileState, IStrategoTerm input, String strategy) {
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
		try {
			IStrategoTerm current = itp.current();
			itp.setCurrent(input);
			itp.invoke(strategy);
			IStrategoTerm result = itp.current();
			itp.setCurrent(current);
			return result;
		}
		finally {
			observer.getLock().unlock();
		}
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
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				editor.getDocument().set(content);
			}
		});
	}
}
