package org.pavanecce.uml.common.util.emulated;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.uml2.uml.Comment;
import org.eclipse.uml2.uml.Dependency;
import org.eclipse.uml2.uml.DirectedRelationship;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Namespace;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.Relationship;
import org.eclipse.uml2.uml.Stereotype;
import org.eclipse.uml2.uml.StringExpression;
import org.eclipse.uml2.uml.VisibilityKind;
import org.eclipse.uml2.uml.internal.impl.MultiplicityElementImpl;

@SuppressWarnings("restriction")
public class EmulatedMultiplicityElement extends MultiplicityElementImpl {
	private NamedElement originalElement;

	public EmulatedMultiplicityElement(NamedElement originalElement, int lower, int upper) {
		this.originalElement = originalElement;
		setLower(lower);
		setUpper(upper);
	}

	@Override
	public EList<EAnnotation> getEAnnotations() {
		return originalElement.getEAnnotations();
	}

	@Override
	public EList<Element> getOwnedElements() {
		return originalElement.getOwnedElements();
	}

	@Override
	public EAnnotation getEAnnotation(String source) {
		return originalElement.getEAnnotation(source);
	}

	public String getName() {
		return originalElement.getName();
	}

	@Override
	public Element getOwner() {
		return originalElement.getOwner();
	}

	@Override
	public EList<Comment> getOwnedComments() {
		return originalElement.getOwnedComments();
	}

	public boolean isSetName() {
		return originalElement.isSetName();
	}

	public VisibilityKind getVisibility() {
		return originalElement.getVisibility();
	}

	public boolean isSetVisibility() {
		return originalElement.isSetVisibility();
	}

	@Override
	public EList<EObject> getStereotypeApplications() {
		return originalElement.getStereotypeApplications();
	}

	public String getQualifiedName() {
		return originalElement.getQualifiedName();
	}

	@Override
	public EObject getStereotypeApplication(Stereotype stereotype) {
		return originalElement.getStereotypeApplication(stereotype);
	}

	@Override
	public EList<Stereotype> getRequiredStereotypes() {
		return originalElement.getRequiredStereotypes();
	}

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

	public Dependency getClientDependency(String name) {
		return originalElement.getClientDependency(name);
	}

	@Override
	public Stereotype getAppliedStereotype(String qualifiedName) {
		return originalElement.getAppliedStereotype(qualifiedName);
	}

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

	public Namespace getNamespace() {
		return originalElement.getNamespace();
	}

	@Override
	public boolean hasValue(Stereotype stereotype, String propertyName) {
		return originalElement.hasValue(stereotype, propertyName);
	}

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

	public String getLabel() {
		return originalElement.getLabel();
	}

	@Override
	public EList<String> getKeywords() {
		return originalElement.getKeywords();
	}

	public String getLabel(boolean localize) {
		return originalElement.getLabel(localize);
	}

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

}
