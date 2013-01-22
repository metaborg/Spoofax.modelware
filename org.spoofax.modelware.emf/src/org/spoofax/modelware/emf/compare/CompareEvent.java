package org.spoofax.modelware.emf.compare;

public enum CompareEvent {
	PreCompare,
	PostCompare,
	PreMerge,
	PostMerge,
	PostMerge2 // TODO: hack/workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=390788
}
