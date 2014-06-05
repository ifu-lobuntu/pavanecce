package org.pavanecce.uml.common.util.emulated;

import org.eclipse.emf.common.util.EList;
import org.eclipse.uml2.uml.Behavior;
import org.eclipse.uml2.uml.TypedElement;

public class EmulatedPropertyHolderForBehavior extends EmulatedPropertyHolderForBehavioredClassifier {
	public EmulatedPropertyHolderForBehavior(Behavior owner, IPropertyEmulation e) {
		super(owner, e, owner.getOwnedParameters());
		// getEmulatedAttributes().addAll(EmfTimeUtil.buildObservationPropertiess(owner, e, owner));
	}

	@SafeVarargs
	protected EmulatedPropertyHolderForBehavior(Behavior owner, IPropertyEmulation e, EList<? extends TypedElement>... typedElements) {
		super(owner, e, typedElements);
		// getEmulatedAttributes().addAll(EmfTimeUtil.buildObservationPropertiess(owner, e, owner));
	}

}
