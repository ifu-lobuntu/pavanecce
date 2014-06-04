package org.pavanecce.common.text.workspace;

import org.pavanecce.common.util.VersionNumber;

public class TextProjectDefinition {
	private String projectQualifier;
	private ProjectNameStrategy nameStrategy;
	private boolean appendVersionNumber;

	public TextProjectDefinition(ProjectNameStrategy nameStrategy, String projectQualifier) {
		this(nameStrategy, projectQualifier, false);
	}

	public TextProjectDefinition(ProjectNameStrategy nameStrategy, String projectQualifier, boolean appendVersionNumber) {
		this.appendVersionNumber = appendVersionNumber;
		this.nameStrategy = nameStrategy;
		this.projectQualifier = projectQualifier;
	}

	public String getProjectQualifier() {
		return projectQualifier;
	}

	public ProjectNameStrategy getNameStrategy() {
		return nameStrategy;
	}

	public String generateProjectName(String workspaceIdentifier, String qualifiedWorkspaceIdentifier, String baseName, VersionNumber vn) {
		String result = nameStrategy.generateProjectName(projectQualifier, workspaceIdentifier, qualifiedWorkspaceIdentifier, baseName);
		if (appendVersionNumber) {
			result = result + vn.getSuffix();
		}
		return result;
	}

}
