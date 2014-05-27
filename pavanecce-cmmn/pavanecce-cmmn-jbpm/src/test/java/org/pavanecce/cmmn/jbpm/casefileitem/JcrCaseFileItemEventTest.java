package org.pavanecce.cmmn.jbpm.casefileitem;

import java.util.Map;

public abstract class JcrCaseFileItemEventTest extends AbstractCaseFileItemEventTests {

	@Override
	protected void addWallPlanAsChildToHousePlan() throws Exception {

	}

	@Override
	protected void removeWallPlansAsReferenceFromHouse() {
	}

	@Override
	protected Map<String, Object> prepareCaseParameters() {
		return null;
	}

	@Override
	protected String getProcessFile() {
		return null;
	}

	@Override
	protected void addRoofPlanAsChildToHousePlan() {
	}

	@Override
	protected void addRoofPlanAsReferenceToHouse() {
	}

	@Override
	protected void addWallPlanAsReferenceToHouse() throws Exception {
	}

	@Override
	protected void removeRoofPlanAsChildFromHousePlan() {
	}

	@Override
	protected void removeRoofPlanAsReferenceFromHouse() {
	}

	@Override
	protected void updateDescriptionOnHouse() {
	}

	@Override
	protected void removeWallPlansFromHousePlan() {
	}

}
