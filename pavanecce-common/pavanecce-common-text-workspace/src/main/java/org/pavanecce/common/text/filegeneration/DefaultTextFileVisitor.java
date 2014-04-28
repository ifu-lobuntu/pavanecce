package org.pavanecce.common.text.filegeneration;

import java.io.File;

import org.pavanecce.common.text.workspace.SourceFolder;
import org.pavanecce.common.text.workspace.TextDirectory;
import org.pavanecce.common.text.workspace.TextFile;
import org.pavanecce.common.text.workspace.TextProject;

public abstract class DefaultTextFileVisitor {

	protected File mappedRoot;

	public DefaultTextFileVisitor() {
		super();
	}

	protected void initialize(File mappedRoot) {
		if (!mappedRoot.exists()) {
			mappedRoot.mkdirs();
		}
		this.mappedRoot = mappedRoot;
	}

	public abstract void visitTextFile(TextFile textFile) throws Exception;

	public abstract void visitTextFileDirectory(TextDirectory textDir) throws Exception;

	public abstract void visitTextProject(TextProject textProject);

	public abstract void visitSourceFolder(SourceFolder n);

}