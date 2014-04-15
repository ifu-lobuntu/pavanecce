package org.pavanecce.cmmn;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

@Entity
public class HousePlan {
	@Id
	@GeneratedValue
	private Long id;
	@OneToMany(cascade=CascadeType.ALL)
	private Set<WallPlan> wallPlans = new HashSet<WallPlan>();
	@OneToOne(cascade=CascadeType.ALL)
	private RoofPlan roofPlan;
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Set<WallPlan> getWallPlans() {
		return wallPlans;
	}

	public void setWallPlans(Set<WallPlan> wallPlans) {
		this.wallPlans = wallPlans;
	}

	public RoofPlan getRoofPlan() {
		return roofPlan;
	}

	public void setRoofPlan(RoofPlan roofPlan) {
		this.roofPlan = roofPlan;
	}
	
}
