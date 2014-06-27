package org.pavanecce.uml.test.uml2code.test;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.StringReader;

import org.junit.Test;
import org.pavanecce.uml.uml2code.AbstractCodeGenerator;
import org.pavanecce.uml.uml2code.java.JavaCodeGenerator;

/**
 * 
 * Tests generation of Java code from CodeXXX objects directly. No underlying UML model is being used.
 * 
 */
public class JavaSourceTests extends AbstractSourceTests {
	AbstractCodeGenerator jg = new JavaCodeGenerator();

	@Test
	public void testSimpleClass() throws Exception {
		BufferedReader r = new BufferedReader(new StringReader(jg.toClassifierDeclaration(simpleClass)));
		assertEquals("package model.pkg1;", r.readLine());
		assertEquals("import java.util.Set;", r.readLine());
		assertEquals("import java.util.HashSet;", r.readLine());
		assertEquals("import model.pkg2.TheClass;", r.readLine());
		assertEquals("public class EmptyClass{", r.readLine());
		assertEquals("  private String name = \"\";", r.readLine());
		assertEquals("  private TheClass theClass = null;", r.readLine());
		assertEquals("  public String getName(){", r.readLine());
		assertEquals("    String result = this.name;", r.readLine());
		assertEquals("    return result;", r.readLine());
		assertEquals("  }", r.readLine());
		assertEquals("  public TheClass getTheClass(){", r.readLine());
		assertEquals("    TheClass result = this.theClass;", r.readLine());
		assertEquals("    return result;", r.readLine());
		assertEquals("  }", r.readLine());
		assertEquals("  public Set<TheClass> manyParameters(Set<TheClass> param1, String param2, TheClass param3){", r.readLine());
		assertEquals("    Set<TheClass> result = new HashSet<TheClass>();", r.readLine());
		assertEquals("    return result;", r.readLine());
		assertEquals("  }", r.readLine());
		assertEquals("  public void setName(String newName){", r.readLine());
		assertEquals("    this.name = newName;", r.readLine());
		assertEquals("  }", r.readLine());
		assertEquals("  public void setTheClass(TheClass newTheClass){", r.readLine());
		assertEquals("    this.theClass = newTheClass;", r.readLine());
		assertEquals("  }", r.readLine());
		assertEquals("}", r.readLine());
	}

	@Test
	public void testInterface() throws Exception {
		BufferedReader r = new BufferedReader(new StringReader(jg.toClassifierDeclaration(simpleInterface)));
		assertEquals("package model.pkg1;", r.readLine());
		assertEquals("import java.util.Set;", r.readLine());
		assertEquals("import model.pkg2.TheClass;", r.readLine());
		assertEquals("public interface SimpleInterface{", r.readLine());
		assertEquals("  public static final String NAME = \"\";", r.readLine());
		assertEquals("  public static final TheClass THE_CLASS = new TheClass();", r.readLine());
		assertEquals("  public String doSomethingBoring();", r.readLine());
		assertEquals("  public Set<TheClass> doSomethingInteresting(Set<TheClass> param1, String param2, TheClass param3);", r.readLine());
		assertEquals("}", r.readLine());
	}
}
