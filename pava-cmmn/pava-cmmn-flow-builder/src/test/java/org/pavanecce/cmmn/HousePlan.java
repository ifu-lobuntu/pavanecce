package org.pavanecce.cmmn;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@Entity
public class HousePlan {
	@Id
	@GeneratedValue
	private Long id;
	@OneToMany(cascade=CascadeType.ALL)
	private Set<WallPlan> wallPlans = new HashSet<WallPlan>();

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
}
