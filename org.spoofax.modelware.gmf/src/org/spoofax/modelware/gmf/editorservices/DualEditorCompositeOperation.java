package org.spoofax.modelware.gmf.editorservices;

import java.util.ArrayList;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.commands.operations.ICompositeOperation;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class DualEditorCompositeOperation extends AbstractOperation implements ICompositeOperation {

	private ArrayList<IUndoableOperation> operations = new ArrayList<IUndoableOperation>();

	public DualEditorCompositeOperation(String label) {
		super(label);
	}

	@Override
	public void add(IUndoableOperation operation) {
		operations.add(operation);
	}

	@Override
	public void remove(IUndoableOperation operation) {
		operations.remove(operation);
	}

	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		return null;
	}

	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		for (IUndoableOperation operation : operations) {
			operation.undo(monitor, info);
		}

		return Status.OK_STATUS;
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		for (IUndoableOperation operation : operations) {
			operation.redo(monitor, info);
		}

		return Status.OK_STATUS;
	}
}
