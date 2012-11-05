package org.spoofax.modelware.gmf;

/**
 * @author Oskar van Rest
 */
public class Language {

	private final String textFileExtension;
	private final String domainFileExtension;
	private final String diagramFileExtension;
	private final String packageName;
	
	public Language(String textFileExtension, String domainFileExtension, String diagramFileExtension, String packageName) {
		this.textFileExtension = textFileExtension;
		this.domainFileExtension = domainFileExtension;
		this.diagramFileExtension = diagramFileExtension;
		this.packageName = packageName;
	}

	public String getTextFileExtension() {
		return textFileExtension;
	}

	public String getDomainFileExtension() {
		return domainFileExtension;
	}

	public String getDiagramFileExtension() {
		return diagramFileExtension;
	}

	public String getPackageName() {
		return packageName;
	}
	
	@Override
	public String toString() {
		return "Language: " + "textFileExtension=" + textFileExtension + ", domainFileExtension=" + domainFileExtension + ", diagramFileExtension=" + diagramFileExtension + ", packageName=" + packageName;
	}
}
