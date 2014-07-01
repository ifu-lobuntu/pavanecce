package org.pavanecce.common.code.metamodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.pavanecce.common.code.metamodel.expressions.NewInstanceExpression;

public class CodeClassifier extends CodeElement {
	private CodeVisibilityKind visibility = CodeVisibilityKind.PUBLIC;
	private CodePackage _package;
	private SortedMap<String, CodeField> fields = new TreeMap<String, CodeField>();
	private SortedMap<String, CodeMethod> methods = new TreeMap<String, CodeMethod>();
	private CodeTypeReference typeReference;
	protected Collection<Enum<?>> librariesToImport = new HashSet<Enum<?>>();

	public CodeClassifier(String name, CodePackage _package) {
		super(name);
		this._package = _package;
		this._package.getClassifiers().put(name, this);
	}

	public CodeTypeReference getPathName() {
		return typeReference;
	}

	public CodePackage getPackage() {
		return _package;
	}

	public SortedMap<String, CodeField> getFields() {
		return fields;
	}

	public CodeMethod getMethod(String name, List<?> params) {
		return getMethods().get(CodeBehaviour.generateIdentifier(name, params));
	}

	public void addStdLibToImports(OclStandardLibrary l) {
		if (librariesToImport == null) {
			librariesToImport = new HashSet<Enum<?>>();
		}
		librariesToImport.add(l);
	}

	public Collection<Enum<?>> getLibrariesToImport() {
		return librariesToImport == null ? Collections.<Enum<?>> emptySet() : librariesToImport;
	}

	public SortedSet<CodeTypeReference> getImports() {
		SortedSet<CodeTypeReference> result = new TreeSet<CodeTypeReference>();
		for (Entry<String, CodeField> entry : this.fields.entrySet()) {
			CodeTypeReference type = entry.getValue().getType();
			addTypeToImports(result, type, true);
			addExpressionToImports(result, entry.getValue().getInitialization());
		}
		Collection<CodeMethod> values = this.methods.values();
		for (CodeMethod codeMethod : values) {
			List<CodeParameter> parameters = codeMethod.getParameters();
			for (CodeParameter codeParameter : parameters) {
				addTypeToImports(result, codeParameter.getType(), false);
			}
			addExpressionToImports(result, codeMethod.getResult());
			addTypeToImports(result, codeMethod.getReturnType(), false);
		}
		for (Enum<?> enum1 : this.getLibrariesToImport()) {
			result.add(new LibraryTypeReference(enum1));
		}
		return result;
	}

	protected void addExpressionToImports(SortedSet<CodeTypeReference> result, CodeExpression exp) {
		if (exp instanceof NewInstanceExpression) {
			NewInstanceExpression nie = (NewInstanceExpression) exp;
			if (nie.getType() instanceof CollectionTypeReference) {
				result.add(((CollectionTypeReference) nie.getType()).getImplementation());
			}
		}
	}

	public List<String> getPath() {
		List<String> path = new ArrayList<String>();
		getPackage().appendPath(path);
		path.add(getName());
		return path;
	}

	private void addTypeToImports(SortedSet<CodeTypeReference> result, CodeTypeReference type, boolean includeImplementation) {
		if (type instanceof PrimitiveTypeReference) {
			// do nothing- they're assumed to be built in
		} else if (type != null) {
			result.add(type);
			for (CodeElementType e : type.getElementTypes()) {
				addTypeToImports(result, e.getType(), includeImplementation);
			}
			if (type instanceof CollectionTypeReference) {
				CollectionTypeReference ctr = (CollectionTypeReference) type;
				if (includeImplementation && !ctr.isImplementation()) {
					result.add(ctr.getImplementation());
				}
			}
		}
	}

	public SortedMap<String, CodeMethod> getMethods() {
		return methods;
	}

	@Override
	public String toString() {
		if (this._package != null) {
			return this._package.toString() + "." + getName();
		}
		return getName();
	}

	public CodeTypeReference getTypeReference() {
		return typeReference;
	}

	public void setTypeReference(CodeTypeReference typeReference) {
		this.typeReference = typeReference;
	}

	public CodeVisibilityKind getVisibility() {
		return visibility;
	}

	public void setVisibility(CodeVisibilityKind visibility) {
		this.visibility = visibility;
	}
}
