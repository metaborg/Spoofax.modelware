package org.spoofax.modelware.emf.compare;

import java.util.List;

import org.eclipse.emf.compare.FactoryException;
import org.eclipse.emf.compare.match.engine.GenericMatchEngine;
import org.eclipse.emf.ecore.EObject;

/**
 * @author Oskar van Rest
 */
public class CommonRootMatchEngine extends GenericMatchEngine {
	
	private final EObject rootA;
	private final EObject rootB;
	
	public CommonRootMatchEngine(EObject rootA, EObject rootB) {
		this.rootA = rootA;
		this.rootB = rootB;
	}
	
	/**
	 * @override
	 */
	protected boolean isSimilar(EObject a, EObject b) throws FactoryException {
		if (a == rootA && b == rootB) {
			return true;
		} else {
			return super.isSimilar(a, b);
		}
	}

	/**
	 * @override
	 */
	protected EObject findMostSimilar(EObject obj, List<EObject> list) throws FactoryException {
		return super.findMostSimilar(obj, list);
	}
}