package org.pavanecce.uml.test.uml2code.ocl;

import org.junit.BeforeClass;
import org.pavanecce.common.test.util.ConstructionCaseExample;

public class JavaCollectionOperationsTests extends AbstractCollectionOperationsTests {
	@BeforeClass
	public static void before() throws Exception {
		example = new ConstructionCaseExample("JavaCollectionOperations");
		addOcl();
		JavaTestInit.initJava();
	}
}
