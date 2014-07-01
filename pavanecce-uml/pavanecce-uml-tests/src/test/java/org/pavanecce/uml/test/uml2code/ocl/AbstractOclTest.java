package org.pavanecce.uml.test.uml2code.ocl;

import javax.script.ScriptException;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.junit.AfterClass;
import org.junit.Assert;
import org.pavanecce.common.test.util.ConstructionCaseExample;

public abstract class AbstractOclTest extends Assert {
	protected static ConstructionCaseExample example;

	public AbstractOclTest() {
		super();
	}

	@AfterClass
	public static void after() {
		example.after();
	}

	protected final static <T> EList<T> emptyList(Class<T> class1) {
		BasicEList<T> result = new BasicEList<T>();
		return result;
	}

	protected final static <T> EList<T> list(@SuppressWarnings("unchecked") T... t) {
		BasicEList<T> result = new BasicEList<T>();
		for (T t2 : t) {
			result.add(t2);
		}
		return result;
	}

	protected static Object eval(String s) throws ScriptException {
		return example.getJavaScriptEngine().eval(s);
	}

	public void assertEquals(int i, Object val) {
		if (val instanceof Number) {
			assertEquals(i, ((Number) val).intValue());
		} else {
			super.assertEquals(i, val);
		}
	}

}
