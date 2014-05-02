package org.pavanecce.common.util;

import java.io.File;
import java.io.IOException;

public class FileUtil {

	public static void deleteAllChildren(File f) {
		File[] listFiles = f.listFiles();
		for (File file : listFiles) {
			if (file.isFile()) {
				file.delete();
			} else {
				deleteAllChildren(file);
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
				} else {
					System.out.println("deleted " + repo.getCanonicalPath());
				}
			}
		} catch (IOException e) {
			throw new RuntimeException("could not delete " + repo.toString(), e);
		}
	}
}
