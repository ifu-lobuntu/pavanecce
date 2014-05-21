package org.pavanecce.cmmn.jbpm;

import org.pavanecce.cmmn.jbpm.lifecycle.impl.CaseInstance;
import org.pavanecce.cmmn.jbpm.ocm.OcmCaseFileItemSubscriptionInfo;
import org.pavanecce.cmmn.jbpm.ocm.OcmCaseSubscriptionInfo;
import org.pavanecce.cmmn.jbpm.test.AbstractCmmnCaseTestCase;

import test.ConstructionCase;
import test.House;
import test.HousePlan;
import test.RoofPlan;
import test.RoomPlan;
import test.Wall;
import test.WallPlan;

public class AbstractConstructionTestCase extends AbstractCmmnCaseTestCase {
	protected HousePlan housePlan;
	protected House house;
	protected CaseInstance caseInstance;

	public AbstractConstructionTestCase() {
		super(true, true, "org.jbpm.persistence.jpa");
	}

	public AbstractConstructionTestCase(boolean setupDataSource, boolean sessionPersistence, String persistenceUnitName) {
		super(setupDataSource, sessionPersistence, persistenceUnitName);
	}

	public AbstractConstructionTestCase(boolean setupDataSource, boolean sessionPersistence) {
		super(setupDataSource, sessionPersistence);
	}

	@Override
	protected Class<?>[] getClasses() {
		return new Class<?>[] { ConstructionCase.class, HousePlan.class, House.class, Wall.class, WallPlan.class, RoofPlan.class, OcmCaseSubscriptionInfo.class, OcmCaseFileItemSubscriptionInfo.class,
				RoomPlan.class };
	}

}
