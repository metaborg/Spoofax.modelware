package org.spoofax.modelware.gmf.editorservices;

import org.eclipse.core.commands.operations.ICompositeOperation;
import org.eclipse.gmf.runtime.common.core.command.CompositeCommand;

public class CompositeOperation extends CompositeCommand implements ICompositeOperation {

	public CompositeOperation(String label) {
		super(label);
	}

}
