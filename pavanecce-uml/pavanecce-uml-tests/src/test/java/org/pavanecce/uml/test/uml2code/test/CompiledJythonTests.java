package org.pavanecce.uml.test.uml2code.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;

import org.junit.Before;
import org.junit.Test;
import org.pavanecce.common.code.metamodel.CodeModel;
import org.pavanecce.common.code.metamodel.CodePackage;
import org.pavanecce.common.util.FileUtil;
import org.pavanecce.uml.uml2code.python.PythonCodeGenerator;
import org.python.core.PyString;
import org.python.core.PySystemState;
import org.python.util.PythonInterpreter;

public class CompiledJythonTests extends AbstractModelBuilderTest {
	PythonCodeGenerator pg = new PythonCodeGenerator();
	private CodeModel cm;
	private File root;

	@Before
	public void before() {
		// PythonInterpreter.initialize(System.getProperties(), new Properties(), new String[] { "" });
	}

	@Test
	public void testIt() throws Exception {

		File home = new File(System.getProperty("user.home"));
		root = new File(home, "tmp/pygen");
		root.mkdirs();
		adaptor.startVisiting(builder, model);
		this.cm = adaptor.getCodeModel();
		File modelDir = new File(root, "model");
		modelDir.mkdirs();
		FileUtil.deleteAllChildren(modelDir);
		writeModule("model", "pkg1");
		writeModule("model", "pkg2");
		PythonInterpreter pi = new PythonInterpreter(null, new PySystemState());
		// pi.getSystemState().dont_write_bytecode=true;
		pi.exec("import sys");
		pi.exec("sys.path.insert(0,'" + root.getAbsolutePath() + "')");
		pi.exec("print(sys.path)");
		pi.exec("from model.pkg1 import *");
		pi.exec("from model.pkg2 import *");
		pi.exec("obj2=TheClass()");
		pi.exec("obj1=EmptyClass()");
		pi.exec("obj1.setName(\"name1\")");
		pi.exec("obj2.setSimpleClass(obj1)");
		assertEquals(new PyString("name1"), pi.eval("obj1.getName()"));
		assertSame(pi.eval("obj1"), pi.eval("obj2.getSimpleClass()"));
	}

	protected void writeModule(String dirName, String moduleName) throws Exception {
		File dir = new File(root, dirName);
		dir.mkdirs();
		if (dir.listFiles(new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				return pathname.getName().equals("__init__.py");
			}
		}).length == 0) {
			FileWriter fw = new FileWriter(new File(dir, "__init__.py"));
			fw.flush();
			fw.close();
		}
		FileWriter fw = new FileWriter(new File(dir, moduleName + ".py"));
		CodePackage emptyClass = cm.getChildren().get(dirName).getChildren().get(moduleName);
		String classDeclaration = pg.getModuleDefinition(emptyClass);
		fw.write(classDeclaration);
		fw.flush();
		fw.close();

	}
}
