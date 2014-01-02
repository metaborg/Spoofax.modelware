package org.spoofax.modelware.primitives;

import org.spoofax.interpreter.library.AbstractStrategoOperatorRegistry;

public class ModelwareLibrary extends AbstractStrategoOperatorRegistry {

	public static final String REGISTRY_NAME = "MODELWARE";
	
	public ModelwareLibrary() {
		add(modelware_api_example_primitive_0_0.instance);
	}
	
	@Override
	public String getOperatorRegistryName() {
		return REGISTRY_NAME;
	}
}
