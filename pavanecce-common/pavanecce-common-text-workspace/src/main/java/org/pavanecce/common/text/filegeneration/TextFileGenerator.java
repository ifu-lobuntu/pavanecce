package org.pavanecce.common.text.filegeneration;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.pavanecce.common.text.workspace.SourceFolder;
import org.pavanecce.common.text.workspace.TextDirectory;
import org.pavanecce.common.text.workspace.TextFile;
import org.pavanecce.common.text.workspace.TextProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TextFileGenerator extends DefaultTextFileVisitor {
	Logger logger = LoggerFactory.getLogger(getClass());
	private Set<File> newFiles=new HashSet<File>();

	public TextFileGenerator(File mappedRoot) {
		initialize(mappedRoot);
	}

	@Override
	public void visitTextFileDirectory(TextDirectory textDir) {
		if (textDir instanceof SourceFolder && !((SourceFolder) textDir).isRegenerated()) {
			// do nothing,nocode generated into this folder
		} else {
			getDirectoryFor(textDir);
		}
	}

	private File getDirectoryFor(TextDirectory textDir) {
		try {

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

	@Override
	public void visitTextFile(TextFile textFile) throws IOException {
		if (textFile.hasContent()) {
			File dir = getDirectoryFor(textFile.getParent());
			dir.mkdirs();
			File osFile = new File(dir, textFile.getName());
			logger.info("writing " + osFile);
			if (!osFile.exists() || textFile.overwrite()) {
				FileWriter fw = new FileWriter(osFile);
				fw.write(textFile.getContent());
				fw.flush();
				fw.close();
				newFiles.add(osFile);
			}
		}
	}

	@Override
	public void visitTextProject(TextProject textProject) {
	}

	@Override
	public void visitSourceFolder(SourceFolder n) {
		visitTextFileDirectory(n);
		
	}

	public Set<File> getNewFiles() {
		return newFiles;
	}

}
