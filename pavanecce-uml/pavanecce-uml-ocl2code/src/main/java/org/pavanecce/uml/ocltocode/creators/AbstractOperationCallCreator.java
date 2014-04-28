package org.pavanecce.uml.ocltocode.creators;

import org.eclipse.uml2.uml.Operation;
import org.pavanecce.common.code.metamodel.CodeClass;
import org.pavanecce.common.code.metamodel.CodeExpression;
import org.pavanecce.common.code.metamodel.OclStandardLibrary;
import org.pavanecce.common.code.metamodel.expressions.IsNullExpression;
import org.pavanecce.common.code.metamodel.expressions.MethodCallExpression;

public class AbstractOperationCallCreator {

	protected CodeClass myClass = null;

	public AbstractOperationCallCreator(CodeClass myClass) {
		super();
		this.myClass=myClass;
	}

	protected CodeExpression commonOperations(CodeExpression source, Operation referedOp) {
		if (referedOp.getName().equals("asBag")) {
			myClass.addStdLibToImports(OclStandardLibrary.COLLECTIONS);
			return new MethodCallExpression(OclStandardLibrary.COLLECTIONS.getPhysicalName() + ".objectAsBag", source);
		} else if (referedOp.getName().equals("asSequence")) {
			myClass.addStdLibToImports(OclStandardLibrary.COLLECTIONS);
			return new MethodCallExpression(OclStandardLibrary.COLLECTIONS.getPhysicalName() + ".objectAsSequence", source);
		} else if (referedOp.getName().equals("asOrderedSet")) {
			myClass.addStdLibToImports(OclStandardLibrary.COLLECTIONS);
			return new MethodCallExpression(OclStandardLibrary.COLLECTIONS.getPhysicalName() + ".objectAsOrderedSet", source);
		} else if (referedOp.getName().equals("asSet")) {
			myClass.addStdLibToImports(OclStandardLibrary.COLLECTIONS);
			return new MethodCallExpression(OclStandardLibrary.COLLECTIONS.getPhysicalName() + ".objectAsSet", source);
		} else if (referedOp.getName().equals("oclIsUndefined") || referedOp.getName().equals("oclIsInvalid")) {
			return new IsNullExpression(source);
		} else {
			return null;
		}
	}

}