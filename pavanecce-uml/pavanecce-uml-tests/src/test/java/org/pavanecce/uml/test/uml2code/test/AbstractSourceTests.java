package org.pavanecce.uml.test.uml2code.test;

import java.util.Collections;
import java.util.Map;

import org.junit.Before;
import org.pavanecce.common.code.metamodel.CodeClass;
import org.pavanecce.common.code.metamodel.CodeCollectionKind;
import org.pavanecce.common.code.metamodel.CodeEnumeration;
import org.pavanecce.common.code.metamodel.CodeEnumerationLiteral;
import org.pavanecce.common.code.metamodel.CodeField;
import org.pavanecce.common.code.metamodel.CodeInterface;
import org.pavanecce.common.code.metamodel.CodeMethod;
import org.pavanecce.common.code.metamodel.CodeModel;
import org.pavanecce.common.code.metamodel.CodePackage;
import org.pavanecce.common.code.metamodel.CodePackageReference;
import org.pavanecce.common.code.metamodel.CodeParameter;
import org.pavanecce.common.code.metamodel.CodePrimitiveTypeKind;
import org.pavanecce.common.code.metamodel.CodeTypeReference;
import org.pavanecce.common.code.metamodel.CodeVisibilityKind;
import org.pavanecce.common.code.metamodel.CollectionTypeReference;
import org.pavanecce.common.code.metamodel.PrimitiveTypeReference;
import org.pavanecce.common.code.metamodel.expressions.NewInstanceExpression;
import org.pavanecce.common.code.metamodel.statements.PortableStatement;

public class AbstractSourceTests {
	protected CodeModel codeModel;
	protected CodeClass simpleClass;
	protected CodeInterface simpleInterface;
	protected CodeEnumeration simpleEnumeration;

	@Before
	public void setup() {
		this.codeModel = new CodeModel();
		CodePackage pkg1 = codeModel.findOrCreatePackage("model").findOrCreatePackage("pkg1");
		Map<String, String> em = Collections.emptyMap();
		pkg1.setPackageReference(new CodePackageReference(new CodePackageReference(null, "model", em), "pkg1", em));
		populateSimpleClass(pkg1);
		populateSimpleInterface(pkg1);
		populateSimpleEnumeration(pkg1);

	}

	protected void populateSimpleClass(CodePackage pkg1) {
		this.simpleClass = new CodeClass("EmptyClass", pkg1);
		PrimitiveTypeReference string = new PrimitiveTypeReference(CodePrimitiveTypeKind.STRING);
		new CodeField(simpleClass, "name", string);
		CodeTypeReference theClass = new CodeTypeReference(true, "model", "pkg2", "TheClass");
		new CodeField(simpleClass, "theClass", theClass);
		CodeMethod getName = new CodeMethod(simpleClass, "getName", string);
		getName.setResultInitialValue("${self}.name");
		CodeMethod getTheClass = new CodeMethod(simpleClass, "getTheClass", theClass);
		getTheClass.setResultInitialValue("${self}.theClass");
		CodeMethod setName = new CodeMethod("setName");
		new CodeParameter("newName", setName, string);
		setName.setDeclaringClass(simpleClass);
		setName.getBody().getStatements().add(new PortableStatement("${self}.name = newName"));
		CodeMethod setTheClass = new CodeMethod("setTheClass");
		new CodeParameter("newTheClass", setTheClass, theClass);
		setTheClass.setDeclaringClass(simpleClass);
		setTheClass.getBody().getStatements().add(new PortableStatement("${self}.theClass = newTheClass"));
		CodeMethod manyParameters = new CodeMethod("manyParameters");
		CollectionTypeReference setOfTheClass = new CollectionTypeReference(CodeCollectionKind.SET);
		setOfTheClass.addToElementTypes(theClass);
		new CodeParameter("param1", manyParameters, setOfTheClass);
		new CodeParameter("param2", manyParameters, new PrimitiveTypeReference(CodePrimitiveTypeKind.STRING));
		new CodeParameter("param3", manyParameters, theClass);
		manyParameters.setReturnType(setOfTheClass);
		manyParameters.setResultInitialValue(new NewInstanceExpression(setOfTheClass));
		manyParameters.setDeclaringClass(simpleClass);
	}

	protected void populateSimpleInterface(CodePackage pkg1) {
		this.simpleInterface = new CodeInterface("SimpleInterface", pkg1);
		PrimitiveTypeReference string = new PrimitiveTypeReference(CodePrimitiveTypeKind.STRING);
		CodeField nameField = new CodeField(simpleInterface, "NAME", string);
		nameField.setStatic(true);
		nameField.setConstant(true);
		nameField.setVisibility(CodeVisibilityKind.PUBLIC);
		CodeTypeReference theClass = new CodeTypeReference(true, "model", "pkg2", "TheClass");
		CodeField codeField = new CodeField(simpleInterface, "THE_CLASS", theClass);
		codeField.setStatic(true);
		codeField.setConstant(true);
		codeField.setVisibility(CodeVisibilityKind.PUBLIC);
		codeField.setInitialization(new NewInstanceExpression(theClass));
		new CodeMethod(simpleInterface, "doSomethingBoring", string);
		CodeMethod doSomethingInteresting = new CodeMethod("doSomethingInteresting");
		CollectionTypeReference setOfTheClass = new CollectionTypeReference(CodeCollectionKind.SET);
		setOfTheClass.addToElementTypes(theClass);
		new CodeParameter("param1", doSomethingInteresting, setOfTheClass);
		new CodeParameter("param2", doSomethingInteresting, new PrimitiveTypeReference(CodePrimitiveTypeKind.STRING));
		new CodeParameter("param3", doSomethingInteresting, theClass);
		doSomethingInteresting.setReturnType(setOfTheClass);
		doSomethingInteresting.setDeclaringClass(simpleInterface);
	}

	protected void populateSimpleEnumeration(CodePackage pkg1) {
		this.simpleEnumeration = new CodeEnumeration("SimpleEnumeration", pkg1);
		new CodeEnumerationLiteral(simpleEnumeration, "CONST1");
		new CodeEnumerationLiteral(simpleEnumeration, "CONST2");
		PrimitiveTypeReference string = new PrimitiveTypeReference(CodePrimitiveTypeKind.STRING);
		new CodeField(simpleEnumeration, "name", string);
		CodeTypeReference theClass = new CodeTypeReference(true, "model", "pkg2", "TheClass");
		CodeMethod doSomethingInteresting = new CodeMethod("doSomethingInteresting");
		CollectionTypeReference setOfTheClass = new CollectionTypeReference(CodeCollectionKind.SET);
		setOfTheClass.addToElementTypes(theClass);
		new CodeParameter("param1", doSomethingInteresting, setOfTheClass);
		new CodeParameter("param2", doSomethingInteresting, new PrimitiveTypeReference(CodePrimitiveTypeKind.STRING));
		new CodeParameter("param3", doSomethingInteresting, theClass);
		doSomethingInteresting.setReturnType(setOfTheClass);
		doSomethingInteresting.setDeclaringClass(simpleEnumeration);
	}
}
