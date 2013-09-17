package org.spoofax.modelware.gmf;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EContentAdapter;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.modelware.emf.utils.SpoofaxEMFConstants;
import org.spoofax.modelware.emf.utils.SpoofaxEMFUtils;
import org.spoofax.modelware.emf.utils.Subobject2Subterm;
import org.spoofax.terms.AbstractTermFactory;
import org.spoofax.terms.attachments.OriginAttachment;
import org.strategoxt.imp.runtime.EditorState;
import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;
import org.strategoxt.imp.runtime.stratego.StrategoTermPath;
import org.strategoxt.lang.Context;

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
		
		if (debounce || n.getEventType() == Notification.REMOVING_ADAPTER) {
			return;
		}
		EditorState editorState = EditorState.getEditorFor(editorPair.getTextEditor());
		if (SpoofaxEMFUtils.strategyExists(editorState, SpoofaxEMFConstants.STRATEGY_ASTgraph_TO_ASTtext)) {
			editorPair.doModelToTerm(EditorPairUtil.getSemanticModel(editorPair.getDiagramEditor()));
		}
		else {
			AbstractTermFactory f = SpoofaxEMFUtils.termFactory;
			IStrategoTerm ASTtext = null;
			try {
				ASTtext = editorState.getCurrentAnalyzedAst();
			} catch (BadDescriptorException e) {
				e.printStackTrace();
			}
			
			IStrategoTerm oldASTtextNode = getEObjectOrigin((EObject) n.getNotifier(), editorPair.ASTgraph);
			IStrategoTerm oldASTtextNodeParent = getEObjectOrigin(((EObject) n.getNotifier()).eContainer(), editorPair.ASTgraph);
			IStrategoTerm featureName = f.makeString(((EStructuralFeature) n.getFeature()).getName());
			
			IStrategoTerm newASTtext = null;
			if (n.getEventType() == Notification.SET) {
				IStrategoTerm oldValue = n.getOldValue() instanceof String? f.makeString(n.getOldStringValue()) : getEObjectOrigin((EObject) n.getOldValue(), editorPair.ASTgraph);
				IStrategoTerm newValue = n.getNewValue() instanceof String? f.makeString(n.getNewStringValue()) : getEObjectOrigin((EObject) n.getNewValue(), editorPair.ASTgraph);
				newASTtext = SpoofaxEMFUtils.invokeStrategy(editorState, "SET", ASTtext, oldASTtextNodeParent, oldASTtextNode, featureName, oldValue, newValue);
			}
			else if (n.getEventType() == Notification.REMOVE) {
				IStrategoTerm oldValue = getEObjectOrigin((EObject) n.getNotifier(), editorPair.ASTgraph, SpoofaxEMFUtils.feature2index(((EObject) n.getNotifier()).eClass(), (EStructuralFeature) n.getFeature()), n.getPosition());
				newASTtext = SpoofaxEMFUtils.invokeStrategy(editorState, "REMOVE", ASTtext, oldASTtextNode, featureName, oldValue);
			}
			
			if (newASTtext != null) {
				editorPair.doReplaceText(newASTtext);
			}
		}
	}

	private IStrategoTerm getEObjectOrigin(EObject eObject, IStrategoTerm AST, int featureIndex, int position) {
		List<Integer> path = Subobject2Subterm.object2path(eObject, new LinkedList<Integer>());
		path.add(featureIndex); path.add(position);
		IStrategoList strategoTermPath = StrategoTermPath.toStrategoPath(path);
		IStrategoTerm term = StrategoTermPath.getTermAtPath(new Context(), AST, strategoTermPath);
		return getEObjectOriginHelper(eObject, term);
	}
	
	private IStrategoTerm getEObjectOrigin(EObject eObject, IStrategoTerm ast) {
		if (eObject == null) {
			return SpoofaxEMFUtils.createNone();
		}
		
		IStrategoTerm term = Subobject2Subterm.object2subterm(eObject, ast);
		return getEObjectOriginHelper(eObject, term);
	}
	
	private IStrategoTerm getEObjectOriginHelper(EObject eObject, IStrategoTerm term) {
		if (term != null) {
			IStrategoTerm origin = OriginAttachment.getOrigin(term);
			if (origin != null) {
				return origin;
			}
		}
		return SpoofaxEMFUtils.termFactory.makeString(eObject.eClass().getName());
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