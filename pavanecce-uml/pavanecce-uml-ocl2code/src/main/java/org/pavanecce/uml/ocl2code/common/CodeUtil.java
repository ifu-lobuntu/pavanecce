package org.pavanecce.uml.ocl2code.common;

import org.eclipse.uml2.uml.EnumerationLiteral;
import org.pavanecce.common.code.metamodel.CodeConstructor;
import org.pavanecce.common.code.metamodel.CodeEnumeration;
import org.pavanecce.common.code.metamodel.CodeField;
import org.pavanecce.common.code.metamodel.statements.PortableStatement;
import org.pavanecce.common.util.NameConverter;

public class CodeUtil {
	public static void addConstructor(CodeEnumeration cls, CodeField... params) {
		CodeConstructor constructor = new CodeConstructor(cls);
		for (CodeField param : params) {
			constructor.addParam(param.getName(), param.getType());
			new PortableStatement(constructor.getBody(), "${self}." + param.getName() + " = " + param.getName());
		}
	}

	public static String toJavaLiteral(EnumerationLiteral l) {
		if (l == null) {
			return null;
		}
		return NameConverter.toValidVariableName(l.getName().toUpperCase());
	}

}