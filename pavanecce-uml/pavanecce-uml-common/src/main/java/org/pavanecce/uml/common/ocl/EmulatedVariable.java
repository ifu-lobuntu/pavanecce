package org.pavanecce.uml.common.ocl;

import org.eclipse.ocl.uml.impl.VariableImpl;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Parameter;

public class EmulatedVariable extends VariableImpl {
	private NamedElement originalElement;

	public EmulatedVariable(NamedElement originalElement) {
		super();
		if (originalElement instanceof Parameter) {
			setRepresentedParameter((Parameter) originalElement);
		}
		this.originalElement = originalElement;
	}

	public NamedElement getOriginalElement() {
		return originalElement;
	}
}
