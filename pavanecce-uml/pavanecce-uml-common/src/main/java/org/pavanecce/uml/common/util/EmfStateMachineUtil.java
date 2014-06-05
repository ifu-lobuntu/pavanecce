package org.pavanecce.uml.common.util;

import java.util.Collection;
import java.util.TreeSet;

import org.eclipse.emf.common.util.EList;
import org.eclipse.uml2.uml.BehavioredClassifier;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Region;
import org.eclipse.uml2.uml.State;
import org.eclipse.uml2.uml.StateMachine;
import org.eclipse.uml2.uml.Transition;
import org.eclipse.uml2.uml.Vertex;

public class EmfStateMachineUtil {
	public static String getStatePath(Vertex v) {
		StringBuilder sb = new StringBuilder();
		maybeAppendParent(v, sb);
		sb.append(v.getName());
		return sb.toString();
	}

	private static void maybeAppendParent(Vertex v, StringBuilder sb) {
		if (v.getContainer().getState() != null) {
			sb.append(v.getContainer().getState().getName());
			sb.append("::");
			maybeAppendParent(v.getContainer().getState(), sb);
		}
	}

	public static Collection<Transition> getTransitionsRecursively(StateMachine ns) {
		Collection<Transition> result = new TreeSet<Transition>(new DefaultElementComparator());
		EList<Region> regions = ns.getRegions();
		addTransitionsRecursively(result, regions);
		return result;
	}

	private static void addTransitionsRecursively(Collection<Transition> result, EList<Region> regions) {
		for (Region region : regions) {
			result.addAll(region.getTransitions());
			for (Vertex vertex : region.getSubvertices()) {
				if (vertex instanceof State) {
					addTransitionsRecursively(result, ((State) vertex).getRegions());
				}
			}
		}
	}

	public static StateMachine getNearestApplicableStateMachine(Element s) {
		while (!(s instanceof StateMachine || s == null)) {
			if (s instanceof BehavioredClassifier && ((BehavioredClassifier) s).getClassifierBehavior() instanceof StateMachine) {
				s = ((BehavioredClassifier) s).getClassifierBehavior();
			} else {
				s = (Element) EmfElementFinder.getContainer(s);
			}
		}
		return (StateMachine) s;
	}

}
