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

public abstract class AbstractUmlVisitorAdaptor<PACKAGE, CLASS, BUILDER extends AbstractBuilder<PACKAGE, CLASS>> {

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
				CLASS codeClass = builder.visitClass(cls, codePackage);
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
				if (cls instanceof AssociationClass) {
					EList<Property> memberEnds = ((AssociationClass) cls).getMemberEnds();
					for (Property property : memberEnds) {
						AssociationClassToEnd etac = new AssociationClassToEnd(property);
						etac.setOtherEnd(new EndToAssociationClass(property));
						builder.visitProperty(etac, codeClass);
					}
				}
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
				Set<String> implementedOperations = new HashSet<String>();
				for (Operation operation : EmfOperationUtil.getDirectlyImplementedOperations(cls)) {
					builder.visitOperation(operation, codeClass);
					implementedOperations.add(identifier(operation));
				}
				if (toImplement != null) {
					Collection<Operation> attributes = EmfOperationUtil.getDirectlyImplementedOperations(toImplement);
					for (Operation operation : attributes) {
						if (!implementedOperations.contains(identifier(operation))) {
							builder.visitOperation(operation, codeClass);
						}
					}
				}
			}
		}
	}

	protected abstract void doArtificialInterfaceImplementation(CLASS codeClass, Classifier toImplement, BUILDER b);
}