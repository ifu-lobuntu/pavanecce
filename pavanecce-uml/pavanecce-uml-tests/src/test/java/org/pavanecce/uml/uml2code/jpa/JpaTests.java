package org.pavanecce.uml.uml2code.jpa;

import java.util.HashMap;

import javax.persistence.EntityManagerFactory;
import javax.script.ScriptContext;
import javax.script.ScriptException;

import org.hibernate.ejb.HibernatePersistence;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pavanecce.common.jpa.JpaObjectPersistence;
import org.pavanecce.common.util.ConstructionCaseExample;
import org.pavanecce.uml.uml2code.java.JavaCodeGenerator;

public class JpaTests extends Assert {
	static ConstructionCaseExample example = new ConstructionCaseExample("SimplePersistence") {
		public void setup(JavaCodeGenerator codeGenerator, AbstractJavaCodeDecorator... decorators) throws Exception {
			super.setup(codeGenerator, decorators);
			TestPersistenceUnitInfo pui = new TestPersistenceUnitInfo("construction", classLoader);
			super.addMappedClasses(pui, adaptor.getCodeModel());
			HibernatePersistence hibernatePersistence = new HibernatePersistence();
			EntityManagerFactory emf = hibernatePersistence.createContainerEntityManagerFactory(pui, new HashMap<String, String>());
			initScriptingEngine();
			javaScriptContext.setAttribute("p", new JpaObjectPersistence(emf), ScriptContext.ENGINE_SCOPE);
		};
	};

	@AfterClass
	public static void after() {
		example.after();
	}

	@BeforeClass
	public static void setup() throws Exception {
		example.setup(new JavaCodeGenerator(), new JpaCodeDecorator());
	}

	@Test
	public void testOneToOne() throws Exception {
		// GIVEN UML model built, CodeModel generated, JPA Code Generated,
		// Classes Compiler, EntityManagerFactory Created:
		// WHEN

		// THEN

		eval("ConstructionCase=Packages.test.ConstructionCase;");
		eval("House=Packages.test.House;");
		eval("Wall=Packages.test.Wall;");
		eval("var constructionCase=new ConstructionCase();");
		eval("var house=new House();");
		eval("constructionCase.setHouse(house);");
		eval("p.start();");
		eval("p.persist(constructionCase);");
		eval("p.commit();");
		eval("p.close();");
		eval("p.start();");
		eval("house=p.find(House,house.getId());");
		assertNotNull(eval("house.getConstructionCase();"));
		assertEquals(eval("constructionCase.getId();"), example.getJavaScriptEngine().eval("house.getConstructionCase().getId();"));
		eval("constructionCase=p.find(ConstructionCase,constructionCase.getId());");
		eval("constructionCase.setHouse(null);");
		eval("p.commit();");
		eval("p.close();");
		eval("p.start();");
		eval("constructionCase=p.find(ConstructionCase,constructionCase.getId());");
		assertNull(eval("constructionCase.getHouse();"));
	}

	@Test
	public void testOneToManyChildren() throws Exception {
		// GIVEN UML model built, CodeModel generated, JPA Code Generated,
		// Classes Compiler, EntityManagerFactory Created:
		// WHEN

		// THEN

		eval("ConstructionCase=Packages.test.ConstructionCase;");
		eval("House=Packages.test.House;");
		eval("Wall=Packages.test.Wall;");
		eval("var constructionCase=new ConstructionCase();");
		eval("var house=new House();");
		eval("constructionCase.setHouse(house);");
		eval("var wall=new Wall();");
		eval("wall.setHouse(house);");
		eval("house.getWall().add(wall);");
		eval("p.start();");
		eval("p.persist(constructionCase);");
		eval("p.commit();");
		eval("p.close();");
		eval("p.start();");
		eval("house=p.find(House,house.getId());");
		assertEquals(1, eval("house.getWall().size()"));
	}

	@Test
	public void testManyToMany() throws Exception {
		// GIVEN UML model built, CodeModel generated, JPA Code Generated,
		// Classes Compiler, EntityManagerFactory Created:
		// WHEN

		// THEN

		eval("ConstructionCase=Packages.test.ConstructionCase;");
		eval("HousePlan=Packages.test.HousePlan;");
		eval("WallPlan=Packages.test.WallPlan;");
		eval("RoomPlan=Packages.test.RoomPlan;");
		eval("var constructionCase=new ConstructionCase();");
		eval("var housePlan=new HousePlan();");
		eval("constructionCase.setHousePlan(housePlan);");
		eval("var wallPlan1=new WallPlan();");
		eval("var wallPlan2=new WallPlan();");
		eval("housePlan.getWallPlan().add(wallPlan1);");
		eval("housePlan.getWallPlan().add(wallPlan2);");
		eval("var roomPlan1=new RoomPlan();");
		eval("var roomPlan2=new RoomPlan();");
		eval("housePlan.getRoomPlan().add(roomPlan1);");
		eval("housePlan.getRoomPlan().add(roomPlan2);");
		eval("roomPlan1.getWallPlan().add(wallPlan1);");
		eval("roomPlan1.getWallPlan().add(wallPlan2);");
		eval("p.start();");
		eval("p.persist(constructionCase);");
		eval("p.commit();");
		eval("p.close();");
		eval("p.start();");
		eval("roomPlan1=p.find(RoomPlan,roomPlan1.getId());");
		assertEquals(2, eval("roomPlan1.getWallPlan().size()"));
		eval("wallPlan1=p.find(WallPlan,wallPlan1.getId());");
		assertEquals(1, eval("wallPlan1.getRoomPlan().size()"));
	}

	private Object eval(String string) throws ScriptException {
		return example.getJavaScriptEngine().eval(string);
	}

}
