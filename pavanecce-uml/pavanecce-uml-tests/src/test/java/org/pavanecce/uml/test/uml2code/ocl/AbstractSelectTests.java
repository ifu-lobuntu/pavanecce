package org.pavanecce.uml.test.uml2code.ocl;

import org.eclipse.uml2.uml.OpaqueExpression;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Parameter;
import org.eclipse.uml2.uml.ParameterDirectionKind;
import org.eclipse.uml2.uml.Type;
import org.eclipse.uml2.uml.UMLPackage;
import org.junit.Test;

public abstract class AbstractSelectTests extends AbstractOclTest {
	protected static void addOcl() {
		Operation find = example.getConstructionCase().createOwnedOperation("findRoomPlan", list("nameToFind"), list((Type) example.getType("String")));
		Parameter result = find.createOwnedParameter("result", example.getRoomPlans());
		result.setDirection(ParameterDirectionKind.RETURN_LITERAL);
		result.setUpper(-1);
		OpaqueExpression ocl = (OpaqueExpression) find.createBodyCondition("body").createSpecification("spec", example.getRoomPlans(),
				UMLPackage.eINSTANCE.getOpaqueExpression());
		ocl.getLanguages().add("ocl");
		ocl.getBodies().add("housePlan.roomPlans->select(name=nameToFind)");
	}

	@Test
	public void testIt() throws Exception {
		eval("var constructionCase=new ConstructionCase();");
		eval("var housePlan=new HousePlan();");
		eval("constructionCase.setHousePlan(housePlan);");
		eval("var wallPlan1=new WallPlan();");
		eval("var wallPlan2=new WallPlan();");
		eval("housePlan.getWallPlans().add(wallPlan1);");
		eval("housePlan.getWallPlans().add(wallPlan2);");
		eval("var roomPlan1=new RoomPlan();");
		eval("roomPlan1.setName('roomPlan1');");
		eval("var roomPlan2=new RoomPlan();");
		eval("roomPlan2.setName('roomPlan2');");
		eval("housePlan.getRoomPlans().add(roomPlan1);");
		eval("housePlan.getRoomPlans().add(roomPlan2);");
		eval("var found=constructionCase.findRoomPlan('roomPlan1');");
		assertEquals(1, eval("found.size();"));
	}
}
