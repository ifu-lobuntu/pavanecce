package org.pavanecce.common.code.metamodel;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

public class CodeClass extends CodeClassifier {
	private CodeTypeReference superClass;
	private SortedSet<CodeTypeReference> implementedInterfaces = new TreeSet<CodeTypeReference>();
	private int uniqueNumber;
	private Collection<OclStandardLibrary> librariesToImport;
	private SortedMap<String,CodeConstructor> constructors = new TreeMap<String,CodeConstructor>();
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
		if(getSuperClass()!=null){
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

	public SortedSet<CodeTypeReference> getImplementedInterfaces() {
		return implementedInterfaces;
	}

	public void addToImplementedInterfaces(CodeTypeReference ref) {
		implementedInterfaces.add(ref);
	}


	public int getUniqueNumber() {
		return uniqueNumber++;
	}

	public void addStdLibToImports(OclStandardLibrary l) {
		if(librariesToImport==null){
			librariesToImport=new HashSet<OclStandardLibrary>();
		}
		librariesToImport.add(l);
	}
	public Collection<OclStandardLibrary> getLibrariesToImport() {
		return librariesToImport==null?Collections.<OclStandardLibrary>emptySet():librariesToImport;
	}

	public CodeConstructor findOrCreateConstructor(List<CodeParameter> parameters) {
		CodeConstructor result = constructors.get(CodeBehaviour.generateIdentifier("", parameters));
		if(result==null){
			result=new CodeConstructor(this);
		}
		return result;
	}
}
