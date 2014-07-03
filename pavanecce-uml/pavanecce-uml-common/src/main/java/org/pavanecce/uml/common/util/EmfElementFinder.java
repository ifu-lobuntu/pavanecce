package org.pavanecce.uml.common.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.impl.DynamicEObjectImpl;
import org.eclipse.uml2.uml.Action;
import org.eclipse.uml2.uml.Activity;
import org.eclipse.uml2.uml.Association;
import org.eclipse.uml2.uml.Behavior;
import org.eclipse.uml2.uml.CallEvent;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Constraint;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Event;
import org.eclipse.uml2.uml.Generalization;
import org.eclipse.uml2.uml.InterfaceRealization;
import org.eclipse.uml2.uml.Namespace;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Parameter;
import org.eclipse.uml2.uml.ParameterDirectionKind;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.SignalEvent;
import org.eclipse.uml2.uml.State;
import org.eclipse.uml2.uml.StructuredActivityNode;
import org.eclipse.uml2.uml.Transition;
import org.eclipse.uml2.uml.Trigger;
import org.eclipse.uml2.uml.TypedElement;
import org.eclipse.uml2.uml.UMLFactory;
import org.eclipse.uml2.uml.util.UMLUtil;
import org.pavanecce.uml.common.util.emulated.EndToAssociationClass;
import org.pavanecce.uml.common.util.emulated.IEmulatedElement;

public class EmfElementFinder {
	public static Namespace getNearestNamespace(Element ns) {
		Element parent = (Element) getContainer(ns);
		while (!(parent instanceof Namespace || parent == null)) {
			parent = (Element) getContainer(parent);
		}
		return (Namespace) parent;
	}

	public static Classifier getNearestClassifier(Element e) {
		if (e instanceof Classifier) {
			return (Classifier) e;
		} else if (e == null) {
			return null;
		} else {
			return getNearestClassifier((Element) getContainer(e));
		}
	}

	public static EObject getContainer(EObject s) {
		if (s instanceof EndToAssociationClass) {
			return ((EndToAssociationClass) s).getOtherEnd().getType();
		} else if (s == null) {
			return null;
		} else if (s instanceof IEmulatedElement) {
			return getContainer(((IEmulatedElement) s).getOriginalElement());
		} else if (s instanceof DynamicEObjectImpl) {
			return UMLUtil.getBaseElement(s);
		} else if (s.eContainer() instanceof DynamicEObjectImpl) {
			return UMLUtil.getBaseElement(s.eContainer());
		} else if (s.eContainer() instanceof EAnnotation) {
			return ((EAnnotation) s.eContainer()).getEModelElement();
		} else if (s instanceof Property && s.eContainer() instanceof Association) {
			Property p = (Property) s;
			if (p.getOtherEnd() != null && p.isNavigable()) {
				return p.getOtherEnd().getType();
			} else {
				return s.eContainer();
			}
		} else if (s instanceof InterfaceRealization) {
			return ((InterfaceRealization) s).getImplementingClassifier();
		} else if (s instanceof Generalization) {
			return ((Generalization) s).getSpecific();
		}
		return s.eContainer();
	}

	public static List<TypedElement> getTypedElementsInScope(Element behavioralElement) {
		List<TypedElement> result = new ArrayList<TypedElement>();
		if (behavioralElement != null) {
			Element a = behavioralElement;
			if (a instanceof Constraint) {
				if (a.getOwner() instanceof Action) {
					Action act = (Action) a.getOwner();
					result.addAll(act.getInputs());
					if (act.getLocalPostconditions().contains(a)) {
						result.addAll(act.getOutputs());
					}
					return result;
				} else if (a.getOwner() instanceof Operation) {
					Operation oper = (Operation) a.getOwner();
					for (Parameter parameter : oper.getOwnedParameters()) {
						if (parameter.getDirection() == ParameterDirectionKind.IN_LITERAL || parameter.getDirection() == ParameterDirectionKind.INOUT_LITERAL) {
							result.add(parameter);
						} else if (oper.getPostconditions().contains(a)) {
							result.add(parameter);
						}
					}
				}
			} else if (a instanceof Operation) {
				Operation oper = (Operation) a;
				for (Parameter parameter : oper.getOwnedParameters()) {
					if (parameter.getDirection() == ParameterDirectionKind.IN_LITERAL || parameter.getDirection() == ParameterDirectionKind.INOUT_LITERAL) {
						result.add(parameter);
					}
				}
			}
			do {
				if (a instanceof StructuredActivityNode) {
					result.addAll(((StructuredActivityNode) a).getVariables());
				}
				if (a instanceof Transition) {
					addTransitionParameters(result, (Transition) a);
				}
				if (a instanceof Behavior) {
					result.addAll(((Behavior) a).getOwnedParameters());
					if (a instanceof Activity) {
						Activity activity = (Activity) a;
						result.addAll(activity.getVariables());
					}
					if (a.getOwner() instanceof Transition) {
						Transition owner = (Transition) a.getOwner();
						addTransitionParameters(result, owner);
						a = EmfStateMachineUtil.getNearestApplicableStateMachine(a.getOwner());
					} else if (a.getOwner() instanceof State) {
						a = EmfStateMachineUtil.getNearestApplicableStateMachine(a.getOwner());
					}
				}
				a = (Element) EmfElementFinder.getContainer(a);
			} while (!(a == null || a instanceof Classifier || a instanceof Operation));
			if (a != null) {
				result.addAll(getTypedElementsInScope(a));
			}
		}
		return result;
	}

	protected static void addTransitionParameters(List<TypedElement> result, Transition a) {
		EList<Trigger> triggers = a.getTriggers();
		if (triggers.size() > 0) {
			Event event = triggers.get(0).getEvent();
			if (event instanceof CallEvent) {
				result.addAll(((CallEvent) event).getOperation().getOwnedParameters());
			} else if (event instanceof SignalEvent) {
				for (Property p : ((SignalEvent) event).getSignal().getAllAttributes()) {
					// Create parameter to emulate parameter behavior in ocl, "self" would be invalid
					Parameter param = UMLFactory.eINSTANCE.createParameter();
					param.setType(p.getType());
					param.setName(p.getName());
					result.add(param);
				}
			}
		}
	}
}
