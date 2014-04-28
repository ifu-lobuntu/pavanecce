package org.pavanecce.uml.uml2code.codemodel;

import java.util.SortedSet;

import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Namespace;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Type;
import org.pavanecce.common.code.metamodel.CodeClass;
import org.pavanecce.common.code.metamodel.CodeModel;
import org.pavanecce.common.code.metamodel.CodePackage;
import org.pavanecce.common.code.metamodel.CodePackageReference;
import org.pavanecce.common.code.metamodel.CodeTypeReference;
import org.pavanecce.uml.uml2code.UmlToCodeReferenceMap;

public class DefaultCodeModelBuilder {
	protected CodeModel codeModel;
	private UmlToCodeReferenceMap umlToCodeReferenceMap = new UmlToCodeReferenceMap();

	public DefaultCodeModelBuilder() {
		super();
	}

	protected CodeTypeReference calculateTypeReference(Type type) {
		return umlToCodeReferenceMap.classifierPathname(type);
	}

	protected CodePackageReference calculatePackageReference(Namespace pkg) {
		return umlToCodeReferenceMap.packagePathname(pkg);
	}

	public CodeClass visitClass(Class c, CodePackage codePackage) {
		return (CodeClass) codePackage.getClassifiers().get(c.getName());
	}

	public CodePackage visitPackage(Package pkg, CodePackage parent) {
		return parent.getChildren().get(pkg.getName());
	}

	public CodePackage visitModel(Model model) {
		return codeModel.getChildren().get(model.getName());
	}

	public void visitProperty(Property property, CodeClass codeClass) {

	}

	public void initialize(SortedSet<Model> models, CodeModel codeModel) {
		this.codeModel = codeModel;
	}

	public void visitOperation(Operation operation, CodeClass codeClass) {

	}

}