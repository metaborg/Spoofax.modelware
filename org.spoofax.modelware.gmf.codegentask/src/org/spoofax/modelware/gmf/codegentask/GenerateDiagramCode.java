package org.spoofax.modelware.gmf.codegentask;

import java.io.File;

import org.apache.tools.ant.Task;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.gmf.codegen.gmfgen.GMFGenPackage;
import org.eclipse.gmf.codegen.gmfgen.GenEditorGenerator;
import org.eclipse.gmf.codegen.util.CodegenEmitters;
import org.eclipse.gmf.codegen.util.EmitterSource;
import org.eclipse.gmf.codegen.util.Generator;

/**
 * @author Oskar van Rest, inspired by
 *         https://dev.eclipse.org/svnroot/modeling/org.eclipse.mdt.sphinx/tags/0.7.0M1/tools
 *         /org.eclipse.sphinx.gmfgen/src/org/eclipse/sphinx/gmfgen/tasks/GenerateDiagramCodeOperation.java
 */
public class GenerateDiagramCode extends Task {

  private String gmfgen;

  public void setgmfgen(String gmfgen) {
    this.gmfgen = gmfgen;
  }

  @SuppressWarnings("restriction")
  @Override
  public void execute() {
    Resource resource = loadGenDiagram(gmfgen);
    GenEditorGenerator genModel = (GenEditorGenerator) resource.getContents().get(0);
    Generator gen = new Generator(genModel, getEmitters(genModel));
    gen.run();
  }

  public Resource loadGenDiagram(String filename) {
    ResourceSet resourceSet = new ResourceSetImpl();
    resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
        .put(Resource.Factory.Registry.DEFAULT_EXTENSION, new XMIResourceFactoryImpl());
    resourceSet.getPackageRegistry().put(GMFGenPackage.eNS_URI, GMFGenPackage.eINSTANCE);
    File file = new File(getProject().getBaseDir(), filename);
    if (!file.exists()) {
      System.out.println("File " + file.getAbsolutePath() + " does not exists");
    }
    else {
      System.out.println("Generating " + file.getAbsolutePath());
    }
    URI uri = file.isFile() ? URI.createFileURI(file.getAbsolutePath()) : URI.createURI(filename);
    return resourceSet.getResource(uri, true);
  }

  public CodegenEmitters getEmitters(GenEditorGenerator genModel) {
    final EmitterSource<GenEditorGenerator, CodegenEmitters> emitterSource = new EmitterSource<GenEditorGenerator, CodegenEmitters>() {
      @Override
      protected CodegenEmitters newEmitters(GenEditorGenerator genModel) {
        return new CodegenEmitters(true, genModel.getTemplateDirectory(), genModel.getModelAccess() != null);
      }
    };

    return emitterSource.getEmitters(genModel, genModel.isDynamicTemplates());
  }
}