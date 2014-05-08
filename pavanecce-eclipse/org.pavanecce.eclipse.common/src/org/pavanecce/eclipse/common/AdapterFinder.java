package org.pavanecce.eclipse.common;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.ecore.EObject;

public class AdapterFinder {
	public static EObject adaptObject(Object object) {
		if (object == null) {
			return null;
		} else if (object instanceof EObject) {
			return (EObject) object;
		} else if (object instanceof IAdaptable) {
			// Try IAdaptable
			IAdaptable adapted = (IAdaptable) object;
			Object eObject = adapted.getAdapter(EObject.class);
			if (eObject != null) {
				return (EObject) eObject;
			}
		} else {
			// Try registered adapter
			Object adapted = Platform.getAdapterManager().getAdapter(object, EObject.class);
			if (adapted != null) {
				return (EObject) adapted;
			}
		}
		return null;
	}
}
