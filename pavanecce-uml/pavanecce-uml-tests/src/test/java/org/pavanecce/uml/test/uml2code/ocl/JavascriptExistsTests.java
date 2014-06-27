package org.pavanecce.uml.test.uml2code.ocl;

import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.Set;

import javax.script.ScriptException;

import org.junit.BeforeClass;
import org.pavanecce.common.test.util.ConstructionCaseExample;
import org.pavanecce.uml.uml2code.javascript.JavaScriptGenerator;

public class JavascriptExistsTests extends AbstractExistsTests {
	@BeforeClass
	public static void before() throws Exception {
		example = new ConstructionCaseExample("JavascriptExists");
		addOcl();
		example.generateCode(new JavaScriptGenerator());
		example.initScriptingEngine();
		Set<File> newFiles = example.getTextFileGenerator().getNewFiles();
		evaluateResource("underscore.js");
		evaluateResource("backbone.js");

		for (File file : newFiles) {
			example.getJavaScriptEngine().eval(new FileReader(file));
		}
		// eval("ConstructionCase=Packages.test.ConstructionCase;");
		// eval("HousePlan=Packages.test.HousePlan;");
		// eval("WallPlan=Packages.test.WallPlan;");
		// eval("RoomPlan=Packages.test.RoomPlan;");
	}

	protected static void evaluateResource(String name2) throws ScriptException {
		example.getJavaScriptEngine().eval(new InputStreamReader(JavascriptExistsTests.class.getClassLoader().getResourceAsStream(name2)));
	}
}
