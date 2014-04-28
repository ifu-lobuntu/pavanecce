package org.pavanecce.cmmn.jbpm.test.domain;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import org.apache.jackrabbit.ocm.mapper.impl.annotation.Bean;
import org.apache.jackrabbit.ocm.mapper.impl.annotation.Field;
import org.apache.jackrabbit.ocm.mapper.impl.annotation.Node;

@Entity
@Node(jcrType = "t:constructionCase", discriminator = false)
public class ConstructionCase {
	@Field(path = true)
	private String path;
	@Id
	@GeneratedValue()
	@Field(uuid = true)
	private String uuid;
	@Field(jcrName = "t:name")
	private String name;
	@Bean(jcrName = "t:housePlan")
	@OneToOne(cascade=CascadeType.ALL)
	private HousePlan housePlan;
	@Bean(jcrName = "t:house")
	@OneToOne(cascade=CascadeType.ALL)
	private House house;
	@Field(jcrName = "t:startDate")
	private Date startDate = new Date();

	public ConstructionCase(String path) {
		this.path = path;
	}

	public ConstructionCase() {
	}

	public HousePlan getHousePlan() {
		return housePlan;
	}

	public void setHousePlan(HousePlan housePlan) {
		this.housePlan = housePlan;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public House getHouse() {
		return house;
	}

	public void setHouse(House house) {
		this.house = house;
	}

}
