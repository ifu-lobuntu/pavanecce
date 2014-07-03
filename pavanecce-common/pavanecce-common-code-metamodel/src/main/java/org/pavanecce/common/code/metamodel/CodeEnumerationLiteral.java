package org.pavanecce.common.code.metamodel;

import java.util.SortedMap;
import java.util.TreeMap;

public class CodeEnumerationLiteral extends CodeElement {
	private SortedMap<String, CodeFieldValue> fieldValues = new TreeMap<String, CodeFieldValue>();

	public CodeEnumerationLiteral(CodeEnumeration owner, String name) {
		super(name);
		owner.getLiterals().add(this);
	}

	public void addToFieldValues(CodeField field, CodeExpression value) {
		CodeFieldValue codeFieldValue = fieldValues.get(field.getName());
		if (codeFieldValue == null) {
			fieldValues.put(field.getName(), codeFieldValue = new CodeFieldValue(field, value));
		} else {
			codeFieldValue.setValue(value);
		}
	}

	public SortedMap<String, CodeFieldValue> getFieldValues() {
		return fieldValues;
	}
}
