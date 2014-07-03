package org.pavanecce.uml.common.util;

import static org.pavanecce.uml.common.util.PersistentNameUtil.getPersistentName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.ocl.expressions.CollectionKind;
import org.eclipse.uml2.uml.Association;
import org.eclipse.uml2.uml.BehavioredClassifier;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.DataType;
import org.eclipse.uml2.uml.EnumerationLiteral;
import org.eclipse.uml2.uml.Generalization;
import org.eclipse.uml2.uml.Interface;
import org.eclipse.uml2.uml.MultiplicityElement;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Stereotype;
import org.eclipse.uml2.uml.TypedElement;
import org.pavanecce.uml.common.util.emulated.AssociationClassToEnd;
import org.pavanecce.uml.common.util.emulated.EndToAssociationClass;
import org.pavanecce.uml.common.util.emulated.InverseArtificialProperty;
import org.pavanecce.uml.common.util.emulated.NonInverseArtificialProperty;

public class EmfPropertyUtil {

	public static boolean isDerived(Property p) {
		return p.isDerived() || p.isDerivedUnion();
	}

	private static Property getImmediateEndToComposite(Classifier c) {
		Property result = null;
		for (Association association : c.getAssociations()) {
			for (Property property : association.getMemberEnds()) {
				if (property.getType() == c && property.isComposite() && property.getOtherEnd().isNavigable()) {
					result = property.getOtherEnd();
					break;
				}
			}
		}
		if (result == null) {
			for (Property p : c.getAttributes()) {
				if (p.getOtherEnd() != null && p.getOtherEnd().isComposite()) {
					result = p;
					break;
				}
			}
		}
		return result;
	}

	public static Property getEndToComposite(Classifier c) {
		Property result = getImmediateEndToComposite(c);
		if (result == null) {
			Iterator<Classifier> classes = c.getGenerals().iterator();
			while (classes.hasNext() && result == null) {
				result = getImmediateEndToComposite(classes.next());
			}
			if (result == null && c instanceof BehavioredClassifier) {
				Iterator<Interface> interfaces = ((BehavioredClassifier) c).getImplementedInterfaces().iterator();
				while (interfaces.hasNext() && result == null) {
					result = getImmediateEndToComposite(interfaces.next());
				}
			}
		}
		return result;
	}

	public static boolean isMarkedAsPrimaryKey(Property property) {
		Classifier owner = getOwningClassifier(property);
		for (Stereotype st : owner.getAppliedStereotypes()) {
			if (st.getMember("primaryKey") != null) {
				return property.equals(owner.getValue(st, "primaryKey"));
			}
		}
		return false;
	}

	public static boolean isMany(TypedElement te) {
		MultiplicityElement me = (MultiplicityElement) te;
		if (te instanceof Property) {
			if (((Property) te).getQualifiers().size() > 0) {
				return true;
			}
		}
		return me.isMultivalued();
	}

	public static List<Property> getEffectiveProperties(Classifier c) {
		List<Property> result = new ArrayList<Property>();
		HashMap<String, Property> nameMap = new HashMap<String, Property>();
		addAttributesTo(c, nameMap, result);
		addAssociationEndsTo(c, nameMap, result);
		return result;
	}

	public static Set<Property> getDirectlyImplementedAttributes(Classifier c) {
		// NB remember that there might be properties specified by this class'
		// interfaces that have already been implemented by a superclass
		Set<String> inheritedConcretePropertyNames = new TreeSet<String>();
		for (Generalization g : c.getGeneralizations()) {
			for (Property p : getEffectiveProperties(g.getGeneral())) {
				inheritedConcretePropertyNames.add(p.getName());
			}
		}
		Set<Property> results = new TreeSet<Property>(new DefaultElementComparator());
		List<Property> effectiveAttributes = EmfPropertyUtil.getEffectiveProperties(c);
		for (Property p : effectiveAttributes) {
			if (EmfPropertyUtil.getOwningClassifier(p) == c || !inheritedConcretePropertyNames.contains(p.getName())) {
				for (Property rp : p.getRedefinedProperties()) {
					if (!rp.getName().equals(p.getName())) {
						// We need to redeclare the redefined properties to
						// ensure that their accessors are available in the
						// subclass for redefinition
						results.add(rp);
					}
				}
				for (Property rp : p.getSubsettedProperties()) {
					if (!rp.getName().equals(p.getName()) && !inheritedConcretePropertyNames.contains(rp.getName())) {
						// We need to redeclare the subsetted properties to
						// ensure that their getters are available in the
						// subclass for subsetting
						results.add(rp);
					}
				}
				results.add(p);
			}
		}
		return results;
	}

	private static void addAttributesTo(Classifier c, Map<String, Property> nameMap, List<Property> result) {
		for (Classifier ir : c.getGenerals()) {
			addAttributesTo(ir, nameMap, result);
		}
		if (c instanceof BehavioredClassifier) {
			BehavioredClassifier cls = (BehavioredClassifier) c;
			for (Interface ir : cls.getImplementedInterfaces()) {
				addAttributesTo(ir, nameMap, result);
			}
		}
		for (Property attribute : c.getAttributes()) {
			maybeAddProperty(c, nameMap, result, attribute);
		}
	}

	private static void addAssociationEndsTo(Classifier c, Map<String, Property> nameMap, List<Property> result) {
		for (Classifier ir : c.getGenerals()) {
			addAssociationEndsTo(ir, nameMap, result);
		}
		if (c instanceof BehavioredClassifier) {
			BehavioredClassifier cls = (BehavioredClassifier) c;
			for (Interface ir : cls.getImplementedInterfaces()) {
				addAssociationEndsTo(ir, nameMap, result);
			}
		}
		for (Association a : c.getAssociations()) {
			for (Property end : a.getMemberEnds()) {
				if (end.getOtherEnd() != null && end.getOtherEnd().getType() != null
						&& EmfClassifierUtil.conformsTo(c, (Classifier) end.getOtherEnd().getType()) && end.isNavigable() && end.getOwner() == a) {
					maybeAddProperty(c, nameMap, result, end);
				}
			}
		}
	}

	private static void maybeAddProperty(Classifier c, Map<String, Property> nameMap, List<Property> result, Property attribute) {
		Property superAttribute = nameMap.get(attribute.getName());
		boolean isDuplicateAttributeDueToUmlEmfOrPapyrusBug = c == attribute.getAssociation();
		if (!isDuplicateAttributeDueToUmlEmfOrPapyrusBug) {
			if (superAttribute != null) {
				if (c instanceof Interface && !(EmfPropertyUtil.getOwningClassifier(superAttribute) instanceof Interface)) {
					// DoNothing implemented property takes preference
				} else if (EmfPropertyUtil.getOwningClassifier(superAttribute) == EmfPropertyUtil.getOwningClassifier(attribute) && attribute.isDerivedUnion()) {
					// DoNothing the concrete property takes preference
				} else {
					result.remove(superAttribute);
					result.add(attribute);
					nameMap.put(attribute.getName(), attribute);
				}
			} else {
				result.add(attribute);
				nameMap.put(attribute.getName(), attribute);
			}
		}
	}

	public static Classifier getOwningClassifier(Property p) {
		if (!p.isNavigable()) {
			return p.getAssociation();
		} else if (p.getOtherEnd() != null) {
			return (Classifier) p.getOtherEnd().getType();
		} else if (p.getOwner() instanceof Classifier) {
			return (Classifier) p.getOwner();
		} else if (p.getAssociationEnd() != null) {
			return getOwningClassifier(p.getAssociationEnd());
		} else {
			throw new IllegalStateException();
		}
	}

	public static boolean isDimension(TypedElement te) {
		return fullfillsRoleInCube(te, "DIMENSION");
	}

	public static boolean isMeasure(TypedElement te) {
		return fullfillsRoleInCube(te, "MEASURE");
	}

	private static boolean fullfillsRoleInCube(TypedElement te, String role) {
		if (EmfPropertyUtil.isMany(te)) {
			return false;
		}
		for (Stereotype st : te.getAppliedStereotypes()) {
			Property roleInCube = st.getAttribute("roleInCube", null);
			if (roleInCube != null) {
				EnumerationLiteral l = (EnumerationLiteral) te.getValue(st, "roleInCube");
				return l.getName().toUpperCase().equals(role);
			}
		}
		return false;
	}

	public static CollectionKind getCollectionKind(MultiplicityElement exp) {
		if (exp == null) {
			return null;
		}
		if (exp.isMultivalued()) {
			if (exp.isOrdered()) {
				if (exp.isUnique()) {
					return CollectionKind.ORDERED_SET_LITERAL;
				} else {
					return CollectionKind.SEQUENCE_LITERAL;
				}
			} else {
				if (exp.isUnique()) {
					return CollectionKind.SET_LITERAL;
				} else {
					return CollectionKind.BAG_LITERAL;
				}
			}
		}
		return null;
	}

	public static boolean isRequired(MultiplicityElement otherEnd) {
		if (otherEnd.getLower() == 1) {
			return true;
		} else if (otherEnd instanceof Property) {
			Property p = (Property) otherEnd;
			return isQualifier(p) || (p.getOtherEnd() != null && p.getOtherEnd().getQualifiers().size() > 0);
		}
		return false;
	}

	public static boolean isQualifier(Property p) {
		EObject container = EmfElementFinder.getContainer(p);
		if (container instanceof Classifier) {
			Classifier c = (Classifier) container;
			List<Property> propertiesInScope = EmfPropertyUtil.getEffectiveProperties(c);
			for (Property property : propertiesInScope) {
				if (property.getOtherEnd() != null) {
					for (Property q : property.getOtherEnd().getQualifiers()) {
						if (p.getName().equals(q.getName())) {
							return true;
						}
					}
				}
			}
		} else {
			System.out.println();
		}
		return false;
	}

	public static boolean isInverse(Property f) {
		if (f.getOtherEnd() == null || !f.getOtherEnd().isNavigable()) {
			return false;
		} else {
			if (f instanceof EndToAssociationClass) {
				return true;
			} else if (f instanceof AssociationClassToEnd) {
				return false;
			} else if (f instanceof NonInverseArtificialProperty) {
				return false;
			} else if (f instanceof InverseArtificialProperty) {
				return true;
			} else if (isMany(f)) {
				if (isMany(f.getOtherEnd())) {
					return f.getAssociation().getMemberEnds().indexOf(f) == 1;
				} else {
					return true;
				}
			} else {
				if (isMany(f.getOtherEnd())) {
					return false;
				} else {
					if (f.isComposite()) {
						return true;
					} else if (f.getOtherEnd().isComposite()) {
						return false;
					} else {
						return f.getAssociation().getMemberEnds().indexOf(f) == 1;
					}
				}
			}
		}
	}

	public static Property getBackingPropertyForQualifier(Property q) {
		Classifier type = (Classifier) q.getAssociationEnd().getType();
		for (Property property : getEffectiveProperties(type)) {
			if (property.getName().equals(q.getName())) {
				return property;
			}
		}
		return null;
	}

	public static Property findTargetPropertyByReferencedColumnName(Property toOne, String referencedColumnName) {
		Property primaryKey = getPrimaryKey((Classifier) toOne.getType());
		if (primaryKey != null) {
			String persistentName = getPersistentName(primaryKey);
			if (persistentName.equals(referencedColumnName)) {
				return primaryKey;
			} else if (primaryKey.getType() instanceof DataType) {
				List<Property> pkProperties = getEffectiveProperties((Classifier) primaryKey.getType());
				for (Property pkProperty : pkProperties) {
					if (getPersistentName(pkProperty).equals(referencedColumnName)) {
						return pkProperty;
					}
				}
			}
		}
		for (Property property : getEffectiveProperties((Classifier) toOne.getType())) {
			if (isMarkedAsPrimaryKey(property)) {
				if (EmfClassifierUtil.isSimpleType(property.getType()) && getPersistentName(property).equals(referencedColumnName)) {
					return property;
				} else if (property.getType() instanceof DataType) {
					List<Property> pkProperties = getEffectiveProperties((Classifier) property.getType());
					for (Property pkProperty : pkProperties) {
						if (getPersistentName(pkProperty).equals(referencedColumnName)) {
							return pkProperty;
						}
					}
				}

			}
		}
		return null;
	}

	public static Property getPrimaryKey(Classifier owner) {
		for (Stereotype st : owner.getAppliedStereotypes()) {
			if (st.getMember("primaryKey") != null) {
				return (Property) owner.getValue(st, "primaryKey");
			}
		}
		return null;
	}

	public static boolean isOneToMany(Property p) {
		return !isMany(p) && isMany(p.getOtherEnd());
	}

	public static boolean isOneToOne(Property p) {
		return !isMany(p) && !isMany(p.getOtherEnd());
	}

	public static boolean isManyToOne(Property p) {
		return isMany(p) && !isMany(p.getOtherEnd());
	}

	public static boolean isManyToMany(Property p) {
		return isMany(p) && isMany(p.getOtherEnd());
	}
}
