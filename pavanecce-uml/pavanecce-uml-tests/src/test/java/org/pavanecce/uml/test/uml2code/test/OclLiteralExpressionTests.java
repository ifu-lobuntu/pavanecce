package org.pavanecce.uml.test.uml2code.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;

import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.LiteralUnlimitedNatural;
import org.eclipse.uml2.uml.OpaqueExpression;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.UMLFactory;
import org.eclipse.uml2.uml.UMLPackage;
import org.junit.Test;
import org.pavanecce.common.code.metamodel.CodeClass;
import org.pavanecce.common.code.metamodel.CodeExpression;
import org.pavanecce.common.code.metamodel.CodeMethod;
import org.pavanecce.common.code.metamodel.expressions.BinaryOperatorExpression;
import org.pavanecce.common.code.metamodel.expressions.MethodCallExpression;
import org.pavanecce.common.code.metamodel.expressions.PortableExpression;
import org.pavanecce.uml.uml2code.AbstractCodeGenerator;
import org.pavanecce.uml.uml2code.java.JavaCodeGenerator;

public class OclLiteralExpressionTests extends AbstractOcl2CodeModelTest {
	AbstractCodeGenerator jcg = new JavaCodeGenerator();

	@Test
	public void testPrimitiveLiterals() throws IOException {
		super.adaptor.startVisiting(builder, model);
		super.adaptor.startVisiting(oclCodeBuilder, model);
		CodeClass theClass = super.adaptor.getCodeModel().getDescendent("model", "pkg2", "TheClass");
		assertGetDefaultInteger(theClass);
		assertGetDefaultIntegers(theClass);

		assertGetDefaultString(theClass);
	}

	protected void assertGetDefaultString(CodeClass theClass) throws IOException {
		CodeMethod getDefaultString = theClass.getMethod("getDefaultString", Collections.emptyList());
		CodeExpression stringResult = getDefaultString.getResult();
		assertTrue(stringResult instanceof MethodCallExpression);
		MethodCallExpression concat = (MethodCallExpression) stringResult;
		assertEquals("OclPrimitives.concat", concat.getMethodName());
		assertEquals("\"asdf\"", ((PortableExpression) concat.getArguments().get(0)).getExpression());
		BufferedReader getDefaultStringBody = new BufferedReader(new StringReader(jcg.toMethodBody(getDefaultString)));
		assertEquals("    String result = OclPrimitives.concat(\"asdf\",\"asdfasdfs\");", getDefaultStringBody.readLine());
	}

	protected void assertGetDefaultInteger(CodeClass theClass) throws IOException {
		CodeMethod getDefaultInteger = theClass.getMethod("getDefaultInteger", Collections.emptyList());
		CodeExpression integerResult = getDefaultInteger.getResult();
		assertTrue(integerResult instanceof BinaryOperatorExpression);
		BinaryOperatorExpression boe = (BinaryOperatorExpression) integerResult;
		assertTrue(boe.getArg1() instanceof BinaryOperatorExpression);
		assertTrue(boe.getArg2() instanceof PortableExpression);
		PortableExpression arg2 = (PortableExpression) boe.getArg2();
		assertEquals("2", arg2.getExpression());
		BufferedReader getDefaultIntegerBody = new BufferedReader(new StringReader(jcg.toMethodBody(getDefaultInteger)));
		assertEquals("    Integer result = ( ( 2 + 3 ) / 2 );", getDefaultIntegerBody.readLine());
	}

	protected void assertGetDefaultIntegers(CodeClass theClass) throws IOException {
		CodeMethod getDefaultInteger = theClass.getMethod("getDefaultIntegers", Collections.emptyList());
		CodeExpression integerResult = getDefaultInteger.getResult();
		assertTrue(integerResult instanceof PortableExpression);
		CodeMethod collectionLiteral0 = theClass.getMethod("collectionLiteral0", Collections.emptyList());
		BufferedReader getDefaultIntegerBody = new BufferedReader(new StringReader(jcg.toMethodBody(collectionLiteral0)));
		assertEquals("    Set<Integer> result = new HashSet<Integer>();", getDefaultIntegerBody.readLine());
		assertEquals("    Integer value0 = 1;", getDefaultIntegerBody.readLine());
		assertEquals("    Integer value1 = ( 3 * 3 );", getDefaultIntegerBody.readLine());
		assertEquals("    Integer value2 = 5;", getDefaultIntegerBody.readLine());
		assertEquals("    if(!(value0 == null)){", getDefaultIntegerBody.readLine());
		assertEquals("      result.add( value0 );", getDefaultIntegerBody.readLine());
		assertEquals("    }", getDefaultIntegerBody.readLine());
		assertEquals("    if(!(value1 == null)){", getDefaultIntegerBody.readLine());
		assertEquals("      result.add( value1 );", getDefaultIntegerBody.readLine());
		assertEquals("    }", getDefaultIntegerBody.readLine());
		assertEquals("    if(!(value2 == null)){", getDefaultIntegerBody.readLine());
		assertEquals("      result.add( value2 );", getDefaultIntegerBody.readLine());
		assertEquals("    }", getDefaultIntegerBody.readLine());
		assertEquals("    return result;", getDefaultIntegerBody.readLine());
	}

	@Override
	protected void populateTheClass(Class theClass) {
		Property defaultString = theClass.getOwnedAttribute("defaultString", library.getStringType(), false, UMLPackage.eINSTANCE.getProperty(), true);
		OpaqueExpression oe = UMLFactory.eINSTANCE.createOpaqueExpression();
		oe.getLanguages().add("OCL");
		oe.getBodies().add("'asdf' + 'asdfasdfs'");
		defaultString.setDefaultValue(oe);
		defaultString.setIsDerived(true);
		Property defaultIntegers = theClass.getOwnedAttribute("defaultIntegers", library.getIntegerType(), false, UMLPackage.eINSTANCE.getProperty(), true);
		OpaqueExpression oe2 = UMLFactory.eINSTANCE.createOpaqueExpression();
		defaultIntegers.setUnlimitedNaturalDefaultValue(LiteralUnlimitedNatural.UNLIMITED);
		oe2.getLanguages().add("OCL");
		oe2.getBodies().add("Set{1,3*3,5}");
		defaultIntegers.setDefaultValue(oe2);
		defaultIntegers.setIsDerived(true);
		Property defaultInteger = theClass.getOwnedAttribute("defaultInteger", library.getIntegerType(), false, UMLPackage.eINSTANCE.getProperty(), true);
		OpaqueExpression oe3 = UMLFactory.eINSTANCE.createOpaqueExpression();
		oe3.getLanguages().add("OCL");
		oe3.getBodies().add("(2+3)/2");
		defaultInteger.setDefaultValue(oe3);
		defaultInteger.setIsDerived(true);
	}
}
