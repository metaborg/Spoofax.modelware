package org.spoofax.modelware.gmf;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * @author Oskar van Rest
 */
public class RuntimeActivator extends AbstractUIPlugin implements IStartup {

	private static final String SYNCHRONIZER_ID = "org.spoofax.modelware.gmf.synchronizer";

	/**
	 * Like Eclipse IMP, this plug-in is activated on startup of the workbench
	 */
	@Override
	public void earlyStartup() {
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		
		System.out.println("hello");

		evaluateSynchronizerExtensions();
		EditorPairRegistry.getInstance(); // initialize
		Bridge.getInstance(); // initialize
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
	}

	private void evaluateSynchronizerExtensions() {
		IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(SYNCHRONIZER_ID);
		for (IConfigurationElement e : config) {
			String textFileExtension = e.getAttribute("textFileExtension");
			String domainFileExtension = e.getAttribute("domainFileExtension");
			String diagramFileExtension = e.getAttribute("diagramFileExtension");
			String packageName = e.getAttribute("packageName");

			if (textFileExtension != null && domainFileExtension != null && diagramFileExtension != null && packageName != null) {
				Language language = new Language(textFileExtension, domainFileExtension, diagramFileExtension, packageName);
				LanguageRegistry.getInstance().add(language);
			}
		}
	}
}
