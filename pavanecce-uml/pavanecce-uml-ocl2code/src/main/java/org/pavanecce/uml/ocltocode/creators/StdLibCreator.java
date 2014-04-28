package org.pavanecce.uml.ocltocode.creators;

import org.pavanecce.common.code.metamodel.CodeClass;
import org.pavanecce.common.code.metamodel.CodePackage;


public class StdLibCreator {
	private String stdLibClassName = "OclCollections";
	private CodeClass stdlibCls = null;

	public StdLibCreator() {
		super();
	}

	public CodeClass makeStdLib(CodePackage utilPack) {
		if (utilPack == null) {
			return null;
		}
		// create the stdlibCls
		stdlibCls = new CodeClass(stdLibClassName, utilPack);
		// set the basic attributes of the stdlibCls
		return stdlibCls;
	}



}
