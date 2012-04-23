package hu.cubussapiens.xtext.reflectiveeditor.editor;

import java.util.EventObject;

import org.eclipse.core.resources.IFile;
import org.eclipse.emf.common.command.BasicCommandStack;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.command.CommandStack;
import org.eclipse.emf.common.command.CommandStackListener;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.presentation.EcoreEditor;
import org.eclipse.emf.ecore.provider.EcoreItemProviderAdapterFactory;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
import org.eclipse.emf.edit.provider.ReflectiveItemProviderAdapterFactory;
import org.eclipse.emf.edit.provider.resource.ResourceItemProviderAdapterFactory;
import org.eclipse.emf.edit.ui.util.EditUIUtil;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.xtext.Constants;
import org.eclipse.xtext.resource.IResourceFactory;
import org.eclipse.xtext.resource.IResourceServiceProvider;
import org.eclipse.xtext.ui.guice.AbstractGuiceAwareExecutableExtensionFactory;
import org.eclipse.xtext.ui.resource.IResourceSetProvider;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * An override of the EcoreEditor class that can open an Xtext model using a
 * Guice-aware extension factory:
 * {@link AbstractGuiceAwareExecutableExtensionFactory}.
 * 
 * @author Zoltan Ujhelyi
 * 
 */
public class ReflectiveXtextEditor extends EcoreEditor {

	ResourceSet resourceSet;

	@Inject
	IResourceFactory resourceFactory;
	@Inject
	IResourceServiceProvider serviceProvider;
	@Inject
	IResourceSetProvider resourceSetProvider;
	String extensions;

	@Inject
	public ReflectiveXtextEditor(@Named(Constants.FILE_EXTENSIONS) String extensions) {
		// The super() intentionally not called to avoid calling the
		// initializeEditingDomain before knowing the project
		this.extensions = extensions;
	}

	@Override
	public void init(IEditorSite site, IEditorInput editorInput) {
		super.init(site, editorInput);
		for (String extension : extensions.split(",")) {
			Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put(extension,
				resourceFactory);
			org.eclipse.xtext.resource.IResourceServiceProvider.Registry.INSTANCE
				.getExtensionToFactoryMap().put(extension, serviceProvider);
		}
		IFile file = (IFile) editorInput.getAdapter(IFile.class);
		this.resourceSet = resourceSetProvider.get(file.getProject());
		initializeEditingDomain();
	}

	@Override
	public void createModel() {
		super.createModel();

		if (!resourceSet.getResources().isEmpty()) {
			Resource resource = resourceSet.getResources().get(0);
			if (!resource.getContents().isEmpty()) {
				EObject rootObject = resource.getContents().get(0);
				Resource metaDataResource = rootObject.eClass().eResource();
				if (metaDataResource != null
						&& metaDataResource.getResourceSet() != null) {
					resourceSet.getResources().addAll(
							metaDataResource.getResourceSet().getResources());
				}
			}
		}
	}

	@Override
	public void createModelGen() {
		URI resourceURI = EditUIUtil.getURI(getEditorInput());
		Exception exception = null;
		Resource resource = null;
		try {
			// Load the resource through the editing domain.
			//
			resource = editingDomain.getResourceSet().getResource(resourceURI,
					true);
		} catch (Exception e) {
			exception = e;
			resource = editingDomain.getResourceSet().getResource(resourceURI,
					false);
		}

		Diagnostic diagnostic = analyzeResourceProblems(resource, exception);
		if (diagnostic.getSeverity() != Diagnostic.OK) {
			resourceToDiagnosticMap.put(resource,
					analyzeResourceProblems(resource, exception));
		}
		editingDomain.getResourceSet().eAdapters()
				.add(problemIndicationAdapter);
	}

	@Override
	protected void initializeEditingDomain() {
		// Create an adapter factory that yields item providers.
		//
		adapterFactory = new ComposedAdapterFactory(
				ComposedAdapterFactory.Descriptor.Registry.INSTANCE);

		adapterFactory
				.addAdapterFactory(new ResourceItemProviderAdapterFactory());
		adapterFactory.addAdapterFactory(new EcoreItemProviderAdapterFactory());
		adapterFactory
				.addAdapterFactory(new ReflectiveItemProviderAdapterFactory());

		// Create the command stack that will notify this editor as commands are
		// executed.
		//
		BasicCommandStack commandStack = new BasicCommandStack();

		// Add a listener to set the most recent command's affected objects to
		// be the selection of the viewer with focus.
		//
		commandStack.addCommandStackListener(new CommandStackListener() {
			public void commandStackChanged(final EventObject event) {
				getContainer().getDisplay().asyncExec(new Runnable() {
					public void run() {
						firePropertyChange(IEditorPart.PROP_DIRTY);

						// Try to select the affected objects.
						//
						Command mostRecentCommand = ((CommandStack) event
								.getSource()).getMostRecentCommand();
						if (mostRecentCommand != null) {
							setSelectionToViewer(mostRecentCommand
									.getAffectedObjects());
						}
						if (propertySheetPage != null
								&& !propertySheetPage.getControl().isDisposed()) {
							propertySheetPage.refresh();
						}
					}
				});
			}
		});
		// The next line is changed to load the editing domain from a resource
		// set
		editingDomain = new AdapterFactoryEditingDomain(adapterFactory,
				commandStack, resourceSet);
	}

}