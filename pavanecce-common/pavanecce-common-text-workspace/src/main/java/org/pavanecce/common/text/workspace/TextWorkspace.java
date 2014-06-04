package org.pavanecce.common.text.workspace;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.pavanecce.common.util.VersionNumber;

public class TextWorkspace extends TextOutputNode {
	Set<TextProject> projects = new HashSet<TextProject>();
	private String workspaceIdentifier;
	private String qualifiedWorkspaceIdentifier;

	public TextWorkspace(String qualifiedWorkspaceIdentifier) {
		super(qualifiedWorkspaceIdentifier);
		String[] split = qualifiedWorkspaceIdentifier.split("\\.");
		this.workspaceIdentifier = split[split.length - 1];
		this.qualifiedWorkspaceIdentifier = qualifiedWorkspaceIdentifier;
	}

	public TextProject findOrCreateTextProject(TextProjectDefinition sfd, String name, VersionNumber vn) {
		String projectName = sfd.generateProjectName(workspaceIdentifier, qualifiedWorkspaceIdentifier, name, vn);
		TextProject result = findOrCreateTextProject(projectName);
		return result;
	}

	private TextProject findOrCreateTextProject(String name) {
		TextProject result = findTextProject(name);
		if (result == null) {
			result = new TextProject(this, name);
			projects.add(result);
		}
		if (!result.isRegenerated()) {
			result.setRegenerated(true);
		}

		return result;
	}

	public TextProject findTextProject(String name) {
		for (TextProject r : projects) {
			if (r.name.equals(name)) {
				return r;
			}
		}
		return null;
	}

	public Collection<TextProject> getTextProjects() {
		return projects;
	}

	@Override
	public boolean hasContent() {
		return projects.size() > 0;
	}

	public TextProject createExistingTextProject(String name) {
		TextProject result = findOrCreateTextProject(name);
		result.setRegenerated(false);
		return result;
	}
}
