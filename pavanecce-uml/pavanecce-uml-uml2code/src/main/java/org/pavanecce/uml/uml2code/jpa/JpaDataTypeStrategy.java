package org.pavanecce.uml.uml2code.jpa;

import java.util.Set;

import org.pavanecce.common.code.metamodel.CodeField;
import org.pavanecce.common.code.metamodel.relationaldb.RelationalColumn;
import org.pavanecce.uml.uml2code.java.JavaCodeGenerator;

public interface JpaDataTypeStrategy {
	Set<String> getImports();

	void beforeField(String padding, JavaCodeGenerator sb, CodeField field, RelationalColumn col);
}
