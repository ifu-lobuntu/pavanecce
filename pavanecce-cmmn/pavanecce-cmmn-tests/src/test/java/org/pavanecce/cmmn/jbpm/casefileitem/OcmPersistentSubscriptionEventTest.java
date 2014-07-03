package org.pavanecce.cmmn.jbpm.casefileitem;

import org.junit.Test;
import org.pavanecce.common.ocm.OcmObjectPersistence;

import test.cmmn.ConstructionCase;
import test.cmmn.House;
import test.cmmn.HousePlan;
import test.cmmn.RoofPlan;
import test.cmmn.Wall;
import test.cmmn.WallPlan;

public class OcmPersistentSubscriptionEventTest extends CaseFileItemEventTest {
	{
		super.isJpa = false;
	}

	@Test
	public void testModel() throws Exception {
		OcmObjectPersistence p = new OcmObjectPersistence(getOcmFactory());
		ConstructionCase constructionCase = new ConstructionCase("/cases/case1");
		constructionCase.setName("MyConstructionCase");
		this.housePlan = new HousePlan(constructionCase);
		this.house = new House(constructionCase);
		house.setDescription("MyHouse");
		new WallPlan(housePlan);
		new WallPlan(housePlan);
		new Wall(house);
		new Wall(house);
		new Wall(house);
		new RoofPlan(housePlan);
		p.persist(constructionCase);
		// TODO figure out why this is necessary
		house.setRoofPlan(housePlan.getRoofPlan());
		p.update(house);
		p.commit();
		constructionCase = p.find(ConstructionCase.class, constructionCase.getId());
		assertEquals("MyConstructionCase", constructionCase.getName());
		assertEquals("MyHouse", constructionCase.getHouse().getDescription());
		assertNotNull(constructionCase.getHousePlan());
		assertNotNull(constructionCase.getHousePlan().getRoofPlan());
		assertNotNull(constructionCase.getHouse().getRoofPlan());
		assertSame(constructionCase.getHousePlan().getRoofPlan(), constructionCase.getHouse().getRoofPlan());
		assertEquals(3, constructionCase.getHouse().getWalls().size());
		assertEquals(2, constructionCase.getHousePlan().getWallPlans().size());

	}
}
