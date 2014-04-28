package org.pavanecce.common.code.metamodel;

import java.util.SortedSet;
import java.util.TreeSet;

public class CodeInterface extends CodeClassifier {
	SortedSet<CodeTypeReference> superInterfaces=new TreeSet<CodeTypeReference>();
	public CodeInterface(String name, CodePackage _package) {
		super(name, _package);
	}
	public SortedSet<CodeTypeReference> getSuperInterfaces() {
		return superInterfaces;
	}
	public void addSuperInterface(CodeTypeReference superInterface){
		superInterfaces.add(superInterface);
	}
}
