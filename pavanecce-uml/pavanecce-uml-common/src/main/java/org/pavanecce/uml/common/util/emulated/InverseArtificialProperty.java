package org.pavanecce.uml.common.util.emulated;

import org.eclipse.ocl.uml.MessageType;
import org.eclipse.uml2.uml.AggregationKind;
import org.eclipse.uml2.uml.Behavior;
import org.eclipse.uml2.uml.BehavioredClassifier;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.LiteralUnlimitedNatural;
import org.pavanecce.common.util.NameConverter;
import org.pavanecce.uml.common.util.EmfWorkspace;

@SuppressWarnings("restriction")
public class InverseArtificialProperty extends AbstractEmulatedProperty {
	private NonInverseArtificialProperty otherEnd;

	public InverseArtificialProperty(BehavioredClassifier context, Behavior behavior) {
		super(context, behavior);
		setType(behavior);
	}

	public InverseArtificialProperty(Classifier nestingClassifier, Classifier type) {
		super(nestingClassifier, type);
		setType(type);
	}

	public InverseArtificialProperty(Classifier owner, MessageType msg) {
		super(owner, msg);
		setType(msg);
	}

	@Override
	public Classifier getOwner() {
		return (Classifier) super.getOwner();
	}

	@Override
	public String getId() {
		return EmfWorkspace.getId(getOriginalElement()) + "@P";
	}

	@Override
	public boolean isComposite() {
		return true;
	}

	@Override
	public AggregationKind getAggregation() {
		return AggregationKind.COMPOSITE_LITERAL;
	}

	@Override
	public boolean isOrdered() {
		return getOriginalElement() instanceof Behavior || getOriginalElement() instanceof MessageType;
	}

	@Override
	public int getUpper() {
		if (isClassifierBehavior()) {
			return 1;
		} else {
			return LiteralUnlimitedNatural.UNLIMITED;
		}
	}

	private boolean isClassifierBehavior() {
		if (getOriginalElement() instanceof Behavior) {
			Behavior behavior = (Behavior) getOriginalElement();
			setType(behavior);
			return behavior.getContext() != null && behavior == behavior.getContext().getClassifierBehavior();
		} else {
			return false;
		}
	}

	@Override
	public String getName() {
		return NameConverter.decapitalize(getOriginalElement().getName());
	}

	@Override
	public NonInverseArtificialProperty getOtherEnd() {
		if (otherEnd == null) {
			initialiseOtherEnd();
		}
		return otherEnd;
	}

	public NonInverseArtificialProperty initialiseOtherEnd() {
		if (getType() instanceof Behavior && ((Behavior) getType()).getContext() == owner) {
			otherEnd = new NonInverseArtificialProperty(this, "contextObject");
		} else if (getType() instanceof Classifier && ((Classifier) getType()).getOwner() == owner) {
			otherEnd = new NonInverseArtificialProperty(this, "ownerObject");
		}
		return otherEnd;
	}

	@Override
	public boolean equals(Object other) {
		if (other == this) {
			return true;
		} else if (other instanceof InverseArtificialProperty) {
			InverseArtificialProperty ap = (InverseArtificialProperty) other;
			return ap.originalElement.equals(originalElement) && ap.owner.equals(owner) && ap.name.equals(name);
		} else {
			return super.equals(other);
		}
	}

	@Override
	public boolean shouldEmulate() {
		if (getType() instanceof IEmulatedElement) {
			return ((IEmulatedElement) getType()).shouldEmulate();
		}
		return true;
	}
}
