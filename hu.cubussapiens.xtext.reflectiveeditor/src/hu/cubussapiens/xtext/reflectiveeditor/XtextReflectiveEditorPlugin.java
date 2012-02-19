package hu.cubussapiens.xtext.reflectiveeditor;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class XtextReflectiveEditorPlugin extends AbstractUIPlugin {

	private static XtextReflectiveEditorPlugin INSTANCE;

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		INSTANCE = this;
	}
	
	
	@Override
	public void stop(BundleContext context) throws Exception {
		INSTANCE = null;
		super.stop(context);
	}
	
	public static XtextReflectiveEditorPlugin getInstance() {
		return INSTANCE;
	}

}
