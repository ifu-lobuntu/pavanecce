package org.pavanecce.common.code.metamodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class CodePackageReference implements Comparable<CodePackageReference> {
	private CodePackageReference parent;
	private Map<String, String> packageMappings;
	private String name;

	public CodePackageReference(CodePackageReference parent, String string, Map<String, String> packageMappings) {
		this.parent = parent;
		this.packageMappings = packageMappings;
		this.name = string;
	}

	public String getPackageMappingFor(String language) {
		if (packageMappings == null) {
			return null;
		} else {
			return packageMappings.get(language);
		}
	}

	@Override
	public int compareTo(CodePackageReference o) {
		int i = 0;
		for (i = 0; i < getQualifiedName().size(); i++) {
			if (o.getQualifiedName().size() <= i) {
				return -1;
			}
			int result = getQualifiedName().get(i).compareTo(o.getQualifiedName().get(i));
			if (result != 0) {
				return result;
			}
		}
		if (o.getQualifiedName().size() > i) {
			return 1;
		}
		return 0;
	}

	@Override
	public int hashCode() {
		int result = 0;
		for (String string : this.getQualifiedName()) {
			result += string.hashCode();
		}
		return result;
	}

	public List<String> getQualifiedName() {
		List<String> qn = new ArrayList<String>();
		CodePackageReference cur = this;
		while (cur != null) {
			qn.add(0, cur.name);
			cur=cur.parent;
		}
		return qn;
	}

	public List<String> getQualifiedNameInLanguage(String language) {
		List<String> qn = new ArrayList<String>();
		addQualifiedNameInLanguage(qn, language, "\\.");
		return qn;
	}

	private void addQualifiedNameInLanguage(List<String> qn, String language, String seperator) {
		String packageMapping = getPackageMappingFor(language);
		if (packageMapping!=null) {
			qn.addAll(Arrays.asList(packageMapping.split(seperator)));
		} else {
			if (parent != null) {
				parent.addQualifiedNameInLanguage(qn, language, seperator);
			}
			qn.add(name);
		}

	}

	public boolean equals(Object other) {
		if (other instanceof CodePackageReference) {
			return compareTo((CodePackageReference) other) == 0;
		}
		return false;
	}

	public CodePackageReference getCopy() {
		return new CodePackageReference(parent, name, packageMappings);
	}

}
