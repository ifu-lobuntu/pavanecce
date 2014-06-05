package org.pavanecce.uml.common.ocl;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage.Registry;
import org.eclipse.ocl.Environment;
import org.eclipse.ocl.uml.UMLEnvironment;
import org.eclipse.ocl.uml.UMLEnvironmentFactory;
import org.eclipse.ocl.uml.Variable;
import org.eclipse.uml2.uml.CallOperationAction;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Constraint;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.EnumerationLiteral;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.Parameter;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.SendSignalAction;
import org.eclipse.uml2.uml.State;
import org.pavanecce.uml.common.util.emulated.DefaultParentOclEnvironment;
import org.pavanecce.uml.common.util.emulated.OclRuntimeLibrary;

public final class ExtendedOclEnvironmentFactory extends UMLEnvironmentFactory {
	private Collection<Variable> variables = new HashSet<Variable>();
	private Element context;
	OclRuntimeLibrary library;

	public ExtendedOclEnvironmentFactory(Element context, Registry registry, OclRuntimeLibrary oclLibrary) {
		super(registry, oclLibrary.getResourceSet());
		this.context = context;
		this.library = oclLibrary;
	}

	public ExtendedOclEnvironmentFactory(Element context, OclRuntimeLibrary oclLibrary) {
		super(oclLibrary.getResourceSet());
		this.context = context;
		this.library = oclLibrary;
	}

	@Override
	public Environment<Package, Classifier, Operation, Property, EnumerationLiteral, Parameter, State, CallOperationAction, SendSignalAction, Constraint, Class, EObject> createEnvironment(
			Environment<Package, Classifier, Operation, Property, EnumerationLiteral, Parameter, State, CallOperationAction, SendSignalAction, Constraint, Class, EObject> parent) {
		if (!(parent instanceof UMLEnvironment)) {
			throw new IllegalArgumentException("Parent environment must be a UML environment: " + parent); //$NON-NLS-1$
		}
		ExtendedOclEnvironment result = new ExtendedOclEnvironment(context, (DefaultParentOclEnvironment) parent);
		Collection<Variable> variables2 = variables;
		// TODO fix this
		Map<String, Classifier> asdf = new HashMap<String, Classifier>();
		for (Variable variable : variables2) {
			asdf.put(variable.getName(), variable.getType());
		}
		result.addVariables(asdf);
		result.setFactory(this);
		return result;
	}

}