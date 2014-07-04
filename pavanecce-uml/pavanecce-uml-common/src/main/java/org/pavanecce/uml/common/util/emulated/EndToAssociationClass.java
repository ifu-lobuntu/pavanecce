package org.pavanecce.uml.common.util.emulated;

import org.eclipse.emf.common.util.EList;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Type;
import org.eclipse.uml2.uml.ValueSpecification;
import org.pavanecce.common.util.NameConverter;
import org.pavanecce.uml.common.util.EmfAssociationUtil;
import org.pavanecce.uml.common.util.EmfWorkspace;

public class EndToAssociationClass extends AbstractEmulatedProperty {
	private Property property;
	private AssociationClassToEnd otherEnd;

	public EndToAssociationClass(Property property) {
		super((Classifier) property.getOtherEnd().getType(), (Classifier) property.getAssociation());
		this.property = property;
	}

	@Override
	public boolean isDerived() {
		return property.isDerived();
	}

	@Override
	public boolean isDerivedUnion() {
		return property.isDerivedUnion();
	}

	@Override
	public int getUpper() {
		return property.getUpper();
	}

	@Override
	public int getLower() {
		return property.getLower();
	}

	@Override
	public ValueSpecification getUpperValue() {
		return property.getUpperValue();
	}

	@Override
	public ValueSpecification getLowerValue() {
		return property.getLowerValue();
	}

	@Override
	public EList<Property> getQualifiers() {
		return property.getQualifiers();
	}

	@Override
	public boolean isMultivalued() {
		return property.isMultivalued();
	}

	@Override
	public boolean isNavigable() {
		return property.isNavigable();
	}

	@Override
	public boolean isComposite() {
		if (property.isComposite()) {
			return true;
		} else {
			return property == property.getAssociation().getMemberEnds().get(0);
		}
	}

	@Override
	public String getName() {
		return NameConverter.decapitalize(property.getAssociation().getName());// + "_" + property.getOtherEnd().getName();
	}

	@Override
	public boolean isOrdered() {
		return property.isOrdered();
	}

	@Override
	public boolean isUnique() {
		return property.isUnique();
	}

	@Override
	public Type getType() {
		return property.getAssociation();
	}

	public int getIndexInAssocation() {
		return property.getAssociation().getMemberEnds().indexOf(property);
	}

	@Override
	public String getId() {
		return EmfWorkspace.getId(property.getOtherEnd().getType()) + "@" + EmfWorkspace.getId(property.getAssociation());
	}

	@Override
	public boolean shouldEmulate() {
		return EmfAssociationUtil.isClass(property.getAssociation());
	}

	public void setOtherEnd(AssociationClassToEnd otherEnd) {
		this.otherEnd = otherEnd;
	}

	@Override
	public Property getOtherEnd() {
		return otherEnd;
	}

	public Property getOriginalProperty() {
		return property;
	}

}
