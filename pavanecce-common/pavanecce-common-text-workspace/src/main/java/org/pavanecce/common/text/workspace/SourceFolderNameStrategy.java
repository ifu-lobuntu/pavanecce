package org.pavanecce.common.text.workspace;

public enum SourceFolderNameStrategy {
	MODEL_NAME {

		@Override
		public String sourceFolderName(String sourceFolderQualifier, String modelIdentifier) {
			return modelIdentifier;
		}

		@Override
		public boolean isOneProjectPerWorkspace() {
			return true;
		}

		@Override
		public boolean definesSeparateSourceFoldersForModels() {
			return true;
		}
	},
	QUALIFIER_ONLY {

		@Override
		public String sourceFolderName(String sourceFolderQualifier, String modelIdentifier) {
			return sourceFolderQualifier;
		}

		@Override
		public boolean isOneProjectPerWorkspace() {
			return true;
		}

		@Override
		public boolean definesSeparateSourceFoldersForModels() {
			return false;
		}
	},
	MODEL_NAME_AND_PREFIX {

		@Override
		public String sourceFolderName(String sourceFolderQualifier, String modelIdentifier) {
			return sourceFolderQualifier  + modelIdentifier;
		}

		@Override
		public boolean isOneProjectPerWorkspace() {
			return true;
		}

		@Override
		public boolean definesSeparateSourceFoldersForModels() {
			return false;
		}
	},
	MODEL_NAME_AND_SUFFIX {
		@Override
		public String sourceFolderName(String sourceFolderQualifier, String modelIdentifier) {
			return modelIdentifier + sourceFolderQualifier;
		}

		@Override
		public boolean isOneProjectPerWorkspace() {
			return true;
		}

		@Override
		public boolean definesSeparateSourceFoldersForModels() {
			return true;
		}
	};
	public abstract String sourceFolderName(String sourceFolderQualifier, String modelIdentifier);

	public abstract boolean isOneProjectPerWorkspace();

	public abstract boolean definesSeparateSourceFoldersForModels();
}
