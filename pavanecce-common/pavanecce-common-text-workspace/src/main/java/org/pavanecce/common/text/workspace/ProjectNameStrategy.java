package org.pavanecce.common.text.workspace;

public enum ProjectNameStrategy {
	WORKSPACE_NAME_AND_SUFFIX {

		@Override
		public String generateProjectName(String projectQualifier, String workspaceIdentifier, String qualifiedWorkspaceIdentifier, String modelIdentifier) {
			return workspaceIdentifier + projectQualifier;
		}

		@Override
		public boolean isOneProjectPerWorkspace() {
			return true;
		}

	},
	MODEL_NAME_AND_SUFFIX {

		@Override
		public String generateProjectName(String projectQualifier, String workspaceIdentifier, String qualifiedWorkspaceIdentifier, String modelIdentifier) {
			return modelIdentifier + projectQualifier;
		}

		@Override
		public boolean isOneProjectPerWorkspace() {
			return false;
		}

	},
	SUFFIX_ONLY {

		@Override
		public String generateProjectName(String projectQualifier, String workspaceIdentifier, String qualifiedWorkspaceIdentifier, String modelIdentifier) {
			return projectQualifier;
		}

		@Override
		public boolean isOneProjectPerWorkspace() {
			return true;
		}

	},
	QUALIFIED_WORKSPACE_NAME_AND_SUFFIX {

		@Override
		public String generateProjectName(String projectQualifier, String workspaceIdentifier, String qualifiedWorkspaceIdentifier, String modelIdentifier) {
			return qualifiedWorkspaceIdentifier + projectQualifier;
		}

		@Override
		public boolean isOneProjectPerWorkspace() {
			return false;
		}

	};
	public abstract String generateProjectName(String projectQualifier, String workspaceIdentifier, String qualifiedWorkspaceIdentifier, String modelIdentifier);

	public abstract boolean isOneProjectPerWorkspace();

}
