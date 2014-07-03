package org.pavanecce.uml.test.uml2code.jpa;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.script.ScriptContext;
import javax.script.ScriptException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;
import org.pavanecce.common.test.util.ConstructionCaseExample;

public abstract class AbstractPersistenceTest extends Assert {

	protected static ConstructionCaseExample example;

	public AbstractPersistenceTest() {
		super();
	}

	public void assertEquals(int i, Object val) {
		if (val instanceof Number) {
			assertEquals(i, ((Number) val).intValue());
		} else {
			super.assertEquals(i, val);
		}
	}

	@AfterClass
	public static void after() {
		example.after();
	}

	@Test
	public void testOneToOne() throws Exception {
		// GIVEN UML model built, CodeModel generated, JPA Code Generated,
		// Classes Compiler, EntityManagerFactory Created:
		// WHEN

		// THEN

		eval("ConstructionCase=Packages.test.ConstructionCase;");
		eval("House=Packages.test.House;");
		eval("HousePlan=Packages.test.HousePlan;");
		eval("RoofPlan=Packages.test.RoofPlan;");
		eval("Wall=Packages.test.Wall;");
		eval("var constructionCase=new ConstructionCase();");
		eval("var house=new House();");
		eval("var housePlan=new HousePlan();");
		eval("var roofPlan=new RoofPlan();");
		eval("constructionCase.setHouse(house);");
		eval("constructionCase.setHousePlan(housePlan);");
		eval("housePlan.setRoofPlan(roofPlan);");
		eval("p.start();");
		// Generate UUIDs
		eval("p.persist(constructionCase);");
		eval("house.setRoofPlan(roofPlan);");
		eval("p.update(constructionCase);");
		eval("p.commit();");
		// eval("p.close();");
		eval("p.start();");
		eval("constructionCase=p.find(ConstructionCase,constructionCase.getId());");
		eval("house=constructionCase.getHouse();");
		eval("housePlan=constructionCase.getHousePlan();");
		eval("roofPlan=housePlan.getRoofPlan();");
		assertNotNull(eval("house.getConstructionCase();"));
		assertNotNull(eval("house.getRoofPlan();"));
		assertNotNull(eval("housePlan.getRoofPlan();"));
		assertNotNull(eval("roofPlan.getHouse();"));
		assertNotNull(eval("roofPlan.getHousePlan();"));
		assertEquals(eval("constructionCase.getId();"), eval("house.getConstructionCase().getId();"));
		assertEquals(eval("housePlan.getRoofPlan().getId();"), eval("house.getRoofPlan().getId();"));
		eval("constructionCase=p.find(ConstructionCase,constructionCase.getId());");
		eval("housePlan.setRoofPlan(null);");
		eval("house.setRoofPlan(null);");
		// eval("p.update(constructionCase);");
		// sadly, OCM requires this
		eval("p.update(housePlan);");
		eval("p.update(house);");
		eval("p.commit();");
		// eval("p.close();");
		eval("p.start();");
		eval("constructionCase=p.find(ConstructionCase,constructionCase.getId());");
		assertNull(eval("constructionCase.getHousePlan().getRoofPlan();"));
		assertNull(eval("constructionCase.getHouse().getRoofPlan();"));
	}

	@After
	public void cleanup() {
		try {
			eval("p.commit();");
		} catch (Exception e) {
		}
		try {
			// eval("p.close();");
		} catch (Exception e) {
		}

	}

	@Test
	public void testSimpleTypes() throws Exception {
		// GIVEN UML model built, CodeModel generated, JPA Code Generated,
		// Classes Compiler, EntityManagerFactory Created:
		// WHEN

		// THEN
		SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dateString = "2013-12-31 23:13:56";
		Date date = parser.parse(dateString);
		example.getJavaScriptContext().setAttribute("date", date, ScriptContext.ENGINE_SCOPE);
		example.getJavaScriptContext().setAttribute("picture", new byte[] { 1, 2, 3, 4, 5, 6 }, ScriptContext.ENGINE_SCOPE);
		eval("ConstructionCase=Packages.test.ConstructionCase;");
		eval("HouseStatus=Packages.test.HouseStatus;");
		eval("House=Packages.test.House;");
		eval("Date=Packages.java.util.Date;");
		eval("var constructionCase = new ConstructionCase();");
		eval("var house = new House();");
		eval("constructionCase.setHouse(house);");
		eval("house.setStatus(HouseStatus.FINISHED)");
		eval("constructionCase.setStartDate(date);");
		eval("constructionCase.setName('Pietie');");
		eval("constructionCase.setActive(true);");
		eval("constructionCase.setNumberOfWalls(10);");
		eval("constructionCase.setPricePerSquareMetre(1240.45);");
		eval("constructionCase.setPicture(picture);");
		eval("p.start();");
		eval("p.persist(constructionCase);");
		eval("p.commit();");
		// eval("p.close();");
		eval("p.start();");
		eval("constructionCase=p.find(ConstructionCase,constructionCase.getId());");
		date = (Date) eval("constructionCase.getStartDate()");
		assertEquals(dateString, parser.format(date));
		assertEquals(Boolean.TRUE, eval("constructionCase.getActive()"));
		assertEquals(10, eval("constructionCase.getNumberOfWalls()"));
		assertEquals(1240.45, eval("constructionCase.getPricePerSquareMetre()"));
		assertEquals(1, eval("constructionCase.getPicture()[0]"));
		assertEquals(6, eval("constructionCase.getPicture()[5]"));
		assertEquals(eval("HouseStatus.FINISHED"), eval("constructionCase.getHouse().getStatus()"));

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
		eval("house.getWalls().add(wall);");
		eval("p.start();");
		eval("p.persist(constructionCase);");
		eval("p.commit();");
		// eval("p.close();");
		eval("p.start();");
		eval("house=p.find(House,house.getId());");
		assertEquals(1, eval("house.getWalls().size()"));
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
		eval("housePlan.getWallPlans().add(wallPlan1);");
		eval("housePlan.getWallPlans().add(wallPlan2);");
		eval("var roomPlan1=new RoomPlan();");
		eval("var roomPlan2=new RoomPlan();");
		eval("housePlan.getRoomPlans().add(roomPlan1);");
		eval("housePlan.getRoomPlans().add(roomPlan2);");
		eval("p.start();");
		// HACK this is a limitation in OCM that it cannot store references to
		// Objects that have not been persisted yet
		eval("p.persist(constructionCase);");
		eval("roomPlan1.getWallPlans().add(wallPlan1);");
		eval("roomPlan1.getWallPlans().add(wallPlan2);");
		assertEquals(1, eval("wallPlan1.getRoomPlans().size()"));
		eval("p.update(constructionCase);");
		eval("p.commit();");
		// eval("p.close();");
		eval("p.start();");
		eval("roomPlan1=p.find(RoomPlan,roomPlan1.getId());");
		assertEquals(2, eval("roomPlan1.getWallPlans().size()"));
		eval("wallPlan1=p.find(WallPlan,wallPlan1.getId());");
		assertEquals(1, eval("wallPlan1.getRoomPlans().size()"));
	}

	private Object eval(String string) throws ScriptException {
		return example.getJavaScriptEngine().eval(string);
	}

}