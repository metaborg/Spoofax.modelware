package org.spoofax.modelware.gmf;

public enum BridgeEvent {
	PostTextLayoutChange,
	PostDiagramLayoutChange, 
	
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
	
	PreTextUndo,  
	PreDiagramUndo,
	PreTextRedo, 
	PreDiagramRedo, 
	
	PreTextSelection,
	PostTextSelection,
	PreDiagramSelection,
	PostDiagramSelection
}
