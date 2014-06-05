package org.pavanecce.uml.common.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EStructuralFeature.Setting;
import org.eclipse.emf.ecore.util.ECrossReferenceAdapter;
import org.eclipse.ocl.expressions.CollectionKind;
import org.eclipse.ocl.uml.CollectionType;
import org.eclipse.ocl.uml.MessageType;
import org.eclipse.uml2.uml.Actor;
import org.eclipse.uml2.uml.Association;
import org.eclipse.uml2.uml.BehavioredClassifier;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Collaboration;
import org.eclipse.uml2.uml.Component;
import org.eclipse.uml2.uml.DataType;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Enumeration;
import org.eclipse.uml2.uml.EnumerationLiteral;
import org.eclipse.uml2.uml.Generalization;
import org.eclipse.uml2.uml.GeneralizationSet;
import org.eclipse.uml2.uml.Interface;
import org.eclipse.uml2.uml.InterfaceRealization;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.PrimitiveType;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Signal;
import org.eclipse.uml2.uml.Stereotype;
import org.eclipse.uml2.uml.Type;
import org.eclipse.uml2.uml.TypedElement;
import org.eclipse.uml2.uml.UMLPackage;
import org.pavanecce.common.util.IntrospectionUtil;
import org.pavanecce.uml.common.util.AbstractStrategyFactory.ISimpleTypeStrategy;

public class EmfClassifierUtil {
	private static Map<String, java.lang.Class<?>> classRegistry;

	public static void setClassRegistry(Map<String, java.lang.Class<?>> reg) {
		classRegistry = reg;
	}

	public static boolean hasMappedImplementationType(Classifier classifier) {
		String mit = getMappedImplementationType(classifier);
		return mit != null && mit.trim().length() > 0;
	}

	public static boolean conformsTo(CollectionType from, CollectionType to) {
		if (from.getKind() == CollectionKind.COLLECTION_LITERAL || from.getKind() == to.getKind()) {
			return conformsTo(from.getElementType(), to.getElementType());
		} else {
			return false;
		}
	}

	public static String getMappedImplementationType(Classifier classifier) {
		return (String) getTagValue(classifier, TagNames.MAPPED_IMPLEMENTATION_TYPE);
	}

	public static Object getTagValue(Classifier dt, String mappedImplementationType) {
		EList<Stereotype> appliedStereotypes = dt.getAppliedStereotypes();
		for (Stereotype st : appliedStereotypes) {
			if (st.getDefinition().getEStructuralFeature(mappedImplementationType) != null) {
				return dt.getValue(st, mappedImplementationType);
			}
		}
		return null;
	}

	public static boolean hasStrategy(DataType dt, java.lang.Class<? extends ISimpleTypeStrategy> strat) {
		AbstractStrategyFactory stf = getStrategyFactory(dt);
		if (stf != null) {
			return stf.hasStrategy(strat);
		} else if (dt.getGenerals().size() >= 1 && dt.getGenerals().get(0) instanceof DataType) {
			return hasStrategy((DataType) dt.getGenerals().get(0), strat);
		} else {
			return false;
		}
	}

	public static <T extends ISimpleTypeStrategy> T getStrategy(DataType dt, java.lang.Class<T> strat) {
		AbstractStrategyFactory stf = getStrategyFactory(dt);
		if (stf != null) {
			return stf.getStrategy(strat);
		} else if (dt.getGenerals().size() >= 1 && dt.getGenerals().get(0) instanceof DataType) {
			return getStrategy((DataType) dt.getGenerals().get(0), strat);
		} else {
			return null;
		}
	}

	public static AbstractStrategyFactory getStrategyFactory(DataType dt) {
		String tagName = "strategyFactory";
		Object value = getTagValue(dt, tagName);
		AbstractStrategyFactory stf = null;
		if (value != null) {
			if (classRegistry.containsKey(value)) {
				stf = (AbstractStrategyFactory) IntrospectionUtil.newInstance(classRegistry.get(value));
			} else {
				stf = (AbstractStrategyFactory) IntrospectionUtil.newInstance((String) value);
			}
		}
		return stf;
	}

	private static Object getTagValue(DataType dt, String tagName) {
		Stereotype st = null;
		if (dt instanceof PrimitiveType) {
			st = StereotypesHelper.getStereotype(dt, StereotypeNames.PRIMITIVE_TYPE);
		} else {
			st = StereotypesHelper.getStereotype(dt, StereotypeNames.VALUE_TYPE);
		}
		Object value = null;
		if (st != null) {
			value = dt.getValue(st, tagName);
		}
		return value;
	}

	public static Classifier findCommonSuperType(Classifier from, Classifier to) {
		Classifier result = null;
		if (conformsTo(from, to)) {
			result = to;
		} else if (conformsTo(to, from)) {
			result = from;
		}
		if (result == null) {
			for (Generalization supr : from.getGeneralizations()) {
				result = findCommonSuperType(supr.getGeneral(), to);
				if (result != null) {
					break;
				}
			}
		}
		if (result == null && from instanceof BehavioredClassifier) {
			for (Interface supr : ((BehavioredClassifier) from).getImplementedInterfaces()) {
				result = findCommonSuperType(supr, to);
				if (result != null) {
					break;
				}
			}
		}
		if (result == null && to instanceof BehavioredClassifier) {
			for (Interface supr : ((BehavioredClassifier) to).getImplementedInterfaces()) {
				result = findCommonSuperType(supr, from);
				if (result != null) {
					break;
				}
			}
		}
		return result;
	}

	public static boolean conformsTo(Classifier from, Classifier to) {
		if (from.getName().equals("OclVoid")) {
			return true;
		}
		if (from instanceof CollectionType && to instanceof CollectionType) {
			return conformsTo((CollectionType) from, (CollectionType) to);
		}
		if (from instanceof PrimitiveType && to instanceof PrimitiveType) {
			return comformsToLibraryType(from, to.getName());
		}
		if (from.equals(to)) {
			return true;
		} else if (from.allParents().contains(to)) {
			return true;
		} else if (from instanceof BehavioredClassifier) {
			for (Interface i : ((BehavioredClassifier) from).getAllImplementedInterfaces()) {
				if (i.equals(to) || i.allParents().contains(to)) {
					return true;
				}
			}
		}
		return false;
	}

	public static boolean comformsToLibraryType(Type type, String string) {
		if (type.getName() != null && type.getName().equalsIgnoreCase(string)) {
			return true;
		} else if (type instanceof Classifier) {
			for (Classifier g : ((Classifier) type).getGenerals()) {
				if (comformsToLibraryType(g, string)) {
					return true;
				}
			}
		}
		return false;
	}

	public static boolean isSimpleType(Type type) {
		return type instanceof PrimitiveType
				|| (type.eClass().equals(UMLPackage.eINSTANCE.getDataType()) && StereotypesHelper.hasStereotype(type, StereotypeNames.VALUE_TYPE));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Collection<BehavioredClassifier> getConcreteEntityImplementationsOf(Interface baseType, Collection<Package> models) {
		SortedSet<Classifier> results = new TreeSet<Classifier>(new DefaultElementComparator());
		addConcreteSubclasses(results, baseType, models, true);
		results.remove(baseType);
		return (Collection) results;
	}

	public static Collection<Classifier> getAllSubClassifiers(Classifier baseType, Collection<Package> models) {
		Set<Classifier> results = new TreeSet<Classifier>(new DefaultElementComparator());
		addConcreteSubclasses(results, baseType, models, false);
		results.remove(baseType);
		return results;
	}

	public static Collection<Classifier> getAllConcreteSubClassifiers(Classifier baseType, Collection<Package> models) {
		Set<Classifier> results = new TreeSet<Classifier>(new DefaultElementComparator());
		addConcreteSubclasses(results, baseType, models, true);
		results.remove(baseType);
		return results;
	}

	private static void addConcreteSubclasses(Set<Classifier> results, Classifier baseType, Collection<Package> models, boolean concreteOnly) {
		if (models.contains(EmfPackageUtil.getRootObject(baseType))) {
			if (!(baseType.isAbstract() && concreteOnly)) {
				if (StereotypesHelper.hasStereotype(baseType, StereotypeNames.HELPER) == false) {
					results.add(baseType);
				}
			}
			if (baseType instanceof Interface) {
				for (Classifier ic : getImplementingClassifiers(((Interface) baseType))) {
					addConcreteSubclasses(results, ic, models, concreteOnly);
				}
			}
			for (Classifier c : getSubClasses(baseType)) {
				addConcreteSubclasses(results, c, models, concreteOnly);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public static <T extends Classifier> Collection<T> getSubClasses(T c) {
		Set<T> result = new TreeSet<T>(new DefaultElementComparator());
		Collection<Setting> refs = ECrossReferenceAdapter.getCrossReferenceAdapter(c.eResource().getResourceSet()).getNonNavigableInverseReferences(c);
		for (Setting setting : refs) {
			if (setting.getEObject() instanceof Generalization && setting.getEStructuralFeature().equals(UMLPackage.eINSTANCE.getGeneralization_General())) {
				result.add((T) ((Generalization) setting.getEObject()).getSpecific());
			}
		}
		return result;
	}

	public static Collection<BehavioredClassifier> getImplementingClassifiers(Interface i) {
		Set<BehavioredClassifier> result = new TreeSet<BehavioredClassifier>(new DefaultElementComparator());
		Collection<Setting> refs = ECrossReferenceAdapter.getCrossReferenceAdapter(i.eResource().getResourceSet()).getNonNavigableInverseReferences(i);
		for (Setting setting : refs) {
			if (setting.getEObject() instanceof InterfaceRealization
					&& setting.getEStructuralFeature().equals(UMLPackage.eINSTANCE.getInterfaceRealization_Contract())) {
				result.add(((InterfaceRealization) setting.getEObject()).getImplementingClassifier());
			}
		}
		return result;
	}

	public static boolean isHelper(Type type) {
		return StereotypesHelper.hasStereotype(type, StereotypeNames.HELPER);
	}

	public static boolean isSchema(Classifier ns) {
		return Boolean.TRUE.equals(getTagValue(ns, TagNames.IS_SCHEMA));
	}

	public static boolean isStructuredDataType(Type type) {
		return type.eClass().equals(UMLPackage.eINSTANCE.getDataType()) && !isSimpleType(type);
	}

	public static boolean isNotification(Signal s) {
		return StereotypesHelper.hasStereotype(s, StereotypeNames.NOTIFICATION);
	}

	public static boolean isCompositionParticipant(Type umlOwner) {
		if (umlOwner instanceof Component || umlOwner instanceof Class || umlOwner instanceof MessageType || umlOwner instanceof Actor) {
			return true;
		} else if (umlOwner instanceof Interface) {
			return !isHelper(umlOwner);
		} else if (umlOwner instanceof Association) {
			return EmfAssociationUtil.isClass((Association) umlOwner);
		}
		return false;
	}

	public static boolean isPersistentClassOrInterface(Type type) {
		return EmfClassifierUtil.isPersistent(type) || (type instanceof Interface && !EmfClassifierUtil.isHelper(type));
	}

	public static boolean isPersistent(Type type) {
		if (!isComplexStructure(type) || type instanceof Stereotype) {
			return false;
		} else {
			return type instanceof Class || type instanceof Actor || (type instanceof Association && EmfAssociationUtil.isClass((Association) type))
					|| EmfClassifierUtil.isStructuredDataType(type);
		}
	}

	public static boolean isComplexStructure(Type type) {
		if (type instanceof PrimitiveType) {
			return false;
		}
		if (type instanceof Signal || type instanceof Enumeration || type instanceof Class || type instanceof Actor || type instanceof Collaboration
				|| type instanceof MessageType || isStructuredDataType(type) || (type instanceof Association && EmfAssociationUtil.isClass((Association) type))) {
			return true;
		}
		return false;
	}

	public static boolean isFact(Type type) {
		for (Property property : EmfPropertyUtil.getEffectiveProperties((Classifier) type)) {
			if (EmfPropertyUtil.isMeasure(property)) {
				return true;
			}
		}
		return false;
	}

	public static Collection<Property> getPrimaryKeyProperties(Class class1) {
		Collection<Property> result = new TreeSet<Property>(new DefaultElementComparator());
		for (Property property : class1.getOwnedAttributes()) {
			if (EmfPropertyUtil.isMarkedAsPrimaryKey(property)) {
				result.add(property);
			}
		}
		return result;
	}

	public static EnumerationLiteral getPowerTypeLiteral(Generalization generalization, Enumeration type) {
		for (GeneralizationSet gs : generalization.getGeneralizationSets()) {
			if (gs.getPowertype() == type) {
				for (EnumerationLiteral l : type.getOwnedLiterals()) {
					if (generalization.getSpecific().getName().equalsIgnoreCase(l.getName())) {
						return l;
					}
				}
			}
		}
		return null;
	}

	public static boolean isPowerTypeInstanceOn(Class entity, Enumeration powerType) {
		for (Generalization generalization : entity.getGeneralizations()) {
			for (GeneralizationSet gs : generalization.getGeneralizationSets()) {
				if (gs.getPowertype() == powerType) {
					return true;
				}
			}
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	public static <T extends Classifier> T getRootClass(T classifier) {
		for (Classifier c : classifier.getGenerals()) {
			if (classifier.eClass().isInstance(c)) {
				return getRootClass((T) c);
			}
		}
		return classifier;
	}

	public static boolean isPersistentComplexStructure(Type type) {
		return isPersistent(type) && isComplexStructure(type);
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

	public static Map<String, String> getMappings(Element e) {
		EList<EObject> sas = e.getStereotypeApplications();
		Map<String, String> typeMappings = new HashMap<String, String>();
		for (EObject eObject : sas) {
			for (EStructuralFeature feat : eObject.eClass().getEAllStructuralFeatures()) {
				if (feat.getEType().getName().equals("Mapping")) {
					@SuppressWarnings("unchecked")
					EList<EObject> mappings = (EList<EObject>) eObject.eGet(feat);
					for (EObject mapping : mappings) {
						typeMappings.put((String) mapping.eGet(mapping.eClass().getEStructuralFeature("language")),
								(String) mapping.eGet(mapping.eClass().getEStructuralFeature("mapping")));
					}
				}
			}
		}
		return typeMappings;
	}
}
