package org.pavanecce.uml.uml2code;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.emf.common.util.EList;
import org.eclipse.uml2.uml.AssociationClass;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Enumeration;
import org.eclipse.uml2.uml.EnumerationLiteral;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Type;
import org.pavanecce.uml.common.util.EmfOperationUtil;
import org.pavanecce.uml.common.util.EmfParameterUtil;
import org.pavanecce.uml.common.util.EmfPropertyUtil;
import org.pavanecce.uml.common.util.emulated.AssociationClassToEnd;
import org.pavanecce.uml.common.util.emulated.EndToAssociationClass;

public abstract class AbstractUmlVisitorAdaptor<PACKAGE, CLASSIFIER, BUILDER extends AbstractBuilder<PACKAGE, CLASSIFIER>> {

	protected static final class ElementComparator implements Comparator<Element> {
		@Override
		public int compare(Element o1, Element o2) {
			if (o1 instanceof NamedElement && o2 instanceof NamedElement) {
				return ((NamedElement) o1).getQualifiedName().compareTo(((NamedElement) o2).getQualifiedName());
			}
			return o1.hashCode() - o2.hashCode();
		}
	}

	private Map<String, Classifier> interfacesToImplement;

	private SortedSet<Model> models = new TreeSet<Model>(new ElementComparator());

	public AbstractUmlVisitorAdaptor() {
		this(new HashMap<String, Classifier>());

	}

	public AbstractUmlVisitorAdaptor(Map<String, Classifier> hashMap) {
		this.interfacesToImplement = hashMap;
	}

	protected abstract PACKAGE getCodeModel();

	protected String identifier(Operation operation) {
		return EmfParameterUtil.toIdentifyingString(operation);
	}

	public void startVisiting(BUILDER builder, Model model) {
		this.models.add(model);
		builder.initialize(models, getCodeModel());
		PACKAGE codeModel = builder.visitModel(model);
		visitNestedPackages(builder, model.getNestedPackages(), codeModel);
		visitTypes(builder, model.getOwnedTypes(), codeModel);
	}

	private void visitNestedPackages(BUILDER builder, EList<Package> nestedPackages, PACKAGE parent) {
		for (Package pkg : nestedPackages) {
			PACKAGE codePackage = builder.visitPackage(pkg, parent);
			visitNestedPackages(builder, pkg.getNestedPackages(), codePackage);
			visitTypes(builder, pkg.getOwnedTypes(), codePackage);
		}
	}

	private void visitTypes(BUILDER builder, EList<Type> ownedTypes, PACKAGE codePackage) {
		for (Type type : ownedTypes) {
			if (type instanceof Class) {
				Class cls = (Class) type;
				CLASSIFIER codeClass = builder.visitClass(cls, codePackage);
				Set<String> implementedProps = buildProperties(builder, cls, codeClass);
				buildAssociationClassProps(builder, cls, codeClass);
				buildPropertiesFromArtificialInterface(builder, cls, codeClass, implementedProps);
				Set<String> implementedOperations = buildOperations(builder, cls, codeClass);
				buildMethodsFromArtificialInterface(builder, cls, codeClass, implementedOperations);
			} else if (type instanceof Enumeration) {
				Enumeration cls = (Enumeration) type;
				CLASSIFIER codeClass = builder.visitEnum(cls, codePackage);
				buildProperties(builder, cls, codeClass);
				buildLilterals(builder, cls, codeClass);
				buildOperations(builder, cls, codeClass);
			}
		}
	}

	private void buildLilterals(BUILDER builder, Enumeration cls, CLASSIFIER codeClass) {
		for (EnumerationLiteral el : cls.getOwnedLiterals()) {
			builder.visitEnumerationLiteral(el, codeClass);
		}
	}

	private Set<String> buildOperations(BUILDER builder, Classifier cls, CLASSIFIER codeClass) {
		Set<String> implementedOperations = new HashSet<String>();
		for (Operation operation : EmfOperationUtil.getDirectlyImplementedOperations(cls)) {
			builder.visitOperation(operation, codeClass);
			implementedOperations.add(identifier(operation));
		}
		return implementedOperations;
	}

	private void buildMethodsFromArtificialInterface(BUILDER builder, Class cls, CLASSIFIER codeClass, Set<String> implementedOperations) {
		Classifier toImplement2 = this.interfacesToImplement.get(cls.getQualifiedName());
		if (toImplement2 != null) {
			Collection<Operation> attributes = EmfOperationUtil.getDirectlyImplementedOperations(toImplement2);
			for (Operation operation : attributes) {
				if (!implementedOperations.contains(identifier(operation))) {
					builder.visitOperation(operation, codeClass);
				}
			}
		}
	}

	private void buildPropertiesFromArtificialInterface(BUILDER builder, Class cls, CLASSIFIER codeClass, Set<String> implementedProps) {
		Classifier toImplement = this.interfacesToImplement.get(cls.getQualifiedName());
		if (toImplement != null) {
			Collection<Property> attributes = EmfPropertyUtil.getEffectiveProperties(toImplement);
			for (Property property : attributes) {
				if (!implementedProps.contains(property.getName())) {
					builder.visitProperty(property, codeClass);
				}
			}
			doArtificialInterfaceImplementation(codeClass, toImplement, builder);
		}
	}

	private void buildAssociationClassProps(BUILDER builder, Class cls, CLASSIFIER codeClass) {
		if (cls instanceof AssociationClass) {
			EList<Property> memberEnds = ((AssociationClass) cls).getMemberEnds();
			for (Property property : memberEnds) {
				AssociationClassToEnd etac = new AssociationClassToEnd(property);
				etac.setOtherEnd(new EndToAssociationClass(property));
				builder.visitProperty(etac, codeClass);
			}
		}
	}

	private Set<String> buildProperties(BUILDER builder, Classifier cls, CLASSIFIER codeClass) {
		Set<String> implementedProps = new HashSet<String>();
		for (Property property : EmfPropertyUtil.getDirectlyImplementedAttributes(cls)) {
			if (property.getAssociation() instanceof AssociationClass) {
				EndToAssociationClass etac = new EndToAssociationClass(property);
				etac.setOtherEnd(new AssociationClassToEnd(property));
				builder.visitProperty(etac, codeClass);
			} else {
				builder.visitProperty(property, codeClass);
			}
			implementedProps.add(property.getName());
		}
		return implementedProps;
	}

	protected abstract void doArtificialInterfaceImplementation(CLASSIFIER codeClass, Classifier toImplement, BUILDER b);
}