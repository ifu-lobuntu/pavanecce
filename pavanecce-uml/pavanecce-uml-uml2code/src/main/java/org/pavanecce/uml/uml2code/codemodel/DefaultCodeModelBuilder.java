package org.pavanecce.uml.uml2code.codemodel;

import java.util.SortedSet;

import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Enumeration;
import org.eclipse.uml2.uml.EnumerationLiteral;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Namespace;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Type;
import org.pavanecce.common.code.metamodel.CodeClass;
import org.pavanecce.common.code.metamodel.CodeClassifier;
import org.pavanecce.common.code.metamodel.CodeEnumeration;
import org.pavanecce.common.code.metamodel.CodeModel;
import org.pavanecce.common.code.metamodel.CodePackage;
import org.pavanecce.common.code.metamodel.CodePackageReference;
import org.pavanecce.common.code.metamodel.CodeTypeReference;
import org.pavanecce.uml.uml2code.AbstractBuilder;
import org.pavanecce.uml.uml2code.UmlToCodeReferenceMap;

/**
 * A default implementation of the callbacks that are invoked from the UmlCodeModelVisitor. To be subclassed by
 * CodeModelBuilders that add CodeModel elements to the resultingCodeModel
 * 
 * @author ampie
 * 
 */
public class DefaultCodeModelBuilder extends AbstractBuilder<CodePackage, CodeClassifier> {
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

	@Override
	public CodeClass visitClass(Class c, CodePackage codePackage) {
		return (CodeClass) codePackage.getClassifiers().get(c.getName());
	}

	@Override
	public CodePackage visitPackage(Package pkg, CodePackage parent) {
		return parent.getChildren().get(pkg.getName());
	}

	@Override
	public CodePackage visitModel(Model model) {
		return codeModel.getChildren().get(model.getName());
	}

	@Override
	public void visitProperty(Property property, CodeClassifier codeClass) {

	}

	@Override
	public void initialize(SortedSet<Model> models, CodePackage codeModel) {
		this.codeModel = (CodeModel) codeModel;
	}

	@Override
	public void visitOperation(Operation operation, CodeClassifier codeClass) {

	}

	@Override
	public CodeEnumeration visitEnum(Enumeration en, CodePackage parent) {
		return (CodeEnumeration) parent.getClassifiers().get(en.getName());
	}

	@Override
	public void visitEnumerationLiteral(EnumerationLiteral el, CodeClassifier parent) {
	}

}