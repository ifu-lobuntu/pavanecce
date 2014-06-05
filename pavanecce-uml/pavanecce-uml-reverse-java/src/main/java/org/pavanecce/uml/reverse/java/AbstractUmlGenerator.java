package org.pavanecce.uml.reverse.java;

import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.URIConverter;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.DataType;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Interface;
import org.eclipse.uml2.uml.LiteralBoolean;
import org.eclipse.uml2.uml.LiteralInteger;
import org.eclipse.uml2.uml.LiteralReal;
import org.eclipse.uml2.uml.LiteralString;
import org.eclipse.uml2.uml.LiteralUnlimitedNatural;
import org.eclipse.uml2.uml.OpaqueExpression;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.Parameter;
import org.eclipse.uml2.uml.ParameterDirectionKind;
import org.eclipse.uml2.uml.PrimitiveType;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Stereotype;
import org.eclipse.uml2.uml.UMLFactory;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.ValueSpecification;
import org.pavanecce.uml.common.util.EmfParameterUtil;
import org.pavanecce.uml.reverse.java.sourcemodel.SourceAnnotation;
import org.pavanecce.uml.reverse.java.sourcemodel.SourceClass;
import org.pavanecce.uml.reverse.java.sourcemodel.SourceCode;
import org.pavanecce.uml.reverse.java.sourcemodel.SourceMethod;
import org.pavanecce.uml.reverse.java.sourcemodel.SourceProperty;

public abstract class AbstractUmlGenerator {
	public static final String MAPPINGS_EXTENSION = "mappings";

	protected ClassifierFactory factory;

	private Map<String, Classifier> classMap;

	private boolean deleteNonMatchedOperations = true;

	protected abstract boolean canReverse(Package p);

	public abstract Collection<Element> generateUmlImpl(Set<SourceClass> types, Package library, IProgressMonitor m) throws Exception;

	public final Set<SourceClass> extractProfileElements(Set<SourceClass> types) throws Exception {
		Set<SourceClass> result = new HashSet<SourceClass>();
		for (SourceClass sourceClass : types) {
			addProfileElementsTo(result, sourceClass.getAnnotations());
			for (SourceProperty sourceProperty : sourceClass.getPropertyDescriptors().values()) {
				addProfileElementsTo(result, sourceProperty.getAnnotations());
			}
			for (SourceMethod sourceMethod : sourceClass.getDeclaredMethods()) {
				addProfileElementsTo(result, sourceMethod.getAnnotations());
			}
		}
		return result;
	}

	private void addProfileElementsTo(Set<SourceClass> result, SourceAnnotation[] annotations) {
		for (SourceAnnotation sourceAnnotation : annotations) {
			addProfileElementsTo(result, sourceAnnotation.getAnnotationType());
		}
	}

	public Map<String, Classifier> getClassMap() {
		if (this.classMap == null) {
			if (factory == null) {
				this.classMap = new HashMap<String, Classifier>();
			} else {
				this.classMap = factory.getClassMap();
			}
		}
		return this.classMap;
	}

	private void addProfileElementsTo(Collection<SourceClass> result, SourceClass annotationType) {
		for (Entry<String, SourceProperty> annotationField : annotationType.getPropertyDescriptors().entrySet()) {
			SourceClass baseType = annotationField.getValue().getBaseType();
			if (baseType.isEnum()) {
				result.add(baseType);
			} else if (baseType.isAnnotation() && !result.contains(baseType)) {
				addProfileElementsTo(result, annotationType);
			}
		}
		result.add(annotationType);
	}

	public final Collection<Element> generateUml(Set<SourceClass> types, Package library, IProgressMonitor m) throws Exception {
		Collection<Element> result;

		if (canReverse(library)) {
			factory = createClassifierFactory(library);
			factory.getClassMap().putAll(getClassMap());
			URI uri = library.eResource().getURI().trimFileExtension().appendFileExtension(MAPPINGS_EXTENSION);
			URIConverter uriConverter = library.eResource().getResourceSet().getURIConverter();
			try {
				if (uriConverter.exists(uri, Collections.EMPTY_MAP)) {
					InputStream is = uriConverter.createInputStream(uri);
					factory.getMappedTypes().load(is);
				}
			} catch (Exception e) {

			}
			result = generateUmlImpl(types, library, m);
			library.eResource().save(null);
			factory.getMappedTypes().store(uriConverter.createOutputStream(uri), "Generated");
		} else {
			result = Collections.emptySet();
		}
		return result;
	}

	protected ClassifierFactory createClassifierFactory(Package library) {
		return new ClassifierFactory(library);
	}

	public Package getRootPackage(Resource resource) {
		EList<EObject> contents = resource.getContents();
		for (EObject eObject : contents) {
			if (eObject instanceof Package) {
				return (Package) eObject;
			}
		}
		return null;
	}

	protected void populateOperations(Package modelOrProfile, Classifier owner, SourceClass t) {
		Set<Operation> existingOperations = new HashSet<Operation>(owner.getOperations());
		SourceMethod[] mds = t.getDeclaredMethods();
		EList<Operation> ownedOperations = getOwnedOperations(owner);
		for (SourceMethod md : mds) {
			if (!md.isConstructor() && !md.isAccessor()) {
				Operation oper = findOrCreateOperation(ownedOperations, md);
				existingOperations.remove(oper);
				oper.setIsStatic(md.isStatic());
				oper.setVisibility(md.getVisibility());
				SourceClass returnType = md.getReturnType();
				SourceClass[] parameterTypes = md.getParameterTypes();
				List<Parameter> args = EmfParameterUtil.getArgumentParameters(oper);
				String[] parameterNames = md.getParameterNames();
				// NB!! at this point the parameters already exist
				for (int i = 0; i < parameterTypes.length; i++) {
					Parameter param = args.get(i);
					param.setName(parameterNames[i]);
					param.setDirection(ParameterDirectionKind.IN_LITERAL);
					populateMultiplicity(parameterTypes[i], param);
				}
				if (returnType == null || returnType.getName().equals("void")) {
					Parameter returnParameter = EmfParameterUtil.getReturnParameter(oper);
					if (returnParameter != null) {
						oper.getOwnedParameters().remove(returnParameter);
					}
				} else {
					Parameter result = oper.getOwnedParameter("result", getClassifierFor(returnType), false, true);
					result.setDirection(ParameterDirectionKind.RETURN_LITERAL);
					populateMultiplicity(returnType, result);
				}
				for (Parameter parameter : EmfParameterUtil.getResultParameters(oper)) {
					if (!parameter.getName().equals("result") && parameter.getDirection() != ParameterDirectionKind.RETURN_LITERAL) {
						oper.getOwnedParameters().remove(parameter);
					}
				}
				if (!(owner instanceof Interface)) {
					SourceCode source = md.getSource();
					if (source != null) {
						if (oper.getBodyCondition() == null) {
							oper.setBodyCondition(UMLFactory.eINSTANCE.createConstraint());
							oper.getBodyCondition().setName(oper.getName() + "Body");
						}
						OpaqueExpression oe = putCode(source, oper.getBodyCondition().getSpecification());
						oper.setIsQuery(true);
						oper.getBodyCondition().setSpecification(oe);
					}
				}
			}
		}
		if (deleteNonMatchedOperations) {
			for (Operation operation : existingOperations) {
				ownedOperations.remove(operation);
			}
		}
	}

	protected OpaqueExpression putCode(SourceCode source, ValueSpecification originalValue) {
		OpaqueExpression oe;
		if (originalValue instanceof OpaqueExpression) {
			oe = ((OpaqueExpression) originalValue);
		} else {
			oe = UMLFactory.eINSTANCE.createOpaqueExpression();
		}
		EList<String> languages = oe.getLanguages();
		if (languages.contains(source.getLanguage())) {
			oe.getBodies().set(languages.indexOf(source.getLanguage()), source.getCode());
		} else {
			languages.add(source.getLanguage());
			oe.getBodies().add(source.getCode());
		}
		return oe;
	}

	private void populateMultiplicity(SourceClass sourceClass, Parameter param) {
		if (sourceClass.isManyType()) {
			param.setIsUnique(sourceClass.isUniqueCollectionType());
			param.setIsOrdered(sourceClass.isOrderedCollectionType());
			param.setUpper(LiteralUnlimitedNatural.UNLIMITED);
		}
	}

	private Operation findOrCreateOperation(EList<Operation> ownedOperations, SourceMethod md) {
		for (Operation oper : ownedOperations) {
			if (oper.getName().equals(md.getName())) {
				List<Parameter> args = EmfParameterUtil.getArgumentParameters(oper);
				if (md.getParameterTypes().length == args.size()) {
					boolean paramsMatch = true;
					for (int i = 0; i < md.getParameterTypes().length; i++) {
						Parameter param = args.get(i);
						if (getClassifierFor(md.getParameterTypes()[i]) != param.getType()) {
							paramsMatch = false;
							break;
						}
					}
					if (paramsMatch) {
						return oper;
					}
				}
			}
		}
		Operation oper = UMLFactory.eINSTANCE.createOperation();
		oper.setName(md.getName());
		String[] parameterNames = md.getParameterNames();
		for (int i = 0; i < parameterNames.length; i++) {
			String string = parameterNames[i];
			oper.getOwnedParameter(string, getClassifierFor(md.getParameterTypes()[i]), false, true);
		}
		ownedOperations.add(oper);
		return oper;
	}

	protected void populateAttributes(Package modelOrProfile, Classifier classifier, SourceClass t) {
		Collection<SourceProperty> fields = t.getPropertyDescriptors().values();
		for (SourceProperty pd : fields) {
			Property attr = findProperty(classifier, pd);
			if (attr == null) {
				attr = createAttribute(classifier, pd);
			}
			Classifier type = getClassifierFor(pd.getBaseType());
			if (classifier instanceof Stereotype) {
				type = getStandardPrimitiveType(type);
			}
			attr.setType(type);
			attr.setIsReadOnly(pd.isReadOnly());
			attr.setIsDerived(pd.isDerived());
			if (pd.getInitialValue() instanceof Double || pd.getInitialValue() instanceof Float) {
				LiteralReal lr = null;
				if (attr.getDefaultValue() instanceof LiteralReal) {
					lr = (LiteralReal) attr.getDefaultValue();
				} else {
					attr.setDefaultValue(lr = UMLFactory.eINSTANCE.createLiteralReal());
				}
				lr.setValue(((Number) pd.getInitialValue()).doubleValue());
			} else if (pd.getInitialValue() instanceof Integer || pd.getInitialValue() instanceof Short || pd.getInitialValue() instanceof Long
					|| pd.getInitialValue() instanceof Byte) {
				LiteralInteger lr = null;
				if (attr.getDefaultValue() instanceof LiteralInteger) {
					lr = (LiteralInteger) attr.getDefaultValue();
				} else {
					attr.setDefaultValue(lr = UMLFactory.eINSTANCE.createLiteralInteger());
				}
				lr.setValue(((Number) pd.getInitialValue()).intValue());
			} else if (pd.getInitialValue() instanceof Boolean) {
				LiteralBoolean lr = null;
				if (attr.getDefaultValue() instanceof LiteralBoolean) {
					lr = (LiteralBoolean) attr.getDefaultValue();
				} else {
					attr.setDefaultValue(lr = UMLFactory.eINSTANCE.createLiteralBoolean());
				}
				lr.setValue(((Boolean) pd.getInitialValue()).booleanValue());
			} else if (pd.getInitialValue() instanceof String) {
				LiteralString lr = null;
				if (attr.getDefaultValue() instanceof LiteralString) {
					lr = (LiteralString) attr.getDefaultValue();
				} else {
					attr.setDefaultValue(lr = UMLFactory.eINSTANCE.createLiteralString());
				}
				lr.setValue(((String) pd.getInitialValue()));
			} else if (pd.getInitialValue() instanceof SourceCode) {
				attr.setDefaultValue(putCode((SourceCode) pd.getInitialValue(), attr.getDefaultValue()));
			}
			if (pd.isMany()) {
				attr.setLower(0);
				attr.setUpper(LiteralUnlimitedNatural.UNLIMITED);
				attr.setIsUnique(pd.isUnique());
				attr.setIsOrdered(pd.isOrdered());
			}
		}
	}

	public Classifier getStandardPrimitiveType(Classifier type) {
		if (type instanceof PrimitiveType && type.getGenerals().size() == 1 && type.getGenerals().get(0) instanceof PrimitiveType) {
			type = type.getGenerals().get(0);
			return getStandardPrimitiveType(type);
		} else {
			return type;
		}
	}

	protected Property findProperty(Classifier classifier, SourceProperty pd) {
		return findProperty(pd, classifier.getAttributes());
	}

	protected Property findProperty(SourceProperty pd, EList<Property> attributes) {
		Property found = null;
		for (Property property : attributes) {
			if (property.getName().equalsIgnoreCase(pd.getName())) {
				found = property;
				break;
			}
		}
		return found;
	}

	private EList<Operation> getOwnedOperations(Classifier cls) {
		EList<Operation> ownedOperations = null;
		if (cls instanceof Interface) {
			ownedOperations = ((Interface) cls).getOwnedOperations();
		} else if (cls instanceof DataType) {
			ownedOperations = ((DataType) cls).getOwnedOperations();
		} else if (cls instanceof Class) {
			ownedOperations = ((Class) cls).getOwnedOperations();
		} else {
			throw new IllegalArgumentException(cls.getQualifiedName() + " not supported");
		}
		return ownedOperations;
	}

	protected Property createAttribute(Classifier cls, SourceProperty pd) {
		Property attr = null;
		if (cls instanceof Interface) {
			attr = ((Interface) cls).getOwnedAttribute(pd.getName(), getClassifierFor(pd.getBaseType()), false, UMLPackage.eINSTANCE.getProperty(), true);
		} else if (cls instanceof org.eclipse.uml2.uml.Class) {
			attr = ((org.eclipse.uml2.uml.Class) cls).getOwnedAttribute(pd.getName(), getClassifierFor(pd.getBaseType()), false,
					UMLPackage.eINSTANCE.getProperty(), true);
		} else if (cls instanceof DataType) {
			attr = ((DataType) cls).getOwnedAttribute(pd.getName(), getClassifierFor(pd.getBaseType()), false, UMLPackage.eINSTANCE.getProperty(), true);
		}
		return attr;
	}

	protected Classifier getClassifierFor(SourceClass baseType) {
		return factory.getClassifierFor(baseType);
	}

}
