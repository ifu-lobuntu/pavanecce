package org.pavanecce.uml.common.util.emulated;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.ocl.uml.UMLEnvironment;

public class DefaultParentOclEnvironment extends UMLEnvironment{
	protected OclLibrary library;
	public DefaultParentOclEnvironment(ResourceSet rst){
		
		super(rst.getPackageRegistry(),rst);
		this.library=new OclLibrary(rst, getOCLStandardLibrary());
	}
	public OclLibrary getLibrary(){
		return library;
	}

}