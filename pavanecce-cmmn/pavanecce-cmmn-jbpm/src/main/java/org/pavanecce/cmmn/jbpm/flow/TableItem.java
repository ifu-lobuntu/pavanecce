package org.pavanecce.cmmn.jbpm.flow;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class TableItem extends AbstractItem implements CMMNElement {
	private static final long serialVersionUID = 6743815602437868413L;
	private Map<String, Role> authorizedRoles = new HashMap<String, Role>();
	private String elementId;
	private PlanningTable planningTable;
	private String description;
	private Map<String, ApplicabilityRule> applicabilityRules = new HashMap<String, ApplicabilityRule>();

	@Override
	public String getElementId() {
		return elementId;
	}

	public void putApplicabilityRule(String id, ApplicabilityRule r) {
		this.applicabilityRules.put(id, r);
	}

	public Map<String, ApplicabilityRule> getApplicabilityRules() {
		return applicabilityRules;
	}

	public void setElementId(String elementId) {
		this.elementId = elementId;
	}

	public void putAuthorizedRole(String id, Role role) {
		authorizedRoles.put(id, role);
	}

	public Map<String, Role> getAuthorizedRoles() {
		return authorizedRoles;
	}

	public static String getPlannerRoles(PlanItem<?> pi) {
		return getPlannerRoles(pi.getPlanItemContainer().getPlanningTable());
	}

	public static String getPlannerRoles(HumanTaskPlanItem htpi) {
		return getPlannerRoles(htpi.getPlanInfo().getDefinition().getPlanningTable(),htpi.getPlanItemContainer().getPlanningTable());
	}

	public static String getPlannerRoles(Case theCase) {
		return getPlannerRoles(theCase.getPlanningTable());
	}

	public static String getPlannerRoles(StagePlanItem spi) {
		return getPlannerRoles(spi.getDefinition().getPlanningTable(),spi.getPlanItemContainer().getPlanningTable());
	}

	private static String getPlannerRoles(PlanningTable... planningTables) {
		for (PlanningTable pt : planningTables) {
			if (pt != null && !pt.getAuthorizedRoles().isEmpty()) {
				String seperator = System.getProperty("org.jbpm.ht.user.separator", ",");
				StringBuilder result = new StringBuilder();
				Iterator<Role> values = pt.getAuthorizedRoles().values().iterator();
				while (values.hasNext()) {
					Role role = (Role) values.next();
					result.append(role.getName());
					if (values.hasNext()) {
						result.append(seperator);
					}
				}
				return result.toString();
			}

		}
		return "Administrators";
	}

	public PlanningTable getPlanningTable() {
		return planningTable;
	}

	public void setPlanningTable(PlanningTable planningTable) {
		this.planningTable = planningTable;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
