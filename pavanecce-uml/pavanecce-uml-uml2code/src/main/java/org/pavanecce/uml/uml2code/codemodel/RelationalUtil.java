package org.pavanecce.uml.uml2code.codemodel;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Enumeration;
import org.eclipse.uml2.uml.Property;
import org.pavanecce.common.code.metamodel.relationaldb.IRelationalElement;
import org.pavanecce.common.code.metamodel.relationaldb.RelationalColumn;
import org.pavanecce.common.code.metamodel.relationaldb.RelationalInverseLink;
import org.pavanecce.common.code.metamodel.relationaldb.RelationalLink;
import org.pavanecce.common.code.metamodel.relationaldb.RelationalLinkTable;
import org.pavanecce.common.code.metamodel.relationaldb.RelationalTable;
import org.pavanecce.common.util.NameConverter;
import org.pavanecce.uml.common.util.EmfClassifierUtil;
import org.pavanecce.uml.common.util.EmfPropertyUtil;
import org.pavanecce.uml.common.util.PersistentNameUtil;

public class RelationalUtil {
	public static IRelationalElement buildRelationalElement(Property p) {
		IRelationalElement result = null;
		if (EmfClassifierUtil.isSimpleType(p.getType())) {
			result = new RelationalColumn(PersistentNameUtil.getPersistentName(p), EmfPropertyUtil.isRequired(p));
		} else if (EmfClassifierUtil.isPersistent(p.getType())) {
			if (EmfPropertyUtil.isMany(p)) {
				if (p.getOtherEnd() != null && EmfPropertyUtil.isMany(p.getOtherEnd())) {
					if (EmfPropertyUtil.isInverse(p)) {
						result = new RelationalInverseLink(p.getOtherEnd().getName(), true, true, false);
					} else {
						result = new RelationalLinkTable(NameConverter.toUnderscoreStyle(p.getAssociation().getName()), buildColumnMap(p),
								buildColumnMap(p.getOtherEnd()));
					}
				} else {
					result = new RelationalInverseLink(p.getOtherEnd().getName(), false, true, p.isComposite());
				}
			} else {
				if (p.getOtherEnd() != null && !EmfPropertyUtil.isMany(p.getOtherEnd()) && EmfPropertyUtil.isInverse(p)) {
					result = new RelationalInverseLink(p.getOtherEnd().getName(), false, false, p.isComposite());
				} else {
					String linkName = PersistentNameUtil.getPersistentName(p);
					for (EObject sa : p.getStereotypeApplications()) {
						EStructuralFeature linkNameFeature = sa.eClass().getEStructuralFeature("linkPersistentName");
						if (linkNameFeature != null) {
							linkName = (String) sa.eGet(linkNameFeature);
						}
					}
					LinkedHashMap<String, String> map = buildColumnMap(p);
					if (p.getOtherEnd() != null && !EmfPropertyUtil.isMany(p.getOtherEnd())) {
						result = new RelationalLink(linkName, map, true);
					} else {
						result = new RelationalLink(linkName, map);
					}
				}
			}
		} else if (p.getType() instanceof Enumeration) {
			result = new RelationalColumn(PersistentNameUtil.getPersistentName(p), EmfPropertyUtil.isRequired(p), true);
		}
		return result;
	}

	protected static LinkedHashMap<String, String> buildColumnMap(Property p) {
		LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
		for (EObject sa : p.getStereotypeApplications()) {
			EStructuralFeature linkedPropertiesFeature = sa.eClass().getEStructuralFeature("linkedProperties");
			if (linkedPropertiesFeature != null) {
				@SuppressWarnings("unchecked")
				EList<EObject> linkedProperties = (EList<EObject>) sa.eGet(linkedPropertiesFeature);
				for (EObject eObject : linkedProperties) {
					Property targetProperty = (Property) eObject.eGet(eObject.eClass().getEStructuralFeature("targetProperty"));
					String targetColumn = "id";
					if (targetProperty != null) {
						targetColumn = PersistentNameUtil.getPersistentName(targetProperty);
					}
					String persistentName = (String) eObject.eGet(eObject.eClass().getEStructuralFeature("sourcePersistentName"));
					map.put(persistentName, targetColumn);
				}
			}
		}
		if (map.isEmpty()) {
			map.put(PersistentNameUtil.getPersistentName(p), "id");
		}
		return map;
	}

	public static IRelationalElement buildRelationalElement(Class c) {
		String pk = "id";
		for (EObject sa : c.getStereotypeApplications()) {
			EStructuralFeature pkFeature = sa.eClass().getEStructuralFeature("primaryKey");
			if (pkFeature != null) {
				Property pkProp = (Property) sa.eGet(pkFeature);
				if (pkProp != null) {
					pk = pkProp.getName();
					break;
				}
			}
		}
		return new RelationalTable(PersistentNameUtil.getPersistentName(c), pk, new HashMap<String, List<String>>());
	}
}
