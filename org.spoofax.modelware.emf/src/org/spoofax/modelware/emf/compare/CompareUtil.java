package org.spoofax.modelware.emf.compare;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.compare.diff.merge.service.MergeService;
import org.eclipse.emf.compare.diff.metamodel.DiffElement;
import org.eclipse.emf.compare.diff.metamodel.DiffModel;
import org.eclipse.emf.compare.diff.service.DiffService;
import org.eclipse.emf.compare.match.MatchOptions;
import org.eclipse.emf.compare.match.metamodel.MatchModel;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.transaction.RecordingCommand;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.util.TransactionUtil;

/**
 * @author Oskar van Rest
 */
public class CompareUtil {

	/**
	 * Compare objects a and b and merge their differences such that b gets updated to reflect a.
	 */
	public static void merge(EObject a, EObject b) {
		TransactionalEditingDomain editingDomain = TransactionUtil.getEditingDomain(b);

		for (int i = 0; i < 2; i++) { // TODO: hack/workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=390788
			final List<DiffElement> differences = CompareUtil.compare(a, b);
			editingDomain.getCommandStack().execute(new RecordingCommand(editingDomain) {
				protected void doExecute() {
					MergeService.merge(differences, true);
				}
			});
		}
		;
	}

	/**
	 * Compare two EObjects and return a list of differences.
	 */
	private static List<DiffElement> compare(EObject a, EObject b) {
		Map<String, Object> options = new HashMap<String, Object>();

		options.put(MatchOptions.OPTION_DISTINCT_METAMODELS, Boolean.TRUE);
		options.put(MatchOptions.OPTION_IGNORE_ID, Boolean.TRUE);
		options.put(MatchOptions.OPTION_IGNORE_XMI_ID, Boolean.TRUE);

		MatchModel match = new CommonRootMatchEngine(a, b).contentMatch(a, b, options);
		DiffModel diff = DiffService.doDiff(match, false);
		return new ArrayList<DiffElement>(diff.getOwnedElements());
	}
}
