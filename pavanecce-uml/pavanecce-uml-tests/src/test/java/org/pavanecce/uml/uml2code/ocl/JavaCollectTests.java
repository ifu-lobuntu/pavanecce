package org.pavanecce.uml.uml2code.ocl;

import org.junit.BeforeClass;
import org.pavanecce.common.util.ConstructionCaseExample;

public class JavaCollectTests extends AbstractCollectTests {
	@BeforeClass
	public static void before() throws Exception {
		example = new ConstructionCaseExample("JavaCollects");
		addOcl();
		JavaTestInit.initJava();
	}
}
