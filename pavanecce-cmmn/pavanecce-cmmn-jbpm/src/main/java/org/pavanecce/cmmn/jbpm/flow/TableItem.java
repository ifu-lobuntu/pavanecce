package org.pavanecce.cmmn.jbpm.flow;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class TableItem extends AbstractItem implements CMMNElement {
	private static final long serialVersionUID = 6743815602437868413L;
	private Map<String, Role> authorizedRoles = new HashMap<String, Role>();

	private PlanningTable planningTable;
	private String description;
	private Map<String, ApplicabilityRule> applicabilityRules = new HashMap<String, ApplicabilityRule>();

	public void putApplicabilityRule(String id, ApplicabilityRule r) {
		this.applicabilityRules.put(id, r);
	}

	public Map<String, ApplicabilityRule> getApplicabilityRules() {
		return applicabilityRules;
	}

	public void putAuthorizedRole(String id, Role role) {
		authorizedRoles.put(id, role);
	}

	public Map<String, Role> getAuthorizedRoles() {
		return authorizedRoles;
	}

	public static String getPlannerRoles(PlanItem<?> pi) {
		if (pi.getDefinition() instanceof PlanningTableContainer) {
			PlanningTableContainer ptc = (PlanningTableContainer) pi.getDefinition();
			return getPlannerRoles(ptc.getPlanningTable(), pi.getPlanItemContainer().getPlanningTable());
		}
		return getPlannerRoles(pi.getPlanItemContainer().getPlanningTable());
	}

	public static String getPlannerRoles(DiscretionaryItem<?> pi) {
		return getPlannerRoles(pi.getAuthorizedRoles().values(), pi.getParentTable().getAuthorizedRoles().values());
	}

	public static String getPlannerRoles(HumanTaskPlanItem htpi) {
		return getPlannerRoles(htpi.getPlanInfo().getDefinition().getPlanningTable(), htpi.getPlanItemContainer().getPlanningTable());
	}

	public static String getPlannerRoles(Case theCase) {
		return getPlannerRoles(theCase.getPlanningTable());
	}

	public static String getPlannerRoles(StagePlanItem spi) {
		return getPlannerRoles(spi.getDefinition().getPlanningTable(), spi.getPlanItemContainer().getPlanningTable());
	}

	@SafeVarargs
	private static String getPlannerRoles(Collection<Role>... authorizedRoles) {
		for (Collection<Role> collection : authorizedRoles) {
			if (collection != null && !collection.isEmpty()) {
				String seperator = System.getProperty("org.jbpm.ht.user.separator", ",");
				StringBuilder result = new StringBuilder();
				Iterator<Role> values = collection.iterator();
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

	public static String getPlannerRoles(PlanningTable... planningTables) {
		@SuppressWarnings("unchecked")
		Collection<Role>[] roles = new Collection[planningTables.length];
		for (int i = 0; i < planningTables.length; i++) {
			PlanningTable pt = planningTables[i];
			if (pt != null) {
				roles[i] = pt.getAuthorizedRoles().values();
			}

		}
		return getPlannerRoles(roles);
	}

	public PlanningTable getParentTable() {
		return planningTable;
	}

	public void setParentTable(PlanningTable planningTable) {
		this.planningTable = planningTable;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
