package org.pavanecce.cmmn.jbpm;

import java.io.IOException;

import javax.jcr.Node;
import javax.jcr.NodeIterator;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pavanecce.common.ocm.OcmObjectPersistence;

import test.ConstructionCase;
import test.House;
import test.HousePlan;
import test.RoofPlan;
import test.Wall;
import test.WallPlan;

public class OcmPersistentSubscriptionEventTest extends CaseFileItemEventTests {
	{
		super.isJpa = false;
	}
	@Before
	public void before() throws Exception{
		OcmObjectPersistence p = new OcmObjectPersistence(getOcmFactory());
		removeChildren(p, "/cases");
		removeChildren(p, "/subscriptions");
	}

	protected void removeChildren(OcmObjectPersistence p, String path) {
		try {
			Node node = p.getSession().getSession().getNode(path);
			NodeIterator nodes = node.getNodes();
			while (nodes.hasNext()) {
				Node object = nodes.nextNode();
				object.remove();
			}
		} catch (Exception e) {
		}
	}

	@BeforeClass
	public static void deleteJcrRepo() throws IOException {
		JcrTestCase.deleteTempRepo();
	}

	@Test
	public void testModel() throws Exception{
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
		//TODO figure out why this is necessary
		house.setRoofPlan(housePlan.getRoofPlan());
		p.update(house);
		p.commit();
		constructionCase=p.find(ConstructionCase.class, constructionCase.getId());
		assertEquals("MyConstructionCase", constructionCase.getName());
		assertEquals("MyHouse", constructionCase.getHouse().getDescription());
		assertNotNull(constructionCase.getHousePlan());
		assertNotNull(constructionCase.getHousePlan().getRoofPlan());
		assertNotNull(constructionCase.getHouse().getRoofPlan());
		assertSame(constructionCase.getHousePlan().getRoofPlan(),constructionCase.getHouse().getRoofPlan());
		assertEquals(3, constructionCase.getHouse().getWalls().size());
		assertEquals(2, constructionCase.getHousePlan().getWallPlans().size());
		
	}
}
