package org.spoofax.modelware.gmf;

import org.spoofax.modelware.gmf.EditorPair.BridgeEvent;

/**
 * @author Oskar van Rest
 */
public interface EditorPairObserver {

	public abstract void notify(BridgeEvent event);
	
}
