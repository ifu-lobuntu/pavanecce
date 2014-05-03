package org.pavanecce.cmmn.jbpm.test.domain;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import org.apache.jackrabbit.ocm.manager.beanconverter.impl.ReferenceBeanConverterImpl;
import org.apache.jackrabbit.ocm.mapper.impl.annotation.Bean;
import org.apache.jackrabbit.ocm.mapper.impl.annotation.Field;
import org.apache.jackrabbit.ocm.mapper.impl.annotation.Node;
import org.pavanecce.common.ocm.GrandParentBeanConverterImpl;

@Entity
@Node(jcrType="t:wallPlan",discriminator=false)
public class WallPlan {
	@Id
	@GeneratedValue
	@Field(uuid=true)
	private String id;
	@OneToOne(cascade=CascadeType.ALL)
	private WallQuote wallQuote;
	@OneToOne()
	@Bean(jcrName="t:wall", converter=ReferenceBeanConverterImpl.class)
	private Wall wall;
	@ManyToOne
	@Bean(converter=GrandParentBeanConverterImpl.class)
	public HousePlan housePlan;
	@Field(path=true)
	String path;
	public WallPlan(HousePlan housePlan) {
		housePlan.getWallPlans().add(this);
	}
	public WallPlan() {
	}

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
	public HousePlan getHousePlan() {
		return housePlan;
	}
	public void setHousePlan(HousePlan housePlan) {
		this.housePlan = housePlan;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	@Override
	public boolean equals(Object obj) {
		return obj instanceof WallPlan &&  ((WallPlan)obj).id!=null && ((WallPlan)obj).id.equals(id);
	}
	@Override
	public int hashCode() {
		return id ==null?0:id.hashCode();
	}
	

	
}
