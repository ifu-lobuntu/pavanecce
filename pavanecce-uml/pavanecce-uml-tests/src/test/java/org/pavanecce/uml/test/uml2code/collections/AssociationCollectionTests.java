package org.pavanecce.uml.test.uml2code.collections;

import javax.script.ScriptException;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pavanecce.common.test.util.ConstructionCaseExample;
import org.pavanecce.uml.uml2code.AbstractCodeGenerator;
import org.pavanecce.uml.uml2code.codemodel.CodeModelBuilder;
import org.pavanecce.uml.uml2code.java.JavaCodeGenerator;
import org.pavanecce.uml.uml2code.jpa.AbstractJavaCodeDecorator;

public class AssociationCollectionTests extends Assert {
	static ConstructionCaseExample example = new ConstructionCaseExample("AssociationCollections") {
		@Override
		public void setup(CodeModelBuilder codeModelBuilder, AbstractCodeGenerator codeGenerator, AbstractJavaCodeDecorator... decorators) throws Exception {
			super.setup(codeModelBuilder, codeGenerator, decorators);
			initScriptingEngine();
		};
	};

	@AfterClass
	public static void after() {
		example.after();
	}

	@BeforeClass
	public static void setup() throws Exception {
		JavaCodeGenerator codeGenerator = new JavaCodeGenerator();
		example.setup(new CodeModelBuilder(true), codeGenerator);
	}

	@Test
	public void testManyToManySet() throws Exception {
		eval("WallPlan=Packages.test.WallPlan;");
		eval("RoomPlan=Packages.test.RoomPlan;");
		eval("var roomPlan=new RoomPlan();");
		eval("var wallPlan1=new WallPlan();");
		eval("var wallPlan2=new WallPlan();");
		eval("roomPlan.getWallPlans().add(wallPlan1);");
		eval("roomPlan.getWallPlans().add(wallPlan2);");
		assertEquals(2, eval("roomPlan.getWallPlans().size();"));
		assertEquals(1, eval("wallPlan1.getRoomPlans().size();"));
		assertEquals(1, eval("wallPlan1.getRoomPlans().size();"));
		eval("roomPlan.getWallPlans().remove(wallPlan1);");
		assertEquals(1, eval("roomPlan.getWallPlans().size();"));
		assertEquals(0, eval("wallPlan1.getRoomPlans().size();"));
	}

	@Test
	public void testOneToManySet() throws Exception {
		eval("WallPlan=Packages.test.WallPlan;");
		eval("HousePlan=Packages.test.HousePlan;");
		eval("var housePlan=new HousePlan();");
		eval("var wallPlan1=new WallPlan();");
		eval("var wallPlan2=new WallPlan();");
		eval("housePlan.getWallPlans().add(wallPlan1);");
		eval("housePlan.getWallPlans().add(wallPlan2);");
		assertEquals(2, eval("housePlan.getWallPlans().size();"));
		assertSame(eval("housePlan"), eval("wallPlan1.getHousePlan();"));
		assertSame(eval("housePlan"), eval("wallPlan2.getHousePlan();"));
		eval("housePlan.getWallPlans().remove(wallPlan1);");
		assertEquals(1, eval("housePlan.getWallPlans().size();"));
		assertNull(eval("wallPlan1.getHousePlan();"));
		// Test the other side
		eval("wallPlan1.setHousePlan(housePlan);");
		assertEquals(2, eval("housePlan.getWallPlans().size();"));
		eval("wallPlan1.setHousePlan(null);");
		assertEquals(1, eval("housePlan.getWallPlans().size();"));
	}

	private Object eval(String string) throws ScriptException {
		return example.getJavaScriptEngine().eval(string);
	}
}
