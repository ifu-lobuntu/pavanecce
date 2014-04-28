package org.pavanecce.cmmn.jbpm.test.domain;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.apache.jackrabbit.ocm.manager.beanconverter.impl.DefaultBeanConverterImpl;
import org.apache.jackrabbit.ocm.manager.beanconverter.impl.ParentBeanConverterImpl;
import org.apache.jackrabbit.ocm.mapper.impl.annotation.Bean;
import org.apache.jackrabbit.ocm.mapper.impl.annotation.Collection;
import org.apache.jackrabbit.ocm.mapper.impl.annotation.Field;
import org.apache.jackrabbit.ocm.mapper.impl.annotation.Node;

@Entity
@Node(jcrType="t:housePlan",discriminator=false)
public class HousePlan {
	@Id
	@GeneratedValue
	@Field(uuid=true)
	private String id;
	@OneToMany(cascade=CascadeType.ALL, mappedBy="housePlan",orphanRemoval=true)
	@Collection(jcrName="t:wallPlans",jcrElementName="t:wallPlan")
	private Set<WallPlan> wallPlans = new HashSet<WallPlan>();
	@OneToOne(cascade=CascadeType.ALL, mappedBy="housePlan",orphanRemoval=true)
	@Bean(jcrName="t:roofPlan",jcrType="t:roofPlan",converter=DefaultBeanConverterImpl.class)
	private RoofPlan roofPlan;
	@Bean(converter=ParentBeanConverterImpl.class)
	@OneToOne(mappedBy="housePlan")
	private ConstructionCase constructionCase;
	@Field(path=true)
	String path;
	
	public HousePlan(){
		
	}
	public HousePlan(ConstructionCase  c){
		this.constructionCase=c;
		c.setHousePlan(this);
	}
	public ConstructionCase getConstructionCase() {
		return constructionCase;
	}

	public void setConstructionCase(ConstructionCase constructionCase) {
		this.constructionCase = constructionCase;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
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
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	@Override
	public boolean equals(Object obj) {
		return obj instanceof HousePlan &&  ((HousePlan)obj).id!=null && ((HousePlan)obj).id.equals(id);
	}
	@Override
	public int hashCode() {
		return id ==null?0:id.hashCode();
	}

}
