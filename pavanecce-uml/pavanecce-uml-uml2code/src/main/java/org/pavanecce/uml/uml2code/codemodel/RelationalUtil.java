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
import org.pavanecce.common.code.metamodel.relationaldb.RelationalTable;
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
				result = new RelationalInverseLink(p.getOtherEnd().getName());
			} else {
				String linkName = PersistentNameUtil.getPersistentName(p);
				LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
				for (EObject sa : p.getStereotypeApplications()) {
					EStructuralFeature linkNameFeature = sa.eClass().getEStructuralFeature("linkPersistentName");
					if (linkNameFeature != null) {
						linkName = (String) sa.eGet(linkNameFeature);
						@SuppressWarnings("unchecked")
						EList<EObject> linkedProperties = (EList<EObject>) sa.eGet(sa.eClass().getEStructuralFeature("linkedProperties"));
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
				result = new RelationalLink(linkName, map);
			}
		} else if (p.getType() instanceof Enumeration) {
			result = new RelationalColumn(PersistentNameUtil.getPersistentName(p), EmfPropertyUtil.isRequired(p));
		}
		return result;
	}

	public static IRelationalElement buildRelationalElement(Class c) {
		String pk = "id";
		for (EObject sa : c.getStereotypeApplications()) {
			EStructuralFeature pkFeature = sa.eClass().getEStructuralFeature("primaryKey");
			if (pkFeature != null) {
				Property pkProp = (Property) sa.eGet(pkFeature);
				if (pkProp != null) {
					pk = PersistentNameUtil.getPersistentName(pkProp);
				}
			}
		}
		return new RelationalTable(PersistentNameUtil.getPersistentName(c), pk, new HashMap<String, List<String>>());
	}
}
