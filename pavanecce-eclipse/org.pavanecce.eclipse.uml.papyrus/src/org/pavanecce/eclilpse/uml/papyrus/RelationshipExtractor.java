package org.pavanecce.eclilpse.uml.papyrus;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.uml2.uml.Association;
import org.eclipse.uml2.uml.AssociationClass;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.DirectedRelationship;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.util.UMLSwitch;
import org.pavanecce.eclipse.uml.visualization.RelationshipDirection;

public class RelationshipExtractor extends UMLSwitch<Object> {
	private Set<Element> relationships = new HashSet<Element>();
	private Set<Element> nodeElements = new HashSet<Element>();
	private List<EClass> relationshipTypes;
	private RelationshipDirection direction;

	public RelationshipExtractor(EClass[] relationshipTypes, RelationshipDirection d) {
		this.relationshipTypes = Arrays.asList(relationshipTypes);
		this.direction = d;
	}

	private boolean forward() {
		return direction == RelationshipDirection.BOTH || direction == RelationshipDirection.FORWARD;
	}

	private boolean backward() {
		return direction == RelationshipDirection.BOTH || direction == RelationshipDirection.BACKWARD;
	}

	@Override
	public Object caseClassifier(Classifier c) {
		if (relationshipTypes.contains(UMLPackage.eINSTANCE.getAssociation())) {
			if (direction != RelationshipDirection.NONE) {
				for (Association association : c.getAssociations()) {
					addAssociation(association);
				}
			}
			if (c instanceof Association) {
				Association s = (Association) c;
				addAssociation(s);
			} else {
				nodeElements.add(c);
			}
		}
		// TODO navigability
		return super.caseClassifier(c);
	}

	@Override
	public Object caseElement(Element object) {
		addGenericDirectedRelationships(object);
		return super.caseElement(object);
	}

	protected void addGenericDirectedRelationships(Element c) {
		nodeElements.add(c);
		List<EClass> list = this.relationshipTypes;
		for (EClass eClass : list) {
			if (UMLPackage.eINSTANCE.getDirectedRelationship().isSuperTypeOf(eClass)) {
				if (backward()) {
					EList<DirectedRelationship> toMe = c.getTargetDirectedRelationships(eClass);
					for (DirectedRelationship r : toMe) {
						if (!getRelationships().contains(r)) {
							getRelationships().add(r);
							Element source = r.getSources().get(0);
							addGenericDirectedRelationships(source);
						}
					}
				}
				if (forward()) {
					EList<DirectedRelationship> fromMe = c.getSourceDirectedRelationships(eClass);
					for (DirectedRelationship r : fromMe) {
						if (!getRelationships().contains(r)) {
							getRelationships().add(r);
							Element target = r.getTargets().get(0);
							addGenericDirectedRelationships(target);
						}
					}
				}
			}
		}
	}

	protected void addAssociation(Association s) {
		getRelationships().add(s);
		nodeElements.addAll(s.getEndTypes());
		if (s instanceof AssociationClass) {
			nodeElements.add(s);
		}
	}

	public Set<Element> getRelationships() {
		return relationships;
	}

	public Set<Element> getNodeElements() {
		return nodeElements;
	}
}
