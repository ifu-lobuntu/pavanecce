package org.pavanecce.uml.reverse.java;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.uml2.uml.AggregationKind;
import org.eclipse.uml2.uml.Association;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Interface;
import org.eclipse.uml2.uml.LiteralUnlimitedNatural;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.PrimitiveType;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Stereotype;
import org.eclipse.uml2.uml.UMLPackage;
import org.pavanecce.common.util.NameConverter;
import org.pavanecce.uml.common.util.EmfPropertyUtil;
import org.pavanecce.uml.common.util.PersistentNameUtil;
import org.pavanecce.uml.common.util.ProfileApplier;
import org.pavanecce.uml.common.util.UmlResourceSetFactory;
import org.pavanecce.uml.reverse.java.sourcemodel.SourceAnnotation;
import org.pavanecce.uml.reverse.java.sourcemodel.SourceClass;
import org.pavanecce.uml.reverse.java.sourcemodel.SourceProperty;

public class UmlGeneratorFromJpa extends AbstractUmlGenerator {
	@Override
	public Collection<Element> generateUmlImpl(Set<SourceClass> selection, Package library, IProgressMonitor m) throws Exception {
		m.beginTask("Importing JPA Entities", selection.size());
		Set<Element> result = new HashSet<Element>();
		ProfileApplier.applyProfile((Model) library, UmlResourceSetFactory.VDFP_MAPPING_PROFILE);
		for (SourceClass t : selection) {
			if (!m.isCanceled() && !t.isAnnotation() && t.getAnnotation("javax.persistence.Entity") != null) {
				// Only do annotations in profiles
				Classifier cls = getClassifierFor(t);
				result.add(cls);
				Stereotype as = cls.getApplicableStereotype("MappingProfile::Entity");
				if (as != null) {
					if (!cls.isStereotypeApplied(as)) {
						cls.applyStereotype(as);
					}
					String persistentName = null;
					SourceAnnotation annotation = t.getAnnotation("javax.persistence.Table");
					if (annotation != null && annotation.getAnnotationAttribute("name") instanceof String) {
						persistentName = (String) annotation.getAnnotationAttribute("name");
					} else {
						persistentName = PersistentNameUtil.getPersistentName(cls);
					}
					cls.setValue(as, "persistentName", persistentName);
					for (SourceProperty sp : t.getPropertyDescriptors().values()) {
						if (sp.getAnnotation("javax.persistence.Id") != null) {
							Property pk = createAttribute(cls, sp);
							cls.setValue(as, "primaryKey", pk);
						}
					}
				}

				Stereotyper.stereotype(cls, t.getAnnotations(), factory);
				populateAttributes(library, cls, t);
				populateOperations(library, cls, t);
			}
			m.worked(1);
		}
		m.done();
		for (Classifier c : factory.getNewClassifiers()) {
			result.add(c);
			result.addAll(c.getAssociations());
		}
		return result;
	}

	@Override
	protected boolean canReverse(Package library) {
		return library instanceof Model;
	}

	private Association createAssociation(Classifier cls, SourceProperty pd) {
		Classifier baseType = getClassifierFor(pd.getBaseType());
		Association assoc = null;
		String assocName = null;
		if (pd.getOtherEnd() == null) {
			assocName = cls.getName() + "References" + NameConverter.capitalize(pd.getName());
		} else if (pd.getOtherEnd().isComposite()) {
			assocName = baseType.getName() + "Has" + NameConverter.capitalize(pd.getOtherEnd().getName());
		} else if (pd.isComposite()) {
			assocName = cls.getName() + "Has" + NameConverter.capitalize(pd.getName());
		} else if (pd.isMany() && !pd.getOtherEnd().isMany()) {
			assocName = cls.getName() + "Has" + NameConverter.capitalize(pd.getName());
		} else if (pd.getOtherEnd().isMany() && !pd.isMany()) {
			assocName = baseType.getName() + "Has" + NameConverter.capitalize(pd.getOtherEnd().getName());
		} else {
			if (cls.getName().compareTo(baseType.getName()) > 0) {
				assocName = baseType.getName() + "Has" + NameConverter.capitalize(pd.getOtherEnd().getName());
			} else {
				assocName = cls.getName() + "Has" + NameConverter.capitalize(pd.getName());
			}
		}
		assoc = (Association) cls.getNearestPackage().getOwnedType(assocName, true, UMLPackage.eINSTANCE.getAssociation(), true);
		return assoc;
	}

	@Override
	protected Property findProperty(Classifier classifier, SourceProperty pd) {
		Property r = findProperty(pd, classifier.getAttributes());
		if (r == null) {
			outer: for (Association a : classifier.getAssociations()) {
				for (Property m : a.getMemberEnds()) {
					if (m.getOtherEnd() != null && m.getOtherEnd().getType().equals(classifier) && m.getName().equals(pd.getName())) {
						r = m;
						break outer;
					}
				}
			}
		}
		return r;
	}

	@Override
	protected Property createAttribute(Classifier cls, SourceProperty pd) {
		Classifier baseType = getClassifierFor(pd.getBaseType());
		if (baseType instanceof PrimitiveType || pd.getBaseType().getAnnotation("javax.persistence.Entity") == null) {
			Property attr = super.createAttribute(cls, pd);
			setLower(pd, attr);
			Stereotyper.stereotype(attr, pd.getAnnotations(), factory);
			Stereotype as = attr.getApplicableStereotype("MappingProfile::Attribute");
			if (as != null) {
				if (!attr.isStereotypeApplied(as)) {
					attr.applyStereotype(as);
				}
				String persistentName = null;
				SourceAnnotation annotation = pd.getAnnotation("javax.persistence.Column");
				if (annotation != null && annotation.getAnnotationAttribute("name") instanceof String) {
					persistentName = (String) annotation.getAnnotationAttribute("name");
				} else {
					persistentName = PersistentNameUtil.getPersistentName(attr);
				}
				attr.setValue(as, "persistentName", persistentName);
			}
			return attr;
		} else {
			Association assoc = createAssociation(cls, pd);
			Property otherEnd = createEnd(baseType, assoc, pd.getOtherEnd(), cls);
			Stereotyper.stereotype(otherEnd, pd.getAnnotations(), factory);
			Property thisEnd = createEnd(cls, assoc, pd, baseType);
			Stereotyper.stereotype(thisEnd, pd.getAnnotations(), factory);
			return thisEnd;
		}
	}

	private Property createEnd(Classifier owner, Association assoc, SourceProperty end, Classifier baseType) {
		Property umlEnd;
		if (end == null) {
			umlEnd = assoc.getOwnedEnd(NameConverter.decapitalize(baseType.getName()), baseType, true, UMLPackage.eINSTANCE.getProperty(), true);
		} else if (end.isComposite()) {
			umlEnd = super.createAttribute(owner, end);
			assoc.getMemberEnds().add(umlEnd);
			umlEnd.setAggregation(AggregationKind.COMPOSITE_LITERAL);
			umlEnd.setIsNavigable(true);
		} else {
			umlEnd = assoc.getNavigableOwnedEnd(end.getName(), baseType, true, UMLPackage.eINSTANCE.getProperty(), true);

		}
		if (end != null) {
			SourceAnnotation joinColumn = end.getAnnotation("javax.persistence.JoinColumn");
			if (joinColumn != null) {
				Stereotype s = umlEnd.getApplicableStereotype("MappingProfile::AssociationEnd");
				if (s != null) {
					if (!umlEnd.isStereotypeApplied(s)) {
						umlEnd.applyStereotype(s);
					}
					@SuppressWarnings("unchecked")
					EList<EObject> linkedProperties = (EList<EObject>) umlEnd.getValue(s, "linkedProperties");
					EClass lpType = (EClass) s.getDefinition().getEStructuralFeature("linkedProperties").getEType();
					EObject lp = EcoreUtil.create(lpType);
					lp.eSet(lpType.getEStructuralFeature("sourcePersistentName"), joinColumn.getAnnotationAttribute("name"));
					String referencedColumnName = (String) joinColumn.getAnnotationAttribute("referencedColumnName");
					if (referencedColumnName != null) {
						lp.eSet(lpType.getEStructuralFeature("targetProperty"),
								EmfPropertyUtil.findTargetPropertyByReferencedColumnName(umlEnd, referencedColumnName));
					}
					linkedProperties.add(lp);
				}
			}
		}
		if (end == null || end.isMany()) {
			umlEnd.setUnlimitedNaturalDefaultValue(LiteralUnlimitedNatural.UNLIMITED);
			umlEnd.setLower(0);
		} else {
			setLower(end, umlEnd);
		}
		return umlEnd;
	}

	public void setLower(SourceProperty end, Property otherEnd) {
		boolean required = isRequired(end);
		if (required) {
			otherEnd.setLower(1);
		} else {
			otherEnd.setLower(0);
		}
	}

	private boolean isRequired(SourceProperty end) {
		boolean required = false;
		SourceAnnotation manyToOne = getJpaAnnotation(end, "ManyToOne", "OneToOne", "Basic");
		if (manyToOne != null) {
			Object optional = manyToOne.getAnnotationAttribute("optional");
			if (optional != null && Boolean.FALSE.equals(optional)) {
				required = true;
			}
		}
		return required;
	}

	private SourceAnnotation getJpaAnnotation(SourceProperty pd, String... names) {
		SourceAnnotation result = null;
		for (String string : names) {
			result = pd.getAnnotation("javax.persistence." + string);
			if (result != null) {
				break;
			}
		}
		return result;
	}

	public static Property getPrimaryKey(Classifier owner) {
		for (Stereotype st : owner.getAppliedStereotypes()) {
			if (st.getMember("primaryKey") != null) {
				return (Property) owner.getValue(st, "primaryKey");
			}
		}
		return null;
	}

	@Override
	protected ClassifierFactory createClassifierFactory(Package library) {
		return new ClassifierFactory(library) {
			@Override
			public Classifier getClassifierFor(SourceClass baseType) {
				Classifier cls = super.getClassifierFor(baseType);
				if (cls instanceof Interface) {
					Stereotype inftSt = cls.getApplicableStereotype("MappingProfile::Interface");
					if (inftSt != null && !cls.isStereotypeApplied(inftSt)) {
						cls.applyStereotype(inftSt);
					}
				}
				if (cls instanceof Class) {
					Stereotype as = cls.getApplicableStereotype("MappingProfile::Entity");
					if (as != null) {
						if (!cls.isStereotypeApplied(as)) {
							cls.applyStereotype(as);
						}
						String persistentName = null;
						SourceAnnotation annotation = baseType.getAnnotation("javax.persistence.Table");
						if (annotation != null && annotation.getAnnotationAttribute("name") instanceof String) {
							persistentName = (String) annotation.getAnnotationAttribute("name");
						} else {
							persistentName = PersistentNameUtil.getPersistentName(cls);
						}
						cls.setValue(as, "persistentName", persistentName);
						for (SourceProperty sp : baseType.getPropertyDescriptors().values()) {
							if (sp.getAnnotation("javax.persistence.Id") != null) {
								Property pk = createAttribute(cls, sp);
								cls.setValue(as, "primaryKey", pk);
							}
						}
					}
				}
				if (baseType.getAnnotation("javax.persistence.Entity") == null && cls != null) {
					for (EObject sa : cls.getStereotypeApplications()) {
						EStructuralFeature tpf = sa.eClass().getEStructuralFeature("typeMappings");
						if (tpf != null) {
							@SuppressWarnings("unchecked")
							EList<EObject> typeMappings = (EList<EObject>) sa.eGet(tpf);
							EClass eType = (EClass) tpf.getEType();
							EObject typeMapping = null;
							for (EObject eObject : typeMappings) {
								if ("java".equals(eObject.eGet(eType.getEStructuralFeature("language")))) {
									typeMapping = eObject;
								}
							}
							if (typeMapping == null) {
								typeMappings.add(typeMapping = EcoreUtil.create(eType));
								typeMapping.eSet(eType.getEStructuralFeature("language"), "java");
							}
							typeMapping.eSet(eType.getEStructuralFeature("mapping"), baseType.getQualifiedName());
						}
					}
				}
				return cls;
			}
		};
	}
}
