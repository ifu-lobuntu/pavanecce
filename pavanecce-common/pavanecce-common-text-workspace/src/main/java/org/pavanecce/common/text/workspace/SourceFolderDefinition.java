package org.pavanecce.common.text.workspace;

import org.pavanecce.common.util.VersionNumber;

public class SourceFolderDefinition {
	private boolean overwriteFiles = true;
	private boolean cleanDirectories = true;
	private SourceFolderNameStrategy sourceFolderNameStrategy;
	private String sourceFolderQualifier;
	private boolean appendVersionNumber;

	public SourceFolderDefinition(SourceFolderNameStrategy sourceFolderNameStrategy, String sourceFolderQualifier, boolean appendVersionNumber) {
		super();
		this.sourceFolderNameStrategy = sourceFolderNameStrategy;
		this.sourceFolderQualifier = sourceFolderQualifier;
		this.appendVersionNumber=appendVersionNumber;
	}

	public SourceFolderDefinition(SourceFolderNameStrategy sourceFolderNameStrategy, String sourceFolderQualifier) {
		this(sourceFolderNameStrategy,sourceFolderQualifier,false);
	}

	public String generateSourceFolderName(String modelIdentifier, VersionNumber versionNumber) {
		String result = sourceFolderNameStrategy.sourceFolderName(sourceFolderQualifier, modelIdentifier);
		if(this.appendVersionNumber){
			result=result+versionNumber.getSuffix();
		}
		return result;
	}

	public void dontCleanDirectories() {
		cleanDirectories = false;
	}

	public void dontCleanDirectoriesOrOverwriteFiles() {
		dontCleanDirectories();
		dontOverwriteFiles();
	}

	public void dontOverwriteFiles() {
		overwriteFiles = false;
	}

	public boolean overwriteFiles() {
		return overwriteFiles;
	}

	public boolean cleanDirectories() {
		return cleanDirectories;
	}

}
