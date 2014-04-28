package org.pavanecce.common.util;

import org.pavanecce.uml.reverse.java.IProgressMonitor;

public class DummyProgressMonitor implements IProgressMonitor {

	@Override
	public void beginTask(String string, int size) {
		System.out.println("DummyProgressMonitor.beginTask("+string+","+size +" )");
	}

	@Override
	public void worked(int i) {
//		System.out.println("DummyProgressMonitor.worked("+i+")");
	}

	@Override
	public void done() {
		System.out.println("DummyProgressMonitor.done()");

	}

	@Override
	public boolean isCanceled() {
		System.out.println("DummyProgressMonitor.isCanceled()");
		return false;
	}

}
