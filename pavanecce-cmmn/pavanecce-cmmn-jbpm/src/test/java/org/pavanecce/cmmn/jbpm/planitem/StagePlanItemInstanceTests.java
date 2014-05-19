package org.pavanecce.cmmn.jbpm.planitem;

import org.kie.api.runtime.process.NodeInstance;
import org.pavanecce.cmmn.jbpm.instance.PlanElementState;
import org.pavanecce.cmmn.jbpm.instance.PlanItemInstanceContainerLifecycle;
import org.pavanecce.cmmn.jbpm.instance.impl.StagePlanItemInstance;

public class StagePlanItemInstanceTests extends AbstractPlanItemInstanceContainerTests {

	{
		super.isJpa = true;
	}

	public StagePlanItemInstanceTests() {
		super(true, true, "org.jbpm.persistence.jpa");
	}

	@Override
	protected void ensurePlanItemContainerIsStarted() {
		assertPlanItemInState(caseInstance.getId(), "TheTopLevelStagePlanItem", PlanElementState.ACTIVE);
	}

	@Override
	protected PlanItemInstanceContainerLifecycle getPlanItemInstanceContainer() {
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

	public StagePlanItemInstance getStagePlanItemInstance() {
		StagePlanItemInstance spii=null;
		for (NodeInstance ni : reloadCaseInstance(caseInstance).getNodeInstances()) {
			if (ni instanceof StagePlanItemInstance) {
				spii = (StagePlanItemInstance) ni;
			}
		}
		return spii;
	}

	@Override
	public String getProcessFile() {
		return "test/StagePlanItemInstanceTests.cmmn";
	}

}