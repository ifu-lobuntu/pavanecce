package org.pavanecce.cmmn.jbpm.planitem;

import java.util.HashMap;
import java.util.Map;

import org.jbpm.workflow.instance.NodeInstanceContainer;
import org.junit.Test;
import org.kie.api.runtime.process.NodeInstance;
import org.pavanecce.cmmn.jbpm.AbstractConstructionTestCase;
import org.pavanecce.cmmn.jbpm.instance.PlanItemInstanceLifecycle;
import org.pavanecce.cmmn.jbpm.instance.impl.CaseInstance;

import test.ConstructionCase;
import test.House;
import test.HousePlan;

public class CaseInstanceTests extends AbstractConstructionTestCase {

	protected HousePlan housePlan;
	protected House house;
	protected CaseInstance caseInstance;
	{
		super.isJpa=true;
	}
	public CaseInstanceTests() {
		super(true, true, "org.jbpm.persistence.jpa");
	}


	@Test
	public void testCaseLifecycleComplete() throws Exception {
		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		// *****WHEN
		// *******THEN
		getPersistence().start();
		NodeInstanceContainer pi = (NodeInstanceContainer) getRuntimeEngine().getKieSession().getProcessInstance(caseInstance.getId());
		printState(" ", pi);

		getPersistence().commit();
		// *****THEN
	}

	protected void printState(String s, NodeInstanceContainer pi) {
		for (NodeInstance ni : pi.getNodeInstances()) {
			if (ni instanceof PlanItemInstanceLifecycle) {
				System.out.println(s + ni.getNodeName() +":" +((PlanItemInstanceLifecycle)ni).getPlanElementState());
			} else {
				System.out.println(s + ni.getNodeName());
			}
			if (ni instanceof NodeInstanceContainer) {
				printState(s + " ", (NodeInstanceContainer) ni);
			}

		}
	}

	private String getEventGeneratingTaskRole() {
		return "ConstructionProjectManager";
	}

	public void testTaskLifecycleTerminate() throws Exception {
		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		// *****WHEN
		// *******THEN
	}

	public void testTaskLifecycleFailed() throws Exception {
		// *****GIVEN
		givenThatTheTestCaseIsStarted();

	}

	protected void givenThatTheTestCaseIsStarted() {
		createRuntimeManager("test/CaseInstanceTests.cmmn");
		Map<String, Object> params = new HashMap<String, Object>();
		getPersistence().start();

		ConstructionCase cc = new ConstructionCase("/cases/case1");
		housePlan = new HousePlan(cc);
		house = new House(cc);
		getPersistence().persist(cc);
		getPersistence().commit();
		params.put("housePlan", housePlan);
		params.put("house", house);
		getPersistence().start();
		caseInstance = (CaseInstance) getRuntimeEngine().getKieSession().startProcess("CaseInstanceTests", params);
		getPersistence().commit();
		assertProcessInstanceActive(caseInstance.getId(), getRuntimeEngine().getKieSession());
		assertNodeTriggered(caseInstance.getId(), "defaultSplit");
		getPersistence().start();
		assertNodeActive(caseInstance.getId(), getRuntimeEngine().getKieSession(), "WaitingForEndUserEventSentry");
		getPersistence().commit();
	}

}