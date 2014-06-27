package org.pavanecce.uml.test.uml2code.test;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.StringReader;

import org.junit.Test;
import org.pavanecce.uml.uml2code.python.PythonCodeGenerator;

/**
 * 
 * Tests generation of Java code from CodeXXX objects directly. No underlying UML model is being used.
 * 
 */
public class PythonSourceTests extends AbstractSourceTests {
	PythonCodeGenerator pg = new PythonCodeGenerator();

	@Test
	public void testSimpleClass() throws Exception {
		BufferedReader r = new BufferedReader(new StringReader(pg.getModuleDefinition(simpleClass.getPackage())));
		assertEquals("class EmptyClass():", r.readLine());
		assertEquals("  def __init__(self):", r.readLine());
		assertEquals("    self.name = \"\"", r.readLine());
		assertEquals("    self.theClass = None", r.readLine());
		assertEquals("", r.readLine());
		assertEquals("  def getName(self):", r.readLine());
		assertEquals("    result = self.name", r.readLine());
		assertEquals("    return result", r.readLine());
		assertEquals("", r.readLine());
		assertEquals("  def getTheClass(self):", r.readLine());
		assertEquals("    result = self.theClass", r.readLine());
		assertEquals("    return result", r.readLine());
		assertEquals("", r.readLine());
		assertEquals("  def manyParameters(self, param1, param2, param3):", r.readLine());
		assertEquals("    result = []", r.readLine());
		assertEquals("    return result", r.readLine());
		assertEquals("", r.readLine());
		assertEquals("  def setName(self, newName):", r.readLine());
		assertEquals("    self.name = newName", r.readLine());
		assertEquals("", r.readLine());
		assertEquals("  def setTheClass(self, newTheClass):", r.readLine());
		assertEquals("    self.theClass = newTheClass", r.readLine());
		assertEquals("", r.readLine());
		assertEquals("", r.readLine());
		assertEquals("", r.readLine());
		assertEquals("class SimpleEnumeration():", r.readLine());
		// TODO/////...

	}
}
