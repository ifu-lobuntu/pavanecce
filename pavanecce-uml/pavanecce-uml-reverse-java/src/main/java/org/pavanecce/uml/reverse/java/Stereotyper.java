package org.pavanecce.uml.reverse.java;

import java.util.Map.Entry;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.impl.DynamicEObjectImpl;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Stereotype;
import org.pavanecce.uml.reverse.java.sourcemodel.SourceAnnotation;
import org.pavanecce.uml.reverse.java.sourcemodel.SourceClass;
import org.pavanecce.uml.reverse.java.sourcemodel.SourceVariable;

public class Stereotyper {
	public static void stereotype(Element e, SourceAnnotation[] javaAnnotations, ClassifierFactory cf) {
		for (SourceAnnotation ab : javaAnnotations) {
			Stereotype st = (Stereotype) cf.getClassifierFor(ab.getAnnotationType());
			if (st != null && e.isStereotypeApplicable(st)) {
				if (!e.isStereotypeApplied(st)) {
					e.applyStereotype(st);
				}
				DynamicEObjectImpl sa = (DynamicEObjectImpl) e.getStereotypeApplication(st);
				populateDynamicEObject(cf, ab, sa);
			}
		}
	}

	private static void populateDynamicEObject(ClassifierFactory cf, SourceAnnotation ab, DynamicEObjectImpl sa) {
		for (Entry<String, Object> v : ab.getMemberValuePairs()) {
			EStructuralFeature feat = sa.eClass().getEStructuralFeature(v.getKey());
			if (v.getValue() instanceof Object[]) {
				Object[] values = (Object[]) v.getValue();
				@SuppressWarnings("unchecked")
				EList<Object> eObjects = (EList<Object>) sa.eGet(feat);
				eObjects.clear();
				for (Object object : values) {
					eObjects.add(convertSingleValue(cf, sa, v.getKey(), object));
				}
			} else {
				Object value = convertSingleValue(cf, sa, v.getKey(), v.getValue());
				sa.eSet(feat, value);
			}
		}
	}

	private static Object convertSingleValue(ClassifierFactory cf, DynamicEObjectImpl st, String name, Object fromValue) {
		Object value = null;
		if (fromValue instanceof String || fromValue instanceof Number || fromValue instanceof Boolean) {
			value = fromValue;
		} else if (fromValue instanceof SourceVariable) {
			SourceVariable vb = (SourceVariable) fromValue;
			if (vb.isEnumConstant()) {
				// Remember annotations wont have associations to enums nor
				// inheritance - this will work:
				EEnum en = (EEnum) st.eClass().getEStructuralFeature(name).getEType();
				value = en.getEEnumLiteral(vb.getName());
			}
		} else if (fromValue instanceof SourceClass) {
			value = cf.getClassifierFor((SourceClass) fromValue);
		} else if (fromValue instanceof SourceAnnotation) {
			EClass eClass = (EClass) st.eClass().getEStructuralFeature(name).getEType();
			value = new DynamicEObjectImpl(eClass);
			populateDynamicEObject(cf, (SourceAnnotation) fromValue, (DynamicEObjectImpl) value);
		}
		return value;
	}
}
