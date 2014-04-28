package org.pavanecce.common.code.metamodel;

import java.util.ArrayList;
import java.util.List;

public class CodeEnumerationLiteral extends CodeField {
	private List<CodeExpression> attributeValues=new ArrayList<CodeExpression>();
	public CodeEnumerationLiteral(CodeEnumeration owner, String name) {
		super(owner, name, owner.getPathName());
	}

	public void addToAttributeValues(CodeExpression init) {
		this.attributeValues.add(init);
	}

}
