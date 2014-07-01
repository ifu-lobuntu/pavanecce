package org.pavanecce.uml.jbpm.testdomain;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.pavanecce.common.collections.OneToManySet;

@Entity(name = "HousePlan")
@Table(name = "house_plan")
public class HousePlan {
	@OneToOne()
	@JoinColumns(value = { @JoinColumn(name = "construction_case_id", referencedColumnName = "id") })
	private ConstructionCase constructionCase = null;
	@Id()
	@GeneratedValue()
	private String id = null;
	@OneToOne(mappedBy = "housePlan", cascade = CascadeType.ALL)
	private RoofPlan roofPlan = null;
	@SuppressWarnings("serial")
	private transient OneToManySet<HousePlan, RoomPlan> roomPlansWrapper = new OneToManySet<HousePlan, RoomPlan>(this) {
		@Override
		public Set<RoomPlan> getDelegate() {
			return roomPlans;
		}

		@Override
		@SuppressWarnings("unchecked")
		protected OneToManySet<HousePlan, RoomPlan> getChildren(HousePlan parent) {
			return (OneToManySet<HousePlan, RoomPlan>) parent.getRoomPlans();
		}

		@Override
		public HousePlan getParent(RoomPlan child) {
			return child.getHousePlan();
		}

		@Override
		public void setParent(RoomPlan child, HousePlan parent) {
			child.zz_internalSetHousePlan(parent);
		}

		@Override
		public boolean isLoaded() {
			return true;
		}

		@Override
		public boolean isInstanceOfChild(Object o) {
			return o instanceof RoomPlan;
		}
	};
	@OneToMany(mappedBy = "housePlan", cascade = CascadeType.ALL)
	private Set<RoomPlan> roomPlans = new HashSet<RoomPlan>();
	@SuppressWarnings("serial")
	private transient OneToManySet<HousePlan, WallPlan> wallPlansWrapper = new OneToManySet<HousePlan, WallPlan>(this) {
		@Override
		public Set<WallPlan> getDelegate() {
			return wallPlans;
		}

		@Override
		@SuppressWarnings("unchecked")
		protected OneToManySet<HousePlan, WallPlan> getChildren(HousePlan parent) {
			return (OneToManySet<HousePlan, WallPlan>) parent.getWallPlans();
		}

		@Override
		public HousePlan getParent(WallPlan child) {
			return child.getHousePlan();
		}

		@Override
		public void setParent(WallPlan child, HousePlan parent) {
			child.zz_internalSetHousePlan(parent);
		}

		@Override
		public boolean isLoaded() {
			return true;
		}

		@Override
		public boolean isInstanceOfChild(Object o) {
			return o instanceof WallPlan;
		}
	};
	@OneToMany(mappedBy = "housePlan", cascade = CascadeType.ALL)
	private Set<WallPlan> wallPlans = new HashSet<WallPlan>();

	public HousePlan() {
	}

	public HousePlan(ConstructionCase owner) {
		this.setConstructionCase(owner);
	}

	public ConstructionCase getConstructionCase() {
		ConstructionCase result = this.constructionCase;
		return result;
	}

	public String getId() {
		String result = this.id;
		return result;
	}

	public RoofPlan getRoofPlan() {
		RoofPlan result = this.roofPlan;
		return result;
	}

	public Set<RoomPlan> getRoomPlans() {
		Set<RoomPlan> result = this.roomPlansWrapper;
		return result;
	}

	public Set<WallPlan> getWallPlans() {
		Set<WallPlan> result = this.wallPlansWrapper;
		return result;
	}

	public void setConstructionCase(ConstructionCase newConstructionCase) {
		ConstructionCase oldValue = this.constructionCase;
		if ((newConstructionCase == null || !(newConstructionCase.equals(oldValue)))) {
			this.constructionCase = newConstructionCase;
			if (!(oldValue == null)) {
				oldValue.setHousePlan(null);
			}
			if (!(newConstructionCase == null)) {
				if (!(this.equals(newConstructionCase.getHousePlan()))) {
					newConstructionCase.setHousePlan(this);
				}
			}
		}
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setRoofPlan(RoofPlan newRoofPlan) {
		RoofPlan oldValue = this.roofPlan;
		if ((newRoofPlan == null || !(newRoofPlan.equals(oldValue)))) {
			this.roofPlan = newRoofPlan;
			if (!(oldValue == null)) {
				oldValue.setHousePlan(null);
			}
			if (!(newRoofPlan == null)) {
				if (!(this.equals(newRoofPlan.getHousePlan()))) {
					newRoofPlan.setHousePlan(this);
				}
			}
		}
	}

	public void setRoomPlans(Set<RoomPlan> newRoomPlans) {
		this.roomPlans = newRoomPlans;
	}

	public void setWallPlans(Set<WallPlan> newWallPlans) {
		this.wallPlans = newWallPlans;
	}

	public void zz_internalSetConstructionCase(ConstructionCase value) {
		this.constructionCase = value;
	}

	public void zz_internalSetRoofPlan(RoofPlan value) {
		this.roofPlan = value;
	}
}
