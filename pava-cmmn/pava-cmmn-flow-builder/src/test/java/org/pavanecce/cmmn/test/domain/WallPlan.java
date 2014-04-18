package org.pavanecce.cmmn.test.domain;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;

@Entity
public class WallPlan {
	@Id
	@GeneratedValue
	private String id;
	@OneToOne(cascade=CascadeType.ALL)
	private WallQuote wallQuote;
	@OneToOne()
	private Wall wall;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public WallQuote getWallQuote() {
		return wallQuote;
	}

	public void setWallQuote(WallQuote wallQuote) {
		this.wallQuote = wallQuote;
	}

	public Wall getWall() {
		return wall;
	}

	public void setWall(Wall wall) {
		this.wall = wall;
	}
	
}
