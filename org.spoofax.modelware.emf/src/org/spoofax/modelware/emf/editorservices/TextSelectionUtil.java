package org.spoofax.modelware.emf.editorservices;

import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.editors.text.TextEditor;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.imploder.ImploderAttachment;
import org.spoofax.modelware.emf.utils.Subobject2Subterm;
import org.strategoxt.imp.runtime.services.StrategoObserver;

/**
 * @author Oskar van Rest
 */
public class TextSelectionUtil {
	
	public static TextSelection calculateTextSelection(StrategoObserver observer, List<EObject> selectedObjects, EObject root, IStrategoTerm AST) {
		int left = Integer.MAX_VALUE;
		int right = Integer.MIN_VALUE;

		for (int i = 0; i < selectedObjects.size(); i++) {
			if (EcoreUtil.isAncestor(root, selectedObjects.get(i))) { // only take non-phantom nodes into account
				IStrategoTerm selectedTerm = Subobject2Subterm.object2subterm(observer, selectedObjects.get(i), root, AST);
				
				if (selectedTerm != null && ImploderAttachment.hasImploderOrigin(selectedTerm)) {
					IStrategoTerm originTerm = ImploderAttachment.getImploderOrigin(selectedTerm);
					
					int newLeft = (ImploderAttachment.getLeftToken(originTerm).getStartOffset());
					int newRight = (ImploderAttachment.getRightToken(originTerm).getEndOffset()) + 1;
	
					if (newLeft < left) {
						left = newLeft;
					}
					if (newRight > right) {
						right = newRight;
					}
				}
			}
		}
		
		if (left != Integer.MAX_VALUE && right != Integer.MIN_VALUE) {
			return new TextSelection(left, right - left);
		} else {
			return new TextSelection(0, 0);
		}
	}
	
	public static void setTextSelection(TextEditor editor, TextSelection selection) {
		ISelectionProvider selectionProvider = editor.getEditorSite().getSelectionProvider();
		selectionProvider.setSelection(selection);
	}
}
