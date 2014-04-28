package org.pavanecce.common.code.metamodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CodeTypeReference implements Comparable<CodeTypeReference> {
	private CodePackageReference packageReference;
	private List<CodeElementType> elementTypes = new ArrayList<CodeElementType>();
	private String lastName;
	private boolean isPeer;
	protected CodeMappedType mappings;

	public CodeTypeReference(boolean isPeer, CodePackageReference packageReference, String lastName, Map<String, String> mappings) {
		this.packageReference = packageReference;
		this.isPeer = isPeer;
		this.lastName = lastName;
		this.mappings = new CodeMappedType(mappings);
	}
	public CodeTypeReference(boolean isPeer, String... qualifiedName) {
		this(isPeer, packageReference(qualifiedName), qualifiedName[qualifiedName.length - 1], Collections.<String, String> emptyMap());
	}

	private static CodePackageReference packageReference(String[] qualifiedName) {
		CodePackageReference result = null;
		for (int i = 0; i < qualifiedName.length - 1; i++) {
			result=new CodePackageReference(result,qualifiedName[i], Collections.<String,String>emptyMap());
		}
		return result;
	}



	public boolean isMapped() {
		return !isPeer;
	}

	public String getMappedType(String language) {
		return getMappings().get(language);
	}

	public String putMappedType(String language, String type) {
		return getMappings().put(language, type);
	}

	private Map<String, String> getMappings() {
		if (mappings == null) {
			mappings = new CodeMappedType(new HashMap<String,String>()); 
		}
		return mappings.getMappings();
	}

	@Override
	public int hashCode() {
		return packageReference.hashCode() + lastName.hashCode();
	}

	public boolean equals(Object other) {
		if (other instanceof CodeTypeReference) {
			return compareTo((CodeTypeReference) other) == 0;
		}
		return false;
	}

	public String getLastName() {
		return lastName;
	}

	public CodePackageReference getCodePackageReference() {
		return this.packageReference;
	}

	@Override
	public int compareTo(CodeTypeReference o) {
		int packageDiff = packageReference.compareTo(o.packageReference);
		if (packageDiff != 0) {
			return packageDiff;
		} else {
			return lastName.compareTo(o.lastName);
		}
	}

	public void addToElementTypes(CodeTypeReference sourceType) {
		this.elementTypes.add(new CodeElementType(sourceType));
	}
	public List<CodeElementType> getElementTypes() {
		return elementTypes;
	}

	public boolean isPeer() {
		return isPeer;
	}
	public CodeTypeReference getCopy() {
		return new CodeTypeReference(isPeer, packageReference, lastName, mappings.getMappings());
	}
	public List<String> getQualifiedNameInLanguage(String language) {
		String mappedType = getMappedType(language);
		if(mappedType==null){
			List<String> result = packageReference.getQualifiedNameInLanguage(language);
			result.add(getLastName());
			return result;
		}else{
			return Arrays.asList(mappedType.split("\\."));
		}
	}
}
