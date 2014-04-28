package org.pavanecce.uml.common.util;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.event.ChangeEvent;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature.Setting;
import org.eclipse.emf.ecore.util.ECrossReferenceAdapter;
import org.eclipse.uml2.uml.AcceptEventAction;
import org.eclipse.uml2.uml.Action;
import org.eclipse.uml2.uml.Behavior;
import org.eclipse.uml2.uml.BehavioredClassifier;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.EnumerationLiteral;
import org.eclipse.uml2.uml.Event;
import org.eclipse.uml2.uml.Interface;
import org.eclipse.uml2.uml.Namespace;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Reception;
import org.eclipse.uml2.uml.Signal;
import org.eclipse.uml2.uml.SignalEvent;
import org.eclipse.uml2.uml.StateMachine;
import org.eclipse.uml2.uml.Stereotype;
import org.eclipse.uml2.uml.StructuredActivityNode;
import org.eclipse.uml2.uml.TimeEvent;
import org.eclipse.uml2.uml.Transition;
import org.eclipse.uml2.uml.Trigger;

public class EmfEventUtil {
	public static boolean hasReceptionOrTriggerFor(BehavioredClassifier bc, Signal s) {
		EList<Classifier> generalizations = bc.getGenerals();
		for (Classifier generalization : generalizations) {
			if (generalization instanceof BehavioredClassifier && hasReceptionOrTriggerFor((BehavioredClassifier) generalization, s)) {
				return true;
			}
		}
		for (Interface interface1 : bc.getImplementedInterfaces()) {
			if (hasReceptionOrTriggerFor(interface1, s)) {
				return true;
			}
		}
		if (bc instanceof Class && hasReceptionFor(s, ((Class) bc).getOwnedReceptions())) {
			return true;
		} else {
			return hasTriggerFor(bc, s);
		}
	}

	private static boolean hasTriggerFor(BehavioredClassifier bc, Signal s) {
		Collection<Event> allEvents = getEventsInScopeForClassAsContext(bc);
		for (Event event : allEvents) {
			if (event instanceof SignalEvent && s.equals(((SignalEvent) event).getSignal())) {
				return true;
			}
		}
		return false;
	}

	public static Collection<Event> getEventsInScopeForClassAsContext(BehavioredClassifier bc) {
		Set<Behavior> effectiveBehaviors = EmfBehaviorUtil. getEffectiveBehaviors(bc);
		Collection<Event> allEvents = new TreeSet<Event>(new DefaultElementComparator());
		for (Behavior behavior : effectiveBehaviors) {
			if (behavior instanceof StateMachine) {
				allEvents.addAll(getAllEvents((StateMachine) behavior));
			}
		}
		return allEvents;
	}

	public static Collection<Event> getAllEvents(StateMachine sm) {
		Collection<Event> events = new TreeSet<Event>(new DefaultElementComparator());
		Collection<Transition> transitions = EmfStateMachineUtil.getTransitionsRecursively(sm);
		for (Transition transition : transitions) {
			EList<Trigger> triggers = transition.getTriggers();
			for (Trigger trigger : triggers) {
				events.add(trigger.getEvent());
			}
		}
		return events;
	}

	public static void addEffectiveBehaviors(BehavioredClassifier bc, List<Behavior> effectiveBehaviors) {
		EList<Classifier> generals = bc.getGenerals();
		for (Classifier classifier : generals) {
			if (classifier instanceof BehavioredClassifier) {
				addEffectiveBehaviors((BehavioredClassifier) classifier, effectiveBehaviors);
			}
		}
		effectiveBehaviors.addAll(bc.getOwnedBehaviors());
	}

	private static boolean hasReceptionOrTriggerFor(Interface interface1, Signal s) {
		EList<Classifier> generals = interface1.getGenerals();
		for (Classifier classifier : generals) {
			if (classifier instanceof Interface && hasReceptionOrTriggerFor((Interface) classifier, s)) {
				return true;
			}
		}
		return hasReceptionFor(s, interface1.getOwnedReceptions());
	}

	public static boolean hasReceptionFor(Interface i, Signal s) {
		return hasReceptionFor(s, i.getOwnedReceptions());
	}

	public static boolean hasReceptionFor(Class i, Signal s) {
		return hasReceptionFor(s, i.getOwnedReceptions());
	}

	private static boolean hasReceptionFor(Signal s, EList<Reception> ownedReceptions) {
		for (Reception reception : ownedReceptions) {
			if (s.equals(reception.getSignal())) {
				return true;
			}
		}
		return false;
	}

	public static Element getDeadlineOrigin(TimeEvent e) {
		EObject o = e;
		while (!(o instanceof Operation || o instanceof Action || o == null)) {
			o = EmfElementFinder.getContainer(o);
		}
		return (Element) o;
	}

	public static Trigger getTriggerSContext(Event e) {
		ECrossReferenceAdapter a = ECrossReferenceAdapter.getCrossReferenceAdapter(e);
		Trigger trigger = null;
		for (Setting setting : a.getNonNavigableInverseReferences(e)) {
			if (setting.getEObject() instanceof Trigger) {
				if (trigger != null) {
					// multiple refs - not an embedded event, but a standalone
					// event
					return null;
				} else {
					trigger = (Trigger) setting.getEObject();
				}
			}
		}
		return trigger;
	}

	public static boolean behaviorHasReceptionOrTriggerFor(Behavior behavior, Signal signal) {
		hasReceptionFor(signal, behavior.getOwnedReceptions());
		Collection<Event> allEvents = null;
		if (behavior instanceof StateMachine) {
			allEvents = getAllEvents((StateMachine) behavior);
		}
		if (allEvents != null) {
			for (Event event : allEvents) {
				if (event instanceof SignalEvent && signal.equals(((SignalEvent) event).getSignal())) {
					return true;
				}
			}
		}
		return false;
	}

	public static boolean containsTriggerType(AcceptEventAction aea, java.lang.Class<TimeEvent> class1) {
		EList<Trigger> triggers = aea.getTriggers();
		for (Trigger trigger : triggers) {
			if (trigger.getEvent() != null && class1.isInstance(trigger.getEvent())) {
				return true;
			}
		}
		return false;
	}

	public static EnumerationLiteral getTimeUnit(TimeEvent event) {
		for (Stereotype stereotype : event.getAppliedStereotypes()) {
			if (stereotype.getMember("timeUnit") != null) {
				return (EnumerationLiteral) event.getValue(stereotype, "timeUnit");
			}
		}
		return null;
	}

	public static Behavior getBehaviorContext(Event event) {
		Element o = EmfEventUtil.getTriggerSContext(event);
		while (!(o instanceof Behavior || o == null)) {
			o = (Element) EmfElementFinder.getContainer(o);
		}
		return (Behavior) o;
	}

	public static Namespace getBehavioralNamespaceContext(Event event) {
		Element o = EmfEventUtil.getTriggerSContext(event);
		while (!(o instanceof Behavior || o instanceof StructuredActivityNode || o == null)) {
			o = (Element) EmfElementFinder.getContainer(o);
		}
		return (Namespace) o;
	}

	public static Collection<Event> getEventsInScopeForClassAsBehavior(Behavior c) {
		if (c instanceof StateMachine) {
			return getAllEvents((StateMachine) c);
		}
		return Collections.emptySet();
	}

	public static boolean requiresEventRequest(AcceptEventAction action) {
		EList<Trigger> triggers = action.getTriggers();
		for (Trigger trigger : triggers) {
			if (trigger.getEvent() instanceof TimeEvent || trigger.getEvent() instanceof ChangeEvent) {
				return true;
			}
		}
		return false;
	}
}
