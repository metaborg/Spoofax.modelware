package org.spoofax.modelware.emf.compare;

import org.eclipse.emf.compare.match.eobject.EditionDistance;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;

public class SpoofaxDistanceFunction extends EditionDistance {
	
	@Override
	public int distance(EObject a, EObject b) {
		int result = super.distance(a, b);
		
		if (EcoreUtil.equals(a, b)) {
			result --;
			if (EcoreUtil.getURI(a).equals(EcoreUtil.getURI(a))) {
				result --;
			}
		}

		return result;
	}
}
