package org.pavanecce.uml.common.util;

import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EModelElement;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.NamedElement;

public class EmfWorkspace {
	private String identifier;

	public static String getId(Element object) {
		if (object == null) {
			return null;
		} else if (object instanceof EmfWorkspace) {
			return ((EmfWorkspace) object).getIdentifier();
		} else {
			String uid = null;
			if (object.eResource() != null) {
				uid = getResourceId(object.eResource()) + "@" + object.eResource().getURIFragment(object);
			} else if (object instanceof EModelElement && ((EModelElement) object).getEAnnotation(StereotypeNames.VDFP_ANNOTATION) != null) {
				return ((EModelElement) object).getEAnnotation(StereotypeNames.VDFP_ANNOTATION).getDetails().get("tempIdStoredOnDeletion");
			} else {
				uid = object.hashCode() + "";
			}
			return uid;
		}
	}

	public String getIdentifier() {
		return identifier;
	}

	public static String getResourceId(Resource eResource) {
		EObject v = eResource.getContents().get(0);
		EAnnotation ann = null;
		if (v instanceof EModelElement) {
			ann = ((EModelElement) v).getEAnnotation(StereotypeNames.VDFP_ANNOTATION);
		}
		if (ann == null) {
			return eResource.getURI().lastSegment();
		} else {
			String uuid = ann.getDetails().get("uuid");
			if (uuid == null || uuid.trim().length() == 0) {
				return eResource.getURI().lastSegment();
			} else {
				return uuid;
			}
		}
	}

	public static long getUniqueNumericId(NamedElement node) {
		char[] charArray = getId(node).toCharArray();
		long result = 1;
		int atSignIndex = 0;
		for (int i = 0; i < charArray.length; i++) {
			if (charArray[i] == '@') {
				atSignIndex = i;
			}
			result = (result * 31) + charArray[i] - i;
		}
		if (charArray.length > atSignIndex + 10) {
			// THis is where the most variation takes place in the emf id
			// Introduce some variation in the calculation
			for (int i = atSignIndex + 2; i < atSignIndex + 10; i++) {
				if (Character.isLowerCase(charArray[i])) {
					result = (result * 31) + Character.toUpperCase(charArray[i]) - i;
				} else {
					result = (result * 31) + Character.toLowerCase(charArray[i]) - i;
				}
			}
		}
		return Math.abs(result);
	}

}
