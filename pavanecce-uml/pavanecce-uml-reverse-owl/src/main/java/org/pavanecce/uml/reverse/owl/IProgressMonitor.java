package org.pavanecce.uml.reverse.owl;

public interface IProgressMonitor {

	void beginTask(String string, int size);

	void worked(int i);

	void done();

	boolean isCanceled();

}
