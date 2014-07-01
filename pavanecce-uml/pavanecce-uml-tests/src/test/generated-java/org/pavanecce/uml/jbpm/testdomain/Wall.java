package org.pavanecce.uml.jbpm.testdomain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity(name = "Wall")
@Table(name = "wall")
public class Wall {
	@ManyToOne()
	@JoinColumns(value = { @JoinColumn(name = "house_id", referencedColumnName = "id") })
	private House house = null;
	@Id()
	@GeneratedValue()
	private String id = null;
	@OneToOne(mappedBy = "wall")
	private WallPlan wallPlan = null;

	public Wall() {
	}

	public Wall(House owner) {
		this.setHouse(owner);
	}

	public House getHouse() {
		House result = this.house;
		return result;
	}

	public String getId() {
		String result = this.id;
		return result;
	}

	public WallPlan getWallPlan() {
		WallPlan result = this.wallPlan;
		return result;
	}

	public void setHouse(House newHouse) {
		if (!(newHouse == null)) {
			newHouse.getWalls().add(this);
		} else {
			if (!(this.house == null)) {
				this.house.getWalls().remove(this);
			}
		}
		this.house = newHouse;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setWallPlan(WallPlan newWallPlan) {
		WallPlan oldValue = this.wallPlan;
		if ((newWallPlan == null || !(newWallPlan.equals(oldValue)))) {
			this.wallPlan = newWallPlan;
			if (!(oldValue == null)) {
				oldValue.setWall(null);
			}
			if (!(newWallPlan == null)) {
				if (!(this.equals(newWallPlan.getWall()))) {
					newWallPlan.setWall(this);
				}
			}
		}
	}

	public void zz_internalSetHouse(House value) {
		this.house = value;
	}

	public void zz_internalSetWallPlan(WallPlan value) {
		this.wallPlan = value;
	}
}
