package org.spoofax.modelware.gmf.resource;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceFactoryImpl;

/**
 * A factory for {@link SpoofaxGMFResource}.
 * 
 * @author oskarvanrest
 */
public class SpoofaxGMFResourceFactory extends ResourceFactoryImpl {
	
	public SpoofaxGMFResourceFactory() {
		super();
	}

	public Resource createResource(URI uri) {
		return new SpoofaxGMFResource(uri);
	}
}