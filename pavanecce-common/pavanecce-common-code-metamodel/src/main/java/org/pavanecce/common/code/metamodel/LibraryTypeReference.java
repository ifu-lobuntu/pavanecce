package org.pavanecce.common.code.metamodel;

import java.util.Collections;
import java.util.Map;

public class LibraryTypeReference extends CodeTypeReference {

	private Enum<?> kind;

	public LibraryTypeReference(Enum<?> kind,Map<String,String> mappings) {
		super(false, "Library", kind.name());
		this.kind=kind;
		super.mappings=new CodeMappedType(mappings);
	}
	public LibraryTypeReference(Enum<?> kind) {
		this(kind,Collections.<String,String>emptyMap());
	}
	public LibraryTypeReference(Enum<?> kind2, CodeMappedType mappings) {
		this(kind2);
		super.mappings=mappings;
	}
	@Override
	public LibraryTypeReference getCopy(){
		return new LibraryTypeReference(kind,mappings);
	}
	public Enum<?> getKind() {
		return kind;
	}
	@Override
	public int compareTo(CodeTypeReference o) {
		if (o instanceof LibraryTypeReference) {
			LibraryTypeReference other = (LibraryTypeReference) o;
			int result = kind.name().compareTo(other.getKind().name());
			return result;
		}else{
			return super.compareTo(o);
		}
	}



}
