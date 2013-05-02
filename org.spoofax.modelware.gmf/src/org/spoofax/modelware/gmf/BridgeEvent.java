package org.spoofax.modelware.gmf;

public enum BridgeEvent {
//	PostTextLayoutChange,
//	PostDiagramLayoutChange, 
	
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
	
	PreTextSelection,
//	PostTextSelection, // can't determine this one because the diagram selection service is asynchronous
	PreDiagramSelection,
	PostDiagramSelection

	
}
