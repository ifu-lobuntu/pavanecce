package org.pavanecce.uml.common.util;

import java.util.Set;
import java.util.TreeSet;

import org.eclipse.emf.common.util.EList;
import org.eclipse.uml2.uml.Behavior;
import org.eclipse.uml2.uml.BehavioredClassifier;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.State;
import org.eclipse.uml2.uml.Transition;

public class EmfBehaviorUtil {
	public static BehavioredClassifier getContext(Element behavioralElement) {
		while (!(behavioralElement instanceof Behavior || behavioralElement == null)) {
			behavioralElement = (Element) EmfElementFinder.getContainer(behavioralElement);
		}
		if (behavioralElement instanceof Behavior) {
			Behavior behavior = (Behavior) behavioralElement;
			if (behavior.getOwner() instanceof Transition || behavior.getOwner() instanceof State) {
				return getContext(behavior.getOwner());
			} else if (behavior.getContext() != null) {
				return behavior.getContext();
			}
		}
		return null;
	}

	public static Classifier getSelf(Element behavioralElement) {
		while (!(behavioralElement instanceof Classifier || behavioralElement == null)) {
			behavioralElement = (Element) EmfElementFinder.getContainer(behavioralElement);
		}
		if (behavioralElement instanceof Behavior) {
			Behavior behavior = (Behavior) behavioralElement;
			if (behavioralElement.getOwner() instanceof Transition || behavioralElement.getOwner() instanceof State) {
				return EmfStateMachineUtil.getNearestApplicableStateMachine(behavioralElement.getOwner());
			} else if (behavior.getContext() != null) {
				return behavior.getContext();
			} else {
				return behavior;
			}
		}
		if (behavioralElement instanceof Classifier) {
			return (Classifier) behavioralElement;
		}
		return null;
	}

	public static Set<Behavior> getEffectiveBehaviors(BehavioredClassifier context) {
		TreeSet<Behavior> operations = new TreeSet<Behavior>(new DefaultElementComparator());
		addBehaviors(operations, context);
		return operations;
	}

	public static void addBehaviorsRecursively(Set<Behavior> behaviors, EList<Classifier> generals) {
		for (Classifier c : generals) {
			addBehaviors(behaviors, c);
		}
	}

	static void addBehaviors(Set<Behavior> behaviors, Classifier c) {
		if (c instanceof BehavioredClassifier) {
			behaviors.addAll(((BehavioredClassifier) c).getOwnedBehaviors());
			addBehaviorsRecursively(behaviors, c.getGenerals());
		}
	}

}
