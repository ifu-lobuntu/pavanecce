package org.pavanecce.uml.common.util.emulated;

import org.eclipse.emf.common.util.EList;
import org.eclipse.ocl.uml.MessageType;
import org.eclipse.uml2.uml.Behavior;
import org.eclipse.uml2.uml.BehavioredClassifier;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.TypedElement;

public class EmulatedPropertyHolderForBehavioredClassifier extends EmulatedPropertyHolder {
	@SafeVarargs
	protected EmulatedPropertyHolderForBehavioredClassifier(BehavioredClassifier owner, IPropertyEmulation e, EList<? extends TypedElement>... typedElements) {
		super(owner, e, typedElements);
	}

	public EmulatedPropertyHolderForBehavioredClassifier(BehavioredClassifier owner, IPropertyEmulation e) {
		super(owner, e);
		EList<Behavior> ownedBehaviors = owner.getOwnedBehaviors();
		for (Behavior behavior : ownedBehaviors) {
			addEmulatedBehaviorProperty(behavior);
		}
	}

	private void addEmulatedBehaviorProperty(Behavior newValue) {
		InverseArtificialProperty iap = new InverseArtificialProperty((BehavioredClassifier) owner, newValue);
		iap.initialiseOtherEnd();
		Classifier otherType = iap.getOtherEnd().getOwner();
		if (!(otherType instanceof MessageType)) {
			emulation.getEmulatedPropertyHolder(otherType).addEmulatedAttribute(iap.getOtherEnd());
		}
		getEmulatedAttributes().add(iap);
	}
}
