package org.pavanecce.uml.common.util.emulated;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Comment;
import org.eclipse.uml2.uml.Dependency;
import org.eclipse.uml2.uml.DirectedRelationship;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.LiteralUnlimitedNatural;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Namespace;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.Relationship;
import org.eclipse.uml2.uml.Stereotype;
import org.eclipse.uml2.uml.StringExpression;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.ValueSpecification;
import org.eclipse.uml2.uml.VisibilityKind;
import org.eclipse.uml2.uml.internal.impl.PropertyImpl;

@SuppressWarnings("restriction")
public abstract class AbstractEmulatedProperty extends PropertyImpl implements Adapter, IEmulatedElement {
	protected NamedElement owner;
	protected NamedElement originalElement;
	private Notifier target;

	public AbstractEmulatedProperty(Classifier owner, NamedElement originalElement) {
		super();
		this.owner = owner;
		this.originalElement = originalElement;
	}

	@Override
	public abstract int getUpper();

	@Override
	public boolean isNavigable() {
		return true;
	}

	@Override
	public Resource eResource() {
		return originalElement.eResource();
	}

	@Override
	public EList<EAnnotation> getEAnnotations() {
		return originalElement.getEAnnotations();
	}

	@Override
	public EList<Adapter> eAdapters() {
		return originalElement.eAdapters();
	}

	@Override
	public boolean eDeliver() {
		return originalElement.eDeliver();
	}

	@Override
	public EClass eClass() {
		return originalElement.eClass();
	}

	@Override
	public EObject eContainer() {
		return originalElement.eContainer();
	}

	@Override
	public EStructuralFeature eContainingFeature() {
		return originalElement.eContainingFeature();
	}

	@Override
	public EReference eContainmentFeature() {
		return originalElement.eContainmentFeature();
	}

	@Override
	public EList<EObject> eContents() {
		return originalElement.eContents();
	}

	@Override
	public TreeIterator<EObject> eAllContents() {
		return originalElement.eAllContents();
	}

	@Override
	public boolean eIsProxy() {
		return originalElement.eIsProxy();
	}

	@Override
	public EList<EObject> eCrossReferences() {
		return originalElement.eCrossReferences();
	}

	@Override
	public Object eGet(EStructuralFeature feature) {
		return originalElement.eGet(feature);
	}

	@Override
	public Object eGet(EStructuralFeature feature, boolean resolve) {
		return originalElement.eGet(feature, resolve);
	}

	@Override
	public boolean eIsSet(EStructuralFeature feature) {
		return originalElement.eIsSet(feature);
	}

	@Override
	public Object eInvoke(EOperation operation, EList<?> arguments) throws InvocationTargetException {
		return originalElement.eInvoke(operation, arguments);
	}

	@Override
	public EList<Element> getOwnedElements() {
		return originalElement.getOwnedElements();
	}

	@Override
	public EAnnotation getEAnnotation(String source) {
		return originalElement.getEAnnotation(source);
	}

	@Override
	public EList<Comment> getOwnedComments() {
		return originalElement.getOwnedComments();
	}

	@Override
	public boolean isSetName() {
		return originalElement.isSetName();
	}

	@Override
	public boolean isSetVisibility() {
		return originalElement.isSetVisibility();
	}

	@Override
	public EList<EObject> getStereotypeApplications() {
		return originalElement.getStereotypeApplications();
	}

	@Override
	public String getQualifiedName() {
		return originalElement.getQualifiedName();
	}

	@Override
	public boolean isMultivalued() {
		return getUpper() == LiteralUnlimitedNatural.UNLIMITED || getUpper() > 1;
	}

	@Override
	public ValueSpecification getUpperValue() {
		ValueSpecification result = super.getUpperValue();
		if (!(result instanceof LiteralUnlimitedNatural)) {
			setUpperValue(result = createUpperValue("", null, UMLPackage.eINSTANCE.getLiteralUnlimitedNatural()));
		}
		((LiteralUnlimitedNatural) result).setValue(getUpper());
		return result;
	}

	@Override
	public EObject getStereotypeApplication(Stereotype stereotype) {
		return originalElement.getStereotypeApplication(stereotype);
	}

	@Override
	public EList<Stereotype> getRequiredStereotypes() {
		return originalElement.getRequiredStereotypes();
	}

	@Override
	public EList<Dependency> getClientDependencies() {
		return originalElement.getClientDependencies();
	}

	@Override
	public Stereotype getRequiredStereotype(String qualifiedName) {
		return originalElement.getRequiredStereotype(qualifiedName);
	}

	@Override
	public EList<Stereotype> getAppliedStereotypes() {
		return originalElement.getAppliedStereotypes();
	}

	@Override
	public Dependency getClientDependency(String name) {
		return originalElement.getClientDependency(name);
	}

	@Override
	public Stereotype getAppliedStereotype(String qualifiedName) {
		return originalElement.getAppliedStereotype(qualifiedName);
	}

	@Override
	public Dependency getClientDependency(String name, boolean ignoreCase, EClass eClass) {
		return originalElement.getClientDependency(name, ignoreCase, eClass);
	}

	@Override
	public EList<Stereotype> getAppliedSubstereotypes(Stereotype stereotype) {
		return originalElement.getAppliedSubstereotypes(stereotype);
	}

	@Override
	public Stereotype getAppliedSubstereotype(Stereotype stereotype, String qualifiedName) {
		return originalElement.getAppliedSubstereotype(stereotype, qualifiedName);
	}

	@Override
	public Namespace getNamespace() {
		return originalElement.getNamespace();
	}

	@Override
	public boolean hasValue(Stereotype stereotype, String propertyName) {
		return originalElement.hasValue(stereotype, propertyName);
	}

	@Override
	public StringExpression getNameExpression() {
		return originalElement.getNameExpression();
	}

	@Override
	public Object getValue(Stereotype stereotype, String propertyName) {
		return originalElement.getValue(stereotype, propertyName);
	}

	@Override
	public EList<Relationship> getRelationships() {
		return originalElement.getRelationships();
	}

	@Override
	public EList<Relationship> getRelationships(EClass eClass) {
		return originalElement.getRelationships(eClass);
	}

	@Override
	public EList<DirectedRelationship> getSourceDirectedRelationships() {
		return originalElement.getSourceDirectedRelationships();
	}

	@Override
	public EList<DirectedRelationship> getSourceDirectedRelationships(EClass eClass) {
		return originalElement.getSourceDirectedRelationships(eClass);
	}

	@Override
	public EList<DirectedRelationship> getTargetDirectedRelationships() {
		return originalElement.getTargetDirectedRelationships();
	}

	@Override
	public EList<DirectedRelationship> getTargetDirectedRelationships(EClass eClass) {
		return originalElement.getTargetDirectedRelationships(eClass);
	}

	@Override
	public String getLabel() {
		return originalElement.getLabel();
	}

	@Override
	public EList<String> getKeywords() {
		return originalElement.getKeywords();
	}

	@Override
	public String getLabel(boolean localize) {
		return originalElement.getLabel(localize);
	}

	@Override
	public EList<Namespace> allNamespaces() {
		return originalElement.allNamespaces();
	}

	@Override
	public Package getNearestPackage() {
		return originalElement.getNearestPackage();
	}

	@Override
	public Model getModel() {
		return originalElement.getModel();
	}

	@Override
	public boolean isDistinguishableFrom(NamedElement n, Namespace ns) {
		return originalElement.isDistinguishableFrom(n, ns);
	}

	@Override
	public boolean isStereotypeApplicable(Stereotype stereotype) {
		return originalElement.isStereotypeApplicable(stereotype);
	}

	@Override
	public boolean isStereotypeRequired(Stereotype stereotype) {
		return originalElement.isStereotypeRequired(stereotype);
	}

	@Override
	public String separator() {
		return originalElement.separator();
	}

	@Override
	public EList<Package> allOwningPackages() {
		return originalElement.allOwningPackages();
	}

	@Override
	public boolean isStereotypeApplied(Stereotype stereotype) {
		return originalElement.isStereotypeApplied(stereotype);
	}

	@Override
	public EList<Stereotype> getApplicableStereotypes() {
		return originalElement.getApplicableStereotypes();
	}

	@Override
	public Stereotype getApplicableStereotype(String qualifiedName) {
		return originalElement.getApplicableStereotype(qualifiedName);
	}

	@Override
	public boolean hasKeyword(String keyword) {
		return originalElement.hasKeyword(keyword);
	}

	@Override
	public EList<Element> allOwnedElements() {
		return originalElement.allOwnedElements();
	}

	@Override
	public boolean mustBeOwned() {
		return originalElement.mustBeOwned();
	}

	@Override
	public NamedElement getOwner() {
		return owner;
	}

	@Override
	public void notifyChanged(Notification notification) {

	}

	@Override
	public String getName() {
		return originalElement.getName();
	}

	@Override
	public VisibilityKind getVisibility() {
		return originalElement.getVisibility();
	}

	@Override
	public Notifier getTarget() {
		return target;
	}

	@Override
	public void setTarget(Notifier newTarget) {
		this.target = newTarget;
	}

	@Override
	public boolean isAdapterForType(Object type) {
		return false;
	}

	@Override
	public NamedElement getOriginalElement() {
		return originalElement;
	}
}
