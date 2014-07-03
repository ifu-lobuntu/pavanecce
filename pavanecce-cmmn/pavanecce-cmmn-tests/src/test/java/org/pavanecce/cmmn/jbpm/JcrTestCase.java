package org.pavanecce.cmmn.jbpm;

import java.io.InputStreamReader;
import java.util.Arrays;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Repository;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.apache.jackrabbit.commons.cnd.CndImporter;
import org.apache.jackrabbit.core.TransientRepository;
import org.apache.jackrabbit.ocm.mapper.impl.annotation.AnnotationMapperImpl;
import org.pavanecce.cmmn.jbpm.ocm.OcmCaseFileItemSubscriptionInfo;
import org.pavanecce.cmmn.jbpm.ocm.OcmCaseSubscriptionInfo;
import org.pavanecce.cmmn.jbpm.ocm.OcmSubscriptionManager;
import org.pavanecce.cmmn.jbpm.test.AbstractCmmnCaseTestCase;
import org.pavanecce.common.ocm.ObjectContentManagerFactory;
import org.pavanecce.common.ocm.OcmObjectPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import test.cmmn.ConstructionCase;
import test.cmmn.House;
import test.cmmn.HousePlan;
import test.cmmn.RoofPlan;
import test.cmmn.Wall;
import test.cmmn.WallPlan;

public class JcrTestCase extends AbstractCmmnCaseTestCase {
	static Logger logger = LoggerFactory.getLogger(JcrTestCase.class);
	long time = System.currentTimeMillis();

	public void logDuration(String name) {
		logger.info(name + " took " + (System.currentTimeMillis() - time));
		time = System.currentTimeMillis();
	}

	public void testit() throws Exception {
		logDuration("1");
		Repository repository = new TransientRepository();
		logDuration("2");
		SimpleCredentials credentials = new SimpleCredentials("admin", "admin".toCharArray());
		Session session = repository.login(credentials);
		logDuration("3");
		CndImporter.registerNodeTypes(new InputStreamReader(JcrTestCase.class.getResourceAsStream("/build.cnd")), session);
		logDuration("4");
		session.getRootNode().addNode("cases");
		session.save();
		OcmSubscriptionManager eventListener = new OcmSubscriptionManager(null);
		ObjectContentManagerFactory factory = new ObjectContentManagerFactory(repository, "admin", "admin", getMapper(), eventListener);
		eventListener.setOcmFactory(factory);
		OcmObjectPersistence oop = new OcmObjectPersistence(factory);
		logDuration("5");
		ConstructionCase constructionCase = new ConstructionCase("/cases/case1");
		HousePlan housePlan = new HousePlan();
		constructionCase.setHousePlan(housePlan);
		constructionCase.setHouse(new House());
		Wall wall = new Wall();
		constructionCase.getHouse().getWalls().add(wall);
		housePlan.setRoofPlan(new RoofPlan());
		new WallPlan(housePlan);
		new WallPlan(housePlan);
		new WallPlan(housePlan);
		try {
			// Node root = session.getRootNode();
			// Node theCase = root.addNode("case1", "t:constructionCase");
			// theCase.setProperty("t:name", "MyName");
			// theCase.addNode("t:housePlans", "t:housePlan");
			// theCase.addNode("t:housePlans", "t:housePlan");
			// Store content
			oop.persist(constructionCase);
			oop.commit();
			logDuration("6");
			oop.start();
			ConstructionCase found = oop.find(ConstructionCase.class, constructionCase.getId());
			assertNotNull(found.getHousePlan());
			assertNotNull(found.getHousePlan().getRoofPlan());
			assertEquals(3, found.getHousePlan().getWallPlans().size());
			// String path=found.getHousePlan().getWallPlans().iterator().next().getPath();
			// Retrieve content
			// Node node = root.getNode("myBuildingCase/housePlan");
			// logger.info(node.getPath());
			// logger.info(node.getProperty("t:name").getString());

			// Remove content
			// root.getNode("myBuildingCase").remove();
			printTree("", oop.getObjectContentManager().getSession().getNode("/cases"));

			session.save();
		} finally {
			session.logout();
		}
	}

	public static void printTree(String padding, Node node) throws Exception {
		logger.info(padding + node.getPath());
		NodeIterator nodes = node.getNodes();
		while (nodes.hasNext()) {
			printTree(padding + "  ", nodes.nextNode());
		}
	}

	@SuppressWarnings("rawtypes")
	protected AnnotationMapperImpl getMapper() {
		return new AnnotationMapperImpl(Arrays.<Class> asList(ConstructionCase.class, HousePlan.class, RoofPlan.class, WallPlan.class, Wall.class, House.class,
				OcmCaseSubscriptionInfo.class, OcmCaseFileItemSubscriptionInfo.class));
	}

	@Override
	protected Class<?>[] getClasses() {
		return new Class[0];
	}

}