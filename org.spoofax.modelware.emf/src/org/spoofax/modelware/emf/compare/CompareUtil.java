package org.spoofax.modelware.emf.compare;

import java.util.List;

import org.eclipse.emf.compare.Comparison;
import org.eclipse.emf.compare.Diff;
import org.eclipse.emf.compare.EMFCompare;
import org.eclipse.emf.compare.match.IMatchEngine;
import org.eclipse.emf.compare.match.impl.MatchEngineFactoryImpl;
import org.eclipse.emf.compare.match.impl.MatchEngineFactoryRegistryImpl;
import org.eclipse.emf.compare.scope.IComparisonScope;
import org.eclipse.emf.compare.utils.UseIdentifiers;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.transaction.RecordingCommand;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.util.TransactionUtil;

/**
 * Utilities for comparing and merging of EMF models.
 * 
 * @author oskarvanrest
 */
public class CompareUtil {

	public static Comparison compare(EObject left, EObject right) {
		IMatchEngine.Factory factory = new MatchEngineFactoryImpl(UseIdentifiers.NEVER);
		IMatchEngine.Factory.Registry matchEngineRegistry = new MatchEngineFactoryRegistryImpl();
		matchEngineRegistry .add(factory);
		EMFCompare comparator = EMFCompare.builder().setMatchEngineFactoryRegistry(matchEngineRegistry).build();
		IComparisonScope scope =  EMFCompare.createDefaultScope(left,  right);
		return comparator.compare(scope);	
	}
	
	public static void merge(Comparison comparison, EObject right) {
		final List<Diff> differences = comparison.getDifferences();
		
		TransactionalEditingDomain editingDomain = TransactionUtil.getEditingDomain(right);
		editingDomain.getCommandStack().execute(new RecordingCommand(editingDomain) {
			protected void doExecute() {
				for (Diff diff : differences) {
					diff.copyLeftToRight();
				}
			}
		});
	}
}
