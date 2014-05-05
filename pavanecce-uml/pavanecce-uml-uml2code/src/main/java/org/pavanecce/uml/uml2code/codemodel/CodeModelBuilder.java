package org.pavanecce.uml.uml2code.codemodel;

import static org.pavanecce.common.util.NameConverter.capitalize;
import static org.pavanecce.common.util.NameConverter.toValidVariableName;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JTable.PrintMode;

import org.eclipse.emf.common.util.EList;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Interface;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.MultiplicityElement;
import org.eclipse.uml2.uml.OpaqueExpression;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.Parameter;
import org.eclipse.uml2.uml.PrimitiveType;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.TypedElement;
import org.pavanecce.common.code.metamodel.AssociationCollectionTypeReference;
import org.pavanecce.common.code.metamodel.CodeClass;
import org.pavanecce.common.code.metamodel.CodeCollectionKind;
import org.pavanecce.common.code.metamodel.CodeField;
import org.pavanecce.common.code.metamodel.CodeMethod;
import org.pavanecce.common.code.metamodel.CodePackage;
import org.pavanecce.common.code.metamodel.CodeParameter;
import org.pavanecce.common.code.metamodel.CodePrimitiveTypeKind;
import org.pavanecce.common.code.metamodel.CodeTypeReference;
import org.pavanecce.common.code.metamodel.CollectionTypeReference;
import org.pavanecce.common.code.metamodel.PrimitiveTypeReference;
import org.pavanecce.common.code.metamodel.documentdb.DocumentProperty;
import org.pavanecce.common.code.metamodel.documentdb.IDocumentElement;
import org.pavanecce.common.code.metamodel.documentdb.IDocumentProperty;
import org.pavanecce.common.code.metamodel.documentdb.PropertyType;
import org.pavanecce.common.code.metamodel.expressions.BinaryOperatorExpression;
import org.pavanecce.common.code.metamodel.expressions.IsNullExpression;
import org.pavanecce.common.code.metamodel.expressions.NewInstanceExpression;
import org.pavanecce.common.code.metamodel.expressions.NotExpression;
import org.pavanecce.common.code.metamodel.expressions.NullExpression;
import org.pavanecce.common.code.metamodel.expressions.PortableExpression;
import org.pavanecce.common.code.metamodel.expressions.PrimitiveDefaultExpression;
import org.pavanecce.common.code.metamodel.expressions.ReadFieldExpression;
import org.pavanecce.common.code.metamodel.relationaldb.IRelationalElement;
import org.pavanecce.common.code.metamodel.statements.AssignmentStatement;
import org.pavanecce.common.code.metamodel.statements.CodeIfStatement;
import org.pavanecce.common.code.metamodel.statements.MappedStatement;
import org.pavanecce.common.code.metamodel.statements.MethodCallStatement;
import org.pavanecce.common.code.metamodel.statements.PortableStatement;
import org.pavanecce.common.util.NameConverter;
import org.pavanecce.uml.common.util.EmfClassifierUtil;
import org.pavanecce.uml.common.util.EmfParameterUtil;
import org.pavanecce.uml.common.util.EmfPropertyUtil;

public class CodeModelBuilder extends DefaultCodeModelBuilder {
	private boolean useAssociationCollections = false;
	private DocumentUtil documentUtil = new DocumentUtil();

	public boolean useAssociationCollections() {
		return useAssociationCollections;
	}

	public CodeModelBuilder(boolean useAssociationCollections) {
		super();
		this.useAssociationCollections = useAssociationCollections;
	}

	public CodeModelBuilder() {
		this(false);
	}

	@Override
	public void visitProperty(Property p, CodeClass codeClass) {
		String fieldName = toValidVariableName(p.getName());
		CodeField cf = new CodeField(codeClass, fieldName);
		cf.setType(calculateType(p));
		cf.putData(IRelationalElement.class, RelationalUtil.buildRelationalElement(p));
		cf.putData(IDocumentElement.class, documentUtil.buildDocumentElement(p));
		String capitalized = toValidVariableName(capitalize(p.getName()));
		String getterName = generateGetterName(p, cf, capitalized);

		CodeMethod getter = new CodeMethod(codeClass, getterName, cf.getType());
		if (useAssociationCollections && p.getOtherEnd() != null && p.getOtherEnd().isNavigable() && EmfPropertyUtil.isMany(p) && p.isUnique()) {
			getter.setResultInitialValue(new ReadFieldExpression(fieldName + "Wrapper"));
		} else {
			getter.setResultInitialValue(new ReadFieldExpression(fieldName));
		}
		CodeMethod setter = new CodeMethod("set" + capitalized);
		CodeParameter param = new CodeParameter("new" + capitalized, setter, cf.getType());
		setter.setDeclaringClass(codeClass);
		if (p.getOtherEnd() != null && p.getOtherEnd().isNavigable()) {
			if (EmfPropertyUtil.isManyToMany(p)) {
				new AssignmentStatement(setter.getBody(),"${self}." + fieldName, new PortableExpression(param.getName()));
			} else if (EmfPropertyUtil.isOneToMany(p)) {
				NotExpression notNull = new NotExpression(new IsNullExpression(new PortableExpression(param.getName())));
				CodeIfStatement ifNewNotNull = new CodeIfStatement(setter.getBody(), notNull);
				new PortableStatement(ifNewNotNull.getThenBlock(), param.getName() + ".get" + NameConverter.capitalize(p.getOtherEnd().getName()) + "().add(this)");
				NotExpression oldNotNull = new NotExpression(new IsNullExpression(new PortableExpression("${self}." + fieldName)));
				CodeIfStatement ifOldNotNull = new CodeIfStatement(ifNewNotNull.getElseBlock(), oldNotNull);
				new PortableStatement(ifOldNotNull.getThenBlock(), "${self}." + fieldName + ".get" + NameConverter.capitalize(p.getOtherEnd().getName()) + "().remove(this)");
				new AssignmentStatement(setter.getBody(),"${self}." + fieldName, new PortableExpression(param.getName()));
			} else if (EmfPropertyUtil.isManyToOne(p)) {
				new AssignmentStatement(setter.getBody(),"${self}." + fieldName, new PortableExpression(param.getName()));
			} else if (EmfPropertyUtil.isOneToOne(p)) {
				new CodeField(setter.getBody(), "oldValue", param.getType()).setInitExp("${self}." + fieldName);
				NotExpression notEquals = new NotExpression(new BinaryOperatorExpression(new PortableExpression(param.getName()), "${equals}", new PortableExpression("oldValue")));
				BinaryOperatorExpression nullOrNotEquals=new BinaryOperatorExpression(new IsNullExpression(new PortableExpression(param.getName())), "||", notEquals);
				CodeIfStatement ifNotEquals = new CodeIfStatement(setter.getBody(), nullOrNotEquals);
				new AssignmentStatement(ifNotEquals.getThenBlock(),"${self}." + fieldName, new PortableExpression(param.getName()));
				CodeIfStatement ifOldNotNull = new CodeIfStatement(ifNotEquals.getThenBlock(), new NotExpression(new IsNullExpression(new PortableExpression("oldValue"))));
				String otherCappedName = toValidVariableName(NameConverter.capitalize(p.getOtherEnd().getName()));
				new MethodCallStatement(ifOldNotNull.getThenBlock(), "oldValue.set" + otherCappedName, new NullExpression());
				CodeIfStatement ifNewNotNull = new CodeIfStatement(ifNotEquals.getThenBlock(), new NotExpression(new IsNullExpression(new PortableExpression(param.getName()))));
				NotExpression otherNotEquals = new NotExpression(new BinaryOperatorExpression(new PortableExpression("${self}"), "${equals}", new PortableExpression(param.getName() + ".get"
						+ otherCappedName + "()")));
				CodeIfStatement ifOtherNotEquals = new CodeIfStatement(ifNewNotNull.getThenBlock(), otherNotEquals);
				new PortableStatement(ifOtherNotEquals.getThenBlock(), param.getName() + ".set" + otherCappedName + "(${self})");
			}
		} else {
			new AssignmentStatement(setter.getBody(),"${self}." + fieldName, new PortableExpression(param.getName()));
		}
	}

	protected String generateGetterName(Property p, CodeField cf, String capitalized) {
		String getterName = "get" + capitalized;

		return getterName;
	}

	private CodeTypeReference calculateType(TypedElement te) {
		CodeTypeReference result = null;
		if (EmfPropertyUtil.isMany(te)) {
			MultiplicityElement me = (MultiplicityElement) te;
			if (useAssociationCollections && me instanceof Property && ((Property) me).getOtherEnd() != null && ((Property) me).getOtherEnd().isNavigable()) {
				Property otherEnd = ((Property) me).getOtherEnd();
				result = calculateAssociationType(me, otherEnd);
				AssociationCollectionTypeReference associationCollectionTypeReference = (AssociationCollectionTypeReference) result;
				if (EmfPropertyUtil.isMany(otherEnd)) {
					AssociationCollectionTypeReference otherType = calculateAssociationType(otherEnd, (Property) me);
					associationCollectionTypeReference.setOtherFieldType(otherType);
					otherType.setOtherFieldType(associationCollectionTypeReference);
				} else {
					associationCollectionTypeReference.setOtherFieldType(calculateType(otherEnd));
				}
			} else {
				if (me.isUnique() && me.isOrdered()) {
					result = new CollectionTypeReference(CodeCollectionKind.ORDERED_SET);
				} else if (me.isUnique()) {
					result = new CollectionTypeReference(CodeCollectionKind.SET);
				} else if (me.isOrdered()) {
					result = new CollectionTypeReference(CodeCollectionKind.SEQUENCE);
				} else {
					result = new CollectionTypeReference(CodeCollectionKind.BAG);
				}
				result.addToElementTypes(calculateTypeReference(te.getType()));
			}
		} else if (te.getType() instanceof PrimitiveType) {
			result = new PrimitiveTypeReference(getPrimitiveTypeKind(te), EmfClassifierUtil.getMappings(te.getType()));
		} else {
			result = calculateTypeReference(te.getType());
		}
		return result;
	}

	protected AssociationCollectionTypeReference calculateAssociationType(MultiplicityElement me, Property otherEnd) {
		AssociationCollectionTypeReference result;
		String otherFieldName = NameConverter.decapitalize(otherEnd.getName());

		Property thisEnd = (Property) me;
		if (me.isUnique() && me.isOrdered()) {
			result = new AssociationCollectionTypeReference(CodeCollectionKind.ORDERED_SET, otherFieldName, thisEnd.isComposite());
		} else if (me.isUnique()) {
			result = new AssociationCollectionTypeReference(CodeCollectionKind.SET, otherFieldName, thisEnd.isComposite());
		} else if (me.isOrdered()) {
			result = new AssociationCollectionTypeReference(CodeCollectionKind.SEQUENCE, otherFieldName, thisEnd.isComposite());
		} else {
			result = new AssociationCollectionTypeReference(CodeCollectionKind.BAG, otherFieldName, thisEnd.isComposite());
		}
		result.addToElementTypes(calculateTypeReference(thisEnd.getType()));

		return result;
	}

	private CodePrimitiveTypeKind getPrimitiveTypeKind(TypedElement te) {
		return getPrimitiveTypeKind((PrimitiveType) te.getType());
	}

	private CodePrimitiveTypeKind getPrimitiveTypeKind(PrimitiveType type) {
		try {
			return CodePrimitiveTypeKind.valueOf(type.getName().toUpperCase());
		} catch (IllegalArgumentException e) {
			return getPrimitiveTypeKind((PrimitiveType) type.getGenerals().get(0));
		}
	}

	@Override
	public CodePackage visitModel(Model model) {
		CodePackage result = new CodePackage(model.getName(), codeModel);
		result.setPackageReference(this.calculatePackageReference(model));
		return result;
	}

	@Override
	public void visitOperation(Operation operation, CodeClass codeClass) {
		CodeMethod cm = new CodeMethod(operation.getName());
		for (Parameter parameter : EmfParameterUtil.getArgumentParameters(operation)) {
			cm.addParam(toValidVariableName(parameter.getName()), calculateType(parameter));
		}
		Parameter returnParameter = EmfParameterUtil.getReturnParameter(operation);
		if (returnParameter != null) {
			cm.setReturnType(calculateType(returnParameter));
		}
		if (hasBodyButNotOclOcl(operation)) {
			OpaqueExpression oe = (OpaqueExpression) operation.getBodyCondition().getSpecification();
			Map<String, String> map = new HashMap<String, String>();
			EList<String> bodies = oe.getBodies();
			for (int i = 0; i < bodies.size(); i++) {
				map.put(oe.getLanguages().get(i), bodies.get(i));
			}
			new MappedStatement(cm.getBody(), map);
		} else if (returnParameter != null) {
			if (cm.getReturnType() instanceof CollectionTypeReference) {
				cm.setResultInitialValue(new NewInstanceExpression(cm.getReturnType()));
			} else if (cm.getReturnType() instanceof PrimitiveTypeReference) {
				cm.setResultInitialValue(new PrimitiveDefaultExpression(((PrimitiveTypeReference) cm.getReturnType()).getKind()));
			} else {
				cm.setResultInitialValue(new NullExpression());
			}

		}
		cm.setDeclaringClass(codeClass);
	}

	protected boolean hasBodyButNotOclOcl(Operation operation) {
		if (operation.getBodyCondition() != null && operation.getBodyCondition().getSpecification() instanceof OpaqueExpression) {
			OpaqueExpression oe = (OpaqueExpression) operation.getBodyCondition().getSpecification();
			return !(oe.getLanguages().contains("ocl") || oe.getLanguages().contains("OCL"));
		}else{
			return false;
		}
	}

	@Override
	public CodePackage visitPackage(Package pkg, CodePackage parent) {
		CodePackage result = parent.findOrCreatePackage(pkg.getName());
		result.setPackageReference(this.calculatePackageReference(pkg));
		return result;
	}

	@Override
	public CodeClass visitClass(Class c, CodePackage codePackage) {
		CodeClass codeClass = new CodeClass(c.getName(), codePackage);
		EList<Class> superClasses = c.getSuperClasses();
		if (superClasses.size() == 1) {
			codeClass.setSuperClass(calculateTypeReference(superClasses.get(0)));
		}
		EList<Interface> implementedInterfaces = c.getImplementedInterfaces();
		for (Interface interface1 : implementedInterfaces) {
			codeClass.addToImplementedInterfaces(calculateTypeReference(interface1));
		}
		codeClass.putData(Model.class, c.getModel());
		codeClass.setTypeReference(this.calculateTypeReference(c));
		codeClass.putData(IRelationalElement.class, RelationalUtil.buildRelationalElement(c));
		codeClass.putData(IDocumentElement.class, documentUtil.getDocumentNode(c));
		if (!c.isAbstract() && EmfPropertyUtil.getEndToComposite(c) == null) {
			// TODO put in a OcmCodeModelBuilder
			CodeField path = new CodeField(codeClass, "path", new PrimitiveTypeReference(CodePrimitiveTypeKind.STRING));
			DocumentProperty prop = new DocumentProperty("path", documentUtil.getNamespaceOf(c), PropertyType.STRING, true, false);
			prop.setPath(true);
			path.putData(IDocumentElement.class, prop);
			path.setInitialization("\"/" + codeClass.getName() + "Collection\"");
			new CodeMethod(codeClass, "getPath", path.getType()).setResultInitialValue("${self}.path");
			CodeMethod setter = new CodeMethod("setPath");
			setter.addParam("path", path.getType());
			setter.setDeclaringClass(codeClass);
			new PortableStatement(setter.getBody(), "${self}.path=path");
		}
		return codeClass;
	}
}
