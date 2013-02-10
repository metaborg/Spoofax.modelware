package org.spoofax.modelware.emf.compare;

import java.util.List;

import org.eclipse.emf.compare.Comparison;
import org.eclipse.emf.compare.Diff;
import org.eclipse.emf.compare.EMFCompare;
import org.eclipse.emf.compare.EMFCompare.Builder;
import org.eclipse.emf.compare.match.DefaultComparisonFactory;
import org.eclipse.emf.compare.match.DefaultEqualityHelperFactory;
import org.eclipse.emf.compare.match.DefaultMatchEngine;
import org.eclipse.emf.compare.match.IComparisonFactory;
import org.eclipse.emf.compare.match.IMatchEngine;
import org.eclipse.emf.compare.match.eobject.IEObjectMatcher;
import org.eclipse.emf.compare.scope.IComparisonScope;
import org.eclipse.emf.compare.utils.UseIdentifiers;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.transaction.RecordingCommand;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.util.TransactionUtil;

/**
 * @author Oskar van Rest
 */
public class CompareUtil {

	public static Comparison compare(EObject left, EObject right) {
		IComparisonScope scope =  EMFCompare.createDefaultScope(left,  right);
		Builder builder = EMFCompare.builder();
		IEObjectMatcher matcher = DefaultMatchEngine.createDefaultEObjectMatcher(UseIdentifiers.NEVER);
		IComparisonFactory comparisonFactory = new DefaultComparisonFactory(new DefaultEqualityHelperFactory());
		IMatchEngine matchEngine = new DefaultMatchEngine(matcher , comparisonFactory);
			
		builder.setMatchEngine(matchEngine);
		return builder.build().compare(scope);		
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
