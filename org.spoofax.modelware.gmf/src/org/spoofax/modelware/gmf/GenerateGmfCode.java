package org.spoofax.modelware.gmf;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IKeyBindingService;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.part.WorkbenchPart;


/**
 * @author Oskar van Rest
 * 
 * Parts have been copied from org.eclipse.epsilon.eugenia.GenerateGmfCodeDelegate
 */
@SuppressWarnings("deprecation")
public class GenerateGmfCode {

	public static void main(String[] args) throws CoreException {
		if (args == null || args.length == 0)
			throw new IllegalArgumentException("Path to .gmfgen file expected");
		
		IAction dummyAction = new Action() {};
		IWorkbenchPart dummyWorkbenchPart = new GenerateGmfCode().new DummyWorkbenchPart();

		IObjectActionDelegate executeTemplateAction = getExecuteTemplateAction();
		if (executeTemplateAction != null) {
			executeTemplateAction.selectionChanged(dummyAction, new StructuredSelection(args[0]));
			executeTemplateAction.setActivePart(dummyAction, dummyWorkbenchPart);

			executeTemplateAction.run(dummyAction);
		}
		
	}

	private static IObjectActionDelegate getExecuteTemplateAction() throws CoreException {
		IExtensionRegistry extensionRegistry = Platform.getExtensionRegistry();
		IConfigurationElement[] configurationElements = extensionRegistry.getConfigurationElementsFor("org.eclipse.ui.popupMenus");
		IObjectActionDelegate executeTemplateAction = null;
		for (IConfigurationElement configurationElement : configurationElements) {
			IConfigurationElement[] children = configurationElement.getChildren();
			for (IConfigurationElement child : children) {
				String id = child.getAttribute("id");
				if ("gmf.codegen.ui.executeTemplatesAction".equals(id)) {
					executeTemplateAction = (IObjectActionDelegate) child.createExecutableExtension("class");
				}
			}
		}
		return executeTemplateAction;
	}
	
	class DummyWorkbenchPart extends WorkbenchPart {
		@Override
		public void setFocus() {
		}

		@Override
		public void createPartControl(Composite parent) {
		}

		@Override
		public IWorkbenchPartSite getSite() {
			return new IWorkbenchPartSite() {

				@Override
				public boolean hasService(@SuppressWarnings("rawtypes") Class api) {
					return false;
				}

				@Override
				public Object getService(@SuppressWarnings("rawtypes") Class api) {
					return null;
				}

				@Override
				public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
					return null;
				}

				@Override
				public void setSelectionProvider(ISelectionProvider provider) {
				}

				@Override
				public IWorkbenchWindow getWorkbenchWindow() {
					return null;
				}

				@Override
				public Shell getShell() {
					return new Shell();
				}

				@Override
				public ISelectionProvider getSelectionProvider() {
					return null;
				}

				@Override
				public IWorkbenchPage getPage() {
					return null;
				}

				@Override
				public void registerContextMenu(String menuId, MenuManager menuManager, ISelectionProvider selectionProvider) {
				}

				@Override
				public void registerContextMenu(MenuManager menuManager, ISelectionProvider selectionProvider) {
				}

				@Override
				public String getRegisteredName() {
					return null;
				}

				@Override
				public String getPluginId() {
					return null;
				}

				@Override
				public IWorkbenchPart getPart() {
					return null;
				}

				@Override
				public IKeyBindingService getKeyBindingService() {
					return null;
				}

				@Override
				public String getId() {
					return null;
				}
			};
		}
	}
}
