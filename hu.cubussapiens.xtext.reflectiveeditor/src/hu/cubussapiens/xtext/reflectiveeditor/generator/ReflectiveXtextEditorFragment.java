/**
 * 
 */
package hu.cubussapiens.xtext.reflectiveeditor.generator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.mwe2.runtime.Mandatory;
import org.eclipse.xtext.Grammar;
import org.eclipse.xtext.GrammarUtil;
import org.eclipse.xtext.generator.AbstractGeneratorFragment;

/**
 * Added code generation fragment that generates the reflective editor contribution.
 * @author Zoltan Ujhelyi
 *
 */
public class ReflectiveXtextEditorFragment extends AbstractGeneratorFragment {

	private List<String> fileExtensions = new ArrayList<String>();

	/**
	 * Sets the fileExtension the generated editor should be registered
	 * to.
	 * 
	 * @param fileExtensions
	 */
	@Mandatory
	public void setFileExtensions(String fileExtensions) {
		if ("".equals(fileExtensions.trim()))
			return;
		String[] split = fileExtensions.split("\\s*,\\s*");
		for (String string : split) {
			this.fileExtensions.add(string);
		}
	}

	@Override
	protected List<Object> getParameters(Grammar grammar) {
		return Collections.singletonList((Object) getFileExtensions(grammar));
	}

	private List<String> getFileExtensions(Grammar grammar) {
		if (!fileExtensions.isEmpty())
			return fileExtensions;
		return Collections.singletonList(GrammarUtil.getName(grammar).toLowerCase());
	}
	
	@Override
	public String[] getRequiredBundlesUi(Grammar grammar) {
		return new String[] { "hu.cubussapiens.xtext.reflectiveeditor" };
	}

}
