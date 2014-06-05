package org.pavanecce.uml.uml2code;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.ocl.uml.CollectionType;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Namespace;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.PrimitiveType;
import org.pavanecce.common.code.metamodel.CodeMappedType;
import org.pavanecce.common.code.metamodel.CodePackageReference;
import org.pavanecce.common.code.metamodel.CodeTypeReference;
import org.pavanecce.uml.common.util.EmfClassifierUtil;

public class UmlToCodeReferenceMap {

	protected static final String MAPPINGS_EXTENSION = "mappings";
	protected Map<NamedElement, CodeTypeReference> oldClassifierPaths = new HashMap<NamedElement, CodeTypeReference>();
	protected Map<Namespace, CodePackageReference> oldPackagePaths = new HashMap<Namespace, CodePackageReference>();
	protected Map<NamedElement, CodeTypeReference> classifierPaths = new HashMap<NamedElement, CodeTypeReference>();
	protected Map<Namespace, CodePackageReference> packagePaths = new HashMap<Namespace, CodePackageReference>();
	protected Map<Package, Map<String, CodeMappedType>> typeMap = new HashMap<Package, Map<String, CodeMappedType>>();

	public Map<String, CodeMappedType> getTypeMap(Package p) {
		Map<String, CodeMappedType> map = typeMap.get(p);
		if (map == null) {
			map = new HashMap<String, CodeMappedType>();
			typeMap.put(p, map);
			if (p.eResource() != null && p.eResource().getResourceSet() != null) {
				Resource eResource = p.eResource();
				URI uri = eResource.getURI();
				URI mappedTypesUri = uri.trimFileExtension().appendFileExtension(MAPPINGS_EXTENSION);
				try {
					InputStream inStream = eResource.getResourceSet().getURIConverter().createInputStream(mappedTypesUri);
					Properties props = new Properties();
					props.load(inStream);
					Set<Entry<Object, Object>> entrySet = props.entrySet();
					for (Entry<Object, Object> entry : entrySet) {
						map.put((String) entry.getKey(), new CodeMappedType((String) entry.getValue()));
					}
					// Ocl2CodePlugin.logInfo("Loaded mappings: " + mappedTypesUri);
				} catch (IOException e1) {
				}
			}
		}
		return map;
	}

	public CodeTypeReference classifierPathname(NamedElement classifier) {
		CodeTypeReference result = classifierPaths.get(classifier);
		if (result == null) {
			if (classifier instanceof PrimitiveType) {
				PrimitiveType root = EmfClassifierUtil.getRootClass((PrimitiveType) classifier);
				if (root.getName().equals("Integer")) {
					return StdlibMap.javaIntegerObjectType;
				} else if (root.getName().equals("Real")) {
					return StdlibMap.javaRealObjectType;
				} else if (root.getName().equals("Boolean")) {
					return StdlibMap.javaBooleanObjectType;
				} else {
					return StdlibMap.javaStringType;
				}
			} else if (classifier instanceof CollectionType) {
				// may never be called - monitor this
				CollectionType t = (CollectionType) classifier;
				switch (t.getKind()) {
				case BAG_LITERAL:
				case COLLECTION_LITERAL:
					return StdlibMap.javaCollectionType;
				case ORDERED_SET_LITERAL:
				case SEQUENCE_LITERAL:
					return StdlibMap.javaSequenceType;
				case SET_LITERAL:
					return StdlibMap.javaSetType;
				default:
					break;
				}
			} else {
				CodePackageReference pr = packagePathname(classifier.getNamespace());
				Map<String, String> mappings = EmfClassifierUtil.getMappings(classifier);
				CodeTypeReference ctr = new CodeTypeReference(mappings.isEmpty(), pr, classifier.getName(), mappings);
				classifierPaths.put(classifier, result = ctr);
				return ctr;
			}
		}
		return result;
	}

	public CodePackageReference packagePathname(Namespace p) {
		// TODO populate mappings
		CodePackageReference result = packagePaths.get(p);
		if (result == null) {
			Namespace parent = p.getNamespace();
			Map<String, String> packageMappings = EmfClassifierUtil.getMappings(p);
			if (parent == null) {
				packagePaths.put(p, result = new CodePackageReference(null, p.getName(), packageMappings));
			} else {
				packagePaths.put(p, result = new CodePackageReference(packagePathname(parent), p.getName(), packageMappings));
			}

		}
		return result;
	}

}