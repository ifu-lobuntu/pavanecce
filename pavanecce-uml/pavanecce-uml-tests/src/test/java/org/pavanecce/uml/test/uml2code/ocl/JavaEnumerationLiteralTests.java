package org.pavanecce.uml.test.uml2code.ocl;

import org.junit.BeforeClass;
import org.pavanecce.common.test.util.ConstructionCaseExample;

public class JavaEnumerationLiteralTests extends AbstractEnumerationLiteralTests {
	@BeforeClass
	public static void before() throws Exception {
		example = new ConstructionCaseExample("JavaEnumerationLiterals");
		addOcl();
		JavaTestInit.initJava();
	}
}