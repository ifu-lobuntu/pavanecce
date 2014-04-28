package org.pavanecce.uml.uml2code;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.StringReader;

import org.junit.Test;
import org.pavanecce.uml.uml2code.javascript.JavaScriptGenerator;

/**
 * 
 * Tests generation of JavaScript code from CodeXXX objects directly. No underlying
 * UML model is being used.
 * 
 */
public class JavaScriptSourceTests  extends AbstractSourceTests{
	JavaScriptGenerator jsg = new JavaScriptGenerator();

	@Test
	public void testSimpleClass() throws Exception {
		BufferedReader r = new BufferedReader(new StringReader(jsg.getModuleDefinition(simpleClass.getPackage())));
		assertEquals("function EmptyClass(){", r.readLine());
		assertEquals("  this.name = \"\";", r.readLine());
		assertEquals("  this.theClass = null;", r.readLine());
		assertEquals("  EmptyClass.prototype.getName = function(){", r.readLine());
		assertEquals("    var result = this.name;", r.readLine());
		assertEquals("    return result;", r.readLine());
		assertEquals("  }", r.readLine());
		assertEquals("  EmptyClass.prototype.getTheClass = function(){", r.readLine());
		assertEquals("    var result = this.theClass;", r.readLine());
		assertEquals("    return result;", r.readLine());
		assertEquals("  }", r.readLine());
		assertEquals("  EmptyClass.prototype.manyParameters = function(param1, param2, param3){", r.readLine());
		assertEquals("    var result = [];", r.readLine());
		assertEquals("    return result;", r.readLine());
		assertEquals("  }", r.readLine());
		assertEquals("  EmptyClass.prototype.setName = function(newName){", r.readLine());
		assertEquals("    this.name = newName;", r.readLine());
		assertEquals("  }", r.readLine());
		assertEquals("  EmptyClass.prototype.setTheClass = function(newTheClass){", r.readLine());
		assertEquals("    this.theClass = newTheClass;", r.readLine());
		assertEquals("  }", r.readLine());
		assertEquals("}", r.readLine());
	}
}
