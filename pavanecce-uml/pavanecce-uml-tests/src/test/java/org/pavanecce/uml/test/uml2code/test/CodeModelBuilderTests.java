package org.pavanecce.uml.test.uml2code.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Test;
import org.pavanecce.common.code.metamodel.CodeBehaviour;
import org.pavanecce.common.code.metamodel.CodeClass;
import org.pavanecce.common.code.metamodel.CodeMethod;
import org.pavanecce.common.code.metamodel.CodeModel;
import org.pavanecce.common.code.metamodel.CodePackage;
import org.pavanecce.common.code.metamodel.CodeTypeReference;

public class CodeModelBuilderTests extends AbstractModelBuilderTest {
	@Test
	public void testPackagesAndClasses() {
		adaptor.startVisiting(builder, model);
		CodeModel codeModel = adaptor.getCodeModel();
		CodePackage modelPackage = codeModel.getChildren().get("model");
		assertNotNull(modelPackage);
		assertEquals("model", modelPackage.getName());
		CodePackage pkg1 = modelPackage.getChildren().get("pkg1");
		assertNotNull(pkg1);
		assertEquals("pkg1", pkg1.getName());
		CodeClass emptyClass = (CodeClass) pkg1.getClassifiers().get("EmptyClass");
		assertNotNull(emptyClass);
		assertEquals("EmptyClass", emptyClass.getName());
		assertNotNull(emptyClass.getFields().get("name"));
		CodePackage pkg2 = modelPackage.getChildren().get("pkg2");
		assertNotNull(pkg2);
		assertEquals("pkg2", pkg2.getName());
		CodeClass theClass = (CodeClass) pkg2.getClassifiers().get("TheClass");
		assertNotNull(theClass);
		assertEquals("TheClass", theClass.getName());
		assertNotNull(theClass.getFields().get("simpleClass"));
		assertNotNull(theClass.getMethods().get(CodeBehaviour.generateIdentifier("getSimpleClass", new ArrayList<CodeTypeReference>())));
		assertNotNull(theClass.getMethods().get(CodeBehaviour.generateIdentifier("setSimpleClass", Arrays.asList(emptyClass.getPathName()))));
		CodeMethod myOper = emptyClass.getMethods().get(CodeBehaviour.generateIdentifier("myOper", Arrays.asList(emptyClass.getPathName())));
		assertEquals(emptyClass.getPathName(), myOper.getReturnType());
	}
}
