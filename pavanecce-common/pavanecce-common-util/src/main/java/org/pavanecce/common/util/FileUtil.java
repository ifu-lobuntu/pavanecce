package org.pavanecce.common.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

public class FileUtil {
	public static void deleteAllChildren(File f) {
		File[] listFiles = f.listFiles();
		for (File file : listFiles) {
			if (file.isFile()) {
				file.delete();
			} else {
				deleteAllChildren(file);
				file.delete();
			}
		}
	}

	public static void deleteRoot(File repo) {

		try {
			if (repo.exists()) {
				if (repo.isDirectory()) {
					deleteAllChildren(repo);
				}
				if (!repo.delete()) {
					throw new RuntimeException("could not delete " + repo.getCanonicalPath());
				}
			}
		} catch (IOException e) {
			throw new RuntimeException("could not delete " + repo.toString(), e);
		}
	}

	public static void write(File output, String text) {
		try {
			PrintWriter w = new PrintWriter(output);
			w.write(text);
			w.close();
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
		
	}
}
