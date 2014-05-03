package org.pavanecce.uml.uml2code.ocl;

import javax.script.ScriptException;

import org.junit.AfterClass;
import org.junit.Assert;
import org.pavanecce.common.util.ConstructionCaseExample;

public abstract class AbstractOclTest extends Assert {
	protected static ConstructionCaseExample example;

	public AbstractOclTest() {
		super();
	}

	@AfterClass
	public static void after() {
		example.after();
	}

	protected static Object eval(String s) throws ScriptException {
		return example.getJavaScriptEngine().eval(s);
	}

	public void assertEquals(int i, Object val) {
		if (val instanceof Number) {
			assertEquals(i, ((Number) val).intValue());
		}else{
			super.assertEquals(i, val);
		}
	}
	

}
