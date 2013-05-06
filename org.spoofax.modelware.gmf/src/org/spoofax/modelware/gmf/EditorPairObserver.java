package org.spoofax.modelware.gmf;

/**
 * Classes can subclass this class to get notified of the various events that are generated
 * during textual and graphical editing.
 * 
 * @author oskarvanrest
 */
public interface EditorPairObserver {

	public abstract void notify(EditorPairEvent event);
	
}
