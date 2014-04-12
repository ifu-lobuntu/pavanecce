package org.pavanecce.cmmn;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;

@Entity
public class WallPlan {
	@Id
	@GeneratedValue
	private Long id;
	@OneToOne(cascade=CascadeType.ALL)
	private WallQuote wallQuote;
	@OneToOne()
	private Wall wall;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
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
