package org.pavanecce.uml.common.util;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Profile;

public class ProfileApplier {

	public static Profile applyProfile(Model model, String profileName) {
		URI uri = URI.createURI(StereotypeNames.MODELS_PATHMAP + "profiles/" + profileName);
		return applyProfileIfNecessary(model, uri);
	}

	public static Profile applyProfileIfNecessary(Model model, URI uri) {
		Profile appliedProfile = getAppliedProfile(model, uri.lastSegment());
		if (model != null) {
			if (appliedProfile == null) {
				Resource resource = model.eResource().getResourceSet().getResource(uri, true);
				appliedProfile = (Profile) resource.getContents().get(0);
				model.applyProfile(appliedProfile);
			} else if (model.getProfileApplication(appliedProfile).getAppliedDefinition() != appliedProfile.getDefinition()) {
				model.applyProfile(appliedProfile);
			}
		}
		return appliedProfile;
	}

	public static Profile getAppliedProfile(Model model, String profileName) {
		if (model != null) {
			for (Profile profile : model.getAllAppliedProfiles()) {
				if (!profile.eIsProxy() && profile.eResource().getURI().lastSegment().equals(profileName)) {
					return profile;
				}
			}
		}
		return null;
	}

	public static Profile getProfile(EObject element, String opiumStandardProfile) {
		URI uri = URI.createURI(StereotypeNames.MODELS_PATHMAP + "profiles/" + opiumStandardProfile);
		return (Profile) element.eResource().getResourceSet().getResource(uri, true).getContents().get(0);
	}
}
