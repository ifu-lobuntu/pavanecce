package org.pavanecce.cmmn.jbpm.planitem;


public class CaseInstanceTests extends AbstractPlanItemInstanceContainerTests {

	{
		super.isJpa = true;
	}

	public CaseInstanceTests() {
		super(true, true, "org.jbpm.persistence.jpa");
	}

	@Override
	protected void ensurePlanItemContainerIsStarted() {
		
	}

	@Override
	public String getCaseName() {
		return "CaseInstanceTests";
	}

	@Override
	public String getProcessFile() {
		return "test/CaseInstanceTests.cmmn";
	}

}