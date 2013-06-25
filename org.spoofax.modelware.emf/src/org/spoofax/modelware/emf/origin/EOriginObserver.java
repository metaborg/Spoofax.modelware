package org.spoofax.modelware.emf.origin;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.impl.AdapterImpl;
import org.eclipse.emf.ecore.EStructuralFeature;

public class EOriginObserver extends AdapterImpl {

	EObjectOrigin eObjectOrigin;
	
	public EOriginObserver(EObjectOrigin eObjectOrigin) {
		this.eObjectOrigin = eObjectOrigin;
	}

	@Override
	public void notifyChanged(Notification notification) {
		System.out.println(notification.toString());
	}

	private void removeFromList(EStructuralFeature eFeature, int index) {
		// TODO
	}

	private void reorder(EStructuralFeature eFeature, int oldIndex, int newIndex) {
		// TODO
	}

	private void addToList(EStructuralFeature eFeature, int index) {
		// TODO
	}
	
	private void set(EStructuralFeature eFeature) {
		
	}
	
	private void unset(EStructuralFeature eFeature) {
		
	}
}
