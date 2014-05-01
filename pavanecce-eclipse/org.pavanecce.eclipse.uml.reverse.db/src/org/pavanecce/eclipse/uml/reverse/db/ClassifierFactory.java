package org.pavanecce.eclipse.uml.reverse.db;

import static org.pavanecce.uml.common.util.TagNames.IS_SCHEMA;
import static org.pavanecce.uml.common.util.TagNames.PERSISTENT_NAME;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.datatools.modelbase.sql.constraints.ForeignKey;
//import org.eclipse.datatools.connectivity.sqm.core.rte.jdbc.JDBCTable;
import org.eclipse.datatools.modelbase.sql.datatypes.ApproximateNumericDataType;
import org.eclipse.datatools.modelbase.sql.datatypes.BinaryStringDataType;
import org.eclipse.datatools.modelbase.sql.datatypes.BooleanDataType;
import org.eclipse.datatools.modelbase.sql.datatypes.CharacterStringDataType;
import org.eclipse.datatools.modelbase.sql.datatypes.DateDataType;
import org.eclipse.datatools.modelbase.sql.datatypes.FixedPrecisionDataType;
import org.eclipse.datatools.modelbase.sql.datatypes.IntegerDataType;
import org.eclipse.datatools.modelbase.sql.datatypes.PredefinedDataType;
import org.eclipse.datatools.modelbase.sql.datatypes.TimeDataType;
import org.eclipse.datatools.modelbase.sql.tables.Column;
import org.eclipse.datatools.modelbase.sql.tables.PersistentTable;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.uml2.uml.Association;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Component;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Namespace;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.PackageImport;
import org.eclipse.uml2.uml.PrimitiveType;
import org.eclipse.uml2.uml.Profile;
import org.eclipse.uml2.uml.Stereotype;
import org.eclipse.uml2.uml.Type;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.resource.UMLResource;
import org.pavanecce.uml.common.util.StereotypeNames;

public class ClassifierFactory {
	private Map<String, Classifier> classMap = new HashMap<String, Classifier>();
	private Package model;
	private Stereotype packageStereotype;
	private Stereotype entityStereotype;
	private Stereotype propertyStereotype;
	private Stereotype associationEndStereotype;
	private Stereotype associationStereotype;
	private Stereotype componentStereotype;
	private INameGenerator nameGenerator;

	public ClassifierFactory(Package model, INameGenerator nameGenerator) {
		this.model = model;
		this.nameGenerator = nameGenerator;
		this.packageStereotype = findInProfiles(model, "Package");
		this.entityStereotype = findInProfiles(model, "Entity");
		this.propertyStereotype = findInProfiles(model, StereotypeNames.ATTRIBUTE_STEREOTYPE);
		this.associationEndStereotype = findInProfiles(model, StereotypeNames.ASSOCIATION_END);
		this.associationStereotype = findInProfiles(model, "Association");
		this.componentStereotype = findInProfiles(model, "Component");
		final TreeIterator<EObject> iter = model.eAllContents();
		while (iter.hasNext()) {
			EObject eObject = (EObject) iter.next();
			if (eObject instanceof Class) {
				maybePutClassInMap((Class) eObject);
			}
		}
	}

	public Stereotype getComponentStereotype() {
		return componentStereotype;
	}

	private void maybePutClassInMap(Class c) {
		if (c.isStereotypeApplied(getEntityStereotype())) {
			String tableName = (String) c.getValue(getEntityStereotype(), PERSISTENT_NAME);
			if (tableName != null) {
				Element owner = c.getOwner();
				while (owner != null) {
					if (owner instanceof Package) {
						if (maybePutSchema(c, tableName, owner, getPackageStereotype(), PERSISTENT_NAME)) {
							break;
						}
					} else if (owner instanceof Component) {
						if (maybePutSchema(c, tableName, owner, getComponentStereotype(), "schemaName")) {
							break;
						}
					}
					owner = owner.getOwner();
				}
			}
		}
	}

	private boolean maybePutSchema(Class c, String tableName, Element owner, Stereotype st, String propertyName) {
		boolean isSchema = false;
		if (owner.isStereotypeApplied(st)) {
			if (Boolean.TRUE.equals(owner.getValue(st, IS_SCHEMA))) {
				String schema = (String) owner.getValue(st, propertyName);
				if (schema != null) {
					classMap.put(schema + "." + tableName, c);
					classMap.put(((NamedElement) owner).getName() + "." + c.getName(), c);
				}
				isSchema = true;
			}
		}
		return isSchema;
	}

	@SuppressWarnings("unchecked")
	private boolean isAssociation(PersistentTable table) {
		if (nameGenerator.isAssociation(table)) {
			return true;
		} else {
			List<? extends ForeignKey> foreignKeys = table.getForeignKeys();
			if (foreignKeys.size() == 2) {
				Set<Column> columns = new HashSet<Column>();
				for (ForeignKey fk : foreignKeys) {
					columns.addAll(fk.getMembers());
				}
				return columns.containsAll(table.getColumns());
			} else {
				return false;
			}
		}
	}

	private boolean isAssociationClass(PersistentTable t) {
		return nameGenerator.isAssociation(t) && t.getPrimaryKey() != null && t.getPrimaryKey().getMembers().size() == 1;
	}

	public Classifier getClassifierFor(PersistentTable table) {
		String qName1 = calcPackageName(table) + "." + calcTypeName(table);
		String qName2 = table.getSchema().getName() + "." + table.getName();
		Classifier classifier = classMap.get(qName1);
		if (classifier == null) {
			classifier = classMap.get(qName2);
		}
		if (classifier == null) {
			classifier = (Classifier) createType(table, calculateClassifierType(table));
			classMap.put(qName1, classifier);
			classMap.put(qName2, classifier);
		}
		if (getEntityStereotype() != null && classifier instanceof Class) {
			if (!classifier.isStereotypeApplied(getEntityStereotype())) {
				classifier.applyStereotype(getEntityStereotype());
			}
			classifier.setValue(getEntityStereotype(), PERSISTENT_NAME, table.getName());
		}else if(getAssociationStereotype() !=null && classifier instanceof Association){
			if (!classifier.isStereotypeApplied(getAssociationStereotype())) {
				classifier.applyStereotype(getAssociationStereotype());
			}
			classifier.setValue(getAssociationStereotype(), PERSISTENT_NAME, table.getName());
		}
		return classifier;
	}

	private EClass calculateClassifierType(PersistentTable table) {
		if (isAssociationClass(table)) {
			return UMLPackage.eINSTANCE.getAssociationClass();
		} else if (isAssociation(table)) {
			return UMLPackage.eINSTANCE.getAssociation();
		} else {
			return UMLPackage.eINSTANCE.getClass_();
		}
	}

	public String calcPackageName(PersistentTable returnType) {
		return nameGenerator.calcPackagename(returnType);
	}

	private Type createType(PersistentTable returnType, EClass eTYpe) {
		Namespace ns = getPackageFor(returnType);
		if (ns instanceof Package) {
			return ((Package) ns).createOwnedType(calcTypeName(returnType), eTYpe);
		} else {
			return ((Class) ns).createNestedClassifier(calcTypeName(returnType), eTYpe);
		}
	}

	public String calcTypeName(PersistentTable returnType) {
		return this.nameGenerator.calcTypeName(returnType);
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

	private Namespace getPackageFor(PersistentTable table) {
		String name = calcPackageName(table);
		Namespace childPackage = null;
		EList<NamedElement> members = model.getMembers();
		for (NamedElement member : members) {
			if ((member instanceof Class || member instanceof Package) && member.getName().equalsIgnoreCase(name)) {
				childPackage = (Namespace) member;
			}
		}
		if (childPackage == null) {
			if (model instanceof Component) {
				childPackage = (Namespace) ((Component) model).createPackagedElement(name, UMLPackage.eINSTANCE.getPackage());
			} else if (model instanceof Package) {
				childPackage = ((Package) model).createNestedPackage(name);
				if (getPackageStereotype() != null) {
					childPackage.applyStereotype(getPackageStereotype());
					childPackage.setValue(getPackageStereotype(), PERSISTENT_NAME, name);
					childPackage.setValue(getPackageStereotype(), "isSchema", Boolean.TRUE);
				}
			} else {
				childPackage = ((Class) model).createNestedClassifier(name, UMLPackage.eINSTANCE.getClass_());
			}
		}
		return childPackage;
	}

	private Classifier findInImports(Package model, String name) {
		EList<Package> pkgs = model.getImportedPackages();
		return findClassifier(name, pkgs);
	}

	private Model getImportedPackage(Package ecoreProfile, String uriString) {
		URI uri = URI.createURI(uriString);
		for (PackageImport pi : ecoreProfile.getPackageImports()) {
			if (pi.getImportedPackage().eResource().getURI().equals(uri)) {
				return (Model) pi.getImportedPackage();
			}
		}
		Model umlLibrary = (Model) ecoreProfile.eResource().getResourceSet().getResource(uri, true).getContents().get(0);
		ecoreProfile.getPackageImport(umlLibrary, true);
		return umlLibrary;
	}

	public Type getDataTypeFor(Column c) {
		org.eclipse.datatools.modelbase.sql.datatypes.DataType dataType = c.getDataType();
		Type result = null;
		if (dataType instanceof PredefinedDataType) {
			Package umlLibrary = getImportedPackage(model, UMLResource.UML_PRIMITIVE_TYPES_LIBRARY_URI);
			if (dataType instanceof BooleanDataType) {
				result = (PrimitiveType) umlLibrary.getOwnedType("Boolean");
			} else if (dataType instanceof CharacterStringDataType) {
				result = (PrimitiveType) umlLibrary.getOwnedType("String");
			} else if (dataType instanceof IntegerDataType) {
				if (dataType.getName().equalsIgnoreCase("bool") || dataType.getName().equalsIgnoreCase("boolean") || dataType.getName().equalsIgnoreCase("bit")) {
					result = (PrimitiveType) umlLibrary.getOwnedType("Boolean");
				} else {
					result = (PrimitiveType) umlLibrary.getOwnedType("Integer");
				}
			} else if (dataType instanceof FixedPrecisionDataType || dataType instanceof ApproximateNumericDataType) {
				result = (PrimitiveType) umlLibrary.getOwnedType("Real");
			} else if (dataType instanceof DateDataType) {
				result = findInImports(model, "DateTime");
			} else if (dataType instanceof TimeDataType) {
				result = findInImports(model, "Time");
			} else if (dataType instanceof BinaryStringDataType) {
				result = findInImports(model, "BinaryLargeObject");
			}
		}
		if(result==null){
			System.out.println();
		}
		return result;
	}

	public Stereotype getAttributeStereotype() {
		return propertyStereotype;
	}

	public Stereotype getEntityStereotype() {
		return entityStereotype;
	}

	public Stereotype getPackageStereotype() {
		return packageStereotype;
	}

	public Stereotype getAssociationEndStereotype() {
		return this.associationEndStereotype;
	}

	public Stereotype getAssociationStereotype() {
		return this.associationStereotype;
	}
}
