package org.pavanecce.common.text.workspace;

public class SourceFolder extends TextDirectory {
	private boolean isRegenerated = true;
	private SourceFolderDefinition definition;

	protected SourceFolder(SourceFolderDefinition definition, TextProject textProject, String name) {
		super(textProject, name);
		this.definition = definition;

	}

	public boolean shouldOverwriteFiles() {
		return definition.overwriteFiles();
	}

	public boolean shouldClean() {
		return definition.cleanDirectories();
	}

	@Override
	public SourceFolder getSourceFolder() {
		return this;
	}

	@Override
	public String getProjectRelativePath() {
		StringBuilder sb = new StringBuilder();
		sb.append("/");
		sb.append(getName());
		return sb.substring(1);
	}

	public TextProject getProject() {
		return (TextProject) getParent();
	}

	public boolean isRegenerated() {
		return isRegenerated;
	}

	public void setRegenerated(boolean isRegenerated) {
		this.isRegenerated = isRegenerated;
	}
}
