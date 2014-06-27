package org.pavanecce.uml.test.uml2code.test;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class TestBundle implements BundleActivator {
	public static Bundle bundle;

	@Override
	public void start(BundleContext context) throws Exception {
		bundle = context.getBundle();

	}

	@Override
	public void stop(BundleContext context) throws Exception {
		// TODO Auto-generated method stub

	}

}
