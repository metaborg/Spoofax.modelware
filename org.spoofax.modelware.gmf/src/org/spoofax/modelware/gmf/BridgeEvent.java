package org.spoofax.modelware.gmf;

public enum BridgeEvent {
	PostTextLayoutChange,
	PostDiagramLayoutChange, 
	
	PreTerm2Model, 
	PostTerm2Model, 
	PreModel2Term, 
	PostModel2Term, 
	
	PreTextUndo,  
	PreDiagramUndo,
	PreTextRedo, 
	PreDiagramRedo, 
	
	PreTextSelection,
	PostTextSelection,
	PreDiagramSelection,
	PostDiagramSelection
}
