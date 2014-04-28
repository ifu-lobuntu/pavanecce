package org.pavanecce.uml.common.util;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Package;

public class LibraryImporter {
	public static Model importLibraryIfNecessary(Package model, String librName) {
		URI uri = URI.createURI(StereotypeNames.MODELS_PATHMAP + "libraries/" + librName);
		return importLibraryIfNecessary(model, uri);
	}

	public static Model importLibraryIfNecessary(Package model, URI uri) {
		Model library = findLibrary(model, uri.lastSegment());
		if (library == null) {
			Resource resource = model.eResource().getResourceSet().getResource(uri, true);
			library = (Model) resource.getContents().get(0);
			model.createPackageImport(library);
		}
		return library;
	}

	public static Model findLibrary(Package model, String librName) {
		Model library = null;
		for (Resource resource : model.eResource().getResourceSet().getResources()) {
			if (resource.getContents().size() > 0 && resource.getContents().get(0) instanceof Model) {
				Model m = (Model) resource.getContents().get(0);
				if (!m.eIsProxy() && resource.getURI().lastSegment().equals(librName)) {
					library = m;
				}
			}
		}
		return library;
	}
}
