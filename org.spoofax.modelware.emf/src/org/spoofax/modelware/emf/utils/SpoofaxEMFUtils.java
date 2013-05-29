package org.spoofax.modelware.emf.utils;

import org.spoofax.interpreter.core.Interpreter;
import org.spoofax.interpreter.core.InterpreterErrorExit;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.core.InterpreterExit;
import org.spoofax.interpreter.core.UndefinedStrategyException;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.services.StrategoObserver;

public class SpoofaxEMFUtils {

	public static IStrategoTerm adjustTree2Model(StrategoObserver observer, IStrategoTerm input) {
		return adjustmentHelper(observer, input, SpoofaxEMFConstants.ADJUST_TREE_2_MODEL_STRATEGY);
	}
	
	public static IStrategoTerm adjustModel2Tree(StrategoObserver observer, IStrategoTerm input) {
		return adjustmentHelper(observer, input, SpoofaxEMFConstants.ADJUST_MODEL_2_TREE_STRATEGY);
	}
	
	private static IStrategoTerm adjustmentHelper(StrategoObserver observer, IStrategoTerm input, String strategy) {
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
		observer.getLock().lock();
		Interpreter itp = observer.getRuntime();
		try {
			itp.setCurrent(input);
			itp.invoke(strategy);
			return itp.current();
		}
		finally {
			observer.getLock().unlock();
		}
	}
}
