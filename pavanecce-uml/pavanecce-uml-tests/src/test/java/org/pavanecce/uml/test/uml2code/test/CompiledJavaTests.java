package org.pavanecce.uml.test.uml2code.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;

import org.pavanecce.common.code.metamodel.CodeClass;
import org.pavanecce.common.code.metamodel.CodeModel;
import org.pavanecce.uml.uml2code.AbstractCodeGenerator;
import org.pavanecce.uml.uml2code.java.JavaCodeGenerator;

/**
 * As a User, I would like to be able to define my own types so that they can be used in many different places in the
 * system
 * 
 * Crap get rid of this and the direct dependencies on javassist it breaks the Jython tests by interfering with the
 * classLoader
 */
@Deprecated
public class CompiledJavaTests extends AbstractModelBuilderTest {
	AbstractCodeGenerator jg = new JavaCodeGenerator();
	CtClassBuilder ctClassBuilder = new CtClassBuilder(jg);

	public void asastesatIt() throws Exception {
		adaptor.startVisiting(builder, model);
		CodeModel codeModel = adaptor.getCodeModel();
		CodeClass emptyClass = (CodeClass) codeModel.getDescendent("model", "pkg1", "EmptyClass");
		CodeClass theClass = (CodeClass) codeModel.getDescendent("model", "pkg2", "TheClass");
		ctClassBuilder.buildCtClass(emptyClass);
		CtClass ctEmptyClass = ctClassBuilder.findOrCreateCtClass(jg.toQualifiedName(emptyClass));
		CtMethod test1 = CtNewMethod.make("private void test1(){}", ctEmptyClass);
		ctEmptyClass.addMethod(test1);
		CtMethod test2 = CtNewMethod.make("private void test2(){test1();}", ctEmptyClass);
		ctEmptyClass.addMethod(test2);
		ctEmptyClass.removeMethod(test1);
		ctClassBuilder.buildCtClass(theClass);
		ctClassBuilder.addMethodSources(emptyClass);
		test1 = CtNewMethod.make("private String test1(){return getName();}", ctEmptyClass);
		ctClassBuilder.addMethodSources(theClass);
		ctClassBuilder.compile();
		Class<?> javaTheClass = ctClassBuilder.loadClass(jg.toQualifiedName(theClass));
		assertEquals("model.pkg2.TheClass", javaTheClass.getName());
		Object theClassInstance = javaTheClass.newInstance();
		assertNotNull(javaTheClass.getDeclaredField("simpleClass"));
		Class<?> javaEmptyClass = ctClassBuilder.loadClass(jg.toQualifiedName(emptyClass));
		assertEquals("model.pkg1.EmptyClass", javaEmptyClass.getName());
		Object emptyClassInstance = javaEmptyClass.newInstance();
		Field nameField = javaEmptyClass.getDeclaredField("name");
		assertNotNull(nameField);
		assertSame(String.class, nameField.getType());
		Field theClassField = javaEmptyClass.getDeclaredField("theClass");
		assertNotNull(theClassField);
		assertSame(javaTheClass, theClassField.getType());
		Method setTheClass = javaEmptyClass.getDeclaredMethod("setTheClass", javaTheClass);
		assertSame(setTheClass.getParameterTypes()[0], theClassField.getType());
		setTheClass.invoke(emptyClassInstance, theClassInstance);
		Method getTheClass = javaEmptyClass.getDeclaredMethod("getTheClass");
		assertSame(theClassInstance, getTheClass.invoke(emptyClassInstance));

	}

}
