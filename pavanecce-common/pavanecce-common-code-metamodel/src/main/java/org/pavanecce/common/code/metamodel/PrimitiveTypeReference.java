package org.pavanecce.common.code.metamodel;

import java.util.Collections;
import java.util.Map;

public class PrimitiveTypeReference extends CodeTypeReference {

	private CodePrimitiveTypeKind kind;

	public PrimitiveTypeReference(CodePrimitiveTypeKind kind, Map<String, String> mappings) {
		super(false, "Primitives", kind.name());
		this.kind = kind;
		super.mappings = new CodeMappedType(mappings);
	}

	public PrimitiveTypeReference(CodePrimitiveTypeKind kind) {
		this(kind, Collections.<String, String> emptyMap());
	}

	public PrimitiveTypeReference(CodePrimitiveTypeKind kind2, CodeMappedType mappings) {
		this(kind2);
		super.mappings = mappings;
	}

	@Override
	public PrimitiveTypeReference getCopy() {
		return new PrimitiveTypeReference(kind, mappings);
	}

	public CodePrimitiveTypeKind getKind() {
		return kind;
	}

	@Override
	public int compareTo(CodeTypeReference o) {
		if (o instanceof PrimitiveTypeReference) {
			PrimitiveTypeReference other = (PrimitiveTypeReference) o;
			int result = kind.compareTo(other.getKind());

			return result;
		} else {
			return super.compareTo(o);
		}
	}

}
