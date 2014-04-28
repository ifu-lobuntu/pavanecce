package org.pavanecce.uml.uml2code;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.junit.Test;
import org.pavanecce.common.code.metamodel.CodeClass;
import org.pavanecce.common.code.metamodel.CodeModel;
import org.pavanecce.uml.uml2code.AbstractCodeGenerator;
import org.pavanecce.uml.uml2code.javascript.JavaScriptGenerator;

public class ExecutingJavascriptTests extends AbstractModelBuilderTest {
	AbstractCodeGenerator pg = new JavaScriptGenerator();
	ScriptEngineManager manager = new ScriptEngineManager();
	ScriptEngine engine = manager.getEngineByName("js");
	ScriptContext sctx = engine.getContext();
	private CodeModel cm;

	@Test
	public void testIt() throws Exception {
		adaptor.startVisiting(builder, model);
		this.cm = adaptor.getCodeModel();
		engine.getContext().setAttribute("console", System.out, ScriptContext.ENGINE_SCOPE);
		registerClass("EmptyClass", "pkg1", "model");
		registerClass("TheClass", "pkg2", "model");
		engine.eval("var obj1=new EmptyClass();");
		engine.eval("var obj2=new TheClass();");
		engine.eval("obj1.setName(\"name1\");");
		engine.eval("obj1.setTheClass(obj2);");
		engine.eval("console.println(\"asdfasdfasd\");");
		assertSame(engine.eval("obj2;"), engine.eval("obj1.getTheClass();"));
		assertEquals("name1", engine.eval("obj1.getName();"));
	}

	protected void registerClass(String string, String string2, String string3) throws ScriptException {
		CodeClass emptyClass = (CodeClass) cm.getDescendent(string3, string2, string);
		String js = pg.toClassifierDeclaration(emptyClass);
		System.out.println(js);
		engine.eval(js);
	}

}
