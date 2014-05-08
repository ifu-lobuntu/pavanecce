package org.pavanecce.common.code.metamodel;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class CodePackage extends CodeElement {
	private CodePackage parent;
	private SortedMap<String, CodePackage> children = new TreeMap<String, CodePackage>();
	private SortedMap<String, CodeClassifier> classifiers = new TreeMap<String, CodeClassifier>();
	private CodePackageReference packageReference;
	public CodePackage(String name, CodePackage parent) {
		super(name);
		this.parent = parent;
		parent.getChildren().put(name, this);
	}
	
	protected CodePackage(String name) {
		super(name);
	}

	public CodePackage findOrCreatePackage(String name) {
		CodePackage result = children.get(name);
		if (result == null) {
			result = new CodePackage(name, this);// will add it to my children
		}
		return result;
	}

	public CodePackage getParent() {
		return parent;
	}

	public SortedMap<String, CodePackage> getChildren() {
		return children;
	}

	public SortedMap<String, CodeClassifier> getClassifiers() {
		return classifiers;
	}

	public void appendPath(List<String> path) {
		getParent().appendPath(path);
		path.add(getName());
	}

	public List<String> getPath() {
		
		List<String> result=new ArrayList<String>();
		appendPath(result);
		return result;
	}
	@Override
	public String toString(){
		if(this.parent!=null){
			return this.parent.toString()+"."+getName();
		}
		return getName();
	}

	public CodePackageReference getPackageReference() {
		return packageReference;
	}

	public void setPackageReference(CodePackageReference packageReference) {
		this.packageReference = packageReference;
	}

}
