package org.pavanecce.uml.reverse.owl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.uml2.uml.AggregationKind;
import org.eclipse.uml2.uml.Association;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.PrimitiveType;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.UMLPackage;
import org.pavanecce.common.util.NameConverter;
import org.pavanecce.uml.common.util.ProfileApplier;
import org.pavanecce.uml.common.util.UmlResourceSetFactory;
import org.pavanecce.uml.reverse.java.AbstractUmlGenerator;
import org.pavanecce.uml.reverse.java.IProgressMonitor;
import org.pavanecce.uml.reverse.java.sourcemodel.SourceClass;
import org.pavanecce.uml.reverse.java.sourcemodel.SourceProperty;

public class UmlGeneratorFromOwl extends AbstractUmlGenerator {
	@Override
	public Collection<Element> generateUmlImpl(Set<SourceClass> selection, Package library, IProgressMonitor m) throws Exception {
		m.beginTask("Importing Owl Entities", selection.size());
		ProfileApplier.applyProfile((Model) library, UmlResourceSetFactory.VDFP_MAPPING_PROFILE);
		for (SourceClass t : selection) {
			if (!m.isCanceled()) {
				// Only do annotations in profiles
				Classifier cls = factory.getClassifierFor(t);
				populateAttributes(library, cls, t);
				populateOperations(library, cls, t);
			}
			m.worked(1);
		}
		m.done();
		Collection<Element> result = new HashSet<Element>();
		for (Classifier c : factory.getNewClassifiers()) {
			result.add(c);
			result.addAll(c.getAssociations());
		}
		return result;
	}

	@Override
	protected boolean canReverse(Package library) {
		return library instanceof Model;
	}

	private Association createAssociation(Classifier cls, SourceProperty pd) {
		Classifier baseType = factory.getClassifierFor(pd.getBaseType());
		Association assoc = null;
		String assocName = null;
		if (pd.getOtherEnd() == null) {
			assocName = cls.getName() + "References" + NameConverter.capitalize(pd.getName());
		} else if (pd.getOtherEnd().isComposite()) {
			assocName = baseType.getName() + "Has" + NameConverter.capitalize(pd.getOtherEnd().getName());
		} else if (pd.isComposite()) {
			assocName = cls.getName() + "Has" + NameConverter.capitalize(pd.getName());
		} else if (pd.isMany() && !pd.getOtherEnd().isMany()) {
			assocName = cls.getName() + "Has" + NameConverter.capitalize(pd.getName());
		} else if (pd.getOtherEnd().isMany() && !pd.isMany()) {
			assocName = baseType.getName() + "Has" + NameConverter.capitalize(pd.getOtherEnd().getName());
		} else {
			if (cls.getName().compareTo(baseType.getName()) > 0) {
				assocName = baseType.getName() + "Has" + NameConverter.capitalize(pd.getOtherEnd().getName());
			} else {
				assocName = cls.getName() + "Has" + NameConverter.capitalize(pd.getName());
			}
		}
		assoc = (Association) cls.getNearestPackage().getOwnedType(assocName, true, UMLPackage.eINSTANCE.getAssociation(), true);
		return assoc;
	}

	@Override
	protected Property findProperty(Classifier classifier, SourceProperty pd) {
		Property r = findProperty(pd, classifier.getAttributes());
		if (r == null) {
			outer: for (Association a : classifier.getAssociations()) {
				for (Property m : a.getMemberEnds()) {
					if (m.getOtherEnd() != null && m.getOtherEnd().getType().equals(classifier) && m.getName().equals(pd.getName())) {
						r = m;
						break outer;
					}
				}
			}
		}
		return r;
	}

	@Override
	protected Property createAttribute(Classifier cls, SourceProperty pd) {
		Classifier baseType = factory.getClassifierFor(pd.getBaseType());
		if (baseType instanceof PrimitiveType) {
			Property attr = super.createAttribute(cls, pd);
			setLower(pd, attr);
			return attr;
		} else {
			Association assoc = createAssociation(cls, pd);
			createEnd(baseType, assoc, pd.getOtherEnd(), cls);
			Property thisEnd = createEnd(cls, assoc, pd, baseType);
			return thisEnd;
		}
	}

	private Property createEnd(Classifier owner, Association assoc, SourceProperty end, Classifier baseType) {
		Property otherEnd;
		if (end == null) {

			otherEnd = assoc.getOwnedEnd(NameConverter.decapitalize(baseType.getName()), baseType, true, UMLPackage.eINSTANCE.getProperty(), true);

		} else if (end.isComposite()) {

			otherEnd = super.createAttribute(owner, end);

			assoc.getMemberEnds().add(otherEnd);
			otherEnd.setAggregation(AggregationKind.COMPOSITE_LITERAL);
			otherEnd.setIsNavigable(true);
		} else {

			otherEnd = assoc.getNavigableOwnedEnd(end.getName(), baseType, true, UMLPackage.eINSTANCE.getProperty(), true);

		}
		if (end == null || end.isMany()) {
			otherEnd.setUpper(-1);
			otherEnd.setLower(0);
		} else {
			setLower(end, otherEnd);
		}
		return otherEnd;
	}

	public void setLower(SourceProperty end, Property otherEnd) {
		boolean required = isRequired(end);
		if (required) {
			otherEnd.setLower(1);
		} else {
			otherEnd.setLower(0);
		}
	}

	private boolean isRequired(SourceProperty end) {
		throw new RuntimeException("Not implemented");
	}

}
