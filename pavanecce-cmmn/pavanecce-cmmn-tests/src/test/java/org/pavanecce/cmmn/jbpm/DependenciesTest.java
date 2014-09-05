package org.pavanecce.cmmn.jbpm;

import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.systemPackages;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;

import java.io.File;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.pavanecce.common.util.FileUtil;

@RunWith(PaxExam.class)
public class DependenciesTest {
	@Test
	public void testIt() throws Exception {
		// if we got here, the dependencies worked
	}

	@After
	public void cleanUp() throws Exception {
		File jcrRepoDir = new File("./repository");
		FileUtil.deleteAllChildren(jcrRepoDir);
	}

	@Configuration
	public Option[] config() {
		return options(
				systemProperty("org.ops4j.pax.logging.DefaultServiceLog.level").value("WARN"),
				systemPackages("javax.xml.transform",
						"javax.xml.transform.dom",
						"javax.xml.transform.stream",
						"javax.security.auth.login",
						"javax.security.auth.callback",
						"javax.security.auth.spi",
						"javax.imageio.spi",
						"javax.security.auth",
						"org.w3c.dom",
						"org.xml.sax",
						"org.xml.sax.helpers",
						"javax.xml.parsers",
						"javax.naming",
						"javax.sql","javax.net.ssl"),
				// mavenBundle("org.slf4j", "slf4j-api", "1.6.4"),
				// mavenBundle("org.slf4j", "slf4j-log4j12", "1.6.6"),
				mavenBundle("org.pavanecce", "pavanecce-environments-jahia", "0.0.1-SNAPSHOT"),
				mavenBundle("org.pavanecce", "pavanecce-common-util", "0.0.1-SNAPSHOT"),
				mavenBundle("org.pavanecce", "pavanecce-common-jcr", "0.0.1-SNAPSHOT"), mavenBundle("org.pavanecce", "pavanecce-common-jpa", "0.0.1-SNAPSHOT"),
				mavenBundle("org.pavanecce", "pavanecce-common-ocm", "0.0.1-SNAPSHOT"), mavenBundle("org.pavanecce", "pavanecce-cmmn-jbpm", "0.0.1-SNAPSHOT"),
				mavenBundle("org.pavanecce", "pavanecce-cmmn-jahia", "0.0.1-SNAPSHOT"),
				mavenBundle("org.pavanecce", "pavanecce-cmmn-cfa", "0.0.1-SNAPSHOT"), junitBundles());
	}
}
