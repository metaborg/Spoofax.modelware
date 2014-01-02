package org.spoofax.modelware.primitives;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;

public class modelware_api_example_primitive_0_0 extends AbstractPrimitive {

	public static modelware_api_example_primitive_0_0 instance = new modelware_api_example_primitive_0_0();
	
	public modelware_api_example_primitive_0_0() {
		super("modelware_api_example_primitive", 0, 0);
	}

	@Override
	public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars) throws InterpreterException {
		return true;
	}

}
