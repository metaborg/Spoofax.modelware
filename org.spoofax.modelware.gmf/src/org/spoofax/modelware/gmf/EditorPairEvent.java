package org.spoofax.modelware.gmf;

/**
 * Events that are generated during textual and graphical editing. One can listen to these events
 * by subclassing {@link EditorPairObserver}.
 * 
 * @author oskarvanrest
 */
public enum EditorPairEvent {
	
	PreTerm2Model, 
	PostTerm2Model, 
	PreModel2Term, 
	PostModel2Term, 
	
	PreParse,
	PostParse,
	PreCompare,
	PostCompare,
	PreMerge,
	PostMerge,
	PreRender,
	PostRender,
	PreLayoutPreservation,
	PostLayoutPreservation,
	
	PreUndo,
	PostUndo,
	PreRedo,
	PostRedo,
	
	PreText2DiagramSelection,
//	PostText2DiagramSelection, // can't determine this one because the diagram selection service is asynchronous
	PreDiagram2TextSelection,
	PostDiagram2TextSelection

	
}
