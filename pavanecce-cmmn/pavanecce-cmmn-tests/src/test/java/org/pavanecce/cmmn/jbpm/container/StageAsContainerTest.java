package org.pavanecce.cmmn.jbpm.container;

import org.kie.api.runtime.process.NodeInstance;
import org.pavanecce.cmmn.jbpm.lifecycle.PlanElementState;
import org.pavanecce.cmmn.jbpm.lifecycle.PlanItemInstanceContainer;
import org.pavanecce.cmmn.jbpm.lifecycle.impl.StageInstance;

public class StageAsContainerTest extends AbstractPlanItemInstanceContainerLifecycleTest {

	{
		super.isJpa = true;
	}

	public StageAsContainerTest() {
		super(true, true, "org.jbpm.persistence.jpa");
	}

	@Override
	protected void ensurePlanItemContainerIsStarted() {
		assertPlanItemInState(caseInstance.getId(), "TheTopLevelStagePlanItem", PlanElementState.ACTIVE);
	}

	@Override
	protected PlanItemInstanceContainer getPlanItemInstanceContainer() {
		return getStagePlanItemInstance();
	}

	@Override
	public String getCaseName() {
		return "StagePlanItemInstanceTests";
	}

	@Override
	public long getWorkitemId() {
		getPersistence().start();
		long workItemId = getStagePlanItemInstance().getWorkItemId();
		getPersistence().commit();
		return workItemId;
	}

	public StageInstance getStagePlanItemInstance() {
		StageInstance spii = null;
		for (NodeInstance ni : reloadCaseInstance(caseInstance).getNodeInstances()) {
			if (ni instanceof StageInstance) {
				spii = (StageInstance) ni;
			}
		}
		return spii;
	}

	@Override
	public String getProcessFile() {
		return "test/container/StageAsContainerTests.cmmn";
	}

}