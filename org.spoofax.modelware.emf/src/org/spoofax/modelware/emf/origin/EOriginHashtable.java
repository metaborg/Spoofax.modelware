package org.spoofax.modelware.emf.origin;

import java.util.Hashtable;

import org.eclipse.emf.ecore.EObject;
import org.spoofax.modelware.emf.origin.model.EObjectOrigin;

public class EOriginHashtable extends Hashtable<EObject, EObjectOrigin> {

	public static EOriginHashtable INSTANCE = new EOriginHashtable();
	
}
