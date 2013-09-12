package org.spoofax.modelware.gmf;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EContentAdapter;
import org.eclipse.imp.parser.IParseController;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.modelware.emf.utils.SpoofaxEMFUtils;
import org.spoofax.modelware.emf.utils.Subobject2Subterm;
import org.spoofax.terms.AbstractTermFactory;
import org.spoofax.terms.attachments.OriginAttachment;
import org.strategoxt.imp.runtime.EditorState;
import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;

/**
 * Listens for changes in GMF's semantic model and perform a model-to-text
 * transformation upon such a change.
 * 
 * @author Oskar van Rest
 */
public class ModelChangeListener extends EContentAdapter {

	private EditorPair editorPair;
	private boolean debounce;

	public ModelChangeListener(EditorPair editorPair) {
		this.editorPair = editorPair;
		editorPair.registerObserver(new Debouncer());
	}

	public void notifyChanged(Notification n) {
		super.notifyChanged(n);
			
		if (debounce) {
			return;
		}
		
		if (!editorPair.doModelToTerm(EditorPairUtil.getSemanticModel(editorPair.getDiagramEditor()))) {
			AbstractTermFactory f = SpoofaxEMFUtils.termFactory;
			IParseController parseController = editorPair.getTextEditor().getParseController();
			IStrategoTerm ASTtext = null;
			try {
				ASTtext = EditorState.getEditorFor(parseController).getAnalyzedAst();
			} catch (BadDescriptorException e) {
				e.printStackTrace();
			}
				
			if (n.getEventType() == Notification.SET) {
				editorPair.debounce = true;
				IStrategoTerm oldASTgraphNode = Subobject2Subterm.object2subterm((EObject) n.getNotifier(), editorPair.ASTgraph);
				IStrategoTerm oldASTtextNode = OriginAttachment.getOrigin(oldASTgraphNode);
				if (oldASTtextNode == null) {
					oldASTtextNode = f.makeString("no origin found");
				}
				IStrategoTerm featureName = f.makeString(((EStructuralFeature) n.getFeature()).getName());
				IStrategoTerm oldValue = f.makeString(n.getOldStringValue());
				IStrategoTerm newValue = f.makeString(n.getNewStringValue());
				
				IStrategoTerm newASTtext = SpoofaxEMFUtils.invokeStrategy(parseController, "SET", ASTtext, oldASTtextNode, featureName, oldValue, newValue);
				editorPair.doReplaceText(newASTtext);
			}
		}
	}

	private class Debouncer implements EditorPairObserver {

		@Override
		public void notify(EditorPairEvent event) {
			
			if (event == EditorPairEvent.PreMerge) {
				debounce = true;
			}
			else if (event == EditorPairEvent.PostMerge) {
				debounce = false;
			}

			else if (event == EditorPairEvent.PreUndo || event == EditorPairEvent.PreRedo) {
				debounce = true;
			}
			else if (event == EditorPairEvent.PostUndo || event == EditorPairEvent.PostRedo) {
				debounce = false;
			}
		}
	}
}