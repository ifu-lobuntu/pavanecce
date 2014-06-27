package org.pavanecce.uml.test.uml2code.ocl;

import org.junit.BeforeClass;
import org.pavanecce.common.test.util.ConstructionCaseExample;
import org.pavanecce.uml.uml2code.java.JavaCodeGenerator;

public class JavaSelectTests extends AbstractSelectTests {
	@BeforeClass
	public static void before() throws Exception {
		example = new ConstructionCaseExample("JavaSelects");
		addOcl();
		example.generateCode(new JavaCodeGenerator());
		example.initScriptingEngine();
		eval("ConstructionCase=Packages.test.ConstructionCase;");
		eval("HousePlan=Packages.test.HousePlan;");
		eval("WallPlan=Packages.test.WallPlan;");
		eval("RoomPlan=Packages.test.RoomPlan;");
	}
}
