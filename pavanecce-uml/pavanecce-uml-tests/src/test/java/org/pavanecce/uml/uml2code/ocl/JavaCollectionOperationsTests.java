package org.pavanecce.uml.uml2code.ocl;

import org.junit.BeforeClass;
import org.pavanecce.common.util.ConstructionCaseExample;

public class JavaCollectionOperationsTests extends AbstractCollectionOperationsTests {
	@BeforeClass
	public static void before() throws Exception {
		example = new ConstructionCaseExample("JavaCollectionOperations");
		addOcl();
		JavaTestInit.initJava();
	}
}
