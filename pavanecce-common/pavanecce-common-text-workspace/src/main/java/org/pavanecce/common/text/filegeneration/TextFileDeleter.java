package org.pavanecce.common.text.filegeneration;

import java.io.File;
import java.io.IOException;

import org.pavanecce.common.text.workspace.SourceFolder;
import org.pavanecce.common.text.workspace.TextDirectory;
import org.pavanecce.common.text.workspace.TextFile;
import org.pavanecce.common.text.workspace.TextProject;

public class TextFileDeleter extends DefaultTextFileVisitor {

	public TextFileDeleter(File mappedRoot) {
		initialize(mappedRoot);
	}

	public void visitTextFileDirectory(TextDirectory textDir) {
		if (textDir instanceof SourceFolder && !((SourceFolder) textDir).isRegenerated()) {
			// Do nothing - no new code generated into this folder
		} else {
			File dir = getDirectoryFor(textDir);
			if (!dir.exists()) {
				if (textDir.hasContent()) {
					dir.mkdir();
				}
			} else {
				for (File child : dir.listFiles()) {
					if (textDir.getSourceFolder().shouldClean() && !textDir.hasChild(child.getName()) && isSourceDirectory(child)) {
						deleteTree(child);
					}
				}
			}
		}
	}

	private boolean isSourceDirectory(File child) {
		boolean b = !child.isHidden() && !child.getName().startsWith(".");
		return b;
	}

	private File getDirectoryFor(TextDirectory textDir) {
		try {
			if (!mappedRoot.exists()) {
				mappedRoot.mkdirs();
			}
			TextProject textProject = (TextProject) textDir.getSourceFolder().getParent();
			File projectDir = new File(mappedRoot, textProject.getName());
			if (!projectDir.exists() && textProject.hasContent()) {
				projectDir.mkdirs();
			}
			File dir = new File(projectDir, textDir.getProjectRelativePath());
			return dir;
		} catch (RuntimeException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	private void deleteTree(File tree) {
		if (tree.isDirectory()) {
			for (File f : tree.listFiles()) {
				if (!f.getName().startsWith(".")) {
					// don't mess around with eclipse internal files
					if (f.isFile() || (f.isDirectory() && f.list().length == 0)) {
						try {
							f.delete();
						} catch (Exception e) {
						}
					} else {
						deleteTree(f);
					}
				}
			}
		}
		try {
			tree.delete();
		} catch (Exception e) {
		}
	}

	@Override
	public void visitTextFile(TextFile textFile) throws IOException {
	}

	@Override
	public void visitTextProject(TextProject textProject) {
	}

	@Override
	public void visitSourceFolder(SourceFolder n) {
		visitTextFileDirectory(n);
	}
}
