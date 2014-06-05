package org.pavanecce.uml.common.ocl;

import org.eclipse.ocl.helper.OCLHelper;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Constraint;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Property;

public class FreeExpressionContext extends AbstractOclContext {

	private String ocl;

	public FreeExpressionContext(String ocl, OCLHelper<Classifier, Operation, Property, Constraint> helper) {
		super(null, helper);
		this.ocl = ocl;
	}

	@Override
	protected String retrieveBody() {
		return ocl;
	}

}
