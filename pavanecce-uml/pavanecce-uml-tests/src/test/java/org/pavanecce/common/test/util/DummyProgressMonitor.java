package org.pavanecce.common.test.util;

import org.pavanecce.uml.reverse.java.IProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DummyProgressMonitor implements IProgressMonitor {
	Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public void beginTask(String string, int size) {
		logger.info("DummyProgressMonitor.beginTask(" + string + "," + size + " )");
	}

	@Override
	public void worked(int i) {
		// logger.info(("DummyProgressMonitor.worked("+i+")");
	}

	@Override
	public void done() {
		logger.info("DummyProgressMonitor.done()");

	}

	@Override
	public boolean isCanceled() {
		logger.info("DummyProgressMonitor.isCanceled()");
		return false;
	}

}
