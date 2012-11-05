package org.spoofax.modelware.gmf;

public enum BridgeEvent {
	PreTextLayoutChange, 
	PostTextLayoutChange,
	PreDiagramLayoutChange,
	PostDiagramLayoutChange, 
	
	PreTerm2Model, 
	PostTerm2Model, 
	PreModel2Term, 
	PostModel2Term, 
	
	PreTextUndo,  
	PostTextUndo,
	PreDiagramUndo,
	PostDiagramUndo,
	
	PreTextRedo, 
	PostTextRedo, 
	PreDiagramRedo,
	PostDiagramRedo, 
	
	PreTextSelection,
	PostTextSelection,
	PreDiagramSelection,
	PostDiagramSelection
}
