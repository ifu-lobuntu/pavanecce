package org.pavanecce.uml.ocl2code.maps;

import org.eclipse.uml2.uml.Property;
import org.pavanecce.uml.common.util.emulated.EmulatedPropertyHolderForAssociation;
import org.pavanecce.uml.ocl2code.common.UmlToCodeMaps;

public class AssociationClassEndMap {
	private PropertyMap map;
	private PropertyMap assocationClassToOtherEndMap;
	private PropertyMap assocationClassToThisEndMap;
	private PropertyMap endToAssocationClassMap;
	private PropertyMap otherEndToAssocationClassMap;

	public AssociationClassEndMap(UmlToCodeMaps ojUtil, Property p) {
		map = ojUtil.buildStructuralFeatureMap(p);
		EmulatedPropertyHolderForAssociation ephfa = ojUtil.getEmulatedPropertyHolder(p.getAssociation());
		assocationClassToOtherEndMap = ojUtil.buildStructuralFeatureMap(ephfa.getEmulatedAttribute(p));
		endToAssocationClassMap = ojUtil.buildStructuralFeatureMap(ephfa.getEndToAssociation(p));
		assocationClassToThisEndMap = ojUtil.buildStructuralFeatureMap(ephfa.getEmulatedAttribute(p.getOtherEnd()));
		otherEndToAssocationClassMap = ojUtil.buildStructuralFeatureMap(ephfa.getEndToAssociation(p.getOtherEnd()));
	}

	public PropertyMap getMap() {
		return map;
	}

	public PropertyMap getEndToAssocationClassMap() {
		return endToAssocationClassMap;
	}

	public PropertyMap getOtherEndToAssocationClassMap() {
		return otherEndToAssocationClassMap;
	}

	public PropertyMap getAssocationClassToOtherEndMap() {
		return assocationClassToOtherEndMap;
	}

	public PropertyMap getAssociationClassToThisEndMap() {
		return assocationClassToThisEndMap;
	}
}
