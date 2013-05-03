package org.spoofax.modelware.gmf.editorservices;

import java.util.LinkedList;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.commands.operations.ICompositeOperation;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class CompositeOperation extends AbstractOperation implements ICompositeOperation {

	private LinkedList<IUndoableOperation> operations = new LinkedList<IUndoableOperation>();

	public CompositeOperation(String label) {
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
	public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		for (int i=operations.size()-1; i>=0; i--) {
			System.out.println("undoing " + operations.get(i).hashCode());
			operations.get(i).undo(monitor, info);
		}

		return Status.OK_STATUS;
	}
	
	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		//TODO
		for (IUndoableOperation operation : operations) {
			operation.redo(monitor, info);
		}

		return Status.OK_STATUS;
	}

}
