package org.pavanecce.common.code.metamodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

public class CodeEnumeration extends CodeClassifier {
	private List<CodeEnumerationLiteral> literals = new ArrayList<CodeEnumerationLiteral>();
	private SortedSet<CodeTypeReference> implementedInterfaces = new TreeSet<CodeTypeReference>();
	private Map<String, CodeConstructor> constructors = new HashMap<String, CodeConstructor>();

	public CodeEnumeration(String name, CodePackage _package) {
		super(name, _package);
	}

	public List<CodeEnumerationLiteral> getLiterals() {
		return literals;
	}

	public SortedSet<CodeTypeReference> getImplementedInterfaces() {
		return implementedInterfaces;
	}

	public void addImplementedInterface(CodeTypeReference implemented) {
		implementedInterfaces.add(implemented);
	}

	public Map<String, CodeConstructor> getConstructors() {
		return constructors;
	}

}
