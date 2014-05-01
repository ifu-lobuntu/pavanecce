package org.pavanecce.uml.uml2code;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.util.Properties;

import org.junit.Test;
import org.pavanecce.common.code.metamodel.CodeModel;
import org.pavanecce.common.code.metamodel.CodePackage;
import org.pavanecce.uml.uml2code.python.PythonCodeGenerator;
import org.python.core.PyString;
import org.python.core.PySystemState;
import org.python.util.PythonInterpreter;

public class CompiledJythonTests extends AbstractModelBuilderTest {
	PythonCodeGenerator pg = new PythonCodeGenerator();
	private CodeModel cm;
	private File root;

	@Test
	public void testIt() throws Exception {
		root = new File("/tmp/pygen");
		root.mkdirs();
		adaptor.startVisiting(builder, model);
		this.cm = adaptor.getCodeModel();
		File modelDir = new File(root, "model");
		if (modelDir.exists()) {
			File[] listFiles = modelDir.listFiles();
			for (File file : listFiles) {
				file.delete();
			}
		}

		writeModule("model", "pkg1");
		writeModule("model", "pkg2");
		Properties props = new Properties();
		props.setProperty("python.path", "/tmp/pygen/");
		PythonInterpreter.initialize(System.getProperties(), props, new String[] { "" });
		PythonInterpreter pi = new PythonInterpreter(null, new PySystemState());
		pi.exec("from model.pkg1 import *");
		pi.exec("from model.pkg2 import *");
		pi.exec("obj1=EmptyClass()");
		pi.exec("obj1.setName(\"name1\")");
		assertEquals(new PyString("name1"), pi.eval("obj1.getName()"));
		pi.exec("obj2=TheClass()");
		pi.exec("obj2.setSimpleClass(obj1)");
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
