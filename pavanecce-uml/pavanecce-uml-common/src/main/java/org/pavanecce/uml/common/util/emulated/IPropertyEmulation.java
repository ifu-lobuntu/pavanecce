package org.pavanecce.uml.common.util.emulated;

import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.DataType;
import org.eclipse.uml2.uml.Namespace;
import org.eclipse.uml2.uml.OpaqueExpression;
import org.pavanecce.uml.common.ocl.OpaqueExpressionContext;

public interface IPropertyEmulation {
	DataType getDateTimeType();

	DataType getDurationType();

	IEmulatedPropertyHolder getEmulatedPropertyHolder(Classifier bc);

	Classifier getMessageStructure(Namespace operation);

	OpaqueExpressionContext getOclExpressionContext(OpaqueExpression valueSpec);

	public abstract DataType getCumulativeDurationType();

	Classifier getQuantityBasedCost();

	Classifier getDurationBasedCost();
}
