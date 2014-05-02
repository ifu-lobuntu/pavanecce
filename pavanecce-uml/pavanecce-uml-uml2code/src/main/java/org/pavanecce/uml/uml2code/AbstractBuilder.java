package org.pavanecce.uml.uml2code;

import java.util.SortedSet;

import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.Property;
public abstract class AbstractBuilder<PACKAGE,CLASS> {
	public abstract PACKAGE visitModel(Model model);
	public abstract void initialize(SortedSet<Model> models, PACKAGE p);
	public abstract PACKAGE visitPackage(Package model,PACKAGE parent);
	public abstract CLASS visitClass(Class cl, PACKAGE parent);
	public abstract void visitProperty(Property p, CLASS parent);
	public abstract void visitOperation(Operation o, CLASS parent);
}
