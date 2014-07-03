package org.pavanecce.common.code.metamodel;

import java.util.List;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

public class CodeClass extends CodeClassifier {
	private CodeTypeReference superClass;
	private SortedSet<CodeTypeReference> implementedInterfaces = new TreeSet<CodeTypeReference>();

	private SortedMap<String, CodeConstructor> constructors = new TreeMap<String, CodeConstructor>();

	public CodeClass(String name, CodePackage _package) {
		super(name, _package);
		_package.getClassifiers().put(name, this);
	}

	public CodeTypeReference getSuperClass() {
		return superClass;
	}

	@Override
	public SortedSet<CodeTypeReference> getImports() {
		SortedSet<CodeTypeReference> imports = super.getImports();
		if (getSuperClass() != null) {
			imports.add(getSuperClass());
		}
		for (CodeTypeReference ii : this.getImplementedInterfaces()) {
			imports.add(ii);
		}
		return imports;
	}

	public void setSuperClass(CodeTypeReference superClass) {
		this.superClass = superClass;
	}

	public SortedMap<String, CodeConstructor> getConstructors() {
		return constructors;
	}

	public SortedSet<CodeTypeReference> getImplementedInterfaces() {
		return implementedInterfaces;
	}

	public void addToImplementedInterfaces(CodeTypeReference ref) {
		implementedInterfaces.add(ref);
	}

	public CodeConstructor findOrCreateConstructor(List<CodeParameter> parameters) {
		CodeConstructor result = constructors.get(CodeBehaviour.generateIdentifier("", parameters));
		if (result == null) {
			result = new CodeConstructor(this);
		}
		return result;
	}
}
