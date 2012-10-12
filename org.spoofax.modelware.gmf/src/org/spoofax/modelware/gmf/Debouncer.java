package org.spoofax.modelware.gmf;

public class Debouncer {

	private final int debounceConstant = 500;
	private long lastText2model;
	private boolean text2model;
	private boolean textSelection;
	private boolean diagramSelection;
	
	public Debouncer() {
		text2model = true;
		textSelection = true;
		diagramSelection = true;
	}
	
	public synchronized boolean text2modelAllowed() {
		if (text2model)
			lastText2model = System.currentTimeMillis();
		
		return text2model;
	}
	
	public synchronized boolean model2textAllowed() { 
		boolean result = lastText2model + debounceConstant < System.currentTimeMillis();
		
		if (result)
			text2model = false;
		
		return result;
	}
	
	public synchronized boolean textSelectionAllowed() {
		boolean result = textSelection;
		
		if (textSelection)
			diagramSelection = false;
		else
			textSelection = true;
		
		return result;
	}
	
	public synchronized boolean diagramSelectionAllowed() {
		boolean result = diagramSelection;
		
		if (diagramSelection)
			textSelection = false;
		else
			diagramSelection = true;
		
		return result;
	}
}
