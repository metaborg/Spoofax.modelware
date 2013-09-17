package org.spoofax.modelware.emf.resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.CommonPlugin;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.impl.EPackageRegistryImpl;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.modelware.emf.Language;
import org.spoofax.modelware.emf.LanguageRegistry;
import org.spoofax.modelware.emf.tree2model.Model2Term;
import org.spoofax.modelware.emf.tree2model.Term2Model;
import org.spoofax.modelware.emf.utils.SpoofaxEMFConstants;
import org.spoofax.modelware.emf.utils.SpoofaxEMFUtils;
import org.spoofax.terms.TermFactory;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.FileState;
import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;

/**
 * An EMF resource implementation for Spoofax, which provides generic functionality for serializing and deserializing EObjects by means of a
 * user-defined syntax. One can use this resource implementation by extending `org.eclipse.emf.ecore.extension_parser` by means of an Eclipse
 * extension.
 * 
 * @author oskarvanrest
 */
public class SpoofaxEMFResource extends ResourceImpl {

	protected IPath path;
	protected ITermFactory termFactory;

	public SpoofaxEMFResource(URI uri) {
		super(uri);

		URI resolvedFile = CommonPlugin.resolve(uri);
		this.path = new Path(resolvedFile.toFileString());
		this.termFactory = new TermFactory();
	}

	/**
	 * @override
	 */
	protected void doLoad(InputStream inputStream, Map<?, ?> options) {
		FileState editorOrFileState = SpoofaxEMFUtils.getEditorOrFileState(path);
		IStrategoTerm adjustedTree = SpoofaxEMFUtils.getASTgraph(editorOrFileState);
		
		String textFileExtension = null;
		try {
			textFileExtension = editorOrFileState.getDescriptor().getLanguage().getFilenameExtensions().iterator().next();
		}
		catch (BadDescriptorException e) {
			e.printStackTrace();
		}
		Language language = LanguageRegistry.getInstance().get(textFileExtension);

		EPackage ePackage = EPackageRegistryImpl.INSTANCE.getEPackage(language.getNsURI());
		if (ePackage == null) {
			Environment.logException("Cannot find EPackage " + textFileExtension + ".");
		}

		EObject eObject = null;

		if (adjustedTree instanceof IStrategoAppl) {
			Term2Model term2Model = new Term2Model(ePackage);
			eObject = term2Model.convert(adjustedTree);
		}
		else {
			EAnnotation rootElementAnnotation = ePackage.getEAnnotation(SpoofaxEMFConstants.SPOOFAX_CONFIG_ANNO);
			if (rootElementAnnotation != null) {
				String rootClass_String = rootElementAnnotation.getDetails().get(SpoofaxEMFConstants.SPOOFAX_CONFIG_ANNO_ROOT);
				if (rootClass_String != null) {
					EClass rootClass_EClass = (EClass) ePackage.getEClassifier(rootClass_String);
					if (rootClass_EClass != null) {
						eObject = ePackage.getEFactoryInstance().create(rootClass_EClass);
					}
				}
			}
			if (eObject == null) {
				Environment.logException("Unknown root class.");
				return;
			}
		}

		getContents().add(0, eObject);
	}

	protected void doSave(OutputStream outputStream, Map<?, ?> options) {
		FileState editorOrFileState = SpoofaxEMFUtils.getEditorOrFileState(path);
		// if (fileState == null) {
		// try {
		// outputStream.write("".getBytes());
		// }
		// catch (IOException e) {
		// e.printStackTrace();
		// }
		// return;
		// }

		if (editorOrFileState.getCurrentAst() == null) {
			Environment.logException("Can't parse file, see Spoofax.modelware/7");
			// TODO: pretty-print newTree
		}

		EObject object = getContents().get(0);
		Model2Term model2term = new Model2Term(new TermFactory());
		IStrategoTerm newTree = model2term.convert(object);
		newTree = SpoofaxEMFUtils.getASTtext(newTree, editorOrFileState);
		
		try {
			String result = SpoofaxEMFUtils.calculateTextReplacement(editorOrFileState.getCurrentAnalyzedAst(), newTree, editorOrFileState);
			outputStream.write(result.getBytes());
		}
		catch (IOException e) {
			e.printStackTrace();
		} catch (BadDescriptorException e) {
			e.printStackTrace();
		}
	}
}
