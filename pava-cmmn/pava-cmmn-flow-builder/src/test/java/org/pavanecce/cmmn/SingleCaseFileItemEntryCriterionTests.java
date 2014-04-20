package org.pavanecce.cmmn;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.shared.services.impl.events.JbpmServicesEventListener;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.task.model.Task;
import org.kie.api.task.model.TaskSummary;
import org.kie.internal.task.api.EventService;
import org.kie.internal.task.api.model.NotificationEvent;
import org.pavanecce.cmmn.flow.builder.CMMNBuilder;
import org.pavanecce.cmmn.instance.CaseInstance;
import org.pavanecce.cmmn.instance.CaseTaskLifecycleListener;
import org.pavanecce.cmmn.test.domain.ConstructionCase;
import org.pavanecce.cmmn.test.domain.House;
import org.pavanecce.cmmn.test.domain.HousePlan;
import org.pavanecce.cmmn.test.domain.RoofPlan;
import org.pavanecce.cmmn.test.domain.Wall;
import org.pavanecce.cmmn.test.domain.WallPlan;

public class SingleCaseFileItemEntryCriterionTests extends AbstrasctJbpmCaseBaseTestCase {

	private HousePlan housePlan;
	private House house;
	private CaseInstance caseInstance;

	public SingleCaseFileItemEntryCriterionTests() {
		super(true, true, "org.jbpm.persistence.jpa");
	}

	@Test
	public void testCreationOfObjectInCollectionFileItem() throws Exception {
		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		// *****WHEN
		addWallPlanAsChildToHousePlan();
		// *****THEN
		/*
		 * Verify Sentry Triggered: Sentries with a single OnPart are
		 * implemented merely as CatchLinkNodes
		 */
		assertNodeTriggered(caseInstance.getId(), "PlanItemEnteredWhenWallPlanCreated");
		assertNodeTriggered(caseInstance.getId(), "WaitingForWallPlanCreatedSentry");
	}

	@Test
	public void testCreationOfObjectInSingletonFileItem() throws Exception {
		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		// *****WHEN
		addRoofPlanAsChildToHousePlan();
		// *****THEN
		/*
		 * Verify Sentry Triggered: Sentries with a single OnPart are
		 * implemented merely as CatchLinkNodes
		 */
		assertNodeTriggered(caseInstance.getId(), "PlanItemEnteredWhenRoofPlanCreated");
		assertNodeTriggered(caseInstance.getId(), "WaitingForRoofPlanCreatedSentry");
	}

	@Test
	public void testAddChildOfObjectInCollectionFileItem() throws Exception {
		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		// *****WHEN
		addWallPlanAsChildToHousePlan();
		// *****THEN
		/*
		 * Verify Sentry Triggered: Sentries with a single OnPart are
		 * implemented merely as CatchLinkNodes
		 */
		assertNodeTriggered(caseInstance.getId(), "PlanItemEnteredWhenWallPlanAddedAsChild");
		assertNodeTriggered(caseInstance.getId(), "WaitingForWallPlanAddedAsChildSentry");
	}

	@Test
	public void testAddChildOfObjectInSingletonFileItem() throws Exception {
		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		// *****WHEN
		addRoofPlanAsChildToHousePlan();
		// *****THEN
		/*
		 * Verify Sentry Triggered: Sentries with a single OnPart are
		 * implemented merely as CatchLinkNodes
		 */
		assertNodeTriggered(caseInstance.getId(), "PlanItemEnteredWhenRoofPlanAddedAsChild");
		assertNodeTriggered(caseInstance.getId(), "WaitingForRoofPlanAddedAsChildSentry");
	}

	@Test
	public void testAddReferenceOfObjectInCollectionFileItem() throws Exception {
		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		// *****WHEN
		addWallPlanAsReferenceToHouse();
		// *****THEN
		/*
		 * Verify Sentry Triggered: Sentries with a single OnPart are
		 * implemented merely as CatchLinkNodes
		 */
		assertNodeTriggered(caseInstance.getId(), "PlanItemEnteredWhenWallPlanAddedAsReference");
		assertNodeTriggered(caseInstance.getId(), "WaitingForWallPlanAddedAsReferenceSentry");
	}

	@Test
	public void testAddReferenceOfObjectInSingletonFileItem() throws Exception {
		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		// *****WHEN
		addRoofPlanAsReferenceToHouse();
		// *****THEN
		/*
		 * Verify Sentry Triggered: Sentries with a single OnPart are
		 * implemented merely as CatchLinkNodes
		 */
		assertNodeTriggered(caseInstance.getId(), "PlanItemEnteredWhenRoofPlanAddedAsReference");
		assertNodeTriggered(caseInstance.getId(), "WaitingForRoofPlanAddedAsReferenceSentry");
	}

	@Test
	public void testDeletionOfObjectInCollectionFileItem() throws Exception {
		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		addWallPlanAsChildToHousePlan();
		
		// *****WHEN
		housePlan=getPersistence().find(HousePlan.class, housePlan.getId());
		housePlan.getWallPlans().clear();
		getPersistence().update(housePlan);
		getPersistence().commit();
		// *****THEN
		/*
		 * Verify Sentry Triggered: Sentries with a single OnPart are
		 * implemented merely as CatchLinkNodes
		 */
		assertNodeTriggered(caseInstance.getId(), "PlanItemEnteredWhenWallPlanDeleted");
		assertNodeTriggered(caseInstance.getId(), "WaitingForWallPlanDeletedSentry");
	}

//	@Test
//	public void testCreationOfObjectInSingletonFileItem() throws Exception {
//		// *****GIVEN
//		givenThatTheTestCaseIsStarted();
//		// *****WHEN
//		addRoofPlanAsChildToHousePlan();
//		// *****THEN
//		/*
//		 * Verify Sentry Triggered: Sentries with a single OnPart are
//		 * implemented merely as CatchLinkNodes
//		 */
//		assertNodeTriggered(caseInstance.getId(), "PlanItemEnteredWhenRoofPlanCreated");
//		assertNodeTriggered(caseInstance.getId(), "WaitingForRoofPlanCreatedSentry");
//	}
//
//	@Test
//	public void testAddChildOfObjectInCollectionFileItem() throws Exception {
//		// *****GIVEN
//		givenThatTheTestCaseIsStarted();
//		// *****WHEN
//		addWallPlanAsChildToHousePlan();
//		// *****THEN
//		/*
//		 * Verify Sentry Triggered: Sentries with a single OnPart are
//		 * implemented merely as CatchLinkNodes
//		 */
//		assertNodeTriggered(caseInstance.getId(), "PlanItemEnteredWhenWallPlanAddedAsChild");
//		assertNodeTriggered(caseInstance.getId(), "WaitingForWallPlanAddedAsChildSentry");
//	}
//
//	@Test
//	public void testAddChildOfObjectInSingletonFileItem() throws Exception {
//		// *****GIVEN
//		givenThatTheTestCaseIsStarted();
//		// *****WHEN
//		addRoofPlanAsChildToHousePlan();
//		// *****THEN
//		/*
//		 * Verify Sentry Triggered: Sentries with a single OnPart are
//		 * implemented merely as CatchLinkNodes
//		 */
//		assertNodeTriggered(caseInstance.getId(), "PlanItemEnteredWhenRoofPlanAddedAsChild");
//		assertNodeTriggered(caseInstance.getId(), "WaitingForRoofPlanAddedAsChildSentry");
//	}
//
//	@Test
//	public void testAddReferenceOfObjectInCollectionFileItem() throws Exception {
//		// *****GIVEN
//		givenThatTheTestCaseIsStarted();
//		// *****WHEN
//		addWallPlanAsReferenceToHouse();
//		// *****THEN
//		/*
//		 * Verify Sentry Triggered: Sentries with a single OnPart are
//		 * implemented merely as CatchLinkNodes
//		 */
//		assertNodeTriggered(caseInstance.getId(), "PlanItemEnteredWhenWallPlanAddedAsReference");
//		assertNodeTriggered(caseInstance.getId(), "WaitingForWallPlanAddedAsReferenceSentry");
//	}
//
//	@Test
//	public void testAddReferenceOfObjectInSingletonFileItem() throws Exception {
//		// *****GIVEN
//		givenThatTheTestCaseIsStarted();
//		// *****WHEN
//		addRoofPlanAsReferenceToHouse();
//		// *****THEN
//		/*
//		 * Verify Sentry Triggered: Sentries with a single OnPart are
//		 * implemented merely as CatchLinkNodes
//		 */
//		assertNodeTriggered(caseInstance.getId(), "PlanItemEnteredWhenRoofPlanAddedAsReference");
//		assertNodeTriggered(caseInstance.getId(), "WaitingForRoofPlanAddedAsReferenceSentry");
//	}
//
	private void addWallPlanAsReferenceToHouse() throws Exception{
		addWallPlanAsChildToHousePlan();
		addWallPlanAsChildToHousePlan();
		this.house=getPersistence().find(House.class, house.getId());
		house.getWallPlans().addAll(housePlan.getWallPlans());
		getPersistence().update(house);
		getPersistence().commit();

	}

	private void addRoofPlanAsReferenceToHouse() {
		addRoofPlanAsChildToHousePlan();
		this.house=getPersistence().find(House.class, house.getId());
		house.setRoofPlan(this.housePlan.getRoofPlan());
		getPersistence().update(house);
		getPersistence().commit();
	}

	private void addRoofPlanAsChildToHousePlan() {
		housePlan = getPersistence().find(HousePlan.class, housePlan.getId());
		new RoofPlan(housePlan);
		getPersistence().update(housePlan);
		getPersistence().commit();

	}

	protected void givenThatTheTestCaseIsStarted() {
		createRuntimeManager("test/SingleCaseFileItemEntryCriterionTests.cmmn");
		EventService<JbpmServicesEventListener<NotificationEvent>, JbpmServicesEventListener<Task>> eventService = (EventService<JbpmServicesEventListener<NotificationEvent>, JbpmServicesEventListener<Task>>) getRuntimeEngine()
				.getTaskService();
		eventService.registerTaskLifecycleEventListener(new CaseTaskLifecycleListener(getRuntimeEngine().getKieSession()));
		Map<String, Object> params = new HashMap<String, Object>();
		getPersistence().start();

		ConstructionCase cc = new ConstructionCase("/cases/case1");
		housePlan = new HousePlan(cc);
		house = new House(cc);
		getPersistence().persist(cc);
		getPersistence().commit();
		params.put("housePlan", Arrays.asList(housePlan));
		params.put("house", Arrays.asList(house));
		getPersistence().start();
		caseInstance = (CaseInstance) getRuntimeEngine().getKieSession().startProcess("SingleCaseFileItemEntryCriterionTests", params);
		getPersistence().commit();
		assertProcessInstanceActive(caseInstance.getId(), getRuntimeEngine().getKieSession());
		assertNodeTriggered(caseInstance.getId(), "defaultSplit");
		getPersistence().start();
		assertNodeActive(caseInstance.getId(), getRuntimeEngine().getKieSession(), "OnWallPlanCreatedPart");
		assertNodeActive(caseInstance.getId(), getRuntimeEngine().getKieSession(), "OnRoofPlanCreatedPart");
		assertNodeActive(caseInstance.getId(), getRuntimeEngine().getKieSession(), "OnWallPlanAddedAsChildPart");
		assertNodeActive(caseInstance.getId(), getRuntimeEngine().getKieSession(), "OnRoofPlanAddedAsChildPart");
		assertNodeActive(caseInstance.getId(), getRuntimeEngine().getKieSession(), "OnWallPlanAddedAsReferencePart");
		assertNodeActive(caseInstance.getId(), getRuntimeEngine().getKieSession(), "OnRoofPlanAddedAsReferencePart");
		getPersistence().commit();

	}

	private void addWallPlanAsChildToHousePlan() throws Exception {
		housePlan = getPersistence().find(HousePlan.class, housePlan.getId());
		new WallPlan(housePlan);
		getPersistence().update(housePlan);
		getPersistence().commit();
	}

	protected RuntimeManager createRuntimeManager(Strategy strategy, String identifier, String... process) {
		Map<String, ResourceType> resources = new HashMap<String, ResourceType>();
		for (String p : process) {
			resources.put(p, CMMNBuilder.CMMN_RESOURCE_TYPE);
		}
		return createRuntimeManager(strategy, resources, identifier);
	}
}
