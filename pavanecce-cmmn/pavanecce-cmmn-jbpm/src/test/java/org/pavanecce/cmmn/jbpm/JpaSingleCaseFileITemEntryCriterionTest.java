package org.pavanecce.cmmn.jbpm;

import org.junit.Test;
import org.pavanecce.cmmn.jbpm.jpa.JpaObjectPersistence;
import org.pavanecce.cmmn.jbpm.test.domain.ConstructionCase;
import org.pavanecce.cmmn.jbpm.test.domain.House;
import org.pavanecce.cmmn.jbpm.test.domain.HousePlan;
import org.pavanecce.cmmn.jbpm.test.domain.RoofPlan;
import org.pavanecce.cmmn.jbpm.test.domain.Wall;
import org.pavanecce.cmmn.jbpm.test.domain.WallPlan;

public class JpaSingleCaseFileITemEntryCriterionTest extends SingleCaseFileItemEntryCriterionTests {
	{
		super.isJpa = true;
	}
	@Test
	public void testModel() throws Exception {
		JpaObjectPersistence p = new JpaObjectPersistence(getEmf());
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
		house.setRoofPlan(housePlan.getRoofPlan());
		p.persist(constructionCase);
		p.commit();
		constructionCase = p.find(ConstructionCase.class, constructionCase.getUuid());
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
