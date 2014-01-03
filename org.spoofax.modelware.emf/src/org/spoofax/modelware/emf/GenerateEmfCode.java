package org.spoofax.modelware.emf;

import java.io.IOException;

import org.eclipse.emf.codegen.ecore.generator.Generator;
import org.eclipse.emf.codegen.ecore.genmodel.GenModel;
import org.eclipse.emf.codegen.ecore.genmodel.generator.GenBaseGeneratorAdapter;
import org.eclipse.emf.common.util.BasicMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;

/**
 * @author Oskar van Rest
 * 
 * Parts have been copied from org.eclipse.epsilon.eugenia.GenerateEmfCodeDelegate
 */
public class GenerateEmfCode {

	public static void main(String[] args) throws IOException {
		if (args == null || args.length == 0)
			throw new IllegalArgumentException("Path to .genmodel file expected");

		ResourceSet resourceSet = new ResourceSetImpl();
		Resource resource = resourceSet.createResource(URI.createURI(args[0]));
		resource.load(null);
		EcoreUtil.resolveAll(resourceSet);

		GenModel genModel = (GenModel) resource.getContents().get(0);
		genModel.setCanGenerate(true);

		// generate the code
		Generator generator = new Generator();
		generator.setInput(genModel);
		generator.generate(genModel, GenBaseGeneratorAdapter.MODEL_PROJECT_TYPE, new BasicMonitor.Printing(System.err));
		generator.generate(genModel, GenBaseGeneratorAdapter.EDIT_PROJECT_TYPE, new BasicMonitor.Printing(System.err));
		generator.generate(genModel, GenBaseGeneratorAdapter.EDITOR_PROJECT_TYPE, new BasicMonitor.Printing(System.err));
		generator.generate(genModel, GenBaseGeneratorAdapter.TESTS_PROJECT_TYPE, new BasicMonitor.Printing(System.err));
	}
}
