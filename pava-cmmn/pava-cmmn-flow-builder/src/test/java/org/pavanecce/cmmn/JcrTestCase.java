package org.pavanecce.cmmn;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.apache.jackrabbit.commons.cnd.CndImporter;
import org.apache.jackrabbit.core.RepositoryImpl;
import org.apache.jackrabbit.core.TransientRepository;
import org.h2.Driver;
import org.junit.Test;

public class JcrTestCase  {
	@Test
	public void testit() throws Exception {
		File repo = new File("./repository");
		if (repo.exists()) {
			repo.delete();
		}
		Repository repository = new TransientRepository();
		SimpleCredentials credentials = new SimpleCredentials("admin", "admin".toCharArray());
		Session session = repository.login(credentials);

		// CndImporter.registerNodeTypes(new InputStreamReader(new
		// FileInputStream("/home/ampie/Code/pavanecce/pava-cmmn/pava-cmmn-flow-builder/src/test/resources/build.cnd")),
		// session);
		CndImporter.registerNodeTypes(new InputStreamReader(JcrTestCase.class.getResourceAsStream("/build.cnd")), session);
		try {
			Node root = session.getRootNode();
			Node theCase= root.addNode("case1", "t:constructionCase");
			theCase.addNode("t:housePlans", "t:housePlan");
			theCase.addNode("t:housePlans", "t:housePlan");
			// Store content
			session.save();

			// Retrieve content
			// Node node = root.getNode("myBuildingCase/housePlan");
			// System.out.println(node.getPath());
			// System.out.println(node.getProperty("t:name").getString());

			// Remove content
			// root.getNode("myBuildingCase").remove();
			session.save();
		} finally {
			session.logout();
		}
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
	}

}