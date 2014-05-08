package org.pavanecce.uml.reverse.java;

import java.beans.IntrospectionException;
import java.lang.annotation.Target;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.DataType;
import org.eclipse.uml2.uml.Enumeration;
import org.eclipse.uml2.uml.Interface;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Namespace;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.PackageImport;
import org.eclipse.uml2.uml.PrimitiveType;
import org.eclipse.uml2.uml.Profile;
import org.eclipse.uml2.uml.Stereotype;
import org.eclipse.uml2.uml.Type;
import org.eclipse.uml2.uml.UMLFactory;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.VisibilityKind;
import org.eclipse.uml2.uml.resource.UMLResource;
import org.pavanecce.uml.common.util.EmfClassifierUtil;
import org.pavanecce.uml.common.util.LibraryImporter;
import org.pavanecce.uml.common.util.StereotypeNames;
import org.pavanecce.uml.reverse.java.sourcemodel.SourceAnnotation;
import org.pavanecce.uml.reverse.java.sourcemodel.SourceClass;
import org.pavanecce.uml.reverse.java.sourcemodel.SourceVariable;

public class ClassifierFactory {
	private Map<String, Classifier> classMap = new HashMap<String, Classifier>();
	private Stereotype helperStereotype;
	private Package model;
	private Properties mappedTypes = new Properties();
	private Model umlMetaModel;
	private Class operationMetaClass;
	private Class propertyMetaClass;
	private Class classMetaClass;
	private Set<Classifier> newClassifiers = new HashSet<Classifier>();

	public ClassifierFactory(Package model) {
		// well, and load them into classMap
		this.model = model;
		TreeIterator<Notifier> eAllContents = model.eResource().getResourceSet().getAllContents();
		while (eAllContents.hasNext()) {
			Notifier eObject = eAllContents.next();
			if (eObject instanceof Classifier) {
				Classifier classifier = (Classifier) eObject;
				String qualifiedName = calculateQualifiedJavaName(classifier);
				classMap.put(qualifiedName, classifier);
			}

		}
		this.helperStereotype = findInProfiles(model, "Helper");
		importPrimitiveTypes(model);
		if (model instanceof Profile) {
			ResourceSet rst = model.eResource().getResourceSet();
			umlMetaModel = (Model) rst.getResource(URI.createURI(UMLResource.UML_METAMODEL_URI), true).getContents().get(0);
			classMetaClass = (org.eclipse.uml2.uml.Class) umlMetaModel.getOwnedType("Class");
			propertyMetaClass = (org.eclipse.uml2.uml.Class) umlMetaModel.getOwnedType("Property");
			operationMetaClass = (org.eclipse.uml2.uml.Class) umlMetaModel.getOwnedType("Operation");
			Profile profile = (Profile) model;
			profile.getMetaclassReference(classMetaClass);
			profile.getMetaclassReference(operationMetaClass);
			profile.getMetaclassReference(propertyMetaClass);
			profile.getMetamodelReference(umlMetaModel, true);
		}
	}

	protected String calculateQualifiedJavaName(Namespace named) {
		String qualifiedName = "";
		Map<String, String> mappings = EmfClassifierUtil.getMappings(named);
		if (mappings != null && mappings.containsKey("java")) {
			qualifiedName = mappings.get("java");
		} else {
			if (named.getNamespace() != null) {
				qualifiedName = calculateQualifiedJavaName(named.getNamespace());
			}
			qualifiedName = qualifiedName + "." + named.getName();
		}
		return qualifiedName;
	}

	public Classifier getClassifierFor(SourceClass javaType) {
		try {
			if (javaType == null) {
				return null;
			} else {
				Classifier classifier = null;
				while (javaType.isManyType()) {
					javaType = javaType.getBaseType();
				}
				if (classMap.containsKey(javaType.getQualifiedName())) {
					classifier = classMap.get(javaType.getQualifiedName());
				} else {
					if (javaType.getQualifiedName().equals("java.lang.Class")) {
						for (SourceClass arg : javaType.getTypeArguments()) {
							if (arg.isInterface()) {
								return (Classifier) umlMetaModel.getOwnedType("Interface");
							}
						}
						return classMetaClass;
					}
					Namespace pkg = getPackageFor(javaType, model);
					if (pkg != null) {
						classifier = (Classifier) pkg.getOwnedMember(javaType.getName());
						if (model instanceof Model && javaType.isAnnotation()) {
							return classifier;// no lazy creation
						} else if (classifier == null) {
							classifier = createClassifier(javaType);
							mappedTypes.put(classifier.getQualifiedName(), javaType.getQualifiedName());
						}
						classMap.put(javaType.getQualifiedName(), classifier);
					}
				}
				return classifier;
			}
		} catch (IntrospectionException e) {
			throw new RuntimeException(e);
		}
	}

	private Classifier createClassifier(SourceClass sourceType) throws IntrospectionException {
		Classifier classifier = null;
		if (sourceType.isAnnotation()) {
			classifier = createStereotype(sourceType);
		} else if (sourceType.isEnum()) {
			classifier = createEnum(sourceType);
		} else if (sourceType.isInterface()) {
			classifier = createInterface(sourceType);
			for (SourceClass intf : sourceType.getInterfaces()) {
				if (!intf.getQualifiedName().startsWith("java.lang")) {
					((Interface) classifier).getGeneralization(getClassifierFor(intf), true);
				}
			}
		} else if (sourceType.isEntity()) {
			classifier = createEntity(sourceType);
			for (SourceClass intf : sourceType.getInterfaces()) {
				if (!intf.getQualifiedName().startsWith("java.lang")) {
					((Class) classifier).getInterfaceRealization(intf.getName() + "Realization", (Interface) getClassifierFor(intf), false, true);
				}
			}
		} else if (sourceType.isDataType()) {
			classifier = createDataType(sourceType);
		} else if (sourceType.isHelper()) {
			classifier = createHelper(sourceType);
		} else {
			// throw new IllegalArgumentException("Type " +
			// sourceType.getQualifiedName() + " not supported");
			classifier = createHelper(sourceType);
		}
		if (!hasRelevantSuperclass(sourceType)) {
			classifier.createGeneralization(getClassifierFor(sourceType.getSuperclass()));
		}
		newClassifiers.add(classifier);
		return classifier;
	}

	private boolean hasRelevantSuperclass(SourceClass returnType) {
		SourceClass superclass = returnType.getSuperclass();
		return superclass == null || superclass.getQualifiedName().equals("java.lang.Object") || returnType.isEnum() || returnType.isAnnotation();
	}

	private Classifier createStereotype(SourceClass type) {
		Stereotype st = (Stereotype) createType(type, UMLPackage.eINSTANCE.getStereotype());
		final SourceAnnotation target = type.getAnnotation(Target.class.getName());
		if (target != null) {
			Object val = target.getAnnotationAttribute("value");
			if (val instanceof Object[]) {
				for (Object o : (Object[]) val) {
					SourceVariable elementType = (SourceVariable) o;
					if (elementType.getName().equals("TYPE") && !st.getAllExtendedMetaclasses().contains(classMetaClass)) {
						createExtension(st, classMetaClass);
					} else if (elementType.getName().equals("FIELD") && !st.getAllExtendedMetaclasses().contains(propertyMetaClass)) {
						createExtension(st, propertyMetaClass);
					} else if (elementType.getName().equals("METHOD") && !st.getAllExtendedMetaclasses().contains(operationMetaClass)) {
						createExtension(st, operationMetaClass);
					}
				}
			}
		}
		return st;
	}

	private void createExtension(Stereotype st, Class dt) {
		if (!st.getAllExtendedMetaclasses().contains(dt)) {
			st.createExtension(dt, false);
		}
	}

	private org.eclipse.uml2.uml.DataType createDataType(SourceClass returnType) {
		org.eclipse.uml2.uml.DataType umlDataType = (DataType) createType(returnType, UMLPackage.eINSTANCE.getDataType());
		return umlDataType;
	}

	private org.eclipse.uml2.uml.Class createHelper(SourceClass returnType) {
		org.eclipse.uml2.uml.Class umlClass = (Class) createType(returnType, UMLPackage.eINSTANCE.getClass_());
		if (helperStereotype != null) {
			umlClass.applyStereotype(helperStereotype);
		}
		return umlClass;
	}

	private org.eclipse.uml2.uml.Class createEntity(SourceClass returnType) {
		org.eclipse.uml2.uml.Class umlClass = (Class) createType(returnType, UMLPackage.eINSTANCE.getClass_());
		SourceClass[] interfaces = returnType.getInterfaces();
		for (SourceClass intfce : interfaces) {
			umlClass.createInterfaceRealization(intfce.getName(), (Interface) getClassifierFor(intfce));
		}
		return umlClass;
	}

	private Interface createInterface(SourceClass returnType) {
		Interface umlInterface = (Interface) createType(returnType, UMLPackage.eINSTANCE.getInterface());
		SourceClass[] interfaces = returnType.getInterfaces();
		for (SourceClass intfce : interfaces) {
			umlInterface.createGeneralization(getClassifierFor(intfce));
		}
		return umlInterface;
	}

	protected Type createType(SourceClass returnType, EClass eTYpe) {
		Namespace ns = getPackageFor(returnType, model);
		if (ns instanceof Package) {
			return ((Package) ns).createOwnedType(returnType.getName(), eTYpe);
		} else {
			return ((Class) ns).createNestedClassifier(returnType.getName(), eTYpe);
		}
	}

	private Classifier createEnum(SourceClass en) throws IntrospectionException {
		Enumeration enumeration = (Enumeration) createType(en, UMLPackage.eINSTANCE.getEnumeration());
		SourceVariable[] enumConstants = en.getDeclaredFields();
		for (SourceVariable e : enumConstants) {
			if (e.isEnumConstant()) {
				if (enumeration.getMember(e.getName()) == null) {
					enumeration.createOwnedLiteral(e.getName());
				}
			}
		}
		return enumeration;
	}

	private Stereotype findInProfiles(Package model, String name) {
		EList<Profile> pkgs = model.getAppliedProfiles();
		return (Stereotype) findClassifier(name, pkgs);
	}

	public Classifier findClassifier(String name, EList<? extends Package> pkgs) {
		for (Package pkg : pkgs) {
			EList<Type> types = pkg.getOwnedTypes();
			for (Type type : types) {
				if (type.getName().equals(name)) {
					return (Classifier) type;
				}
			}
		}
		return null;
	}

	private Namespace getPackageFor(SourceClass javaClass, Namespace ecoreModelOrProfile) {
		// on demand. Calculate a good name ignoring com, org, etc
		if (javaClass.isAnnotation()) {
			if (ecoreModelOrProfile instanceof Profile) {
				return ecoreModelOrProfile;// Probably to be created
			} else {
				// to be looked up
				EList<Resource> resources = ecoreModelOrProfile.eResource().getResourceSet().getResources();
				for (Resource resource : resources) {
					if (resource.getContents().size() > 0 && resource.getContents().get(0) instanceof Profile) {
						Profile p = (Profile) resource.getContents().get(0);
						if (p.getOwnedStereotype(javaClass.getName()) != null) {
							return p;
						}
					}
				}
				return null;
			}
		} else {
			StringTokenizer st = new StringTokenizer(javaClass.getPackageName(), ".");
			Namespace currentPackage = null;
			String name = st.nextToken();
			if (name.equalsIgnoreCase(ecoreModelOrProfile.getName())) {
				currentPackage = ecoreModelOrProfile;
			} else {
				if (ecoreModelOrProfile instanceof Profile && !javaClass.isAnnotation()) {
					Resource res = ecoreModelOrProfile.eResource();
					EList<Resource> resources = res.getResourceSet().getResources();
					outer: for (Resource resource : resources) {
						for (EObject eObject : resource.getContents()) {
							if (eObject instanceof Model && ((Model) eObject).getName().equals(name)) {
								currentPackage = model;
								break outer;
							}
						}
					}
					if (currentPackage == null) {
						URI uri = res.getURI().trimFileExtension().trimSegments(1).appendSegment(name).appendFileExtension("uml");
						currentPackage = UMLFactory.eINSTANCE.createModel();
						currentPackage.setName(name);
						Resource newRes = res.getResourceSet().createResource(uri);
						res.getResourceSet().getResources().add(newRes);
						newRes.getContents().add(currentPackage);
						ecoreModelOrProfile.createPackageImport((Package) currentPackage, VisibilityKind.PUBLIC_LITERAL);
					}
				} else {
					// just create it in the current model
					// TODOmaybe revisit in future
					currentPackage = findOrCreateChild(ecoreModelOrProfile, name);
				}
			}
			while (st.hasMoreTokens()) {
				name = st.nextToken();
				currentPackage = findOrCreateChild(currentPackage, name);
			}
			return currentPackage;
		}
	}

	private Namespace findOrCreateChild(Namespace ecoreModelOrProfile, String name) {
		Namespace childPackage = null;
		EList<NamedElement> members = ecoreModelOrProfile.getMembers();
		for (NamedElement member : members) {
			if ((member instanceof Class || member instanceof Package) && member.getName().equalsIgnoreCase(name)) {
				childPackage = (Namespace) member;
			}
		}
		if (childPackage == null) {
			if (ecoreModelOrProfile instanceof Package) {
				childPackage = ((Package) ecoreModelOrProfile).createNestedPackage(name);
			} else {
				childPackage = ((Class) ecoreModelOrProfile).createNestedClassifier(name, UMLPackage.eINSTANCE.getClass_());
			}
		}
		return childPackage;
	}

	private void importPrimitiveTypes(Package model) {
		Model simpleTypes = LibraryImporter.importLibraryIfNecessary(model, StereotypeNames.STANDARD_SIMPLE_TYPES);
		EList<Type> ownedTypes = simpleTypes.getOwnedTypes();
		for (Type type : ownedTypes) {
			Map<String, String> typeMappings = EmfClassifierUtil.getMappings(type);
			if (typeMappings.containsKey("java")) {
				this.classMap.put(typeMappings.get("java"), (Classifier) type);
			}
		}
		Package umlLibrary = getImportedPackage(model, UMLResource.UML_PRIMITIVE_TYPES_LIBRARY_URI);
		this.classMap.put(Boolean.class.getName(), (PrimitiveType) umlLibrary.getOwnedType("Boolean"));
		this.classMap.put(String.class.getName(), (PrimitiveType) umlLibrary.getOwnedType("String"));
		this.classMap.put(Double.class.getName(), (PrimitiveType) umlLibrary.getOwnedType("Real"));
		this.classMap.put(Integer.class.getName(), (PrimitiveType) umlLibrary.getOwnedType("Integer"));
	}

	private Model getImportedPackage(Package ecoreProfile, String uriString) {
		URI uri = URI.createURI(uriString);
		for (PackageImport pi : ecoreProfile.getPackageImports()) {
			if (pi.getImportedPackage().eResource().getURI().equals(uri)) {
				return (Model) pi.getImportedPackage();
			}
		}
		EList<EObject> contents = ecoreProfile.eResource().getResourceSet().getResource(uri, true).getContents();
		if (contents.size() == 0) {
			return null;
		}
		Model umlLibrary = (Model) contents.get(0);
		ecoreProfile.getPackageImport(umlLibrary, true);
		return umlLibrary;
	}

	public Properties getMappedTypes() {
		return mappedTypes;
	}

	public Set<Classifier> getNewClassifiers() {
		return newClassifiers;
	}

	public Map<String, org.eclipse.uml2.uml.Classifier> getClassMap() {
		return classMap;
	}
}
