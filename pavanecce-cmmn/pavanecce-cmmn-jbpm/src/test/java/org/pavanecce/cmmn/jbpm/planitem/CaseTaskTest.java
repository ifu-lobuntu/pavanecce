package org.pavanecce.cmmn.jbpm.planitem;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.kie.api.runtime.process.NodeInstance;
import org.kie.api.runtime.process.ProcessInstance;
import org.pavanecce.cmmn.jbpm.AbstractConstructionTestCase;
import org.pavanecce.cmmn.jbpm.instance.CaseInstance;
import org.pavanecce.cmmn.jbpm.instance.CaseTaskPlanItemInstance;

import test.ConstructionCase;
import test.House;
import test.HousePlan;

public class CaseTaskTest extends AbstractConstructionTestCase {
	protected HousePlan housePlan;
	protected House house;
	private CaseInstance caseInstance;

	public CaseTaskTest() {
		super(true, true, "org.jbpm.persistence.jpa");
	}

	@Test
	public void testCreateAndDeleteSubscriptionsAgainstParent() throws Exception {
		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		// *****WHEN

		triggerStartOfCaseTask();
		getPersistence().start();
		assertProcessInstanceActive(caseInstance.getId(), getRuntimeEngine().getKieSession());
		getPersistence().commit();
		// *****THEN
		assertNodeTriggered(caseInstance.getId(), "TheCaseTaskPlanItem");
		getPersistence().start();
		caseInstance = (CaseInstance) getRuntimeEngine().getKieSession().getProcessInstance(caseInstance.getId());
		boolean found=false;
		for (NodeInstance nodeInstance : caseInstance.getNodeInstances()) {
			if(nodeInstance instanceof CaseTaskPlanItemInstance){
				found=true;
				assertProcessInstanceActive(((CaseTaskPlanItemInstance) nodeInstance).getProcessInstanceId(), getRuntimeEngine().getKieSession());
			}
		}
		assertTrue(found);
		getPersistence().commit();

	}

	protected void givenThatTheTestCaseIsStarted() {
		createRuntimeManager("test/CaseTaskTests.cmmn", "test/SubCase.cmmn");
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
		caseInstance = (CaseInstance) getRuntimeEngine().getKieSession().startProcess("CaseTaskTests", params);
		Collection<ProcessInstance> processInstances = getRuntimeEngine().getKieSession().getProcessInstances();
		assertEquals(1, processInstances.size());
		getPersistence().commit();
		assertProcessInstanceActive(caseInstance.getId(), getRuntimeEngine().getKieSession());
		assertNodeTriggered(caseInstance.getId(), "defaultSplit");
		getPersistence().start();
		assertNodeActive(caseInstance.getId(), getRuntimeEngine().getKieSession(), "onTheUserEventOccurPartId");
		getPersistence().commit();
		getPersistence().start();
		assertProcessInstanceActive(caseInstance.getId(), getRuntimeEngine().getKieSession());
		getPersistence().commit();
	}

	private void triggerStartOfCaseTask() throws Exception {
		getPersistence().start();
		caseInstance = (CaseInstance) getRuntimeEngine().getKieSession().getProcessInstance(caseInstance.getId());
		caseInstance.signalEvent("TheUserEvent", new Object());
		getPersistence().commit();
	}

}
