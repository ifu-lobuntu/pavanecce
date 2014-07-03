package org.pavanecce.uml.test.uml2code.ocl;

import javax.script.ScriptException;

import org.pavanecce.uml.uml2code.java.JavaCodeGenerator;

public class JavaTestInit {

	public static void initJava() throws Exception, ScriptException {
		AbstractOclTest.example.generateCode(new JavaCodeGenerator());
		AbstractOclTest.example.initScriptingEngine();
		AbstractOclTest.eval("ConstructionCase=Packages.test.ConstructionCase;");
		AbstractOclTest.eval("HousePlan=Packages.test.HousePlan;");
		AbstractOclTest.eval("House=Packages.test.House;");
		AbstractOclTest.eval("WallPlan=Packages.test.WallPlan;");
		AbstractOclTest.eval("RoomPlan=Packages.test.RoomPlan;");
		AbstractOclTest.eval("HouseStatus=Packages.test.HouseStatus;");
	}

}
