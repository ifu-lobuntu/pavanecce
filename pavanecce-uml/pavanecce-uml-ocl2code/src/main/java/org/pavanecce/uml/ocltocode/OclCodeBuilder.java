package org.pavanecce.uml.ocltocode;

import java.util.Collections;
import java.util.SortedSet;

import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.OpaqueExpression;
import org.eclipse.uml2.uml.Property;
import org.pavanecce.common.code.metamodel.CodeClass;
import org.pavanecce.common.code.metamodel.CodeConstructor;
import org.pavanecce.common.code.metamodel.CodeExpression;
import org.pavanecce.common.code.metamodel.CodeMethod;
import org.pavanecce.common.code.metamodel.CodeModel;
import org.pavanecce.common.code.metamodel.CodePackage;
import org.pavanecce.common.code.metamodel.CodeParameter;
import org.pavanecce.common.code.metamodel.statements.AssignmentStatement;
import org.pavanecce.uml.common.ocl.OpaqueExpressionContext;
import org.pavanecce.uml.common.util.emulated.OclContextFactory;
import org.pavanecce.uml.ocltocode.common.UmlToCodeMaps;
import org.pavanecce.uml.ocltocode.creators.ExpressionCreator;
import org.pavanecce.uml.ocltocode.maps.PropertyMap;
import org.pavanecce.uml.uml2code.codemodel.DefaultCodeModelBuilder;

public class OclCodeBuilder extends DefaultCodeModelBuilder {
	private OclContextFactory oclContextFactory;
	private UmlToCodeMaps codeMaps;

	@Override
	public void initialize(SortedSet<Model> models, CodePackage codeModel) {
		super.initialize(models, codeModel);
		this.oclContextFactory = new OclContextFactory(models.iterator().next().eResource().getResourceSet());
		this.codeMaps = new UmlToCodeMaps(false);

	}

	@Override
	public void visitProperty(Property property, CodeClass codeClass) {
		if (property.getDefaultValue() instanceof OpaqueExpression) {
			PropertyMap map = codeMaps.buildStructuralFeatureMap(property);
			OpaqueExpressionContext ctx = oclContextFactory.getOclExpressionContext((OpaqueExpression) property.getDefaultValue());
			ExpressionCreator ec = new ExpressionCreator(codeMaps, codeClass);
			CodeExpression codeExpression = ec.makeExpression(ctx, property.isStatic(), Collections.<CodeParameter> emptyList());
			if (property.isDerived()) {
				CodeMethod getter = codeClass.getMethod(map.getter(), Collections.<CodeParameter> emptyList());
				getter.setResultInitialValue(codeExpression);
			} else {
				CodeConstructor constr= codeClass.findOrCreateConstructor(Collections.<CodeParameter> emptyList());
				new AssignmentStatement(constr.getBody(), map.fieldname(), codeExpression);
			}
		}
	}
}
