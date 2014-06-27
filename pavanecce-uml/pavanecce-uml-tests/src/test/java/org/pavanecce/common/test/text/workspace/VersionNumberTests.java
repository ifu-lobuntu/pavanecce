package org.pavanecce.common.test.text.workspace;

import static org.junit.Assert.assertTrue;

import java.util.Properties;

import org.junit.Test;
import org.pavanecce.common.util.VersionNumber;

public class VersionNumberTests {
	@Test
	public void testIt() {
		final VersionNumber first = new VersionNumber("0.0.1.2.1");
		final VersionNumber second = new VersionNumber("0.0.1.2.2");
		assertTrue(first.compareTo(second) < 1);
		final Properties props = new Properties();
		second.writeTo(props);
		VersionNumber second2 = new VersionNumber();
		second2.readFrom(props);
		assertTrue(second2.compareTo(second) == 0);

	}
}
