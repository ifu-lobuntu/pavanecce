package org.pavanecce.uml.common.util.emulated;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.ocl.uml.UMLEnvironment;

public class DefaultParentOclEnvironment extends UMLEnvironment {
	protected OclRuntimeLibrary library;

	public DefaultParentOclEnvironment(ResourceSet rst) {

		super(rst.getPackageRegistry(), rst);
		this.library = new OclRuntimeLibrary(rst, getOCLStandardLibrary());
	}

	public OclRuntimeLibrary getLibrary() {
		return library;
	}

}