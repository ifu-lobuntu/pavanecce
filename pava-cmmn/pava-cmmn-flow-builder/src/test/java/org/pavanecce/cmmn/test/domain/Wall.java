package org.pavanecce.cmmn.test.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.apache.jackrabbit.ocm.mapper.impl.annotation.Bean;
import org.apache.jackrabbit.ocm.mapper.impl.annotation.Field;
import org.apache.jackrabbit.ocm.mapper.impl.annotation.Node;
import org.pavanecce.cmmn.ocm.GrandParentBeanConverterImpl;

@Entity
@Node(jcrType="t:wall",discriminator=false)
public class Wall {
	@Id
	@GeneratedValue
	@Field(uuid=true)
	private String id;
	@ManyToOne
	@Bean(converter=GrandParentBeanConverterImpl.class)
	private House house;
	public Wall(House house) {
		this.house=house;
		house.getWalls().add(this);		
	}
	public Wall() {
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	public House getHouse() {
		return house;
	}
	public void setHouse(House house) {
		this.house = house;
	}
	
	
}
