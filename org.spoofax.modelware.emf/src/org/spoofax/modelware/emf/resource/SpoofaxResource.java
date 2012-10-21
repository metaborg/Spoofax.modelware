package org.spoofax.modelware.emf.resource;

import java.io.File;
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
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.modelware.emf.Model2Term;
import org.spoofax.modelware.emf.Term2Model;
import org.spoofax.terms.TermFactory;
import org.strategoxt.imp.generator.construct_textual_change_4_0;
import org.strategoxt.imp.runtime.FileState;
import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;
import org.strategoxt.imp.runtime.dynamicloading.Descriptor;
import org.strategoxt.imp.runtime.dynamicloading.RefactoringFactory;
import org.strategoxt.imp.runtime.services.StrategoObserver;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class SpoofaxResource extends ResourceImpl {

	private IPath filePath;
	private FileState fileState;
	private ITermFactory termFactory;

	public SpoofaxResource(URI uri) {
		this.uri = uri;

		URI resolvedFile = CommonPlugin.resolve(uri);
		this.filePath = new Path(resolvedFile.toFileString());

		try {
			fileState = FileState.getFile(filePath, null);
		} catch (FileNotFoundException | BadDescriptorException | ModelException e) {
			e.printStackTrace();
		}

		this.termFactory = new TermFactory();
	}

	/**
	 * @override
	 */
	protected void doLoad(InputStream inputStream, Map<?, ?> options) {
		IStrategoTerm analysedAST = null;
		try {
			analysedAST = fileState.getAnalyzedAst();
		} catch (BadDescriptorException e) {
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
		EObject object = getContents().get(0);
		Model2Term model2term = new Model2Term(new TermFactory());

		IStrategoTerm newAST = model2term.convert(object);
		IStrategoTerm oldAST = fileState.getCurrentAst();

		if (oldAST == null) {
			try {
				outputStream.write("".getBytes());
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}

		IStrategoTerm resultTuple = termFactory.makeList(termFactory.makeTuple(oldAST, newAST));

		File file = filePath.toFile();
		IStrategoTerm textreplace = null;
		String result = null;

		// TODO call TextReplacer instead
		try {
			Descriptor descriptor = fileState.getDescriptor();
			StrategoObserver observer = descriptor.createService(StrategoObserver.class, fileState.getParseController());
			textreplace = construct_textual_change_4_0.instance.invoke(observer.getRuntime().getCompiledContext(), resultTuple, createStrategy(RefactoringFactory.getPPStrategy(descriptor), file, observer), createStrategy(RefactoringFactory.getParenthesizeStrategy(descriptor), file, observer), createStrategy(RefactoringFactory.getOverrideReconstructionStrategy(descriptor), file, observer), createStrategy(RefactoringFactory.getResugarStrategy(descriptor), file, observer));
			result = ((IStrategoString) textreplace.getSubterm(0).getSubterm(2)).stringValue();
		} catch (BadDescriptorException e) {
			e.printStackTrace();
		}

		try {
			outputStream.write(result.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// TODO: remove from here
	private Strategy createStrategy(final String sname, final File file, final StrategoObserver observer) {
		return new Strategy() {
			@Override
			public IStrategoTerm invoke(Context context, IStrategoTerm current) {
				if (sname != null)
					return observer.invokeSilent(sname, current, file);
				return null;
			}
		};
	}
}
