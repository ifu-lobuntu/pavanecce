package org.pavanecce.cmmn.jbpm.lifecycle.impl;

import java.util.Set;

import org.drools.core.process.core.impl.WorkImpl;
import org.pavanecce.cmmn.jbpm.flow.Role;

public class PlannedWork extends WorkImpl{
	private String planElementId;
	private Set<Role> roles;
	private Set<Role> plannerRoles;
	public String getPlanElementId() {
		return planElementId;
	}
	public void setPlanElementId(String planElementId) {
		this.planElementId = planElementId;
	}
	public Set<Role> getRoles() {
		return roles;
	}
	public void setRoles(Set<Role> roles) {
		this.roles = roles;
	}
	public Set<Role> getPlannerRoles() {
		return plannerRoles;
	}
	public void setPlannerRoles(Set<Role> plannerRoles) {
		this.plannerRoles = plannerRoles;
	}
}
