package org.pavanecce.cmmn;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Repository;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.apache.jackrabbit.commons.cnd.CndImporter;
import org.apache.jackrabbit.core.TransientRepository;
import org.apache.jackrabbit.ocm.mapper.impl.annotation.AnnotationMapperImpl;
import org.junit.Test;
import org.pavanecce.cmmn.ocm.OcmCaseFileItemSubscriptionInfo;
import org.pavanecce.cmmn.ocm.OcmCaseSubscriptionInfo;
import org.pavanecce.cmmn.ocm.OcmFactory;
import org.pavanecce.cmmn.ocm.OcmObjectPersistence;
import org.pavanecce.cmmn.test.domain.ConstructionCase;
import org.pavanecce.cmmn.test.domain.House;
import org.pavanecce.cmmn.test.domain.HousePlan;
import org.pavanecce.cmmn.test.domain.RoofPlan;
import org.pavanecce.cmmn.test.domain.Wall;
import org.pavanecce.cmmn.test.domain.WallPlan;

public class JcrTestCase {
	long time = System.currentTimeMillis();

	public void logDuration(String name) {
		System.out.println(name + " took " + (System.currentTimeMillis() - time));
		time = System.currentTimeMillis();
	}

	@Test
	public void testit() throws Exception {
		deleteTempRepo();
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
		OcmObjectPersistence oop = new OcmObjectPersistence(new OcmFactory(repository, "admin", "admin", getMapper()));
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
			Node root = session.getRootNode();
			// Node theCase = root.addNode("case1", "t:constructionCase");
			// theCase.setProperty("t:name", "MyName");
			// theCase.addNode("t:housePlans", "t:housePlan");
			// theCase.addNode("t:housePlans", "t:housePlan");
			// Store content
			oop.persist(constructionCase);
			oop.commit();
			logDuration("6");
			oop.start();
			ConstructionCase found = oop.find(ConstructionCase.class, constructionCase.getUuid());
			assertNotNull(found.getHousePlan());
			assertNotNull(found.getHousePlan().getRoofPlan());
			assertEquals(3, found.getHousePlan().getWallPlans().size());
			String path=found.getHousePlan().getWallPlans().iterator().next().getPath();
			// Retrieve content
			// Node node = root.getNode("myBuildingCase/housePlan");
			// System.out.println(node.getPath());
			// System.out.println(node.getProperty("t:name").getString());

			// Remove content
			// root.getNode("myBuildingCase").remove();
			printTree("", oop.getSession().getSession().getNode("/cases"));

			session.save();
		} finally {
			session.logout();
		}
	}

	public static void printTree(String padding, Node node) throws Exception {
		System.out.println(padding + node.getPath());
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

	public static void deleteTempRepo() throws IOException {
		File repo = new File("./repository");
		if (repo.exists() && !delete(repo)) {
			throw new RuntimeException("could not delete old repo");
		}
	}

	public static boolean delete(File repo) throws IOException {
		if (repo.isDirectory()) {
			File[] listFiles = repo.listFiles();
			for (File file : listFiles) {
				delete(file);
			}
		}
		if (!repo.delete()) {
			throw new RuntimeException("could not delete " + repo.getCanonicalPath());
		} else {
			System.out.println("deleted " + repo.getCanonicalPath());
		}
		return true;
	}

	protected void asdf() throws IOException, SQLException {
		URL ddl = JcrTestCase.class.getResource("/jackrabbit.ddl");
		BufferedReader br = new BufferedReader(new InputStreamReader(ddl.openStream()));
		String line = null;
		org.apache.derby.jdbc.Driver42.class.getName();
		Connection conn = DriverManager.getConnection("jdbc:derby:memory:myDB;create=true");
		while ((line = br.readLine()) != null) {
			line = line.trim();
			if (line.length() > 5 && !line.startsWith("--")) {
				if (line.endsWith(";")) {
					line = line.substring(0, line.length() - 1);
				}
				System.out.println(line);
				conn.createStatement().execute(line);
			}
		}
		URL resource = JcrTestCase.class.getResource("/repository.xml");
		File file = new File(resource.getPath());
		System.out.println(file);
	}

}