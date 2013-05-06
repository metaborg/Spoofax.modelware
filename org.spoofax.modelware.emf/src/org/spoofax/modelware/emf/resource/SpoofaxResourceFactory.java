package org.spoofax.modelware.emf.resource;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceFactoryImpl;

/**
 * A factory implementation for {@link SpoofaxResource}.
 * 
 * @author oskarvanrest
 *
 */
public class SpoofaxResourceFactory extends ResourceFactoryImpl {
	
	public SpoofaxResourceFactory() {
		super();
	}

	public Resource createResource(URI uri) {
		return new SpoofaxResource(uri);
	}
}