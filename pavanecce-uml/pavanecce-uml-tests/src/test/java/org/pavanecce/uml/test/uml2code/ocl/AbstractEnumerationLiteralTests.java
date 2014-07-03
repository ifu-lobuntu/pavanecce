package org.pavanecce.uml.test.uml2code.ocl;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.uml2.uml.OpaqueExpression;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Parameter;
import org.eclipse.uml2.uml.ParameterDirectionKind;
import org.eclipse.uml2.uml.Type;
import org.eclipse.uml2.uml.UMLPackage;
import org.junit.Test;

public abstract class AbstractEnumerationLiteralTests extends AbstractOclTest {
	protected static void addOcl() {
		Operation find = example.getConstructionCase().createOwnedOperation("doesHouseExist", new BasicEList<String>(), new BasicEList<Type>());
		Parameter result = find.createOwnedParameter("result", example.getType("Boolean"));
		result.setDirection(ParameterDirectionKind.RETURN_LITERAL);
		result.setUpper(1);
		OpaqueExpression ocl = (OpaqueExpression) find.createBodyCondition("body").createSpecification("spec", example.getRoomPlans(),
				UMLPackage.eINSTANCE.getOpaqueExpression());
		ocl.getLanguages().add("ocl");
		ocl.getBodies().add("house.status.exists");
		find = example.getConstructionCase().createOwnedOperation("isHouseFinished", new BasicEList<String>(), new BasicEList<Type>());
		result = find.createOwnedParameter("result", example.getType("Boolean"));
		result.setDirection(ParameterDirectionKind.RETURN_LITERAL);
		result.setUpper(1);
		ocl = (OpaqueExpression) find.createBodyCondition("body").createSpecification("spec", example.getRoomPlans(),
				UMLPackage.eINSTANCE.getOpaqueExpression());
		ocl.getLanguages().add("ocl");
		ocl.getBodies().add("house.status=HouseStatus::Finished");
	}

	@Test
	public void testIt() throws Exception {
		eval("var constructionCase=new ConstructionCase();");
		eval("var house=new House();");
		eval("constructionCase.setHouse(house);");
		eval("house.setStatus(HouseStatus.FINISHED)");
		assertEquals(true, eval("constructionCase.doesHouseExist();"));
		assertEquals(true, eval("constructionCase.isHouseFinished();"));
		eval("house.setStatus(HouseStatus.PLANNED)");
		assertEquals(false, eval("constructionCase.doesHouseExist();"));
		assertEquals(false, eval("constructionCase.isHouseFinished();"));
	}
}
