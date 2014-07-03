package org.pavanecce.cmmn.jbpm.controllable;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jbpm.services.task.utils.ContentMarshallerHelper;
import org.kie.api.task.model.Content;
import org.kie.api.task.model.Task;

import test.cmmn.ConstructionCase;
import test.cmmn.HousePlan;
import test.cmmn.RoofPlan;
import test.cmmn.WallPlan;

public class HumanTaskTest extends AbstractControllableLifecycleTest {
	{
		super.isJpa = true;
	}

	public HumanTaskTest() {
		super(true, true, "org.jbpm.persistence.jpa");
	}

	public String getEventGeneratingTaskUser() {
		return "EventGeneratingBuilder";
	}

	@SuppressWarnings("unchecked")
	@Override
	public void completeTask(long taskId) {
		getPersistence().start();
		// Create an unrelated ConstructionCase, but associated its content with our instance.
		ConstructionCase otherCase = new ConstructionCase();
		HousePlan otherHousePlan = new HousePlan(otherCase);
		Set<WallPlan> newWallPlans = new HashSet<WallPlan>();
		newWallPlans.add(new WallPlan(otherHousePlan));
		newWallPlans.add(new WallPlan(otherHousePlan));
		newWallPlans.add(new WallPlan(otherHousePlan));
		RoofPlan newRoofPlan = new RoofPlan(otherHousePlan);
		getPersistence().persist(otherCase);
		getPersistence().commit();
		// Now complete the Case
		HashMap<String, Object> resultFromTask = new HashMap<String, Object>();
		resultFromTask.put("wallPlanOutput", newWallPlans);
		resultFromTask.put("roofPlanOutput", newRoofPlan);
		getPersistence().start();
		getRuntimeEngine().getTaskService().complete(taskId, getEventGeneratingTaskUser(), resultFromTask);
		// now look at the result - those new WallPlans must be added to the original housePlan due to the refinement
		getPersistence().commit();
		getPersistence().start();
		this.housePlan = getPersistence().find(HousePlan.class, housePlan.getId());
		assertTrue(this.housePlan.getWallPlans().containsAll(newWallPlans));
		assertEquals(5, this.housePlan.getWallPlans().size());
		assertEquals(newRoofPlan, this.housePlan.getRoofPlan());
		getPersistence().commit();
		// Check the task's result
		Task task = getTaskService().getTaskById(taskId);
		Content outputContent = getTaskService().getContentById(task.getTaskData().getOutputContentId());
		getPersistence().start();
		Map<String, Object> outputAsMap = (Map<String, Object>) ContentMarshallerHelper.unmarshall(outputContent.getContent(), getTaskService()
				.getMarshallerContext(task).getEnvironment());
		Collection<WallPlan> wallPlanTaskOutput = (Collection<WallPlan>) outputAsMap.get("wallPlanOutput");
		assertEquals(3, wallPlanTaskOutput.size());
		assertEquals(newRoofPlan, outputAsMap.get("roofPlanOutput"));
		getPersistence().commit();
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
