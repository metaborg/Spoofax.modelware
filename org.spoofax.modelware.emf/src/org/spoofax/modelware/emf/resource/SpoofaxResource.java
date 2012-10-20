package org.spoofax.modelware.emf.resource;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.CommonPlugin;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.impl.EPackageRegistryImpl;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
import org.eclipse.imp.model.ModelFactory.ModelException;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.modelware.emf.Term2Model;
import org.strategoxt.imp.runtime.FileState;
import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;

public class SpoofaxResource extends ResourceImpl {

	public SpoofaxResource(URI uri) {
		this.uri = uri;
	}

	/**
	 * @override
	 */
	protected void doLoad(InputStream inputStream, Map<?, ?> options) {
		System.out.println("doLoad");
		
		URI resolvedFile = CommonPlugin.resolve(uri);
		IPath filePath = new Path(resolvedFile.toFileString());

		FileState fileState = null;
		IStrategoTerm analysedAST = null;
		try {
			fileState = FileState.getFile(filePath, null);
			analysedAST = fileState.getAnalyzedAst();
		} catch (FileNotFoundException | BadDescriptorException | ModelException e) {
			e.printStackTrace();
		}

		Term2Model term2Model = null;
		try {
			term2Model = new Term2Model(EPackageRegistryImpl.INSTANCE.getEPackage(fileState.getDescriptor().getLanguage().getName()));
		} catch (BadDescriptorException e) {
			e.printStackTrace();
		}
		EObject eObject = term2Model.convert(analysedAST);

		getContents().add(0, eObject);

	}

	protected void doSave(OutputStream outputStream, Map<?, ?> options) {
		String result = "";
		try {
			outputStream.write(result.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
