package hu.cubussapiens.xtext.reflectiveeditor.factories;

import hu.cubussapiens.xtext.reflectiveeditor.XtextReflectiveEditorPlugin;

import org.eclipse.xtext.ui.XtextExecutableExtensionFactory;
import org.osgi.framework.Bundle;

public class XtextReflectiveExecutableExtensionFactory extends
		XtextExecutableExtensionFactory {
	
	@Override
	protected Bundle getBundle() {
		return XtextReflectiveEditorPlugin.getInstance().getBundle();
	}
}
