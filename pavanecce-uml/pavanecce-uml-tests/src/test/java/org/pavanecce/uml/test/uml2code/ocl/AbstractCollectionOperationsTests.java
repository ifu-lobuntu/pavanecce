package org.pavanecce.uml.test.uml2code.ocl;

import org.eclipse.uml2.uml.OpaqueExpression;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Parameter;
import org.eclipse.uml2.uml.ParameterDirectionKind;
import org.eclipse.uml2.uml.Type;
import org.eclipse.uml2.uml.UMLPackage;
import org.junit.Test;

public abstract class AbstractCollectionOperationsTests extends AbstractOclTest {
	protected static void addOcl() {
		buildBooleanOperation("excludesAll", "housePlan.wallPlans->excludesAll(house.wallPlans)");
		buildBooleanOperation("excludes", "housePlan.wallPlans->excludes(house.wallPlans->any(true))");
		buildBooleanOperation("includesAll", "housePlan.wallPlans->includesAll(house.wallPlans)");
		buildBooleanOperation("includes", "housePlan.wallPlans->includes(house.wallPlans->any(true))");
	}

	protected static void buildBooleanOperation(String name, String oclString) {
		Operation find = example.getConstructionCase().createOwnedOperation(name, emptyList(String.class), emptyList(Type.class));
		Parameter result = find.createOwnedParameter("result", example.getType("Boolean"));
		result.setDirection(ParameterDirectionKind.RETURN_LITERAL);
		result.setUpper(1);
		OpaqueExpression ocl = (OpaqueExpression) find.createBodyCondition("body").createSpecification("spec", example.getRoomPlans(),
				UMLPackage.eINSTANCE.getOpaqueExpression());
		ocl.getLanguages().add("ocl");
		ocl.getBodies().add(oclString);
	}

	@Test
	public void testIt() throws Exception {
		eval("var constructionCase=new ConstructionCase();");
		eval("var housePlan=new HousePlan();");
		eval("var house=new House();");
		eval("constructionCase.setHousePlan(housePlan);");
		eval("constructionCase.setHouse(house);");
		eval("var wallPlan1=new WallPlan();");
		eval("var wallPlan2=new WallPlan();");
		eval("housePlan.getWallPlans().add(wallPlan1);");
		eval("housePlan.getWallPlans().add(wallPlan2);");
		eval("house.getWallPlans().add(wallPlan1);");
		assertEquals(Boolean.FALSE, eval("constructionCase.excludesAll()"));
		assertEquals(Boolean.FALSE, eval("constructionCase.excludes()"));
		assertEquals(Boolean.TRUE, eval("constructionCase.includesAll()"));
		assertEquals(Boolean.TRUE, eval("constructionCase.includes()"));
	}
}
