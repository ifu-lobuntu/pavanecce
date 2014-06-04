package org.pavanecce.cmmn.jbpm.casefileitem;

import java.util.Map;

import org.junit.Test;
import org.pavanecce.cmmn.jbpm.AbstractConstructionTestCase;
import org.pavanecce.cmmn.jbpm.TaskParameters;
import org.pavanecce.cmmn.jbpm.lifecycle.impl.CaseInstance;

public abstract class AbstractCaseFileItemEventTest extends AbstractConstructionTestCase {

	public AbstractCaseFileItemEventTest() {
		super();
	}

	public AbstractCaseFileItemEventTest(boolean setupDataSource, boolean sessionPersistence, String persistenceUnitName) {
		super(setupDataSource, sessionPersistence, persistenceUnitName);
	}

	public AbstractCaseFileItemEventTest(boolean setupDataSource, boolean sessionPersistence) {
		super(setupDataSource, sessionPersistence);
	}

	protected abstract void addWallPlanAsChildToHousePlan() throws Exception;

	protected abstract void removeWallPlansAsReferenceFromHouse();

	protected abstract Map<String, Object> prepareCaseParameters();

	protected abstract String getProcessFile();

	protected abstract void addRoofPlanAsChildToHousePlan();

	protected abstract void addRoofPlanAsReferenceToHouse();

	protected abstract void addWallPlanAsReferenceToHouse() throws Exception;

	protected abstract void removeRoofPlanAsChildFromHousePlan();

	protected abstract void removeRoofPlanAsReferenceFromHouse();

	protected abstract void updateDescriptionOnHouse();

	protected abstract void removeWallPlansFromHousePlan();

	protected void maybeStartSubscription() {

	}

	protected void endSubscription() {

	}

	@Test
	public void testCreationOfObjectInCollectionFileItem() throws Exception {
		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		// *****WHEN
		maybeStartSubscription();
		addWallPlanAsChildToHousePlan();
		endSubscription();
		// *****THEN
		/*
		 * Verify Sentry Triggered: Sentries with a single OnPart are implemented merely as CatchLinkNodes
		 */
		assertNodeTriggered(caseInstance.getId(), "PlanItemEnteredWhenWallPlanCreated");
		assertNodeTriggered(caseInstance.getId(), "WaitingForWallPlanCreatedSentry");
	}

	@Test
	public void testCreationOfObjectInSingletonFileItem() throws Exception {
		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		// *****WHEN
		maybeStartSubscription();
		addRoofPlanAsChildToHousePlan();
		endSubscription();
		// *****THEN
		/*
		 * Verify Sentry Triggered: Sentries with a single OnPart are implemented merely as CatchLinkNodes
		 */
		assertNodeTriggered(caseInstance.getId(), "PlanItemEnteredWhenRoofPlanCreated");
		assertNodeTriggered(caseInstance.getId(), "WaitingForRoofPlanCreatedSentry");
	}

	@Test
	public void testAddChildOfObjectInCollectionFileItem() throws Exception {
		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		// *****WHEN
		maybeStartSubscription();
		addWallPlanAsChildToHousePlan();
		endSubscription();
		// *****THEN
		/*
		 * Verify Sentry Triggered: Sentries with a single OnPart are implemented merely as CatchLinkNodes
		 */
		assertNodeTriggered(caseInstance.getId(), "PlanItemEnteredWhenWallPlanAddedAsChild");
		assertNodeTriggered(caseInstance.getId(), "WaitingForWallPlanAddedAsChildSentry");
	}

	@Test
	public void testAddChildOfObjectInSingletonFileItem() throws Exception {
		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		// *****WHEN
		maybeStartSubscription();
		addRoofPlanAsChildToHousePlan();
		endSubscription();
		// *****THEN
		/*
		 * Verify Sentry Triggered: Sentries with a single OnPart are implemented merely as CatchLinkNodes
		 */
		assertNodeTriggered(caseInstance.getId(), "PlanItemEnteredWhenRoofPlanAddedAsChild");
		assertNodeTriggered(caseInstance.getId(), "WaitingForRoofPlanAddedAsChildSentry");
	}

	@Test
	public void testAddReferenceOfObjectInCollectionFileItem() throws Exception {
		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		addWallPlanAsChildToHousePlan();
		// *****WHEN
		maybeStartSubscription();
		addWallPlanAsReferenceToHouse();
		endSubscription();
		// *****THEN
		/*
		 * Verify Sentry Triggered: Sentries with a single OnPart are implemented merely as CatchLinkNodes
		 */
		assertNodeTriggered(caseInstance.getId(), "PlanItemEnteredWhenWallPlanAddedAsReference");
		assertNodeTriggered(caseInstance.getId(), "WaitingForWallPlanAddedAsReferenceSentry");
	}

	@Test
	public void testAddReferenceOfObjectInSingletonFileItem() throws Exception {
		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		addRoofPlanAsChildToHousePlan();
		// *****WHEN

		maybeStartSubscription();
		addRoofPlanAsReferenceToHouse();
		endSubscription();
		// *****THEN
		/*
		 * Verify Sentry Triggered: Sentries with a single OnPart are implemented merely as CatchLinkNodes
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
		maybeStartSubscription();
		removeWallPlansFromHousePlan();
		endSubscription();
		// *****THEN
		/*
		 * Verify Sentry Triggered: Sentries with a single OnPart are implemented merely as CatchLinkNodes
		 */
		assertNodeTriggered(caseInstance.getId(), "PlanItemEnteredWhenWallPlanDeleted");
		assertNodeTriggered(caseInstance.getId(), "WaitingForWallPlanDeletedSentry");
	}

	@Test
	public void testDeletionOfObjectInSingletonFileItem() throws Exception {
		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		addRoofPlanAsChildToHousePlan();
		// *****WHEN
		maybeStartSubscription();
		removeRoofPlanAsChildFromHousePlan();
		endSubscription();
		// *****THEN
		/*
		 * Verify Sentry Triggered: Sentries with a single OnPart are implemented merely as CatchLinkNodes
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
		maybeStartSubscription();
		removeWallPlansFromHousePlan();
		endSubscription();
		// *****THEN
		/*
		 * Verify Sentry Triggered: Sentries with a single OnPart are implemented merely as CatchLinkNodes
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
		maybeStartSubscription();
		removeRoofPlanAsChildFromHousePlan();
		endSubscription();
		// *****THEN
		/*
		 * Verify Sentry Triggered: Sentries with a single OnPart are implemented merely as CatchLinkNodes
		 */
		assertNodeTriggered(caseInstance.getId(), "PlanItemEnteredWhenRoofPlanRemovedAsChild");
		assertNodeTriggered(caseInstance.getId(), "WaitingForRoofPlanRemovedAsChildSentry");
	}

	@Test
	public void testRemoveReferenceOfObjectInCollectionFileItem() throws Exception {
		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		addWallPlanAsChildToHousePlan();
		addWallPlanAsReferenceToHouse();
		// *****WHEN
		maybeStartSubscription();
		removeWallPlansAsReferenceFromHouse();
		endSubscription();
		// *****THEN
		/*
		 * Verify Sentry Triggered: Sentries with a single OnPart are implemented merely as CatchLinkNodes
		 */
		assertNodeTriggered(caseInstance.getId(), "PlanItemEnteredWhenWallPlanRemovedAsReference");
		assertNodeTriggered(caseInstance.getId(), "WaitingForWallPlanRemovedAsReferenceSentry");
	}

	@Test
	public void testRemoveReferenceOfObjectInSingletonFileItem() throws Exception {
		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		addRoofPlanAsChildToHousePlan();
		addRoofPlanAsReferenceToHouse();

		// *****WHEN
		maybeStartSubscription();
		removeRoofPlanAsReferenceFromHouse();
		endSubscription();
		// *****THEN
		/*
		 * Verify Sentry Triggered: Sentries with a single OnPart are implemented merely as CatchLinkNodes
		 */
		assertNodeTriggered(caseInstance.getId(), "PlanItemEnteredWhenRoofPlanRemovedAsReference");
		assertNodeTriggered(caseInstance.getId(), "WaitingForRoofPlanRemovedAsReferenceSentry");
	}

	@Test
	public void testObjectUpdated() throws Exception {
		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		// *****WHEN
		this.maybeStartSubscription();
		updateDescriptionOnHouse();
		endSubscription();
		// *****THEN
		/*
		 * Verify Sentry Triggered: Sentries with a single OnPart are implemented merely as CatchLinkNodes
		 */
		assertNodeTriggered(caseInstance.getId(), "PlanItemEnteredWhenHouseUpdated");
		assertNodeTriggered(caseInstance.getId(), "WaitingForHouseUpdatedSentry");
	}

	protected void givenThatTheTestCaseIsStarted() {
		createRuntimeManager(getProcessFile());
		Map<String, Object> params = prepareCaseParameters();
		params.put(TaskParameters.INITIATOR, "Spielman");
		getPersistence().start();
		caseInstance = (CaseInstance) getRuntimeEngine().getKieSession().startProcess("CaseFileItemEventTests", params);
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

}