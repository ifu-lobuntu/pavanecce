package org.pavanecce.common.code.metamodel;

import java.util.List;

public class CodeModel extends CodePackage {

	public CodeModel() {
		super("Root");
	}
	public void appendPath(List<String> path) {
	}

	@SuppressWarnings("unchecked")
	public <T extends CodeElement> T getDescendent(String... names) {
		CodePackage cur = this;
		for (String string : names) {
			CodePackage codePackage = cur.getChildren().get(string);
			if (codePackage == null) {
				return (T)cur.getClassifiers().get(string);
			} else {
				cur = codePackage;
			}
		}
		return (T)cur;
	}
	public CodePackage findOrCreatePackage(CodePackageReference path) {
		List<String> qualifiedName = path.getQualifiedName();
		CodePackage cur = this;
		for (String string : qualifiedName) {
			cur=findOrCreatePackage(string);
		}
		return cur;
	}

}
