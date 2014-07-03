package org.pavanecce.uml.uml2code.codemodel;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Enumeration;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Namespace;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Type;
import org.pavanecce.common.code.metamodel.documentdb.ChildDocument;
import org.pavanecce.common.code.metamodel.documentdb.ChildDocumentCollection;
import org.pavanecce.common.code.metamodel.documentdb.DocumentEnumProperty;
import org.pavanecce.common.code.metamodel.documentdb.DocumentEnumeratedType;
import org.pavanecce.common.code.metamodel.documentdb.DocumentNamespace;
import org.pavanecce.common.code.metamodel.documentdb.DocumentNodeType;
import org.pavanecce.common.code.metamodel.documentdb.DocumentProperty;
import org.pavanecce.common.code.metamodel.documentdb.IDocumentElement;
import org.pavanecce.common.code.metamodel.documentdb.ParentDocument;
import org.pavanecce.common.code.metamodel.documentdb.PropertyType;
import org.pavanecce.common.code.metamodel.documentdb.ReferencedDocument;
import org.pavanecce.common.code.metamodel.documentdb.ReferencedDocumentCollection;
import org.pavanecce.uml.common.util.EmfClassifierUtil;
import org.pavanecce.uml.common.util.EmfElementFinder;
import org.pavanecce.uml.common.util.EmfPropertyUtil;

public class DocumentUtil {
	Map<Classifier, DocumentNodeType> nodes = new HashMap<Classifier, DocumentNodeType>();
	Map<Namespace, DocumentNamespace> namespaces = new HashMap<Namespace, DocumentNamespace>();

	public IDocumentElement buildDocumentElement(Property p) {
		IDocumentElement result = null;
		if (EmfClassifierUtil.isSimpleType(p.getType())) {
			result = new DocumentProperty(DocumentNameUtil.name(p), getNamespaceOf(p), simpleType(p.getType()), EmfPropertyUtil.isRequired(p),
					EmfPropertyUtil.isMany(p));
		} else if (EmfClassifierUtil.isPersistent(p.getType())) {
			if (EmfPropertyUtil.isMany(p)) {
				if (p.isComposite()) {
					result = new ChildDocumentCollection(getNamespaceOf(p), DocumentNameUtil.name(p), getDocumentNode((Class) p.getType()));
				} else {
					result = new ReferencedDocumentCollection(getNamespaceOf(p), DocumentNameUtil.name(p), getDocumentNode((Class) p.getType()));
				}
			} else {
				if (p.getOtherEnd() != null && p.getOtherEnd().isComposite()) {
					result = new ParentDocument(getNamespaceOf(p), DocumentNameUtil.name(p), getDocumentNode((Class) p.getType()), EmfPropertyUtil.isMany(p
							.getOtherEnd()));
				} else if (p.isComposite()) {
					result = new ChildDocument(getNamespaceOf(p), DocumentNameUtil.name(p), getDocumentNode((Class) p.getType()), EmfPropertyUtil.isRequired(p));
				} else {
					result = new ReferencedDocument(getNamespaceOf(p), DocumentNameUtil.name(p), getDocumentNode((Class) p.getType()),
							EmfPropertyUtil.isRequired(p));
				}
			}
		} else if (p.getType() instanceof Enumeration) {
			result = new DocumentEnumProperty(DocumentNameUtil.name(p), getNamespaceOf(p), EmfPropertyUtil.isRequired(p), EmfPropertyUtil.isMany(p),
					getDocumentNode((Enumeration) p.getType()));
		}
		return result;
	}

	private PropertyType simpleType(Type type) {
		if (EmfClassifierUtil.comformsToLibraryType(type, "String")) {
			return PropertyType.STRING;
		}
		if (EmfClassifierUtil.comformsToLibraryType(type, "Integer")) {
			return PropertyType.LONG;
		}
		if (EmfClassifierUtil.comformsToLibraryType(type, "Long")) {
			return PropertyType.LONG;
		}
		if (EmfClassifierUtil.comformsToLibraryType(type, "Boolean")) {
			return PropertyType.BOOLEAN;
		}
		if (EmfClassifierUtil.comformsToLibraryType(type, "Real")) {
			return PropertyType.DOUBLE;
		}
		if (EmfClassifierUtil.comformsToLibraryType(type, "Date")) {
			return PropertyType.DATE;
		}
		if (EmfClassifierUtil.comformsToLibraryType(type, "DateTime")) {
			return PropertyType.DATE;
		}
		if (EmfClassifierUtil.comformsToLibraryType(type, "BinaryLargeObject")) {
			return PropertyType.BINARY;
		}
		return PropertyType.BINARY;
	}

	protected DocumentNamespace getNamespaceOf(NamedElement p) {
		return buildNamespace(p);
	}

	public DocumentNamespace buildNamespace(NamedElement e) {
		if (e instanceof Model) {
			DocumentNamespace result = namespaces.get(e);
			if (result == null) {
				// TODO make it more clever - look for maven co-ordinates, etc
				namespaces.put((Namespace) e, result = new DocumentNamespace("http://" + e.getName() + "/", e.getName()));
			}
			return result;
		} else {
			EObject container = EmfElementFinder.getContainer(e);
			if (container instanceof NamedElement) {
				return buildNamespace((NamedElement) container);
			} else {
				return new DocumentNamespace("http://nonamspace/", "non");
			}
		}
	}

	public DocumentNodeType getDocumentNode(Class clss) {
		DocumentNodeType result = nodes.get(clss);
		if (result == null) {
			nodes.put(clss, result = buildDocumentNode(clss));
		}
		return result;
	}

	public DocumentEnumeratedType getDocumentNode(Enumeration clss) {
		DocumentEnumeratedType result = (DocumentEnumeratedType) nodes.get(clss);
		if (result == null) {
			nodes.put(clss, result = new DocumentEnumeratedType(getNamespaceOf(clss), DocumentNameUtil.name(clss)));
		}
		return result;
	}

	private DocumentNodeType buildDocumentNode(Class c) {
		DocumentNodeType result = new DocumentNodeType(getNamespaceOf(c), DocumentNameUtil.name(c));
		result.setPathField("path");
		result.setUuidField("id");
		return result;
	}
}
