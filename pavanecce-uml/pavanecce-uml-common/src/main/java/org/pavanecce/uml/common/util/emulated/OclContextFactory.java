package org.pavanecce.uml.common.util.emulated;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.ocl.TypeResolver;
import org.eclipse.ocl.expressions.CollectionKind;
import org.eclipse.ocl.uml.OCL;
import org.eclipse.ocl.uml.OCL.Helper;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.MultiplicityElement;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.OpaqueExpression;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.TypedElement;
import org.eclipse.uml2.uml.Variable;
import org.pavanecce.uml.common.ocl.AbstractOclContext;
import org.pavanecce.uml.common.ocl.ExtendedOclEnvironment;
import org.pavanecce.uml.common.ocl.FreeExpressionContext;
import org.pavanecce.uml.common.ocl.OpaqueExpressionContext;
import org.pavanecce.uml.common.util.EmfPropertyUtil;
import org.pavanecce.uml.common.util.EmfValueSpecificationUtil;

public class OclContextFactory {
	private Map<OpaqueExpression, OpaqueExpressionContext> opaqueExpressions = new HashMap<OpaqueExpression, OpaqueExpressionContext>();
	private Map<String, FreeExpressionContext> freeExpressions = new HashMap<String, FreeExpressionContext>();
	private DefaultParentOclEnvironment parentEnvironment;

	public OclContextFactory(ResourceSet rst) {
		super();
		this.parentEnvironment = new DefaultParentOclEnvironment(rst);
	}

	public FreeExpressionContext getFreeOclExpressionContext(Package next, Map<String, Classifier> vars, String valueSpec) {
		FreeExpressionContext result = freeExpressions.get(valueSpec);
		if (result == null) {
			OCL ocl = createOcl(next, vars);
			Helper helper = ocl.createOCLHelper();
			result = new FreeExpressionContext(valueSpec, helper);
			freeExpressions.put(valueSpec, result);
		}
		return result;
	}

	public OpaqueExpressionContext getOclExpressionContext(OpaqueExpression valueSpec) {
		OpaqueExpressionContext result = opaqueExpressions.get(valueSpec);
		if (result == null) {
			Element context = EmfValueSpecificationUtil.getContext(valueSpec);
			OCL ocl = createOcl(context, Collections.<String, Classifier> emptyMap());
			Helper helper = ocl.createOCLHelper();
			result = new OpaqueExpressionContext(valueSpec, helper);
			opaqueExpressions.put(valueSpec, result);
		}
		return result;
	}

	public void reset() {
		opaqueExpressions.clear();
	}

	public OCL createOcl(Element context, Map<String, Classifier> vars) {
		ExtendedOclEnvironment env = new ExtendedOclEnvironment(context, parentEnvironment);
		env.addVariables(vars);
		return OCL.newInstance(env);
	}

	public TypeResolver<Classifier, Operation, Property> getTypeResolver() {
		return parentEnvironment.getTypeResolver();
	}

	public Collection<AbstractOclContext> getOclContexts() {
		Collection<AbstractOclContext> result = new HashSet<AbstractOclContext>();
		result.addAll(this.opaqueExpressions.values());
		return result;
	}

	public OpaqueExpressionContext getArtificationExpression(NamedElement ne, String tagName) {
		for (EObject e : ne.getStereotypeApplications()) {
			EStructuralFeature f = e.eClass().getEStructuralFeature(tagName);
			if (f != null) {
				OpaqueExpression oe = (OpaqueExpression) e.eGet(f);
				if (oe != null) {
					return getOclExpressionContext(oe);
				}
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public Collection<OpaqueExpressionContext> getArtificialExpressions(NamedElement ne, String tagName) {
		Collection<OpaqueExpressionContext> result = new HashSet<OpaqueExpressionContext>();
		for (EObject e : ne.getStereotypeApplications()) {
			EStructuralFeature f = e.eClass().getEStructuralFeature(tagName);
			if (f != null) {
				Collection<OpaqueExpression> val = (Collection<OpaqueExpression>) e.eGet(f);
				for (OpaqueExpression oe : val) {
					result.add(getOclExpressionContext(oe));
				}
			}
		}
		return result;
	}

	public Classifier getActualType(MultiplicityElement me) {
		Classifier type = null;
		if (me instanceof TypedElement) {
			type = (Classifier) ((TypedElement) me).getType();
		} else if (me instanceof Variable) {
			type = (Classifier) ((Variable) me).getType();
		}
		if (type != null) {
			CollectionKind collectionKind = EmfPropertyUtil.getCollectionKind(me);
			if (collectionKind == null) {
				return type;
			} else {
				return (Classifier) parentEnvironment.getTypeResolver().resolveCollectionType(collectionKind, type);
			}
		}
		return type;
	}

	public OclRuntimeLibrary getLibrary() {
		return this.parentEnvironment.getLibrary();
	}
}