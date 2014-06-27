package org.pavanecce.uml.test.uml2code.test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.NotFoundException;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.SignatureAttribute;

import org.pavanecce.common.code.metamodel.CodeClass;
import org.pavanecce.common.code.metamodel.CodeElementType;
import org.pavanecce.common.code.metamodel.CodeField;
import org.pavanecce.common.code.metamodel.CodeMethod;
import org.pavanecce.common.code.metamodel.CodeParameter;
import org.pavanecce.common.code.metamodel.CodeTypeReference;
import org.pavanecce.uml.uml2code.AbstractCodeGenerator;

public class CtClassBuilder {
	Set<CtClass> generatedClasses = new HashSet<CtClass>();
	private Map<String, String> primitiveTypeSignatures = new HashMap<String, String>();
	{
		primitiveTypeSignatures.put("byte", "B"); // byte
		primitiveTypeSignatures.put("char", "C"); // char
		primitiveTypeSignatures.put("double", "D"); // double
		primitiveTypeSignatures.put("float", "F"); // float
		primitiveTypeSignatures.put("int", "I"); // int
		primitiveTypeSignatures.put("long", "J"); // long
		primitiveTypeSignatures.put("short", "S"); // short
		primitiveTypeSignatures.put("boolean", "Z"); // boolean
	}
	private ClassPool pool = new ClassPool() {
		{
			appendSystemPath();
		}

		@Override
		public ClassLoader getClassLoader() {
			return CtClassBuilder.this.getClass().getClassLoader();
		}
	};

	private AbstractCodeGenerator jg;

	public CtClassBuilder(AbstractCodeGenerator jg) {
		super();
		this.jg = jg;
	}

	public void buildCtClass(CodeClass codeClass) throws CannotCompileException {
		populateClass(codeClass, findOrCreateCtClass(jg.toQualifiedName(codeClass)));
	}

	public void createCtClass(CodeClass codeClass) throws CannotCompileException {
		CtClass ctClass = getPool().makeClass(jg.toQualifiedName(codeClass));
		generatedClasses.add(ctClass);
		populateClass(codeClass, ctClass);
	}

	private void populateClass(CodeClass codeClass, CtClass ctClass) throws CannotCompileException {
		for (CodeTypeReference codeTypeReference : codeClass.getImports()) {
			String qualifiedName = jg.toQualifiedName(codeTypeReference);
			getPool().importPackage(qualifiedName);
		}
		for (Entry<String, CodeField> entry : codeClass.getFields().entrySet()) {
			addField(ctClass, entry.getValue());
		}
		for (Entry<String, CodeMethod> entry : codeClass.getMethods().entrySet()) {
			addMethod(ctClass, entry.getValue());
		}
	}

	public void addMethodSources(CodeClass codeClass) throws CannotCompileException, NotFoundException {
		CtClass ctClass = findOrCreateCtClass(jg.toQualifiedName(codeClass));
		for (CodeMethod method : codeClass.getMethods().values()) {
			String signatureAsString = toSignatureString(method, false);
			CtMethod ctMethod = ctClass.getMethod(method.getName(), signatureAsString);
			if (ctMethod != null) {
				ctClass.removeMethod(ctMethod);
				ctClass.addMethod(CtNewMethod.make(jg.toMethodDeclaration(method).replaceAll("\\<\\w*\\>", ""), ctClass));
				// String bodyString = javaCodeGenerator.toMethodBody(method);
				// List<CodeParameter> parameters = method.getParameters();
				// for (int i = 0; i < parameters.size(); i++) {
				// CodeParameter codeParameter = parameters.get(i);
				// bodyString = bodyString.replaceAll("\\b" + codeParameter.getName() + "\\b", "\\$" + i);
				// }
				// logger.info((bodyString);
				// ctMethod.setBody("{" + bodyString + "}");
			}
		}
	}

	private String toSignatureString(CodeMethod method, boolean withGenerics) {
		StringBuilder signature = new StringBuilder("(");
		for (CodeParameter p : method.getParameters()) {
			appendType(signature, p.getType(), withGenerics);
		}
		signature.append(")");
		if (!method.returnsResult()) {
			signature.append("V");
		} else {
			CodeTypeReference returnType = method.getReturnType();
			appendType(signature, returnType, withGenerics);
		}

		String signatureAsString = signature.toString();
		return signatureAsString;
	}

	private void appendType(StringBuilder signature, CodeTypeReference returnType, boolean withGenerics) {
		String qn = jg.toQualifiedName(returnType).replaceAll("\\.", "/");
		if (qn.equals("boolean")) {
			signature.append("Z");
		} else {
			String string = primitiveTypeSignatures.get(qn);
			if (string != null) {
				signature.append(string);
			} else {
				signature.append("L");
				signature.append(qn);
				if (returnType.getElementTypes().size() > 0 && withGenerics) {
					signature.append("<");
					Iterator<CodeElementType> iterator = returnType.getElementTypes().iterator();
					while (iterator.hasNext()) {
						CodeTypeReference et = iterator.next().getType();
						appendType(signature, et, withGenerics);
					}
					signature.append(">");
				}
				signature.append(";");
			}
		}
	}

	private ClassPool getPool() {
		return this.pool;
	}

	private void addMethod(CtClass ctClass, CodeMethod value) throws CannotCompileException {
		String emptyBody;
		CtClass result;
		if (value.getReturnType() == null) {
			emptyBody = "{}";
			result = findOrCreateCtClass("void");
		} else {
			emptyBody = "{return null;}";
			result = findOrCreateCtClass(jg.toQualifiedName(value.getReturnType()));
			if (result.getName().equals("boolean")) {
				emptyBody = "{return false;}";
			} else if (isPrimitiveNumber(result)) {
				emptyBody = "{return 0;}";
			}
		}
		CtMethod method = CtNewMethod.make(result, value.getName(), getCtParameters(value.getParameters()), new CtClass[0], emptyBody, ctClass);
		MethodInfo methodInfo = method.getMethodInfo();
		SignatureAttribute signatureAttribute = new SignatureAttribute(methodInfo.getConstPool(), toSignatureString(value, true));
		methodInfo.addAttribute(signatureAttribute);
		ctClass.addMethod(method);
	}

	private boolean isPrimitiveNumber(CtClass result) {
		Set<String> primitives = new HashSet<String>();
		primitives.add("byte");
		primitives.add("short");
		primitives.add("int");
		primitives.add("long");
		primitives.add("float");
		primitives.add("double");
		primitives.add("char");
		return primitives.contains(result.getName());
	}

	private CtClass[] getCtParameters(List<CodeParameter> parameters) {
		CtClass[] result = new CtClass[parameters.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = findOrCreateCtClass(jg.toQualifiedName(parameters.get(i).getType()));
		}
		return result;
	}

	protected void compile() throws CannotCompileException {
		for (CtClass ctClass : generatedClasses) {
			ctClass.toClass();
		}
	}

	private void addField(CtClass declaringClass, CodeField codeField) throws CannotCompileException {
		String qualifiedJavaname = jg.toQualifiedName(codeField.getType());
		findOrCreateCtClass(qualifiedJavaname);// need to register the type
		String javaSource = jg.toFieldDeclaration(codeField);
		CtField field = CtField.make(javaSource.replaceAll("\\<.*\\>", ""), declaringClass);
		declaringClass.addField(field);
	}

	protected CtClass findOrCreateCtClass(String qualifiedJavaname) {
		CtClass ctClass;
		try {
			ctClass = getPool().get(qualifiedJavaname);
		} catch (NotFoundException e) {
			ctClass = getPool().makeClass(qualifiedJavaname);
			generatedClasses.add(ctClass);
			getPool().importPackage(qualifiedJavaname);
		}
		return ctClass;
	}

	public Class<?> loadClass(String qualifiedName) throws ClassNotFoundException {
		return getPool().getClassLoader().loadClass(qualifiedName);
	}

}
