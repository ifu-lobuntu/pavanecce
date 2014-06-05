package org.pavanecce.uml.common.util.emulated;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.uml2.uml.Association;
import org.eclipse.uml2.uml.Property;

public class EmulatedPropertyHolderForAssociation extends EmulatedPropertyHolder {
	Association owner;
	private List<EndToAssociationClass> endsToAssociationClass = new ArrayList<EndToAssociationClass>();

	public EmulatedPropertyHolderForAssociation(Association owner, IPropertyEmulation e) {
		super(owner, e);
		this.owner = owner;
		for (Property p : owner.getMemberEnds()) {
			AssociationClassToEnd otherEnd = new AssociationClassToEnd(p);
			super.addEmulatedAttribute(otherEnd);
		}
		for (Property p : owner.getMemberEnds()) {
			EndToAssociationClass thisEnd = new EndToAssociationClass(p);
			endsToAssociationClass.add(thisEnd);
			Property emulatedAttribute = getEmulatedAttribute(p.getOtherEnd());
			AssociationClassToEnd associationToEnd = (AssociationClassToEnd) emulatedAttribute;
			thisEnd.setOtherEnd(associationToEnd);
			associationToEnd.setOtherEnd(thisEnd);
		}
	}

	public EndToAssociationClass getEndToAssociation(Property property) {
		for (EndToAssociationClass p : this.endsToAssociationClass) {
			if (p.getOriginalProperty() == property) {
				return p;
			}
		}
		return null;
	}
}
