package org.pavanecce.uml.ocl2code.maps;

import static org.pavanecce.common.util.NameConverter.capitalize;
import static org.pavanecce.common.util.NameConverter.decapitalize;
import static org.pavanecce.common.util.NameConverter.toValidVariableName;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ocl.expressions.CollectionKind;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.StructuralFeature;
import org.pavanecce.common.code.metamodel.CodeTypeReference;
import org.pavanecce.uml.common.util.EmfAssociationUtil;
import org.pavanecce.uml.common.util.EmfElementFinder;
import org.pavanecce.uml.common.util.EmfPropertyUtil;
import org.pavanecce.uml.common.util.PersistentNameUtil;
import org.pavanecce.uml.common.util.emulated.AssociationClassToEnd;
import org.pavanecce.uml.common.util.emulated.EndToAssociationClass;
import org.pavanecce.uml.ocl2code.common.UmlToCodeMaps;
import org.pavanecce.uml.uml2code.StdlibMap;

public final class PropertyMap extends PackageableElementMap {
	private Property property;
	protected ClassifierMap actualTypeMap = null;
	protected ClassifierMap baseTypeMap = null;

	public PropertyMap(UmlToCodeMaps codeUtil, Property property) {
		super(codeUtil, property);
		if (property.getAssociationEnd() != null) {
			// qualifier - might have backing attribute
			Classifier c = (Classifier) EmfElementFinder.getContainer(property.getAssociationEnd());
			Property attribute = c.getAttribute(property.getName(), null);
			if (attribute != null) {
				property = attribute;
			}
		}
		this.setProperty(property);
		Classifier type = (Classifier) property.getType();
		if (type == null) {
			type = codeUtil.getLibrary().getStringType();
		}
		baseTypeMap = codeUtil.buildClassifierMap(type);
		if (EmfPropertyUtil.isMany(property)) {
			actualTypeMap = codeUtil.buildClassifierMap(type, getCollectionKind(property));
		} else {
			actualTypeMap = baseTypeMap;
		}
	}

	public Classifier getBaseType() {
		return baseTypeMap.elementType;
	}

	public CodeTypeReference javaDefaultTypePath() {
		if (isMany()) {
			CodeTypeReference baseType = javaBaseDefaultTypePath();
			CodeTypeReference copy = actualTypeMap.javaDefaultTypePath().getCopy();
			copy.addToElementTypes(baseType);
			return copy;
		} else {
			return actualTypeMap.javaDefaultTypePath();
		}
	}

	public CodeTypeReference javaTypePath() {
		if (isMany()) {
			CodeTypeReference copy = actualTypeMap.javaTypePath().getCopy();
			CodeTypeReference javaBaseTypePath = javaBaseTypePath();
			copy.addToElementTypes(javaBaseTypePath);
			if (property.isDerivedUnion()) {
				copy.getElementTypes().get(0).setExtends(true);
			}
			return copy;
		} else if (isJavaPrimitive()) {
			return actualTypeMap.javaObjectTypePath();
		} else {
			return actualTypeMap.javaTypePath();
		}
	}

	private CollectionKind getCollectionKind(StructuralFeature feature) {
		return super.getCollectionKind(feature);
	}

	public String umlName() {
		return property.getName();
	}

	public boolean isUnique() {
		return property.isUnique();
	}

	public boolean isStatic() {
		return property.isStatic();
	}

	public boolean isOptional() {
		return property.getLower() == 0;
	}

	public boolean isCollection() {
		return actualTypeMap.isCollection();
	}

	public boolean isUmlPrimitive() {
		return actualTypeMap.isUmlPrimitive();
	}

	public boolean isJavaPrimitive() {
		return actualTypeMap.isJavaPrimitive();
	}

	public boolean elementTypeIsUmlPrimitive() {
		return baseTypeMap.isUmlPrimitive();
	}

	public boolean elementTypeIsJavaPrimitive() {
		return baseTypeMap.isJavaPrimitive();
	}

	public String fieldname() {
		String asIs = decapitalize(toValidVariableName(property.getName()));
		return asIs;
	}

	public String getter() {
		String name = toValidVariableName(property.getName());
		// if(name.indexOf("is") == 0){
		// return name;
		// }
		return "get" + capitalize(name);
	}

	public String setter() {
		String name = toValidVariableName(property.getName());
		if (name.indexOf("is") == 0) {
			name = name.substring(2, name.length());
		}
		return "set" + capitalize(name);
	}

	public String adder() {
		return "addTo" + capitalize(toValidVariableName(property.getName()));
	}

	public String remover() {
		return "removeFrom" + capitalize(toValidVariableName(property.getName()));
	}

	public String removeAll() {
		return "removeAllFrom" + capitalize(toValidVariableName(property.getName()));
	}

	public String internalAdder() {
		return "z_internalAddTo" + capitalize(toValidVariableName(property.getName()));
	}

	public String internalRemover() {
		return "z_internalRemoveFrom" + capitalize(toValidVariableName(property.getName()));
	}

	public CodeTypeReference javaObjectTypePath() {
		return actualTypeMap.javaObjectTypePath();
	}

	public CodeTypeReference javaFacadeTypePath() {
		return actualTypeMap.javaTypePath();
	}

	public CodeTypeReference javaBaseDefaultTypePath() {
		return baseTypeMap.javaDefaultTypePath();
	}

	public CodeTypeReference javaBaseObjectTypePath() {
		return baseTypeMap.javaObjectTypePath();
	}

	public boolean isOne() {
		return !isMany();
	}

	public boolean isOneToMany() {
		return otherEndIsOne() && isMany();
	}

	public boolean isManyToMany() {
		return !otherEndIsOne() && isMany();
	}

	public String allAdder() {
		return "addAllTo" + capitalize(toValidVariableName(property.getName()));
	}

	public String clearer() {
		return "clear" + capitalize(toValidVariableName(property.getName()));
	}

	public CodeTypeReference javaBaseTypePath() {
		if (baseTypeMap.isJavaPrimitive()) {
			return baseTypeMap.javaObjectTypePath();
		} else {
			return baseTypeMap.javaTypePath();
		}
	}

	public String qualifierProperty() {
		Property property = this.property instanceof EndToAssociationClass ? ((EndToAssociationClass) this.property).getOriginalProperty() : this.property;
		Classifier owner = EmfPropertyUtil.getOwningClassifier(property);
		return "z_keyOf" + capitalize(toValidVariableName(property.getName())) + "On" + owner.getName();
	}

	public String qualifierPropertySetter() {
		Property property = this.property instanceof EndToAssociationClass ? ((EndToAssociationClass) this.property).getOriginalProperty() : this.property;
		Classifier owner = EmfPropertyUtil.getOwningClassifier(property);
		return "setZ_keyOf" + capitalize(toValidVariableName(property.getName())) + "On" + (owner).getName();
	}

	public String getPersistentName() {
		return PersistentNameUtil.getPersistentName(property);
	}

	public boolean isManyToOne() {
		return !otherEndIsOne() && isOne();
	}

	public boolean isOneToOne() {
		return otherEndIsOne() && isOne();
	}

	public boolean isMany() {
		if (property instanceof Property) {
			int qualifierCount = property.getQualifiers().size();
			return property.isMultivalued() || qualifierCount > 0;
		} else {
			return property.isMultivalued();
		}
	}

	/**
	 * IF the other end is not navigable or there is no other end, an assumption of otherEnd=Many is made
	 * 
	 * @return
	 */
	protected boolean otherEndIsOne() {
		if (property.getOtherEnd() != null) {
			Property otherEnd = property.getOtherEnd();
			return otherEnd.getUpper() == 1 && otherEnd.getQualifiers().size() == 0;
		} else {
			return false;
		}
	}

	public Property getProperty() {
		return property;
	}

	public Classifier getDefiningClassifier() {
		return (Classifier) EmfElementFinder.getContainer(property);
	}

	public boolean isInverse() {
		return EmfPropertyUtil.isInverse(property);
	}

	public boolean isQualifier() {
		return EmfPropertyUtil.isQualifier(property);
	}

	protected void setProperty(Property property) {
		this.property = property;
	}

	public List<CodeTypeReference> qualifiedArgumentsForWriter() {
		List<CodeTypeReference> result = qualifiedArgsForReader();
		result.add(javaBaseTypePath());
		return result;
	}

	public List<CodeTypeReference> qualifiedArgsForReader() {
		List<CodeTypeReference> result = new ArrayList<CodeTypeReference>();
		for (Property q : property.getQualifiers()) {
			Property bp = EmfPropertyUtil.getBackingPropertyForQualifier(q);
			if (bp == null) {
				bp = q;
			}
			if (bp.getType() == null) {
				result.add(StdlibMap.javaStringType);
			} else {
				result.add(codeUtil.classifierPathname(bp.getType()));
			}
		}
		return result;
	}

	public boolean isEndToAssociationClass() {
		return property instanceof EndToAssociationClass;
	}

	public AssociationClassEndMap getAssocationClassMap() {
		if (isEndToAssociationClass()) {
			return codeUtil.buildAssociationClassEndMap(((EndToAssociationClass) property).getOriginalProperty());
		} else if (isAssociationClassToEnd()) {
			return codeUtil.buildAssociationClassEndMap(((AssociationClassToEnd) property).getOriginalProperty());
		}
		return codeUtil.buildAssociationClassEndMap(property);
	}

	public boolean isAssociationClassToEnd() {
		return property instanceof AssociationClassToEnd;
	}

	public boolean isInvolvedInAssociationClass() {
		return isAssociationClassToEnd() || isEndToAssociationClass() || EmfAssociationUtil.isClass(property.getAssociation());
	}
}
