package org.pavanecce.uml.ocl2code.common;

import static org.pavanecce.common.util.NameConverter.toValidVariableName;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EEnumLiteral;
import org.eclipse.ocl.TypeResolver;
import org.eclipse.ocl.expressions.CollectionKind;
import org.eclipse.ocl.uml.CollectionType;
import org.eclipse.ocl.uml.TupleType;
import org.eclipse.uml2.uml.Association;
import org.eclipse.uml2.uml.Behavior;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Collaboration;
import org.eclipse.uml2.uml.DataType;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.EnumerationLiteral;
import org.eclipse.uml2.uml.MultiplicityElement;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Namespace;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.Parameter;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.StateMachine;
import org.eclipse.uml2.uml.Stereotype;
import org.eclipse.uml2.uml.TypedElement;
import org.eclipse.uml2.uml.UseCase;
import org.eclipse.uml2.uml.Vertex;
import org.pavanecce.common.code.metamodel.CodePackageReference;
import org.pavanecce.common.code.metamodel.CodeTypeReference;
import org.pavanecce.common.util.NameConverter;
import org.pavanecce.uml.common.util.CodeGenerationStrategy;
import org.pavanecce.uml.common.util.EmfAssociationUtil;
import org.pavanecce.uml.common.util.EmfClassifierUtil;
import org.pavanecce.uml.common.util.EmfElementFinder;
import org.pavanecce.uml.common.util.EmfPackageUtil;
import org.pavanecce.uml.common.util.EmfPropertyUtil;
import org.pavanecce.uml.common.util.EmfWorkspace;
import org.pavanecce.uml.common.util.TagNames;
import org.pavanecce.uml.common.util.emulated.ArtificialElementFactory;
import org.pavanecce.uml.common.util.emulated.EmulatedPropertyHolderForAssociation;
import org.pavanecce.uml.common.util.emulated.OclRuntimeLibrary;
import org.pavanecce.uml.ocl2code.maps.AssociationClassEndMap;
import org.pavanecce.uml.ocl2code.maps.ClassifierMap;
import org.pavanecce.uml.ocl2code.maps.OperationMap;
import org.pavanecce.uml.ocl2code.maps.PropertyMap;
import org.pavanecce.uml.ocl2code.maps.StateMap;
import org.pavanecce.uml.ocl2code.maps.TupleTypeMap;
import org.pavanecce.uml.uml2code.UmlToCodeReferenceMap;

public class UmlToCodeMaps extends UmlToCodeReferenceMap {
	public static int instanceCount;
	// CLean this duplication up
	private OclRuntimeLibrary library;

	private Map<NamedElement, StateMap> stateMaps = new HashMap<NamedElement, StateMap>();
	private Map<NamedElement, OperationMap> operationMaps = new HashMap<NamedElement, OperationMap>();
	private Map<NamedElement, PropertyMap> structuralFeatureMaps = new HashMap<NamedElement, PropertyMap>();
	private Map<String, ClassifierMap> classifierMaps = new HashMap<String, ClassifierMap>();
	private Map<Namespace, CodeTypeReference> statePathnames = new HashMap<Namespace, CodeTypeReference>();
	private CodeTypeReference environmentPathname;
	private boolean regenMappedTypes;
	private ArtificialElementFactory artificialElementFactory = null;
	TypeResolver<Classifier, Operation, Property> typeResolver;

	public UmlToCodeMaps(OclRuntimeLibrary library, TypeResolver<Classifier, Operation, Property> typeResolver) {
		super();
		this.library = library;
		this.typeResolver = typeResolver;
		instanceCount++;
	}

	public CodeTypeReference environmentPathname() {
		return environmentPathname;
	}

	public OclRuntimeLibrary getLibrary() {
		return library;
	}

	public void clearCache() {
		oldClassifierPaths = classifierPaths;
		oldPackagePaths = packagePaths;
		classifierPaths = new HashMap<NamedElement, CodeTypeReference>();
		packagePaths = new HashMap<Namespace, CodePackageReference>();
		operationMaps = new HashMap<NamedElement, OperationMap>();
		structuralFeatureMaps = new HashMap<NamedElement, PropertyMap>();
		classifierMaps = new HashMap<String, ClassifierMap>();
		statePathnames = new HashMap<Namespace, CodeTypeReference>();
	}

	public void lock() {
		// structuralFeatureMaps =
		// Collections.unmodifiableMap(structuralFeatureMaps);
		// packagePaths = Collections.unmodifiableMap(packagePaths);
		// classifierPaths = Collections.unmodifiableMap(classifierPaths);
		// classifierMaps = Collections.unmodifiableMap(classifierMaps);
		// operationMaps = Collections.unmodifiableMap(operationMaps);
		// signalMaps = Collections.unmodifiableMap(signalMaps);
		// statePathnames = Collections.unmodifiableMap(statePathnames);
	}

	public CodePackageReference utilPackagePath(Element e) {
		Map<String, String> emptyMap = Collections.emptyMap();
		if (e instanceof Namespace) {
			return new CodePackageReference(packagePathname((Namespace) e).getCopy(), "util", emptyMap);
		} else {
			// TODO
			return new CodePackageReference(null, "util", emptyMap);
		}
	}

	public PropertyMap buildStructuralFeatureMap(TypedElement typedAndOrdered) {
		PropertyMap map = structuralFeatureMaps.get(typedAndOrdered);
		if (map == null) {
			Property prop = null;
			if (typedAndOrdered instanceof Property) {
				prop = (Property) typedAndOrdered;
			} else if (typedAndOrdered instanceof Parameter) {
				Parameter param = (Parameter) typedAndOrdered;
				if (param.getOwner() instanceof StateMachine) {
					StateMachine b = (StateMachine) param.getOwner();
					prop = artificialElementFactory.getEmulatedPropertyHolder(b).getEmulatedAttribute(param);
				}
			}
			map = new PropertyMap(this, prop);
			structuralFeatureMaps.put(typedAndOrdered, map);
		}
		return map;
	}

	public CodeTypeReference getOldClassifierPathname(NamedElement c) {
		if (oldClassifierPaths != null) {
			return oldClassifierPaths.get(c);
		}
		return null;
	}

	public String delegateQualifierArguments(List<Property> qualifiers) {
		StringBuilder sb = new StringBuilder();
		// Assume qualifiers are available as parameters in the calling
		// CodeMethod context
		for (Property q : qualifiers) {
			PropertyMap qMap = buildStructuralFeatureMap(q);
			sb.append(qMap.fieldname());
			sb.append(",");
		}
		String string = sb.toString();
		return string;
	}

	public String addQualifierArguments(List<Property> qualifiers, String varName) {
		StringBuilder sb = new StringBuilder();
		// Assume qualifiers are backed by attributes
		for (Property q : qualifiers) {
			PropertyMap qMap = buildStructuralFeatureMap(q);
			sb.append(varName);
			sb.append(".");
			sb.append(qMap.getter());
			sb.append("(),");
		}
		String string = sb.toString();
		return string;
	}

	public ClassifierMap buildClassifierMap(Classifier c) {
		String key = c.getQualifiedName();
		if (c instanceof CollectionType) {
			key += ((CollectionType) c).getElementType().getQualifiedName();
		}
		ClassifierMap result = classifierMaps.get(key);
		if (result == null) {
			classifierMaps.put(key, result = new ClassifierMap(this, c));
		}
		return result;
	}

	public ClassifierMap buildClassifierMap(Classifier c, MultiplicityElement m) {
		CollectionKind kind = EmfPropertyUtil.getCollectionKind(m);
		return buildClassifierMap(c, kind);
	}

	public ClassifierMap buildClassifierMap(Classifier c, CollectionKind kind) {
		if (kind != null) {
			Classifier actualType = (Classifier) typeResolver.resolveCollectionType(kind, c);
			return buildClassifierMap(actualType);
		} else {
			return buildClassifierMap(c);
		}
	}

	public OperationMap buildOperationMap(Operation s) {
		OperationMap result = operationMaps.get(s);
		if (result == null) {
			operationMaps.put(s, result = new OperationMap(this, s));
		}
		return result;
	}

	public CodeTypeReference statePathname(Namespace activity) {
		CodeTypeReference result = statePathnames.get(activity);
		if (result == null) {
			Namespace namespace = (Namespace) EmfElementFinder.getContainer(activity);
			statePathnames.put(activity, result = new CodeTypeReference(true, packagePathname(namespace), classifierPathname(activity).getLastName() + "State",
					Collections.<String, String> emptyMap()));
		}
		return result;
	}

	public OperationMap buildOperationMap(NamedElement o) {
		if (o instanceof Behavior) {
			OperationMap result = operationMaps.get(o);
			if (result == null) {
				operationMaps.put(o, result = new OperationMap(this, o, ((Behavior) o).getOwnedParameters()));
			}
			return result;
		} else {
			return buildOperationMap((Operation) o);
		}
	}

	public boolean requiresJavaRename(NamedElement a) {
		return oldClassifierPaths != null && oldClassifierPaths.containsKey(a) && classifierPaths.containsKey(a)
				&& !oldClassifierPaths.get(a).equals(classifierPaths.get(a));
	}

	public CodePackageReference getOldPackagePathname(Namespace c) {
		if (oldPackagePaths != null) {
			return oldPackagePaths.get(c);
		}
		return null;
	}

	public StateMap buildStateMap(Vertex referredState) {
		StateMap stateMap = this.stateMaps.get(referredState);
		if (stateMap == null) {
			this.stateMaps.put(referredState, stateMap = new StateMap(this, referredState));
		}
		return stateMap;
	}

	public TupleTypeMap buildTupleTypeMap(TupleType in) {
		return new TupleTypeMap(this, in);
	}

	public AssociationClassEndMap buildAssociationClassEndMap(Property redefinedProperty) {
		return new AssociationClassEndMap(this, redefinedProperty);
	}

	public CodeTypeReference utilClass(Element e, String suffix) {
		if (e instanceof EmfWorkspace) {
			CodePackageReference utilPackagePath = utilPackagePath(e);
			return new CodeTypeReference(true, utilPackagePath, NameConverter.capitalize(((EmfWorkspace) e).getIdentifier()) + suffix,
					Collections.<String, String> emptyMap());
		} else {
			Package owner = EmfPackageUtil.getRootObject(e);
			CodePackageReference result = utilPackagePath(owner).getCopy();
			return new CodeTypeReference(true, result, NameConverter.capitalize(((Namespace) e).getName()) + suffix, Collections.<String, String> emptyMap());
		}
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		instanceCount--;
	}

	public CodeTypeReference tokenPathName(Namespace b) {
		CodeTypeReference tr = classifierPathname(b);
		CodePackageReference copy2 = tr.getCodePackageReference().getCopy();
		return new CodeTypeReference(true, copy2, tr.getLastName() + "Token", Collections.<String, String> emptyMap());
	}

	/**
	 * Some classifiers in UML would not necessarily be generated as Java classes. Returns false for NakedBehaviors that
	 * have one or less resulting parameters
	 * 
	 */
	public boolean hasCodeClass(Classifier c) {
		if (c == null || c instanceof Stereotype || c instanceof Collaboration || c instanceof UseCase) {
			return false;
		} else if (isMarkedForDeletion(c) || getCodeGenerationStrategy(c) == CodeGenerationStrategy.NO_CODE) {
			return false;
		} else if (c instanceof DataType) {
			return !EmfClassifierUtil.isSimpleType(c);
		} else if (c instanceof Association) {
			return EmfAssociationUtil.isClass((Association) c);
		} else {
			return true;
		}
	}

	private boolean isMarkedForDeletion(Classifier c) {
		return c.eContainer() == null;
	}

	public CodeGenerationStrategy getCodeGenerationStrategy(NamedElement c) {
		CodeGenerationStrategy codeGenerationStrategy = CodeGenerationStrategy.ALL;
		if (regenMappedTypes) {
			return codeGenerationStrategy;
		} else if (getTypeMap(EmfPackageUtil.getRootObject(c)).containsKey(c.getQualifiedName())) {
			codeGenerationStrategy = CodeGenerationStrategy.NO_CODE;
		} else if (c instanceof Classifier) {
			Classifier cl = (Classifier) c;
			String s = (String) EmfClassifierUtil.getTagValue(cl, TagNames.MAPPED_IMPLEMENTATION_TYPE);
			if (s != null && s.length() > 0) {
				codeGenerationStrategy = CodeGenerationStrategy.NO_CODE;
			}
			// TODO this bit is obsolete
			EEnumLiteral l = (EEnumLiteral) EmfClassifierUtil.getTagValue(cl, TagNames.CODE_GENERATION_STRATEGY);
			if (l != null) {
				codeGenerationStrategy = Enum.valueOf(CodeGenerationStrategy.class, l.getName().toUpperCase());
			}
			if (Boolean.TRUE.equals(EmfClassifierUtil.getTagValue(cl, "generateAbstractSupertype"))) {
				codeGenerationStrategy = CodeGenerationStrategy.ABSTRACT_SUPERTYPE_ONLY;
			}
		} else if (c instanceof Package && EmfPackageUtil.hasMappedImplementationPackage((Package) c)) {
			codeGenerationStrategy = CodeGenerationStrategy.NO_CODE;
		}
		return codeGenerationStrategy;
	}

	public String toCodeLiteral(EnumerationLiteral lit) {
		return NameConverter.toUnderscoreStyle(toValidVariableName(lit.getName())).toUpperCase();
	}

	public EmulatedPropertyHolderForAssociation getEmulatedPropertyHolder(Association association) {
		return (EmulatedPropertyHolderForAssociation) artificialElementFactory.getEmulatedPropertyHolder(association);
	}
}
