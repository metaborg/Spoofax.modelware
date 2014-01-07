package org.spoofax.modelware.emf;

/**
 * @author Oskar van Rest
 */
import java.util.ArrayList;
import java.util.Collection;

public class LanguageRegistry {

	private static LanguageRegistry instance = new LanguageRegistry();

	private Collection<Language> languages = new ArrayList<Language>();
	
	private LanguageRegistry() {
	}
	
	public static LanguageRegistry getInstance() {
		return instance;
	}
	
	public void add(Language language) {
		languages.add(language);
	}
	
	public Language get(String fileExtension) {
		for (Language language : languages) {
			if (language.getTextFileExtension().equals(fileExtension) || language.getDiagramFileExtension().equals(fileExtension)) {
				return language;
			}
		}
		
		// fallback: some user may choose a file extension like .fml.diagram, in which case the language can be obtained using extensions .fml.diagram and .diagram (the actual file extension).
		for (Language language : languages) {
			if (language.getTextFileExtension().endsWith(fileExtension) || language.getDiagramFileExtension().endsWith(fileExtension)) {
				return language;
			}
		}
		return null;
	}
}
