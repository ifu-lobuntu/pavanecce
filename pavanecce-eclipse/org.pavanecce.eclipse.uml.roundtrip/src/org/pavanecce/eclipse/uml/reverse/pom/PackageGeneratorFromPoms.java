package org.pavanecce.eclipse.uml.reverse.pom;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.m2e.model.edit.pom.Dependency;
import org.eclipse.m2e.model.edit.pom.DocumentRoot;
import org.eclipse.m2e.model.edit.pom.Parent;
import org.eclipse.m2e.model.edit.pom.PomPackage;
import org.eclipse.m2e.model.edit.pom.util.PomResourceFactoryImpl;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.Profile;
import org.eclipse.uml2.uml.Stereotype;
import org.eclipse.uml2.uml.UMLPackage;

@SuppressWarnings("restriction")
public class PackageGeneratorFromPoms {
	ResourceSet resourceSet = new ResourceSetImpl();
	private Map<File, DocumentRoot> dirRootMap = new HashMap<File, DocumentRoot>();
	// private Map<DocumentRoot,Package> rootPackageMap = new
	// HashMap<DocumentRoot,Package>();
	private Map<String, Package> coordinatePackageMap = new HashMap<String, Package>();
	private Model model;
	private Map<DocumentRoot, Package> rootPackageMap = new HashMap<DocumentRoot, Package>();
	private Stereotype projectStereotype;

	public PackageGeneratorFromPoms(Model model) {
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(Resource.Factory.Registry.DEFAULT_EXTENSION, new PomResourceFactoryImpl());
		resourceSet.getPackageRegistry().put(PomPackage.eNS_URI, PomPackage.eINSTANCE);
		this.model = model;
		Profile mavenProfile = model.getAppliedProfile("maven");
		if (mavenProfile != null) {
			projectStereotype = mavenProfile.getOwnedStereotype("Project");
		}
	}

	public Collection<Package> generateUml(Collection<IContainer> containers, IProgressMonitor m) throws Exception {
		for (IContainer c : containers) {
			findOrCreatePackage(model, new File(c.getLocation().toFile(), "pom.xml"));
		}
		Set<Entry<DocumentRoot, Package>> entrySet = rootPackageMap.entrySet();
		for (Entry<DocumentRoot, Package> entry : entrySet) {
			EList<Dependency> dependencies = entry.getKey().getProject().getDependencies();
			for (Dependency dependency : dependencies) {
				Package imported = coordinatePackageMap.get(key(dependency));
				if (imported != null) {
					entry.getValue().getPackageImport(imported, true);
				}
			}
		}
		return coordinatePackageMap.values();
	}

	public String key(Dependency dependency) {
		return dependency.getGroupId() + dependency.getArtifactId();
	}

	private Package findOrCreatePackage(Model model, File pomFile) {
		DocumentRoot root = getRoot(pomFile);
		Package parent = model;
		if (root.getProject().getParent() != null) {
			parent = findOrCreateParentPackage(root.getProject().getParent(), model, pomFile);
			if (parent == null) {
				// parent not available, just create in root of model
				parent = model;
			}
		}
		Package result = parent.getNestedPackage(calculatePackageName(root), false, UMLPackage.eINSTANCE.getPackage(), true);
		if (projectStereotype != null && !result.isStereotypeApplied(projectStereotype)) {
			result.applyStereotype(projectStereotype);
		}
		result.setValue(projectStereotype, "artifactId", root.getProject().getArtifactId());
		String groupId = root.getProject().getGroupId();
		if ((groupId == null || groupId.isEmpty()) && root.getProject() != null) {
			groupId = root.getProject().getGroupId();
		}
		result.setValue(projectStereotype, "groupId", groupId);
		result.setValue(projectStereotype, "packaging", root.getProject().getPackaging());
		coordinatePackageMap.put(key(root), result);
		rootPackageMap.put(root, result);
		return result;
	}

	private String key(DocumentRoot root) {
		String groupId = root.getProject().getGroupId();
		if ((groupId == null || groupId.isEmpty()) && root.getProject().getParent() != null) {
			groupId = root.getProject().getParent().getGroupId();
		}
		return groupId + root.getProject().getArtifactId();
	}

	private Package findOrCreateParentPackage(Parent parent, Model model, File pomFile) {
		File parentPomFile = null;
		if (parent.getRelativePath() == null || parent.getRelativePath().isEmpty()) {
			parentPomFile = new File(pomFile.getParentFile().getParentFile(), "pom.xml");
		} else {
			parentPomFile = new File(pomFile.getParentFile(), parent.getRelativePath());
		}
		parentPomFile = parentPomFile.getAbsoluteFile();
		if (parentPomFile.exists()) {
			if (!parentPomFile.isFile()) {
				// Happens sometimes, valid for Maven
				parentPomFile = new File(parentPomFile, "pom.xml");
			}
			return findOrCreatePackage(model, parentPomFile);
		} else {
			return null;
		}
	}

	private String calculatePackageName(DocumentRoot root) {
		return root.getProject().getArtifactId().replace('-', '_');
	}

	private DocumentRoot getRoot(File pomFile) {
		DocumentRoot result = dirRootMap.get(pomFile);
		if (result == null) {
			URI uri = URI.createFileURI(pomFile.getAbsolutePath());
			dirRootMap.put(pomFile, result = (DocumentRoot) resourceSet.getResource(uri, true).getContents().get(0));
		}
		return result;
	}
}
