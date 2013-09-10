package org.spoofax.modelware.emf;

/**
 * A language for which both a textual and graphical editor is provided.
 * 
 * @author Oskar van Rest
 */
public class Language {

	private final String textFileExtension;
	private final String domainFileExtension;
	private final String diagramFileExtension; // optional
	private final String NsURI;
	
	public Language(String textFileExtension, String domainFileExtension, String diagramFileExtension, String NsURI) {
		this.textFileExtension = textFileExtension;
		this.domainFileExtension = domainFileExtension;
		this.diagramFileExtension = diagramFileExtension;
		this.NsURI = NsURI;
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

	public String getNsURI() {
		return NsURI;
	}
	
	@Override
	public String toString() {
		return "Language: " + "textFileExtension=" + textFileExtension + ", domainFileExtension=" + domainFileExtension + ", diagramFileExtension=" + diagramFileExtension + ", NsURI=" + NsURI;
	}
}
