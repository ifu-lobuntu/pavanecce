package org.pavanecce.common.test;

import static org.ops4j.pax.exam.CoreOptions.*;

import java.io.File;

import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.apache.jackrabbit.core.TransientRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.pavanecce.common.ocm.ObjectContentManagerFactory;
import org.pavanecce.common.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(PaxExam.class)
public class DependenciesTest {
	@Test
	public void testIt() throws Exception {
		File jcrRepoDir = new File("./repository");
		FileUtil.deleteAllChildren(jcrRepoDir);
		jcrRepoDir.mkdirs();

		TransientRepository tr = new TransientRepository();
		Session transientSession = tr.login(new SimpleCredentials("admin",
				"admin".toCharArray()));
		new ObjectContentManagerFactory(transientSession, null, null);
	}

	@Configuration
	public Option[] config() {
		return options(
				systemProperty("org.ops4j.pax.logging.DefaultServiceLog.level")
						.value("WARN"),
				systemPackages("javax.xml.transform", "javax.sql"),
				// mavenBundle("org.slf4j", "slf4j-api", "1.6.4"),
				// mavenBundle("org.slf4j", "slf4j-log4j12", "1.6.6"),
				mavenBundle("org.pavanecce", "pavanecce-environments-jahia",
						"0.0.1-SNAPSHOT"),
				mavenBundle("org.pavanecce", "pavanecce-common-util",
						"0.0.1-SNAPSHOT"),
				mavenBundle("org.pavanecce", "pavanecce-common-jcr",
						"0.0.1-SNAPSHOT"),
				mavenBundle("org.pavanecce", "pavanecce-common-jpa",
						"0.0.1-SNAPSHOT"),
				mavenBundle("org.pavanecce", "pavanecce-common-ocm",
						"0.0.1-SNAPSHOT"), junitBundles());
	}
}
