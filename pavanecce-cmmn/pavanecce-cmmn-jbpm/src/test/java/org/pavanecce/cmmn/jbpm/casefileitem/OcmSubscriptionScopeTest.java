package org.pavanecce.cmmn.jbpm.casefileitem;

import java.io.File;

import org.junit.BeforeClass;
import org.pavanecce.common.util.FileUtil;

public class OcmSubscriptionScopeTest extends SubscriptionScopeTest {
	{
		isJpa = false;
	}

	@BeforeClass
	public static void deleteRepo() {
		FileUtil.deleteRoot(new File("./repository"));
	}
}
