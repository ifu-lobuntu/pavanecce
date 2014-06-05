package org.pavanecce.uml.common.ocl;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.ocl.helper.OCLHelper;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Constraint;
import org.eclipse.uml2.uml.OpaqueExpression;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.UMLPackage;
import org.pavanecce.uml.common.util.EmfValueSpecificationUtil;

public class OpaqueExpressionContext extends AbstractOclContext {
	private OpaqueExpression opaqueExpression;

	public OpaqueExpressionContext(OpaqueExpression oe, OCLHelper<Classifier, Operation, Property, Constraint> helper) {
		super(oe, helper);
		this.opaqueExpression = oe;
		oe.eAdapters().add(this);
		getExpression();
	}

	@Override
	protected String retrieveBody() {
		return EmfValueSpecificationUtil.getOclBody(opaqueExpression);
	}

	@Override
	public void notifyChanged(Notification notification) {
		if (notification.getNotifier() instanceof OpaqueExpression) {
			switch (notification.getFeatureID(OpaqueExpression.class)) {
			case UMLPackage.OPAQUE_EXPRESSION__BODY:
				reset();
				break;
			default:
				break;
			}
		}
	}

}
