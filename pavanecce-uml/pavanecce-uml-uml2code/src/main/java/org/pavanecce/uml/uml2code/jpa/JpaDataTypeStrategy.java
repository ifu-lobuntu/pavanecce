package org.pavanecce.uml.uml2code.jpa;

import java.util.Set;

import org.pavanecce.common.code.metamodel.CodeField;
import org.pavanecce.common.code.metamodel.relationaldb.RelationalColumn;

public interface JpaDataTypeStrategy {
	Set<String> getImports();

	void beforeField(String padding, StringBuilder sb, CodeField field, RelationalColumn col);
}
