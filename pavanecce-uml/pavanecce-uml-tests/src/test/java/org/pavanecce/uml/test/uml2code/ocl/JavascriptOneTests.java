package org.pavanecce.uml.test.uml2code.ocl;

import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.Set;

import javax.script.ScriptException;

import org.junit.BeforeClass;
import org.pavanecce.common.test.util.ConstructionCaseExample;
import org.pavanecce.uml.uml2code.javascript.JavaScriptGenerator;

public class JavascriptOneTests extends AbstractOneTests {
	@BeforeClass
	public static void before() throws Exception {
		example = new ConstructionCaseExample("JavascriptOne");
		addOcl();
		example.generateCode(new JavaScriptGenerator());
		example.initScriptingEngine();
		Set<File> newFiles = example.getTextFileGenerator().getNewFiles();
		evaluateResource("underscore.js");
		evaluateResource("backbone.js");
		for (File file : newFiles) {
			example.getJavaScriptEngine().eval(new FileReader(file));
		}
	}

	protected static void evaluateResource(String name2) throws ScriptException {
		example.getJavaScriptEngine().eval(new InputStreamReader(JavascriptOneTests.class.getClassLoader().getResourceAsStream(name2)));
	}
}
