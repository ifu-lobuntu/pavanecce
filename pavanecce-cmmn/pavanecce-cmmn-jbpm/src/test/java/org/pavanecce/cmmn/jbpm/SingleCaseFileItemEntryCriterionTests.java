package org.pavanecce.cmmn.jbpm;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.jbpm.shared.services.impl.events.JbpmServicesEventListener;
import org.junit.Test;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.task.model.Task;
import org.kie.internal.task.api.EventService;
import org.kie.internal.task.api.model.NotificationEvent;
import org.pavanecce.cmmn.jbpm.flow.builder.CMMNBuilder;
import org.pavanecce.cmmn.jbpm.instance.CaseInstance;
import org.pavanecce.cmmn.jbpm.instance.CaseTaskLifecycleListener;

import test.ConstructionCase;
import test.House;
import test.HousePlan;
import test.RoofPlan;
import test.WallPlan;

public abstract class SingleCaseFileItemEntryCriterionTests extends AbstractJbpmCaseTestCase {

	protected HousePlan housePlan;
	protected House house;
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
		removeWallPlansFromHousePlan();
		// *****THEN
		/*
		 * Verify Sentry Triggered: Sentries with a single OnPart are
		 * implemented merely as CatchLinkNodes
		 */
		assertNodeTriggered(caseInstance.getId(), "PlanItemEnteredWhenWallPlanDeleted");
		assertNodeTriggered(caseInstance.getId(), "WaitingForWallPlanDeletedSentry");
	}

	protected void removeWallPlansFromHousePlan() {
		housePlan = getPersistence().find(HousePlan.class, housePlan.getId());
		housePlan.getWallPlans().clear();
		getPersistence().update(housePlan);
		getPersistence().commit();
	}

	@Test
	public void testDeletionOfObjectInSingletonFileItem() throws Exception {
		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		addRoofPlanAsChildToHousePlan();
		// *****WHEN
		removeRoofPlanAsChildFromHousePlan();
		// *****THEN
		/*
		 * Verify Sentry Triggered: Sentries with a single OnPart are
		 * implemented merely as CatchLinkNodes
		 */
		assertNodeTriggered(caseInstance.getId(), "PlanItemEnteredWhenRoofPlanDeleted");
		assertNodeTriggered(caseInstance.getId(), "WaitingForRoofPlanDeletedSentry");
	}

	@Test
	public void testRemoveChildOfObjectInCollectionFileItem() throws Exception {
		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		addWallPlanAsChildToHousePlan();
		// *****WHEN
		removeWallPlansFromHousePlan();
		// *****THEN
		/*
		 * Verify Sentry Triggered: Sentries with a single OnPart are
		 * implemented merely as CatchLinkNodes
		 */
		assertNodeTriggered(caseInstance.getId(), "PlanItemEnteredWhenWallPlanRemovedAsChild");
		assertNodeTriggered(caseInstance.getId(), "WaitingForWallPlanRemovedAsChildSentry");
	}

	@Test
	public void testRemoveChildOfObjectInSingletonFileItem() throws Exception {
		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		addRoofPlanAsChildToHousePlan();
		// *****WHEN
		removeRoofPlanAsChildFromHousePlan();
		// *****THEN
		/*
		 * Verify Sentry Triggered: Sentries with a single OnPart are
		 * implemented merely as CatchLinkNodes
		 */
		assertNodeTriggered(caseInstance.getId(), "PlanItemEnteredWhenRoofPlanRemovedAsChild");
		assertNodeTriggered(caseInstance.getId(), "WaitingForRoofPlanRemovedAsChildSentry");
	}

	@Test
	public void testRemoveReferenceOfObjectInCollectionFileItem() throws Exception {
		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		addWallPlanAsReferenceToHouse();
		// *****WHEN
		removeWallPlansAsReferenceFromHouse();
		// *****THEN
		/*
		 * Verify Sentry Triggered: Sentries with a single OnPart are
		 * implemented merely as CatchLinkNodes
		 */
		assertNodeTriggered(caseInstance.getId(), "PlanItemEnteredWhenWallPlanRemovedAsReference");
		assertNodeTriggered(caseInstance.getId(), "WaitingForWallPlanRemovedAsReferenceSentry");
	}

	@Test
	public void testRemoveReferenceOfObjectInSingletonFileItem() throws Exception {
		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		addRoofPlanAsReferenceToHouse();
		// *****WHEN
		removeRoofPlanAsReferenceFromHouse();
		// *****THEN
		/*
		 * Verify Sentry Triggered: Sentries with a single OnPart are
		 * implemented merely as CatchLinkNodes
		 */
		assertNodeTriggered(caseInstance.getId(), "PlanItemEnteredWhenRoofPlanRemovedAsReference");
		assertNodeTriggered(caseInstance.getId(), "WaitingForRoofPlanRemovedAsReferenceSentry");
	}

	@Test
	public void testObjectUpdated() throws Exception {
		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		// *****WHEN
		house = getPersistence().find(House.class, house.getId());
		house.setDescription("newDescription");
		getPersistence().update(house);
		getPersistence().commit();
		// *****THEN
		/*
		 * Verify Sentry Triggered: Sentries with a single OnPart are
		 * implemented merely as CatchLinkNodes
		 */
		assertNodeTriggered(caseInstance.getId(), "PlanItemEnteredWhenHouseUpdated");
		assertNodeTriggered(caseInstance.getId(), "WaitingForHouseUpdatedSentry");
	}

	protected void removeRoofPlanAsReferenceFromHouse() {
		house = getPersistence().find(House.class, house.getId());
		house.setRoofPlan(null);
		getPersistence().update(house);
		getPersistence().commit();
	}

	private void removeRoofPlanAsChildFromHousePlan() {
		housePlan = getPersistence().find(HousePlan.class, housePlan.getId());
		housePlan.setRoofPlan(null);
		getPersistence().update(housePlan);
		getPersistence().commit();
	}

	private void addWallPlanAsReferenceToHouse() throws Exception {
		addWallPlanAsChildToHousePlan();
		addWallPlanAsChildToHousePlan();
		this.house = getPersistence().find(House.class, house.getId());
		house.getWallPlans().addAll(housePlan.getWallPlans());
		getPersistence().update(house);
		getPersistence().commit();

	}

	private void addRoofPlanAsReferenceToHouse() {
		addRoofPlanAsChildToHousePlan();
		this.house = getPersistence().find(House.class, house.getId());
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
		@SuppressWarnings("unchecked")
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

	private void removeWallPlansAsReferenceFromHouse() {
		house = getPersistence().find(House.class, house.getId());
		house.getWallPlans().clear();
		getPersistence().update(house);
		getPersistence().commit();
	}

	private void addWallPlanAsChildToHousePlan() throws Exception {
		housePlan = getPersistence().find(HousePlan.class, housePlan.getId());
		new WallPlan(housePlan);
		getPersistence().update(housePlan);
		getPersistence().commit();
	}

}
