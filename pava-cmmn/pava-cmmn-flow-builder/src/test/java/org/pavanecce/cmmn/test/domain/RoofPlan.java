package org.pavanecce.cmmn.test.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import org.apache.jackrabbit.ocm.manager.beanconverter.impl.ParentBeanConverterImpl;
import org.apache.jackrabbit.ocm.mapper.impl.annotation.Bean;
import org.apache.jackrabbit.ocm.mapper.impl.annotation.Field;
import org.apache.jackrabbit.ocm.mapper.impl.annotation.Node;

@Entity
@Node(jcrType="t:roofPlan",discriminator=false)
public class RoofPlan {
	@Id
	@GeneratedValue
	@Field(uuid=true)
	private String id;
	@OneToOne()
	@Bean(converter=ParentBeanConverterImpl.class)
	private HousePlan housePlan;

	public RoofPlan(HousePlan housePlan) {
		this.housePlan=housePlan;
		this.housePlan.setRoofPlan(this);
	}
	public RoofPlan() {
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	public HousePlan getHousePlan() {
		return housePlan;
	}
	public void setHousePlan(HousePlan housePlan) {
		this.housePlan = housePlan;
	}
	
}
