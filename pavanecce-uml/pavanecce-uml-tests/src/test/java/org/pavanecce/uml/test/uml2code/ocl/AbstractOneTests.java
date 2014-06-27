package org.pavanecce.uml.test.uml2code.ocl;

import org.eclipse.uml2.uml.OpaqueExpression;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Parameter;
import org.eclipse.uml2.uml.ParameterDirectionKind;
import org.eclipse.uml2.uml.Type;
import org.eclipse.uml2.uml.UMLPackage;
import org.junit.Test;

public abstract class AbstractOneTests extends AbstractOclTest {
	protected static void addOcl() {
		Operation find = example.getConstructionCase().createOwnedOperation("existsOneRoomPlan", list("nameToFind"), list((Type) example.getType("String")));
		Parameter result = find.createOwnedParameter("result", example.getType("Boolean"));
		result.setDirection(ParameterDirectionKind.RETURN_LITERAL);
		result.setUpper(1);
		OpaqueExpression ocl = (OpaqueExpression) find.createBodyCondition("body").createSpecification("spec", example.getRoomPlans(),
				UMLPackage.eINSTANCE.getOpaqueExpression());
		ocl.getLanguages().add("ocl");
		ocl.getBodies().add("housePlan.roomPlans->one(rp|rp.name=nameToFind)");
	}

	@Test
	public void testIt() throws Exception {
		eval("var constructionCase=new ConstructionCase();");
		eval("var housePlan=new HousePlan();");
		eval("constructionCase.setHousePlan(housePlan);");
		eval("var roomPlan1=new RoomPlan();");
		eval("roomPlan1.setName('roomPlan1');");
		eval("var roomPlan2=new RoomPlan();");
		eval("roomPlan2.setName('roomPlan2');");
		eval("var roomPlan3=new RoomPlan();");
		eval("roomPlan3.setName('roomPlan2');");
		eval("housePlan.getRoomPlans().add(roomPlan1);");
		eval("housePlan.getRoomPlans().add(roomPlan2);");
		eval("housePlan.getRoomPlans().add(roomPlan3);");
		assertEquals(true, eval("constructionCase.existsOneRoomPlan('roomPlan1');"));
		assertEquals(false, eval("constructionCase.existsOneRoomPlan('roomPlan2');"));
	}
}
