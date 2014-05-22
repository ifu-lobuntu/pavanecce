package org.pavanecce.cmmn.jbpm.controllable;

import java.util.HashMap;

public class HumanTaskTest extends AbstractControllableLifecycleTests {
	{
		super.isJpa = true;
	}

	public HumanTaskTest() {
		super(true, true, "org.jbpm.persistence.jpa");
	}

	public String getEventGeneratingTaskUser() {
		return "EventGeneratingBuilder";
	}

	@Override
	public void completeTask(long taskId) {
		getRuntimeEngine().getTaskService().complete(taskId, getEventGeneratingTaskUser(), new HashMap<String, Object>());
	}

	@Override
	public void failTask(long taskId) {
		getRuntimeEngine().getTaskService().fail(taskId, getEventGeneratingTaskUser(), new HashMap<String, Object>());
	}

	public String[] getProcessFileNames() {
		return new String[] { "test/controllable/HumanTaskTests.cmmn" };
	}

	public String getNameOfProcessToStart() {
		return "PlanItemEventTests";
	}

	@Override
	protected String getCaseOwner() {
		return "Spielman";
	}
}