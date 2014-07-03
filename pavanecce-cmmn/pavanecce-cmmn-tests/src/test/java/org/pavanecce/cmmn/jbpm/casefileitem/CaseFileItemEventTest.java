package org.pavanecce.cmmn.jbpm.casefileitem;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import test.cmmn.ConstructionCase;
import test.cmmn.House;
import test.cmmn.HousePlan;
import test.cmmn.RoofPlan;
import test.cmmn.WallPlan;

public abstract class CaseFileItemEventTest extends AbstractCaseFileItemEventTest {

	public CaseFileItemEventTest() {
		super(true, true, "org.jbpm.persistence.jpa");
	}

	@Override
	protected void removeWallPlansFromHousePlan() {
		getPersistence().start();
		housePlan = getPersistence().find(HousePlan.class, housePlan.getId());
		Set<WallPlan> wallPlans = new HashSet<WallPlan>(housePlan.getWallPlans());
		for (WallPlan wallPlan : wallPlans) {
			getPersistence().remove(wallPlan);
		}
		housePlan.getWallPlans().clear();
		getPersistence().update(housePlan);
		getPersistence().commit();
	}

	@Override
	protected void updateDescriptionOnHouse() {
		getPersistence().start();
		house = getPersistence().find(House.class, house.getId());
		house.setDescription("newDescription");
		getPersistence().update(house);
		getPersistence().commit();
	}

	@Override
	protected void removeRoofPlanAsReferenceFromHouse() {
		getPersistence().start();
		house = getPersistence().find(House.class, house.getId());
		house.setRoofPlan(null);
		getPersistence().update(house);
		getPersistence().commit();
	}

	@Override
	protected void removeRoofPlanAsChildFromHousePlan() {
		getPersistence().start();
		housePlan = getPersistence().find(HousePlan.class, housePlan.getId());
		RoofPlan roofPlan = housePlan.getRoofPlan();
		housePlan.zz_internalSetRoofPlan(null);
		getPersistence().remove(roofPlan);
		getPersistence().update(housePlan);
		getPersistence().commit();
	}

	@Override
	protected void addWallPlanAsReferenceToHouse() throws Exception {
		getPersistence().start();
		this.house = getPersistence().find(House.class, house.getId());
		this.housePlan = getPersistence().find(HousePlan.class, housePlan.getId());
		house.getWallPlans().addAll(housePlan.getWallPlans());
		getPersistence().update(house);
		getPersistence().commit();

	}

	@Override
	protected void addRoofPlanAsReferenceToHouse() {
		getPersistence().start();
		this.house = getPersistence().find(House.class, house.getId());
		house.setRoofPlan(this.housePlan.getRoofPlan());
		getPersistence().update(house);
		getPersistence().commit();
	}

	@Override
	protected void addRoofPlanAsChildToHousePlan() {
		getPersistence().start();
		housePlan = getPersistence().find(HousePlan.class, housePlan.getId());
		new RoofPlan(housePlan);
		getPersistence().update(housePlan);
		getPersistence().commit();

	}

	@Override
	protected String getProcessFile() {
		return "test/casefileitem/CaseFileItemEventTests.cmmn";
	}

	@Override
	protected Map<String, Object> prepareCaseParameters() {
		Map<String, Object> params = new HashMap<String, Object>();
		getPersistence().start();
		ConstructionCase cc = new ConstructionCase("/cases/case1");
		housePlan = new HousePlan(cc);
		house = new House(cc);
		getPersistence().persist(cc);
		getPersistence().commit();
		params.put("housePlan", housePlan);
		params.put("house", house);
		return params;
	}

	@Override
	protected void removeWallPlansAsReferenceFromHouse() {
		getPersistence().start();
		house = getPersistence().find(House.class, house.getId());
		house.getWallPlans().clear();
		getPersistence().update(house);
		getPersistence().commit();
	}

	@Override
	protected void addWallPlanAsChildToHousePlan() throws Exception {
		getPersistence().start();
		housePlan = getPersistence().find(HousePlan.class, housePlan.getId());
		new WallPlan(housePlan);
		getPersistence().update(housePlan);
		getPersistence().commit();
	}

}
